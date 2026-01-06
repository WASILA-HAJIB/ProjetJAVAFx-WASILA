package com.example.javaproject.dao;

import com.example.javaproject.models.Projet;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjetDAO {
    private String url = "jdbc:mysql://localhost:3306/usersdb";
    private String user = "root";
    private String pass = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }



    public int countTotalProjets() {
        String sql = "SELECT COUNT(*) FROM projets";
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getProjetsStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as nb FROM projets GROUP BY status";
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("nb"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }
    public void addProjet(Projet p) {
        String sql = "INSERT INTO projets (nom, description, date_debut, date_fin, status, lieu) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setDate(3, Date.valueOf(p.getDateDebut()));
            pst.setDate(4, Date.valueOf(p.getDateFin()));
            pst.setString(5, p.getStatus());
            pst.setString(6, p.getLieu());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Projet> getAllProjets() {
        List<Projet> list = new ArrayList<>();
        String sql = "SELECT * FROM projets";
        try (Connection con = getConnection(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Projet(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getString("status"),
                        rs.getString("lieu")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void updateProjet(Projet p) {
        String sql = "UPDATE projets SET nom=?, description=?, date_debut=?, date_fin=?, status=?, lieu=? WHERE id=?";
        try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, p.getNom());
            pst.setString(2, p.getDescription());
            pst.setDate(3, Date.valueOf(p.getDateDebut()));
            pst.setDate(4, Date.valueOf(p.getDateFin()));
            pst.setString(5, p.getStatus());
            pst.setString(6, p.getLieu());
            pst.setInt(7, p.getId());
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deleteProjet(int id) {
        String sql = "DELETE FROM projets WHERE id=?";
        try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    // Dans EmployeDAO.java
    public void checkIn(int employeId) {
        String sql = "INSERT INTO pointages (employe_id, date_pointage, heure_arrivee) VALUES (?, CURDATE(), CURTIME())";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void checkOut(int employeId) {
        String sql = "UPDATE pointages SET heure_depart = CURTIME() WHERE employe_id = ? AND date_pointage = CURDATE() AND heure_depart IS NULL";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public ResultSet getPointageJournalier(int employeId) {
        String sql = "SELECT * FROM pointages WHERE employe_id = ? AND date_pointage = CURDATE()";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeId);
            return stmt.executeQuery();
        } catch (SQLException e) { return null; }
    }
}