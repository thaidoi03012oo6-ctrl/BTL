package com.recruitment.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LlmPdfExtractionService {

    private static final Logger log = LoggerFactory.getLogger(LlmPdfExtractionService.class);

    private final ChatLanguageModel geminiChatModel;
    private final ObjectMapper objectMapper;

    public LlmPdfExtractionService(ChatLanguageModel geminiChatModel, ObjectMapper objectMapper) {
        this.geminiChatModel = geminiChatModel;
        this.objectMapper = objectMapper;
    }

    public record ExtractedCvData(
            String cvTitle,
            String domain,
            String experience,
            String khuVuc,
            String candidateFullName,
            List<String> skills,
            String summary
    ) {}

    public record ExtractedJdData(
            String jobTitle,
            String description,
            String domain,
            String experience,
            String khuVuc,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            LocalDate deadline
    ) {}

    private String generateWithRetry(String prompt, int maxRetries) {
        Exception lastError = null;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return geminiChatModel.generate(prompt);
            } catch (Exception e) {
                lastError = e;
                log.warn("Gemini LLM generate failed (attempt {}/{}): {}", i, maxRetries, e.getMessage());
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
        throw new RuntimeException("Gemini LLM failed: " + (lastError != null ? lastError.getMessage() : "unknown"), lastError);
    }

    public ExtractedCvData extractCvFieldsWithLlm(String rawPdfText) {
        String prompt = String.format("""
            Bạn là một AI chuyên gia bóc tách hồ sơ ứng viên (CV Parser).
            Hãy đọc văn bản CV trích xuất từ file PDF sau đây và bóc tách thành các trường thông tin có cấu trúc dưới định dạng JSON thuần túy (không dùng markdown block ```json):

            Cấu trúc JSON yêu cầu:
            {
               "cvTitle": "<Vị trí/chức danh ứng tuyển>",
               "domain": "<Lĩnh vực chuyên môn, ví dụ: Information Technology, Finance, Marketing, v.v.>",
               "experience": "<Số năm hoặc mức độ kinh nghiệm, ví dụ: 3 năm, Fresher, v.v.>",
               "khuVuc": "<Địa điểm làm việc / nơi cư trú của ứng viên, ví dụ: Ha Noi, TP.HCM, Da Nang, v.v.>",
               "candidateFullName": "<Họ và tên ứng viên nếu có>",
               "skills": ["<Kỹ năng 1>", "<Kỹ năng 2>", "<Kỹ năng 3>"],
               "summary": "<Tóm tắt 2-3 câu về năng lực nổi bật của ứng viên>"
            }

            VĂN BẢN CV GỐC:
            %s
            """,
            rawPdfText != null ? (rawPdfText.length() > 6000 ? rawPdfText.substring(0, 6000) : rawPdfText) : ""
        );

        try {
            String response = generateWithRetry(prompt, 3);
            String cleanJson = cleanJsonString(response);
            JsonNode root = objectMapper.readTree(cleanJson);

            String title = root.path("cvTitle").asText("Hồ Sơ Ứng Viên");
            String domain = root.path("domain").asText("Chung");
            String exp = root.path("experience").asText("Chưa rõ");
            String kv = root.path("khuVuc").asText("Toàn quốc");
            String fullName = root.path("candidateFullName").asText("");
            String summary = root.path("summary").asText("");

            List<String> skills = new ArrayList<>();
            JsonNode skillsNode = root.path("skills");
            if (skillsNode.isArray()) {
                for (JsonNode s : skillsNode) {
                    skills.add(s.asText());
                }
            }

            return new ExtractedCvData(title, domain, exp, kv, fullName, skills, summary);
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất thông tin CV qua Gemini: {}", e.getMessage());
            return new ExtractedCvData("Hồ Sơ Ứng Viên", "Information Technology", "Chưa xác định", "Toàn quốc", "", List.of(), rawPdfText);
        }
    }

    public ExtractedJdData extractJdFieldsWithLlm(String rawPdfText) {
        String prompt = String.format("""
            Bạn là một AI chuyên gia bóc tách mô tả công việc (JD Parser).
            Hãy đọc văn bản JD trích xuất từ file PDF sau đây và bóc tách thành các trường thông tin có cấu trúc dưới định dạng JSON thuần túy (không dùng markdown block ```json):

            Cấu trúc JSON yêu cầu:
            {
               "jobTitle": "<Chức danh công việc>",
               "description": "<Bản tóm tắt chi tiết mô tả công việc, trách nhiệm và quyền lợi>",
               "domain": "<Lĩnh vực chuyên môn, ví dụ: Information Technology, Finance, Marketing, v.v.>",
               "experience": "<Yêu cầu kinh nghiệm, ví dụ: 2-3 năm, Senior, v.v.>",
               "khuVuc": "<Địa điểm làm việc, ví dụ: Ha Noi, TP.HCM, Remote, v.v.>",
               "minSalary": <Mức lương tối thiểu bằng số, đơn vị VNĐ, hoặc null nếu thỏa thuận>,
               "maxSalary": <Mức lương tối đa bằng số, đơn vị VNĐ, hoặc null nếu thỏa thuận>,
               "deadline": "<Hạn chót nộp hồ sơ theo định dạng YYYY-MM-DD hoặc null nếu không có>"
            }

            VĂN BẢN JD GỐC:
            %s
            """,
            rawPdfText != null ? (rawPdfText.length() > 6000 ? rawPdfText.substring(0, 6000) : rawPdfText) : ""
        );

        try {
            String response = generateWithRetry(prompt, 3);
            String cleanJson = cleanJsonString(response);
            JsonNode root = objectMapper.readTree(cleanJson);

            String jobTitle = root.path("jobTitle").asText("Tin Tuyển Dụng Mới");
            String description = root.path("description").asText(rawPdfText);
            String domain = root.path("domain").asText("Chung");
            String exp = root.path("experience").asText("Không yêu cầu");
            String kv = root.path("khuVuc").asText("Toàn quốc");

            BigDecimal minSal = root.hasNonNull("minSalary") ? BigDecimal.valueOf(root.path("minSalary").asDouble()) : null;
            BigDecimal maxSal = root.hasNonNull("maxSalary") ? BigDecimal.valueOf(root.path("maxSalary").asDouble()) : null;

            LocalDate deadline = null;
            if (root.hasNonNull("deadline") && !root.path("deadline").asText().isBlank()) {
                try {
                    deadline = LocalDate.parse(root.path("deadline").asText().trim());
                } catch (Exception ignored) {}
            }

            return new ExtractedJdData(jobTitle, description, domain, exp, kv, minSal, maxSal, deadline);
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất thông tin JD qua Gemini: {}", e.getMessage());
            return new ExtractedJdData("Tin Tuyển Dụng", rawPdfText, "Information Technology", "Không yêu cầu", "Toàn quốc", null, null, null);
        }
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```json")) s = s.substring(7);
        if (s.startsWith("```")) s = s.substring(3);
        if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        return s.trim();
    }
}
