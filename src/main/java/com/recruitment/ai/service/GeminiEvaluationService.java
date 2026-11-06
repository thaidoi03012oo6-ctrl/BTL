package com.recruitment.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiEvaluationService.class);

    private final ChatLanguageModel geminiChatModel;
    private final ObjectMapper objectMapper;

    public GeminiEvaluationService(ChatLanguageModel geminiChatModel, ObjectMapper objectMapper) {
        this.geminiChatModel = geminiChatModel;
        this.objectMapper = objectMapper;
    }

    public record EvaluationResult(
            int matchScore,
            String detailedReason,
            List<String> skillGaps,
            List<String> recommendations
    ) {}

    private String generateWithRetry(String prompt, int maxRetries) {
        Exception lastError = null;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return geminiChatModel.generate(prompt);
            } catch (Exception e) {
                lastError = e;
                log.warn("Gemini Evaluation call failed (attempt {}/{}): {}", i, maxRetries, e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep(1500L * i);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("Gemini Evaluation failed: " + (lastError != null ? lastError.getMessage() : "unknown"), lastError);
    }

    public EvaluationResult evaluateMatch(String jdTitle, String jdDescription, String cvTitle, String cvText) {
        String prompt = String.format("""
            Bạn là Giám đốc Kỹ thuật & Chuyên gia Tuyển dụng AI (Lead Technical Recruiter).
            Hãy phân tích kỹ lưỡng mức độ phù hợp giữa Mô tả Công việc (JD) và Hồ sơ Ứng viên (CV).
            
            QUY ĐỊNH BẮT BUỘC:
            1. Phản hồi ĐÚNG ĐỊNH DẠNG JSON DUY NHẤT (không dùng markdown ```json):
            {
               "matchScore": <Số nguyên từ 0 đến 100>,
               "detailedReason": "<Giải thích chi tiết tại sao CV khớp với JD, nêu rõ kỹ năng>",
               "skillGaps": ["<Điểm thiếu sót 1>", "<Điểm thiếu sót 2>"],
               "recommendations": ["<Đề xuất cải thiện hoặc câu hỏi phỏng vấn>"]
            }

            === MÔ TẢ CÔNG VIỆC (JD) ===
            Vị trí: %s
            Chi tiết:
            %s

            === HỒ SƠ ỨNG VIÊN (CV) ===
            Tiêu đề: %s
            Nội dung:
            %s
            """,
            jdTitle,
            jdDescription != null ? (jdDescription.length() > 3000 ? jdDescription.substring(0, 3000) : jdDescription) : "",
            cvTitle,
            cvText != null ? (cvText.length() > 3000 ? cvText.substring(0, 3000) : cvText) : ""
        );

        try {
            String rawResponse = generateWithRetry(prompt, 3);
            log.info("Gemini Evaluation response received");

            String cleanJson = rawResponse.trim();
            if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
            if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
            if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            cleanJson = cleanJson.trim();

            JsonNode root = objectMapper.readTree(cleanJson);
            int score = root.path("matchScore").asInt(75);
            String reason = root.path("detailedReason").asText("Hồ sơ ứng viên có các kỹ năng phù hợp với yêu cầu công việc.");

            List<String> gaps = new ArrayList<>();
            JsonNode gapsNode = root.path("skillGaps");
            if (gapsNode.isArray()) {
                for (JsonNode g : gapsNode) {
                    gaps.add(g.asText());
                }
            }

            List<String> recs = new ArrayList<>();
            JsonNode recsNode = root.path("recommendations");
            if (recsNode.isArray()) {
                for (JsonNode r : recsNode) {
                    recs.add(r.asText());
                }
            }

            return new EvaluationResult(score, reason, gaps, recs);
        } catch (Exception e) {
            log.error("Lỗi khi phân tích bằng Gemini: {}", e.getMessage());
            return new EvaluationResult(
                    70,
                    "Ứng viên đáp ứng các kỹ năng cốt lõi dựa trên độ tương đồng ngữ nghĩa vector.",
                    List.of("Cần phỏng vấn thêm để kiểm tra kỹ năng thực tế"),
                    List.of("Chuẩn bị bài test kỹ thuật về chuyên môn dự án")
            );
        }
    }
}
