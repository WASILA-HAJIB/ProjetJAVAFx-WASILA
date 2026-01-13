package com.example.javaproject.controllers;

import com.example.javaproject.models.Projet;
import com.example.javaproject.services.ProjetService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.awt.Desktop;
import java.net.URI;
import java.util.Optional;
import com.example.javaproject.utils.SceneManager;
import javafx.event.ActionEvent;

public class ProjetController {

    @FXML private TextField nomField, lieuField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateDebutPicker, dateFinPicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TableView<Projet> tableProjets;
    @FXML private TableColumn<Projet, String> colNom, colStatus, colDebut, colFin, colLieu;

    private final ProjetService service = new ProjetService();
    private ObservableList<Projet> listeProjets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("En attente", "En cours", "Terminé"));

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));

        loadProjets();

        // Écouteur de sélection conservé
        tableProjets.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nomField.setText(newSelection.getNom());
                descriptionArea.setText(newSelection.getDescription());
                dateDebutPicker.setValue(newSelection.getDateDebut());
                dateFinPicker.setValue(newSelection.getDateFin());
                statusCombo.setValue(newSelection.getStatus());
                lieuField.setText(newSelection.getLieu());
            }
        });
    }

    private void loadProjets() {
        listeProjets.setAll(service.getAllProjets());
        tableProjets.setItems(listeProjets);
    }

    @FXML
    private void onAdd() {
        try {
            Projet p = new Projet(
                    nomField.getText(),
                    descriptionArea.getText(),
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue(),
                    statusCombo.getValue(),
                    lieuField.getText()
            );

            service.validerEtAjouter(p);
            showNotification("SUCCÈS", "Le projet a été ajouté avec succès !", Alert.AlertType.INFORMATION);
            loadProjets();
            clearFields();
        } catch (IllegalArgumentException e) {
            showNotification("ERREUR DE SAISIE", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            showNotification("ERREUR", "Impossible d'ajouter le projet.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void onUpdate() {
        Projet selected = tableProjets.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setNom(nomField.getText());
                selected.setDescription(descriptionArea.getText());
                selected.setDateDebut(dateDebutPicker.getValue());
                selected.setDateFin(dateFinPicker.getValue());
                selected.setStatus(statusCombo.getValue());
                selected.setLieu(lieuField.getText());

                service.validerEtModifier(selected);
                showNotification("SUCCÈS", "Le projet a été mis à jour !", Alert.AlertType.INFORMATION);
                loadProjets();
                tableProjets.refresh();
                clearFields();
            } catch (IllegalArgumentException e) {
                showNotification("ERREUR DE SAISIE", e.getMessage(), Alert.AlertType.WARNING);
            } catch (Exception e) {
                showNotification("ERREUR", "Échec de la mise à jour.", Alert.AlertType.ERROR);
            }
        } else {
            showNotification("SÉLECTION", "Sélectionnez un projet dans le tableau pour le modifier.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onDelete() {
        Projet selected = tableProjets.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Ajout d'une confirmation de suppression
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le projet : " + selected.getNom());
            alert.setContentText("Voulez-vous vraiment supprimer ce projet ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                service.supprimer(selected.getId());
                showNotification("SUPPRIMÉ", "Le projet a été supprimé avec succès.", Alert.AlertType.INFORMATION);
                loadProjets();
                clearFields();
            }
        } else {
            showNotification("SÉLECTION", "Sélectionnez un projet à supprimer.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void openGoogleMaps() {
        try {
            Desktop.getDesktop().browse(new URI("https://www.google.com/maps"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showNotification(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        nomField.clear();
        descriptionArea.clear();
        lieuField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        statusCombo.setValue(null);
    }

    // --- NAVIGATION CONSERVÉE ---
    @FXML
    private void goToDashboard(ActionEvent event) {
        SceneManager.switchScene(event, "dashboard-view.fxml");
    }

    @FXML
    private void goToFactures(ActionEvent event) {
        SceneManager.switchScene(event, "facture-view.fxml");
    }

    @FXML
    private void goToProjets(ActionEvent event) {
        SceneManager.switchScene(event, "projet-view.fxml");
    }

    @FXML
    private void goToEmployes(ActionEvent event) {
        SceneManager.switchScene(event, "employe-view.fxml");
    }

    @FXML
    private void goToProduits(ActionEvent event) {
        SceneManager.switchScene(event, "produit-view.fxml");
    }

    @FXML
    private void goToChatbot(ActionEvent event) {
        SceneManager.switchScene(event, "chatbot-page-view.fxml");
    }

    @FXML
    private void onLogout(ActionEvent event) {
        SceneManager.switchScene(event, "hello-view.fxml");
    }
}