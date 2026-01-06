package com.example.javaproject.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.example.javaproject.models.ChatMessage;
public class MessageDAO {
    private static final String URL = "jdbc:mysql://localhost:3306/usersdb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public List<Integer> getAllSessionIds() {
        List<Integer> sessions = new ArrayList<>();
        String query = "SELECT DISTINCT session_id FROM chatbot_history ORDER BY session_id DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                sessions.add(rs.getInt("session_id"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sessions;
    }

    public List<ChatMessage> getFavorites(int sessionId) {
        List<ChatMessage> favorites = new ArrayList<>();
        String query = "SELECT * FROM chatbot_history WHERE session_id = ? AND is_favorite = 1 ORDER BY timestamp ASC";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                favorites.add(new ChatMessage(
                        rs.getInt("id"),
                        rs.getString("sender"),
                        rs.getString("message"),
                        true, // On sait que c'est un favori
                        rs.getInt("session_id")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return favorites;
    }
    public void saveMessage(String sender, String text, int sessionId) {
        String query = "INSERT INTO chatbot_history (sender, message, session_id) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, text);
            pstmt.setInt(3, sessionId);
            pstmt.executeUpdate();
            System.out.println("✅ Message sauvegardé (Session " + sessionId + ")");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<ChatMessage> getHistory(int sessionId, String searchTerm) {
        List<ChatMessage> history = new ArrayList<>();
        String query = "SELECT * FROM chatbot_history WHERE session_id = ? AND message LIKE ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, sessionId);
            pstmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                history.add(new ChatMessage(
                        rs.getInt("id"),
                        rs.getString("sender"),
                        rs.getString("message"),
                        rs.getBoolean("is_favorite"),
                        rs.getInt("session_id")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }



    public void toggleFavorite(int msgId, boolean isFav) {
        String query = "UPDATE chatbot_history SET is_favorite = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setBoolean(1, isFav);
            pstmt.setInt(2, msgId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }



    public void clearAllMessages() {
        String query = "DELETE FROM chatbot_history";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(query);
            stmt.execute("ALTER TABLE chatbot_history AUTO_INCREMENT = 1");
        } catch (SQLException e) { e.printStackTrace(); }
    }

}
