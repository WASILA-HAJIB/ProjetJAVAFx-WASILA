package com.example.javaproject.controllers;

import com.example.javaproject.models.Pointage;
import com.example.javaproject.services.PointageService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PointageController {
    @FXML private Circle statusDot;
    @FXML private Label statusLabel;

    @FXML private TableView<Pointage> historyTable;
    @FXML private TableColumn<Pointage, Object> colDate, colHeure, colType;
    @FXML private Label totalHoursLabel;
    @FXML private Label statusMessage;
    @FXML private TextField filterField;

    private final PointageService service = new PointageService();
    private int empId;
    private String savedNom, savedPrenom;
    private final ObservableList<Pointage> masterData = FXCollections.observableArrayList();
    private void updateStatusVisual(String dernierType) {
        if (dernierType == null || dernierType.equals("SORTIE")) {
            statusDot.setFill(Color.web("#e53e3e")); // Rouge
            statusLabel.setText("HORS SERVICE (Déconnecté)");
        } else if (dernierType.equals("ENTRÉE") || dernierType.equals("REPRISE DÉJEUNER")) {
            statusDot.setFill(Color.web("#38a169")); // Vert
            statusLabel.setText("EN POSTE (Au travail)");
        } else if (dernierType.equals("PAUSE DÉJEUNER")) {
            statusDot.setFill(Color.web("#ecc94b")); // Jaune
            statusLabel.setText("EN PAUSE DÉJEUNER");
        }
    }
    public void setInfo(int id, String nom, String prenom) {
        this.empId = id;
        this.savedNom = nom;
        this.savedPrenom = prenom;
        setupTable();
        setupFilter();
        refreshTable();
    }

    private void setupTable() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePointage"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heurePointage"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typePointage"));
    }

    private void setupFilter() {
        FilteredList<Pointage> filteredData = new FilteredList<>(masterData, p -> true);
        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return p.getDatePointage().toString().contains(lowerCaseFilter) ||
                        p.getTypePointage().toLowerCase().contains(lowerCaseFilter);
            });
        });
        historyTable.setItems(filteredData);
    }

    @FXML
    private void handleArrivee() {
        String res = service.enregistrerEntree(empId);
        if ("OK".equals(res)) {
            showStatus("Entrée matinale validée à " + now() + " ✅");
            refreshTable();
        } else {
            afficherAlerte("Action refusée", "Vous avez déjà une entrée active.");
        }
    }

    @FXML
    private void handleDepart() {
        String res = service.enregistrerSortie(empId);
        if ("OK".equals(res)) {
            showStatus("Sortie définitive enregistrée à " + now() + " 👋");
            refreshTable();
        } else {
            afficherAlerte("Action refusée", "Impossible de sortir sans avoir pointé l'entrée.");
        }
    }

    @FXML
    private void handlePause() {
        String res = service.enregistrerPause(empId);
        switch (res) {
            case "DEBUT_PAUSE":
                showStatus("Pause déjeuner enregistrée ☕");
                refreshTable();
                break;
            case "RETOUR_PAUSE":
                showStatus("Reprise de l'activité 💻");
                refreshTable();
                break;
            case "ERREUR_SEQUENCE":
                afficherAlerte("Action impossible", "Vous ne pouvez pas prendre de pause sans être entré.");
                break;
            default:
                afficherAlerte("Erreur", "Problème lors de l'enregistrement de la pause.");
                break;
        }
    }

    private void refreshTable() {
        if (empId != 0) {
            List<Pointage> list = service.chargerHistorique(empId);
            masterData.setAll(list);
            totalHoursLabel.setText(service.calculerTempsTravailAujourdhui(empId));

            // Mise à jour de l'indicateur visuel
            if (!list.isEmpty()) {
                updateStatusVisual(list.get(0).getTypePointage()); // list.get(0) est le plus récent (DESC)
            } else {
                updateStatusVisual(null);
            }
        }
    }
    private void showStatus(String msg) {
        statusMessage.setText(msg);
        new Thread(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> statusMessage.setText(""));
        }).start();
    }

    private String now() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private void afficherAlerte(String titre, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titre);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/welcome-view.fxml"));
            Parent root = loader.load();
            WelcomeController wc = loader.getController();
            wc.setUserInfo(empId, savedNom, savedPrenom);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}