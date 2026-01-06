package com.example.javaproject.controllers;

import com.example.javaproject.services.DashboardService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Map;

public class DashboardController {

    @FXML private Label statProjets, statEmployes, statFactures, statStockAlerte;
    @FXML private PieChart factureChart;
    @FXML private PieChart employeChart; // Remplacement de congeChart

    private final DashboardService service = new DashboardService();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            // 1. Stats numériques via le Service
            statProjets.setText(String.valueOf(service.getCountProjets()));
            statEmployes.setText(String.valueOf(service.getCountEmployes()));
            statFactures.setText(String.valueOf(service.getCountFactures()));
            statStockAlerte.setText(String.valueOf(service.getCountAlertesStock()));

            // 2. Graphiques
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

    private void navigate(String fxmlFile) {
        try {
            Stage stage = (Stage) statProjets.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goToDashboard() { navigate("/com/example/javaproject/dashboard-view.fxml"); }
    @FXML private void goToFactures() { navigate("/com/example/javaproject/facture-view.fxml"); }
    @FXML private void goToProjets() { navigate("/com/example/javaproject/projet-view.fxml"); }
    @FXML private void goToEmployes() { navigate("/com/example/javaproject/employe-view.fxml"); }
    @FXML private void goToProduits() { navigate("/com/example/javaproject/produit-view.fxml"); }
    @FXML private void goToChatbot() { navigate("/com/example/javaproject/chatbot-page-view.fxml"); }
    @FXML private void onLogout() { navigate("/com/example/javaproject/hello-view.fxml"); }
}