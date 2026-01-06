package com.example.javaproject.dao;

import com.example.javaproject.models.Employe;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeDAO {
    // Configuration de la base de données
    private static final String URL = "jdbc:mysql://localhost:3306/usersdb?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Méthode pour obtenir la connexion
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public boolean registerEmploye(String email, String password) {
        // On considère que l'email sert d'identifiant
        String sql = "INSERT INTO employes (email, password, nom, prenom, poste) VALUES (?, ?, 'Nouveau', 'Employé', 'Stagiaire')";
        try (Connection conn = getConnection(); // Utilisez votre méthode de connexion
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Récupérer tous les employés
    public List<Employe> getAll() {
        List<Employe> list = new ArrayList<>();
        String sql = "SELECT * FROM employes";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("poste"),
                        rs.getString("photo_path")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Ajouter un employé
    public void add(Employe e) {
        String sql = "INSERT INTO employes (nom, prenom, email, poste, photo_path, password) VALUES (?, ?, ?, ?, ?, '123456')";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNom());
            stmt.setString(2, e.getPrenom());
            stmt.setString(3, e.getEmail());
            stmt.setString(4, e.getPoste());
            stmt.setString(5, e.getPhotoPath());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Mettre à jour un employé
    public void update(Employe e) {
        String sql = "UPDATE employes SET nom=?, prenom=?, email=?, poste=?, photo_path=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, e.getNom());
            stmt.setString(2, e.getPrenom());
            stmt.setString(3, e.getEmail());
            stmt.setString(4, e.getPoste());
            stmt.setString(5, e.getPhotoPath());
            stmt.setInt(6, e.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Supprimer un employé
    public void delete(int id) {
        String sql = "DELETE FROM employes WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Compter le nombre total d'employés (pour les cartes du Dashboard)
    public int countTotalEmployes() {
        String sql = "SELECT COUNT(*) FROM employes";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Authentification pour le Login
    public Employe authenticate(String email, String password) {
        String sql = "SELECT * FROM employes WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Employe(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("poste"),
                        rs.getString("photo_path")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Statistiques pour le PieChart du Dashboard
    public Map<String, Integer> getStatsPostes() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT poste, COUNT(*) as total FROM employes GROUP BY poste";
        try (Connection conn = getConnection(); // Utilisation de getConnection() interne
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String poste = rs.getString("poste");
                // Si le poste est vide en BDD, on affiche "Inconnu"
                if (poste == null || poste.isEmpty()) poste = "Non défini";
                stats.put(poste, rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}