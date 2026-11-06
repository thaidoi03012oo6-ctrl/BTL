package com.recruitment.ai.service;

import com.recruitment.ai.repository.CvChunkRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final EmbeddingHelperService embeddingHelper;
    private final CvChunkRepository cvChunkRepository;
    private final ChatLanguageModel geminiChatModel;

    public ChatbotService(
            EmbeddingHelperService embeddingHelper,
            CvChunkRepository cvChunkRepository,
            ChatLanguageModel geminiChatModel) {
        this.embeddingHelper = embeddingHelper;
        this.cvChunkRepository = cvChunkRepository;
        this.geminiChatModel = geminiChatModel;
    }

    private String generateWithRetry(String prompt, int maxRetries) {
        Exception lastError = null;
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return geminiChatModel.generate(prompt);
            } catch (Exception e) {
                lastError = e;
                log.warn("Gemini Chatbot call failed (attempt {}/{}): {}", i, maxRetries, e.getMessage());
                if (i < maxRetries) {
                    try {
                        Thread.sleep(1200L * i);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("Gemini Chatbot failed: " + (lastError != null ? lastError.getMessage() : "unknown"), lastError);
    }

    public String askAssistant(String userQuestion) {
        log.info("Chatbot query: {}", userQuestion);

        String questionVector = embeddingHelper.embedToPgVectorString(userQuestion);
        List<Object[]> relevantRows = cvChunkRepository.findTopRelevantCvChunks(questionVector, 4);

        StringBuilder contextBuilder = new StringBuilder();
        for (Object[] row : relevantRows) {
            String title = (String) row[1];
            String chunkContent = (String) row[2];
            double similarity = ((Number) row[3]).doubleValue();

            contextBuilder.append("--- TÀI LIỆU: ").append(title).append(" (Độ tương đồng: ")
                    .append(String.format("%.2f", similarity)).append(") ---\n")
                    .append(chunkContent).append("\n\n");
        }

        String ragPrompt = String.format("""
            Bạn là Trợ lý AI Tuyển dụng thông minh của Hệ thống Web Tuyển dụng và Ghép nối CV - JD.
            Nhiệm vụ của bạn là hỗ trợ Nhà tuyển dụng, Ứng viên và Quản trị viên giải đáp thắc mắc, tư vấn nghề nghiệp và sàng lọc hồ sơ.

            HƯỚNG DẪN TRẢ LỜI:
            - Dựa trên [NGỮ CẢNH HỆ THỐNG] được trích xuất từ cơ sở dữ liệu bên dưới để trả lời câu hỏi.
            - Trả lời bằng tiếng Việt lịch sự, súc tích, chuyên nghiệp và có căn cứ rõ ràng.
            - Nếu thông tin trong ngữ cảnh không đủ để kết luận, hãy nêu rõ điều đó.

            [NGỮ CẢNH HỆ THỐNG]:
            %s

            [CÂU HỎI CỦA NGƯỜI DÙNG]:
            %s
            """,
            contextBuilder.length() > 0 ? contextBuilder.toString() : "Chưa có tài liệu cụ thể phù hợp.",
            userQuestion
        );

        try {
            return generateWithRetry(ragPrompt, 3);
        } catch (Exception e) {
            log.error("Lỗi Chatbot gọi Gemini: {}", e.getMessage());
            return "Xin lỗi, hiện tại dịch vụ AI đang tạm thời gián đoạn (" + e.getMessage() + "). Vui lòng thử lại sau giây lát!";
        }
    }

    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        long start = System.currentTimeMillis();
        try {
            String embedVector = embeddingHelper.embedToPgVectorString("health check ping");
            String chatReply = generateWithRetry("Trả lời đúng 1 chữ: OK", 2);
            long latency = System.currentTimeMillis() - start;
            health.put("status", "CONNECTED");
            health.put("latencyMs", latency);
            health.put("message", "Kết nối Gemini AI hoạt động bình thường.");
            health.put("reply", chatReply.trim());
            health.put("vectorDimensions", embedVector.split(",").length);
        } catch (Exception e) {
            health.put("status", "DISCONNECTED");
            health.put("error", e.getMessage());
        }
        return health;
    }
}
