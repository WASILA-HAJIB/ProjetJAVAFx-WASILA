package com.example.javaproject.models;

import java.time.LocalDateTime;

public class ChatMessage {
    private int id;
    private String sender; // "USER" ou "AI"
    private String content;
    private boolean favorite;
    private int sessionId;
    private LocalDateTime timestamp;

    // Constructeur complet (pour la récupération depuis la DB)
    public ChatMessage(int id, String sender, String content, boolean favorite, int sessionId) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.favorite = favorite;
        this.sessionId = sessionId;
    }

    // Constructeur simplifié (pour les nouveaux messages)
    public ChatMessage(String sender, String content, int sessionId) {
        this.sender = sender;
        this.content = content;
        this.sessionId = sessionId;
        this.favorite = false;
    }

    // --- GETTERS ET SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
}