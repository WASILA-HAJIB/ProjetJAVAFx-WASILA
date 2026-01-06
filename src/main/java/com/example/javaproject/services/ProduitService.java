package com.example.javaproject.services;

import com.example.javaproject.dao.ProduitDAO;
import com.example.javaproject.models.Produit;
import java.util.Comparator;
import java.util.List;

public class ProduitService {
    private final ProduitDAO dao = new ProduitDAO();

    public List<Produit> getAll() { return dao.getAll(); }

    public void ajouterProduit(String nom, String cat, String prixS, String stockS) throws Exception {
        // Validation : Champs vides
        if (nom == null || nom.trim().isEmpty() || cat == null || prixS.isEmpty() || stockS.isEmpty()) {
            throw new IllegalArgumentException("Veuillez remplir tous les champs.");
        }

        // Validation : Nombres et Valeurs négatives
        double prix = Double.parseDouble(prixS);
        int stock = Integer.parseInt(stockS);

        if (prix < 0 || stock < 0) {
            throw new IllegalArgumentException("Le prix et le stock ne peuvent pas être négatifs.");
        }

        dao.add(nom.trim(), cat, prix, stock);
    }

    public void supprimerProduit(int id) { dao.delete(id); }

    // Logique métier pour les statistiques
    public double calculerValeurTotale(List<Produit> list) {
        return list.stream().mapToDouble(p -> p.getPrix() * p.getStock()).sum();
    }

    public Produit trouverLePlusCher(List<Produit> list) {
        return list.stream().max(Comparator.comparingDouble(Produit::getPrix)).orElse(null);
    }

    public long compterRuptures(List<Produit> list) {
        return list.stream().filter(p -> p.getStock() == 0).count();
    }
}