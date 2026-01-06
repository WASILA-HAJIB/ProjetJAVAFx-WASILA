package com.example.javaproject.models;

import java.time.LocalDate;

public class Projet {
    private int id;
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String status;
    private String lieu;

    // Constructeur pour l'ajout
    public Projet(String nom, String description, LocalDate dateDebut, LocalDate dateFin, String status, String lieu) {
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.status = status;
        this.lieu = lieu;
    }

    // Constructeur pour la récupération (avec ID)
    public Projet(int id, String nom, String description, LocalDate dateDebut, LocalDate dateFin, String status, String lieu) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.status = status;
        this.lieu = lieu;
    }

    // Getters et Setters
    public int getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
}