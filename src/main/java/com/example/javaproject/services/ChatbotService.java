package com.example.javaproject.services;

import com.example.javaproject.dao.MessageDAO;
import com.example.javaproject.models.ChatMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.List;

public class ChatbotService {
    private final MessageDAO messageDAO = new MessageDAO(); // Le Service possède le DAO
    private final OpenAiChatModel model;

    public ChatbotService() {
        this.model = OpenAiChatModel.builder()
                //.apiKey("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")
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.1-8b-instant")
                .temperature(0.7)
                .build();
    }

    // --- LOGIQUE IA ---
    public String generateAIResponse(String userPrompt) {
        try {
            return model.generate(userPrompt);
        } catch (Exception e) {
            return "Erreur technique : " + e.getMessage();
        }
    }

    // --- LOGIQUE MÉTIER (Anciennement dans le Controller) ---
    public List<ChatMessage> getFullHistory(int sessionId, String filter) {
        return messageDAO.getHistory(sessionId, filter);
    }

    public List<ChatMessage> getFavoritesOnly(int sessionId) {
        return messageDAO.getFavorites(sessionId);
    }

    public void saveNewMessage(String sender, String content, int sessionId) {
        messageDAO.saveMessage(sender, content, sessionId);
    }

    public List<Integer> getAvailableSessions() {
        return messageDAO.getAllSessionIds();
    }

    public void updateFavoriteStatus(int msgId, boolean status) {
        messageDAO.toggleFavorite(msgId, status);
    }

    public void deleteEverything() {
        messageDAO.clearAllMessages();
    }
}