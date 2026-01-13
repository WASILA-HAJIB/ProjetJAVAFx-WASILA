package com.example.javaproject.controllers;

import com.example.javaproject.utils.SceneManager; // IMPORT DU MANAGER
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.StageStyle;

import java.io.IOException;

public class WelcomeController {

    @FXML private Label userNameLabel;
    private int currentEmployeId;
    private String nomStocke;
    private String prenomStocke;

    public void setUserInfo(int id, String nom, String prenom) {
        this.currentEmployeId = id;
        this.nomStocke = nom;
        this.prenomStocke = prenom;
        if (userNameLabel != null) {
            userNameLabel.setText(nom.toUpperCase() + " " + prenom);
        }
    }

    /**
     * Aller vers la page de pointage CLASSIQUE (Cas avec passage de données)
     */
    @FXML
    private void goToPointage(ActionEvent event) {
        try {
            // Pour les pages où on doit ENVOYER des données (ID, Nom),
            // on charge le loader pour accéder au contrôleur
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/pointage-view.fxml"));
            Parent root = loader.load();

            PointageController pCtrl = loader.getController();
            pCtrl.setInfo(currentEmployeId, nomStocke, prenomStocke);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pointage Quotidien");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/qr-pointage-view.fxml"));
            Parent root = loader.load();

            QrPointageController qrController = loader.getController();
            qrController.setUserInfo(this.currentEmployeId, this.nomStocke, this.prenomStocke);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pointage par QR Code");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * DÉCONNEXION : Ici on utilise le SwitchManager car il n'y a pas de données à transmettre !
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // Simple, propre et en une seule ligne !
        SceneManager.switchScene(event, "hello-view.fxml");
    }
    @FXML
    private void handleGesturePointage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/gesture-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Pointage Geste");
            stage.setScene(new Scene(root));

            GesturePointageController ctrl = loader.getController();

            // On affiche la fenêtre D'ABORD
            stage.show();

            // On initialise la caméra APRÈS (le thread interne de initData gérera la lourdeur)
            ctrl.initData(this.currentEmployeId);

            stage.setOnCloseRequest(e -> ctrl.closeAction());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}