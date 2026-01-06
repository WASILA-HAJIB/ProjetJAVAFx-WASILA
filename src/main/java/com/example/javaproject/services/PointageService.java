package com.example.javaproject.services;

import com.example.javaproject.dao.PointageDAO;
import com.example.javaproject.models.Pointage;
import java.time.LocalDate;
import java.util.List;
import java.time.Duration;
import java.time.LocalTime;

public class PointageService {
    private final PointageDAO dao = new PointageDAO();

    public String enregistrerEntree(int empId) {
        Pointage dernier = getDernierPointage(empId);
        // On ne peut entrer que si on n'est pas déjà là
        if (dernier != null && (dernier.getTypePointage().equals("ENTRÉE") || dernier.getTypePointage().equals("REPRISE DÉJEUNER"))) {
            return "ERREUR_DEUX_ENTREES";
        }
        boolean ok = dao.insererPointage(empId, "ENTRÉE");
        return ok ? "OK" : "ERREUR_SQL";
    }

    public String enregistrerSortie(int empId) {
        Pointage dernier = getDernierPointage(empId);
        // On ne peut sortir définitivement que si on est en train de travailler
        if (dernier == null || dernier.getTypePointage().equals("SORTIE") || dernier.getTypePointage().equals("PAUSE DÉJEUNER")) {
            return "ERREUR_DEUX_SORTIES";
        }
        boolean ok = dao.insererPointage(empId, "SORTIE");
        return ok ? "OK" : "ERREUR_SQL";
    }

    public String enregistrerPause(int empId) {
        Pointage dernier = getDernierPointage(empId);
        if (dernier == null) return "ERREUR_SEQUENCE";

        String type = dernier.getTypePointage().trim(); // .trim() enlève les espaces inutiles

        // Si on travaille (ENTRÉE ou REPRISE) -> On part en PAUSE
        if (type.equalsIgnoreCase("ENTRÉE") || type.equalsIgnoreCase("REPRISE DÉJEUNER")) {
            return dao.insererPointage(empId, "PAUSE DÉJEUNER") ? "DEBUT_PAUSE" : "ERREUR_SQL";
        }

        // Si on est déjà en pause -> On revient
        else if (type.equalsIgnoreCase("PAUSE DÉJEUNER")) {
            return dao.insererPointage(empId, "REPRISE DÉJEUNER") ? "RETOUR_PAUSE" : "ERREUR_SQL";
        }

        return "ERREUR_SEQUENCE";
    }

    private Pointage getDernierPointage(int empId) {
        List<Pointage> historique = dao.getHistoriqueComplet(empId);
        if (historique.isEmpty()) return null;
        return historique.get(0); // Le DAO doit renvoyer ORDER BY id DESC
    }

    public List<Pointage> chargerHistorique(int empId) {
        return dao.getHistoriqueComplet(empId);
    }

    public String calculerTempsTravailAujourdhui(int empId) {
        List<Pointage> hist = new java.util.ArrayList<>(dao.getHistoriqueComplet(empId));
        LocalDate aujourdhui = LocalDate.now();
        Duration totalDuration = Duration.ZERO;
        Pointage entreePrecedente = null;

        hist.sort(java.util.Comparator.comparingInt(Pointage::getId));

        for (Pointage p : hist) {
            if (p.getDatePointage().equals(aujourdhui)) {
                String type = p.getTypePointage();
                // DÉBUT DE SESSION
                if (type.equals("ENTRÉE") || type.equals("REPRISE DÉJEUNER")) {
                    entreePrecedente = p;
                }
                // FIN DE SESSION
                else if ((type.equals("SORTIE") || type.equals("PAUSE DÉJEUNER")) && entreePrecedente != null) {
                    totalDuration = totalDuration.plus(Duration.between(entreePrecedente.getHeurePointage(), p.getHeurePointage()));
                    entreePrecedente = null;
                }
            }
        }

        long hours = totalDuration.toHours();
        int minutes = totalDuration.toMinutesPart();
        int seconds = totalDuration.toSecondsPart();
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
    public String enregistrerPointageAutomatique(int empId) {
        Pointage dernier = getDernierPointage(empId);

        // 1. Si aucun pointage ou dernier était une SORTIE -> On entre
        if (dernier == null || dernier.getTypePointage().equals("SORTIE")) {
            return dao.insererPointage(empId, "ENTRÉE") ? "ENTREE_OK" : "ERREUR_SQL";
        }

        String type = dernier.getTypePointage().trim();

        // 2. Si l'employé est en poste (ENTRÉE) -> Il part en PAUSE
        if (type.equals("ENTRÉE")) {
            return dao.insererPointage(empId, "PAUSE DÉJEUNER") ? "PAUSE_OK" : "ERREUR_SQL";
        }

        // 3. Si l'employé est en PAUSE -> Il revient (REPRISE)
        if (type.equals("PAUSE DÉJEUNER")) {
            return dao.insererPointage(empId, "REPRISE DÉJEUNER") ? "REPRISE_OK" : "ERREUR_SQL";
        }

        // 4. Si l'employé est en REPRISE -> Le prochain scan est la SORTIE définitive
        if (type.equals("REPRISE DÉJEUNER")) {
            return dao.insererPointage(empId, "SORTIE") ? "SORTIE_OK" : "ERREUR_SQL";
        }

        return "ERREUR_SEQUENCE";
    }
}