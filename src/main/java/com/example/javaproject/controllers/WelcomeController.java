package com.example.javaproject.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.io.IOException;

public class WelcomeController {

    @FXML private Label userNameLabel;

    // Variables pour stocker les infos de l'employé connecté
    private int currentEmployeId;
    private String nomStocke;
    private String prenomStocke;

    /**
     * Cette méthode reçoit les infos depuis le Login (HelloController)
     */
    public void setUserInfo(int id, String nom, String prenom) {
        this.currentEmployeId = id;
        this.nomStocke = nom;
        this.prenomStocke = prenom;

        // Mise à jour de l'affichage du nom
        if (userNameLabel != null) {
            userNameLabel.setText(nom.toUpperCase() + " " + prenom);
        }
    }

    /**
     * Aller vers la page de pointage CLASSIQUE
     */
    @FXML
    private void goToPointage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/pointage-view.fxml"));
            Parent root = loader.load();

            // Passage des infos au PointageController
            PointageController pCtrl = loader.getController();
            pCtrl.setInfo(currentEmployeId, nomStocke, prenomStocke);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pointage Quotidien");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aller vers la page de pointage AVANCÉ (QR Code)
     */
    @FXML
    private void handleAdvancedPointage(ActionEvent event) {
        try {
            // 1. Charger le fichier FXML de la vue QR
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/qr-pointage-view.fxml"));
            Parent root = loader.load();

            // 2. Récupérer le contrôleur QR
            QrPointageController qrController = loader.getController();

            // CORRECTION ICI : Utilisation des bonnes variables (nomStocke, etc.)
            qrController.setUserInfo(this.currentEmployeId, this.nomStocke, this.prenomStocke);

            // 3. Changement de scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pointage par QR Code");
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur chargement QR View: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/hello-view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Connexion");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}