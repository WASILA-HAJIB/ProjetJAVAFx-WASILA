package com.example.javaproject.dao;
import com.example.javaproject.models.Pointage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PointageDAO {
    private String url = "jdbc:mysql://localhost:3306/usersdb";
    private String user = "root";
    private String pass = "";

    public boolean insererPointage(int empId, String type) {
        String sql = "INSERT INTO pointages (employe_id, date_pointage, heure_pointage, type_pointage) VALUES (?, CURDATE(), CURTIME(), ?)";
        try (Connection c = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ps.setString(2, type);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // AJOUTEZ CECI pour voir l'erreur SQL réelle
            return false;
        }
    }
    public List<Pointage> getHistoriqueComplet(int empId) {
        List<Pointage> list = new ArrayList<>();
        String sql = "SELECT * FROM pointages WHERE employe_id = ? ORDER BY id DESC";
        try (Connection c = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, empId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Pointage(
                        rs.getInt("id"), rs.getInt("employe_id"),
                        rs.getDate("date_pointage").toLocalDate(),
                        rs.getTime("heure_pointage").toLocalTime(),
                        rs.getString("type_pointage")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}