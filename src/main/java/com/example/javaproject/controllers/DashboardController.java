package com.example.javaproject.controllers;

import com.example.javaproject.services.DashboardService;
import com.example.javaproject.utils.SceneManager; // Manager utilisé
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import java.util.Map;

public class DashboardController {

    @FXML private Label statProjets, statEmployes, statFactures, statStockAlerte;
    @FXML private PieChart factureChart;
    @FXML private PieChart employeChart;

    private final DashboardService service = new DashboardService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            statProjets.setText(String.valueOf(service.getCountProjets()));
            statEmployes.setText(String.valueOf(service.getCountEmployes()));
            statFactures.setText(String.valueOf(service.getCountFactures()));
            statStockAlerte.setText(String.valueOf(service.getCountAlertesStock()));
            setupFacturePieChart();
            setupEmployePieChart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFacturePieChart() {
        factureChart.getData().clear();
        Map<String, Integer> stats = service.getStatsFactures();
        if (stats != null) {
            stats.forEach((statut, count) ->
                    factureChart.getData().add(new PieChart.Data(statut + " (" + count + ")", count)));
        }
    }

    private void setupEmployePieChart() {
        employeChart.getData().clear();
        Map<String, Integer> stats = service.getStatsEmployesParPoste();
        if (stats != null) {
            stats.forEach((poste, count) ->
                    employeChart.getData().add(new PieChart.Data(poste + " (" + count + ")", count)));
        }
        employeChart.setTitle("Répartition par Poste");
    }

    // --- NAVIGATION CENTRALISÉE VIA SWITCHMANAGER ---

    @FXML private void goToDashboard(ActionEvent event) { SceneManager.switchScene(event, "dashboard-view.fxml"); }
    @FXML private void goToFactures(ActionEvent event) { SceneManager.switchScene(event, "facture-view.fxml"); }
    @FXML private void goToProjets(ActionEvent event) { SceneManager.switchScene(event, "projet-view.fxml"); }
    @FXML private void goToEmployes(ActionEvent event) { SceneManager.switchScene(event, "employe-view.fxml"); }
    @FXML private void goToProduits(ActionEvent event) { SceneManager.switchScene(event, "produit-view.fxml"); }
    @FXML private void goToChatbot(ActionEvent event) { SceneManager.switchScene(event, "chatbot-page-view.fxml"); }
    @FXML private void onLogout(ActionEvent event) { SceneManager.switchScene(event, "hello-view.fxml"); }
}