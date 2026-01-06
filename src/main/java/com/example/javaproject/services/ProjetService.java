package com.example.javaproject.services;

import com.example.javaproject.dao.ProjetDAO;
import com.example.javaproject.models.Projet;
import java.util.List;

public class ProjetService {
    private final ProjetDAO dao = new ProjetDAO();

    public List<Projet> getAllProjets() {
        return dao.getAllProjets();
    }

    public void validerEtAjouter(Projet p) throws Exception {
        validerProjet(p);
        dao.addProjet(p);
    }

    public void validerEtModifier(Projet p) throws Exception {
        validerProjet(p);
        dao.updateProjet(p);
    }

    public void supprimer(int id) {
        dao.deleteProjet(id);
    }

    // Logique de validation partagée
    private void validerProjet(Projet p) throws Exception {
        if (p.getNom() == null || p.getNom().isEmpty() || p.getDateDebut() == null || p.getStatus() == null) {
            throw new IllegalArgumentException("Le nom, la date de début et le statut sont obligatoires.");
        }
        if (p.getDateFin() != null && p.getDateFin().isBefore(p.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
        }
    }
}