package com.example.javaproject.services;

import com.example.javaproject.dao.*;
import java.util.Map;

public class DashboardService {
    private final FactureDAO factureDao = new FactureDAO();
    private final ProjetDAO projetDao = new ProjetDAO();
    private final ProduitDAO produitDao = new ProduitDAO();
    private final EmployeDAO employeDao = new EmployeDAO();

    public int getCountProjets() { return projetDao.countTotalProjets(); }
    public int getCountEmployes() { return employeDao.countTotalEmployes(); }
    public int getCountFactures() { return factureDao.countTotalFactures(); }
    public int getCountAlertesStock() { return produitDao.countAlertesStock(); }

    public Map<String, Integer> getStatsFactures() {
        return factureDao.getFacturesStats();
    }

    public Map<String, Integer> getStatsEmployesParPoste() {
        // Appelle une méthode dans le DAO pour compter les employés par poste
        return employeDao.getStatsPostes();
    }
}