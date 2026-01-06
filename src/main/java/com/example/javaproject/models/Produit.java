package com.example.javaproject.models;

public class Produit {
    private int id;
    private String nom;
    private String categorie;
    private double prix;
    private int stock;

    // Constructeur
    public Produit(int id, String nom, String categorie, double prix, int stock) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.stock = stock;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getCategorie() { return categorie; }
    public double getPrix() { return prix; }
    public int getStock() { return stock; }

    // Setters (optionnels mais recommandés)
    public void setId(int id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setStock(int stock) { this.stock = stock; }
}