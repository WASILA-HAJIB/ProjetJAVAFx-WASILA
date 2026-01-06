package com.example.javaproject.controllers;

import com.example.javaproject.models.Facture;
import com.example.javaproject.services.FactureService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FactureController {

    @FXML private TextField clientField, montantField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private DatePicker datePicker;
    @FXML private Label revenuLabel, attenteLabel, tauxLabel;
    @FXML private TableView<Facture> tableFactures;
    @FXML private TableColumn<Facture, Integer> colId;
    @FXML private TableColumn<Facture, String> colClient, colStatut;
    @FXML private TableColumn<Facture, Double> colMontant;
    @FXML private TableColumn<Facture, LocalDate> colDate;
    @FXML private TableColumn<Facture, Void> colAction;

    private final FactureService service = new FactureService();

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList("Payé", "Non Payé"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientNom"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateEcheance"));

        setupActionColumn();
        refreshData();
    }

    // --- AJOUT AVEC VALIDATION ET NOTIFICATION ---
    @FXML
    public void onAddFacture() {
        try {
            String client = clientField.getText().trim();
            String montantStr = montantField.getText().trim();
            String statut = statusCombo.getValue();
            LocalDate date = datePicker.getValue();

            // Validation des champs vides
            if (client.isEmpty() || montantStr.isEmpty() || statut == null || date == null) {
                showSimpleAlert("Champs obligatoires", "Veuillez remplir tous les champs avant de générer la facture.", Alert.AlertType.WARNING);
                return;
            }

            // Validation du montant
            double montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                showSimpleAlert("Montant invalide", "Le montant doit être un nombre positif.", Alert.AlertType.ERROR);
                return;
            }

            // Appel au service pour l'insertion
            service.creerFacture(client, montantStr, statut, date);

            // Notification de succès
            showSimpleAlert("Validation réussie", "La facture pour " + client + " a été ajoutée avec succès !", Alert.AlertType.INFORMATION);

            refreshData();
            clearForm();

        } catch (NumberFormatException e) {
            showSimpleAlert("Erreur de format", "Le montant saisi n'est pas un nombre valide.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showSimpleAlert("Erreur Système", "Impossible d'enregistrer la facture : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // --- SUPPRESSION AVEC CONFIRMATION ET NOTIFICATION ---
    @FXML
    public void onDeleteFacture() {
        Facture selected = tableFactures.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showSimpleAlert("Aucune sélection", "Veuillez sélectionner une facture dans le tableau pour la supprimer.", Alert.AlertType.WARNING);
            return;
        }

        // Demande de confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la facture n° " + selected.getId() + " ?");
        confirm.setContentText("Voulez-vous vraiment supprimer définitivement cette facture ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                service.supprimerFacture(selected.getId());

                // Notification de succès de suppression
                showSimpleAlert("Suppression effectuée", "La facture a été supprimée de la base de données.", Alert.AlertType.INFORMATION);

                refreshData();
            } catch (Exception e) {
                showSimpleAlert("Erreur", "La suppression a échoué : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnPdf = new Button("📄 PDF");
            private final Button btnExcel = new Button("📊 XLS");
            private final HBox container = new HBox(10, btnPdf, btnExcel);
            {
                container.setAlignment(Pos.CENTER);
                btnPdf.setStyle("-fx-background-color: #6a11cb; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btnExcel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

                btnPdf.setOnAction(e -> {
                    try { service.genererPdfFactureDetaillee(getTableView().getItems().get(getIndex())); } catch (Exception ex) { ex.printStackTrace(); }
                });
                btnExcel.setOnAction(e -> {
                    try { service.genererExcelFacture(getTableView().getItems().get(getIndex())); } catch (Exception ex) { ex.printStackTrace(); }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void refreshData() {
        List<Facture> list = service.recupererToutesLesFactures();
        tableFactures.setItems(FXCollections.observableArrayList(list));
        revenuLabel.setText(String.format("%.2f €", service.calculerTotalPaye(list)));
        attenteLabel.setText(String.valueOf(service.compterNonPaye(list)));
        tauxLabel.setText(String.format("%.1f%%", service.calculerTauxRecouvrement(list)));
    }

    private void clearForm() {
        clientField.clear(); montantField.clear();
        statusCombo.setValue(null); datePicker.setValue(null);
    }

    private void showSimpleAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation FXML
    @FXML public void exportPDF() { showSimpleAlert("Info Export", "Utilisez les boutons dans le tableau pour exporter.", Alert.AlertType.INFORMATION); }
    @FXML private void goToDashboard() { navigate("/com/example/javaproject/dashboard-view.fxml"); }
    @FXML private void goToFactures() { refreshData(); }
    @FXML private void goToProduits() { navigate("/com/example/javaproject/produit-view.fxml"); }
    @FXML private void goToEmployes() { navigate("/com/example/javaproject/employe-view.fxml"); }
    @FXML private void goToProjets() { navigate("/com/example/javaproject/projet-view.fxml"); }
    @FXML private void onLogout() { navigate("/com/example/javaproject/hello-view.fxml"); }
    @FXML
    private void goToChatbot() {
        navigate("/com/example/javaproject/chatbot-page-view.fxml");
    }
    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) tableFactures.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}