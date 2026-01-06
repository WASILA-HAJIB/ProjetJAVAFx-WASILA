package com.example.javaproject.dao;

import com.example.javaproject.models.Produit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {
    private final String url = "jdbc:mysql://localhost:3306/usersdb";
    private final String user = "root";
    private final String password = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // UNIFIÉ : On utilise "stock" et non "quantite"
    public int countAlertesStock() {
        // Le seuil est fixé à 5 ici
        String sql = "SELECT COUNT(*) FROM produits WHERE stock < 5";
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Produit> getAll() {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produits";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("categorie"),
                        rs.getDouble("prix"),
                        rs.getInt("stock") // Colonne correcte
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void add(String nom, String categorie, double prix, int stock) {
        String sql = "INSERT INTO produits (nom, categorie, prix, stock) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, categorie);
            pstmt.setDouble(3, prix);
            pstmt.setInt(4, stock);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int id) {
        String sql = "DELETE FROM produits WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}