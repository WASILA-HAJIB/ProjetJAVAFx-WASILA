package com.example.javaproject.models;

import java.time.LocalDate;

public class Facture {
    private int id;
    private String clientNom;
    private double montant;
    private String statut;
    private LocalDate dateEcheance;

    public Facture(int id, String clientNom, double montant, String statut, LocalDate dateEcheance) {
        this.id = id;
        this.clientNom = clientNom;
        this.montant = montant;
        this.statut = statut;
        this.dateEcheance = dateEcheance;
    }

    // Getters indispensables pour la TableView (PropertyValueFactory)
    public int getId() { return id; }
    public String getClientNom() { return clientNom; }
    public double getMontant() { return montant; }
    public String getStatut() { return statut; }
    public LocalDate getDateEcheance() { return dateEcheance; }
}