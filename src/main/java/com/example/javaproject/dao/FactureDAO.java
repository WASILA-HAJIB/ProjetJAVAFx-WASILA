package com.example.javaproject.dao;

import com.example.javaproject.models.Facture;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FactureDAO {
    private final String url = "jdbc:mysql://localhost:3306/usersdb?serverTimezone=UTC";
    private final String user = "root";
    private final String password = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public List<Facture> getAll() {
        List<Facture> list = new ArrayList<>();
        String sql = "SELECT * FROM factures ORDER BY date_echeance ASC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Date d = rs.getDate("date_echeance");
                list.add(new Facture(
                        rs.getInt("id"),
                        rs.getString("client_nom"),
                        rs.getDouble("montant"),
                        rs.getString("statut"),
                        (d != null) ? d.toLocalDate() : null
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void add(String client, double montant, String statut, LocalDate date) {
        String sql = "INSERT INTO factures (client_nom, montant, statut, date_echeance) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client);
            pstmt.setDouble(2, montant);
            pstmt.setString(3, statut);
            pstmt.setDate(4, (date != null) ? Date.valueOf(date) : null);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM factures WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public int countTotalFactures() {
        String sql = "SELECT COUNT(*) FROM factures";
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getFacturesStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT statut, COUNT(*) as nb FROM factures GROUP BY statut";
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) { stats.put(rs.getString("statut"), rs.getInt("nb")); }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
}