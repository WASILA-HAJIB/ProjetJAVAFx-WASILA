package com.example.javaproject.controllers;

import com.example.javaproject.models.Produit;
import com.example.javaproject.services.ProduitService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.awt.Desktop;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.example.javaproject.utils.SceneManager;

public class ProduitController {
    @FXML private TextField nomField, prixField, stockField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, Integer> colId, colStock;
    @FXML private TableColumn<Produit, String> colNom, colCategorie;
    @FXML private TableColumn<Produit, Double> colPrix;

    @FXML private Label totalStockValueLabel, mostExpensiveLabel, ruptureStockLabel;

    private final ProduitService service = new ProduitService();

    @FXML
    public void initialize() {
        categorieCombo.setItems(FXCollections.observableArrayList("Électronique", "Mobilier", "Fournitures de Bureau", "Logiciels", "Services"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Ton code de style visuel conservé
        tableProduits.setRowFactory(tv -> new TableRow<Produit>() {
            @Override
            protected void updateItem(Produit item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if (item.getStock() == 0) setStyle("-fx-background-color: #ffcccc; -fx-border-color: red; -fx-border-width: 0 0 0 8;");
                else if (item.getStock() < 5) setStyle("-fx-background-color: #fff4e5; -fx-border-color: orange; -fx-border-width: 0 0 0 8;");
                else setStyle("");
            }
        });

        refreshData();
    }

    private void refreshData() {
        List<Produit> list = service.getAll();
        tableProduits.setItems(FXCollections.observableArrayList(list));
        updateDashboard(list);
        verifierRuptureStock(list);
    }

    private void updateDashboard(List<Produit> produits) {
        totalStockValueLabel.setText(String.format("%.2f €", service.calculerValeurTotale(produits)));
        Produit mostExpensive = service.trouverLePlusCher(produits);
        mostExpensiveLabel.setText(mostExpensive != null ? String.format("%s (%.2f €)", mostExpensive.getNom(), mostExpensive.getPrix()) : "N/A");
        ruptureStockLabel.setText(String.valueOf(service.compterRuptures(produits)));
    }

    private void verifierRuptureStock(List<Produit> produits) {
        long nbRupture = service.compterRuptures(produits);
        if (nbRupture > 0) {
            showAlert("ALERTE STOCK", "Attention : " + nbRupture + " produit(s) sont en rupture de stock (0) !", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void onAdd() {
        try {
            // Appel au service avec validation
            service.ajouterProduit(nomField.getText(), categorieCombo.getValue(), prixField.getText(), stockField.getText());

            // NOUVELLE NOTIFICATION DE VALIDATION
            showAlert("Succès", "Le produit a été ajouté avec succès !", Alert.AlertType.INFORMATION);

            refreshData();
            clearFields();
        } catch (IllegalArgumentException e) {
            showAlert("Erreur de saisie", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ajouter le produit.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        Produit selected = tableProduits.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // AJOUT D'UNE CONFIRMATION
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de suppression");
            confirm.setHeaderText("Supprimer le produit : " + selected.getNom());
            confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                service.supprimerProduit(selected.getId());
                showAlert("Validation", "Produit supprimé avec succès.", Alert.AlertType.INFORMATION);
                refreshData();
            }
        } else {
            showAlert("Sélection requise", "Veuillez sélectionner un produit à supprimer.", Alert.AlertType.WARNING);
        }
    }



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
    // --- HELPERS ---
    private void clearFields() {
        nomField.clear();
        categorieCombo.getSelectionModel().clearSelection();
        prixField.clear();
        stockField.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void ouvrirFichier(File file) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
        } catch (IOException e) { e.printStackTrace(); }
    }
}
