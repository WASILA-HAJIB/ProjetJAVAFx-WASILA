package com.example.javaproject.models;

public class Employe {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String poste;
    private String photoPath;

    public Employe(int id, String nom, String prenom, String email, String poste, String photoPath) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.poste = poste;
        this.photoPath = photoPath;
    }

    // Getters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getPoste() { return poste; }
    public String getPhotoPath() { return photoPath; }

    // Setters
    public void setNom(String nom) { this.nom = nom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setEmail(String email) { this.email = email; }
    public void setPoste(String poste) { this.poste = poste; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}