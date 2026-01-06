package com.example.javaproject.services;

import com.example.javaproject.dao.EmployeDAO;
import com.example.javaproject.models.Employe;
import java.util.List;

public class EmployeService {
    private final EmployeDAO dao = new EmployeDAO();

    public List<Employe> recupererTousLesEmployes() {
        return dao.getAll();
    }

    public void enregistrerEmploye(String nom, String prenom, String email, String poste, String photoPath) throws Exception {
        validerDonnees(nom, prenom, email);
        dao.add(new Employe(0, nom, prenom, email, poste, photoPath));
    }

    public void modifierEmploye(Employe e) throws Exception {
        validerDonnees(e.getNom(), e.getPrenom(), e.getEmail());
        dao.update(e);
    }

    private void validerDonnees(String nom, String prenom, String email) {
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty())
            throw new IllegalArgumentException("Veuillez remplir les champs obligatoires.");
        if (nom.matches(".*\\d.*") || prenom.matches(".*\\d.*"))
            throw new IllegalArgumentException("Le nom et le prénom ne doivent pas contenir de chiffres.");
        if (!email.contains("@"))
            throw new IllegalArgumentException("Email invalide.");
    }

    public void supprimerEmploye(int id) { dao.delete(id); }
}