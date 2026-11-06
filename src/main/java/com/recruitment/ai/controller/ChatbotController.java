package com.recruitment.ai.controller;

import com.recruitment.ai.dto.request.ChatRequest;
import com.recruitment.ai.dto.response.ChatResponse;
import com.recruitment.ai.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askAssistant(@Valid @RequestBody ChatRequest request) {
        String answer = chatbotService.askAssistant(request.getQuestion());
        return ResponseEntity.ok(new ChatResponse(answer));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getGeminiHealth() {
        return ResponseEntity.ok(chatbotService.checkHealth());
    }
}
