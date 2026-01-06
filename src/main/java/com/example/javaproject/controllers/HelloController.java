package com.example.javaproject.controllers;

import com.example.javaproject.models.Employe;
import com.example.javaproject.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class HelloController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private StackPane registerModal;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private PasswordField confirmPasswordField;

    // Utilisation du Service au lieu du DAO
    private final AuthService authService = new AuthService();

    @FXML
    private void onLoginButtonClick() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showStatus("Veuillez remplir tous les champs.", "#e74c3c");
            return;
        }

        // 1. Appel au service pour l'ADMIN
        if (authService.isAdmin(user, pass)) {
            changerDePage("/com/example/javaproject/dashboard-view.fxml", "Espace Admin");
            return;
        }

        // 2. Appel au service pour l'EMPLOYÉ
        Employe emp = authService.authenticateEmploye(user, pass);
        if (emp != null) {
            chargerEspaceEmploye(emp);
        } else {
            showStatus("Identifiants incorrects.", "#e74c3c");
        }
    }
    @FXML
    private void onForgotPasswordClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Récupération");
        dialog.setHeaderText("Réinitialiser le mot de passe");
        dialog.setContentText("Nom d'utilisateur :");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(username -> {
            TextInputDialog passDialog = new TextInputDialog();
            passDialog.setContentText("Nouveau mot de passe :");
            passDialog.showAndWait().ifPresent(newPass -> {
                if (authService.resetPassword(username, newPass)) {
                    showStatus("Mot de passe mis à jour !", "#27ae60");
                } else {
                    showStatus("Utilisateur non trouvé.", "#e74c3c");
                }
            });
        });
    }
    private void chargerEspaceEmploye(Employe emp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/welcome-view.fxml"));
            Scene scene = new Scene(loader.load());

            WelcomeController controller = loader.getController();
            controller.setUserInfo(emp.getId(), emp.getNom(), emp.getPrenom());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bienvenue - Espace Employé");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onRegisterButtonClick() {
        try {
            String user = regUsernameField.getText();
            String pass = regPasswordField.getText();
            String confirm = confirmPasswordField.getText();

            // Inscription en tant qu'employé
            boolean success = authService.registerNewUser(user, pass, confirm);

            if (success) {
                hideRegisterModal();
                showStatus("Compte Employé créé ! Connectez-vous.", "#27ae60");

                // On vide les champs
                regUsernameField.clear();
                regPasswordField.clear();
            }
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), "#e74c3c");
        }
    }

    // --- Méthodes utilitaires ---
    private void changerDePage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void showRegisterModal() { registerModal.setVisible(true); }
    @FXML private void hideRegisterModal() { registerModal.setVisible(false); }

    private void showStatus(String text, String colorHex) {
        messageLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-weight: bold;");
        messageLabel.setText(text);
    }
}