package com.example.javaproject.controllers;

import com.example.javaproject.models.Employe;
import com.example.javaproject.services.EmployeService;

// iText PDF - Spécifiques pour éviter les conflits avec JavaFX
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

// JavaFX
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Utilitaires
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmployeController {
    @FXML private TextField nomField, prenomField, emailField, posteField;
    @FXML private TableView<Employe> tableEmployes;
    @FXML private TableColumn<Employe, Integer> colId;
    @FXML private TableColumn<Employe, String> colNom, colPrenom, colEmail, colPoste;

    private final EmployeService service = new EmployeService();
    private String selectedPhotoPath = "";

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));

        loadData();

        tableEmployes.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                nomField.setText(newV.getNom());
                prenomField.setText(newV.getPrenom());
                emailField.setText(newV.getEmail());
                posteField.setText(newV.getPoste());
                selectedPhotoPath = newV.getPhotoPath();
            }
        });
    }

    private void loadData() {
        tableEmployes.setItems(FXCollections.observableArrayList(service.recupererTousLesEmployes()));
    }

    @FXML
    private void onSelectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir la photo de l'employé");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            showInfo("Photo", "Image chargée avec succès.");
        }
    }

    @FXML
    private void onAdd() {
        try {
            service.enregistrerEmploye(nomField.getText(), prenomField.getText(), emailField.getText(), posteField.getText(), selectedPhotoPath);
            showInfo("Succès", "Employé ajouté avec succès !");
            loadData();
            clearFields();
        } catch (Exception e) {
            showError("Erreur Système", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void onUpdate() {
        Employe selected = tableEmployes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                selected.setNom(nomField.getText());
                selected.setPrenom(prenomField.getText());
                selected.setEmail(emailField.getText());
                selected.setPoste(posteField.getText());
                selected.setPhotoPath(selectedPhotoPath);
                service.modifierEmploye(selected);
                showInfo("Succès", "Mise à jour effectuée.");
                loadData();
                tableEmployes.refresh();
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void onDelete() {
        Employe selected = tableEmployes.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + selected.getNom() + " ?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                service.supprimerEmploye(selected.getId());
                showInfo("Supprimé", "L'employé a été retiré.");
                loadData();
                clearFields();
            }
        }
    }

    // --- NOUVEAU DESIGN DE BADGE (Style GROUPE FRAME) ---
    @FXML
    private void onGenerateBadge() {
        Employe s = tableEmployes.getSelectionModel().getSelectedItem();
        if (s == null) {
            showError("Sélection", "Veuillez choisir un employé.");
            return;
        }

        File file = new File("Badge_" + s.getNom() + ".pdf");
        try {
            // Format Horizontal (Style carte ID paysage)
            Rectangle pageSize = new Rectangle(400, 250);
            Document doc = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            PdfContentByte cb = writer.getDirectContent();
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfReg = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

            // 1. Bandeau Supérieur (Bleu Marine)
            cb.setColorFill(new BaseColor(34, 30, 94));
            cb.rectangle(0, 190, 400, 60);
            cb.fill();
            cb.beginText();
            cb.setFontAndSize(bfBold, 18);
            cb.setColorFill(BaseColor.WHITE);
            cb.showTextAligned(Element.ALIGN_CENTER, "IDENTIFICATION EMPLOYÉ", 200, 215, 0);
            cb.endText();

            // 2. Pied de Page (Lavande / Mauve)
            cb.setColorFill(new BaseColor(204, 194, 239));
            cb.rectangle(0, 0, 400, 55);
            cb.fill();
            cb.beginText();
            cb.setFontAndSize(bfBold, 22);
            cb.setColorFill(new BaseColor(34, 30, 94));
            cb.showTextAligned(Element.ALIGN_CENTER, "GROUPE FRAME", 200, 18, 0);
            cb.endText();

            // 3. Cadre Photo
            cb.setLineWidth(1.5f);
            cb.setColorStroke(new BaseColor(34, 30, 94));
            cb.rectangle(25, 75, 100, 105);
            cb.stroke();
            if (s.getPhotoPath() != null && !s.getPhotoPath().isEmpty()) {
                Image img = Image.getInstance(s.getPhotoPath());
                img.scaleToFit(98, 103);
                img.setAbsolutePosition(26, 76);
                doc.add(img);
            }

            // 4. Détails (ID, Nom, Poste)
            float startX = 140;
            BaseColor labelBg = new BaseColor(235, 230, 250); // Violet pâle

            drawLabel(cb, bfBold, "N° D'IDENTIFICATION", startX, 175, labelBg);
            drawValue(cb, bfReg, String.format("%09d", s.getId()), startX, 160);

            drawLabel(cb, bfBold, "NOM", startX, 135, labelBg);
            drawValue(cb, bfReg, (s.getPrenom() + " " + s.getNom()).toUpperCase(), startX, 120);

            drawLabel(cb, bfBold, "POSTE / EMPLOI", startX, 95, labelBg);
            drawValue(cb, bfReg, s.getPoste(), startX, 80);

            // 5. QR Code
            BarcodeQRCode qr = new BarcodeQRCode("FRAME_ID:" + s.getId(), 75, 75, null);
            Image qrImg = qr.getImage();
            qrImg.setAbsolutePosition(305, 80);
            doc.add(qrImg);

            doc.close();
            openFile(file);
        } catch (Exception e) {
            showError("Erreur PDF", "Erreur badge : " + e.getMessage());
        }
    }

    // --- Utilitaires de dessin ---
    private void drawLabel(PdfContentByte cb, BaseFont bf, String text, float x, float y, BaseColor bg) {
        cb.setColorFill(bg);
        cb.rectangle(x, y, 150, 12);
        cb.fill();
        cb.beginText();
        cb.setFontAndSize(bf, 8);
        cb.setColorFill(new BaseColor(60, 50, 130)); // Texte label
        cb.showTextAligned(Element.ALIGN_LEFT, text, x + 5, y + 3, 0);
        cb.endText();
    }

    private void drawValue(PdfContentByte cb, BaseFont bf, String text, float x, float y) {
        cb.beginText();
        cb.setFontAndSize(bf, 11);
        cb.setColorFill(BaseColor.DARK_GRAY);
        cb.showTextAligned(Element.ALIGN_LEFT, text, x + 5, y, 0);
        cb.endText();
    }

    @FXML
    private void onGeneratePDF() {
        // 1. Récupération de l'employé sélectionné dans la TableView
        Employe s = tableEmployes.getSelectionModel().getSelectedItem();

        if (s == null) {
            showError("Sélection requise", "Veuillez sélectionner un employé dans la liste pour générer son certificat.");
            return;
        }

        File file = new File("Certificat_Travail_" + s.getNom() + ".pdf");
        try {
            // Création du document A4
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            // --- DESIGN : Bordure élégante ---
            PdfContentByte cb = writer.getDirectContent();
            cb.setLineWidth(2f);
            cb.setColorStroke(new BaseColor(34, 30, 94)); // Bleu Marine assorti au badge
            cb.rectangle(30, 30, 535, 782);
            cb.stroke();

            // --- POLICES ---
            BaseFont bfBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            BaseFont bfReg = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfBold, 22, Font.UNDERLINE, new BaseColor(34, 30, 94));
            Font headerFont = new Font(bfBold, 14, Font.NORMAL, BaseColor.GRAY);
            Font bodyFont = new Font(bfReg, 12, Font.NORMAL, BaseColor.BLACK);
            Font boldBodyFont = new Font(bfBold, 12, Font.NORMAL, BaseColor.BLACK);

            // --- EN-TÊTE ---
            Paragraph brand = new Paragraph("WASILA GESTION / GROUPE FRAME", headerFont);
            brand.setAlignment(Element.ALIGN_RIGHT);
            doc.add(brand);

            doc.add(new Paragraph("\n\n")); // Espacement

            // --- TITRE DU DOCUMENT ---
            Paragraph title = new Paragraph("CERTIFICAT DE TRAVAIL", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            doc.add(new Paragraph("\n\n\n")); // Espacement

            // --- CORPS DU TEXTE (Récupération dynamique des infos) ---
            Paragraph intro = new Paragraph();
            intro.setAlignment(Element.ALIGN_JUSTIFIED);
            intro.setLeading(20f); // Interligne

            intro.add(new Chunk("Nous soussignés, ", bodyFont));
            intro.add(new Chunk("WASILA GESTION", boldBodyFont));
            intro.add(new Chunk(", certifions par la présente que :\n\n", bodyFont));

            intro.add(new Chunk("Monsieur / Madame : ", bodyFont));
            intro.add(new Chunk(s.getPrenom() + " " + s.getNom().toUpperCase(), boldBodyFont));
            intro.add(new Chunk("\nNuméro d'identification : ", bodyFont));
            intro.add(new Chunk(String.valueOf(s.getId()), boldBodyFont));
            intro.add(new Chunk("\nE-mail de contact : ", bodyFont));
            intro.add(new Chunk(s.getEmail(), bodyFont));

            intro.add(new Chunk("\n\na fait partie de notre effectif au poste de : ", bodyFont));
            intro.add(new Chunk(s.getPoste(), boldBodyFont));

            intro.add(new Chunk("\n\nCe certificat est délivré à l'intéressé(e) pour servir et valoir ce que de droit.", bodyFont));

            doc.add(intro);

            doc.add(new Paragraph("\n\n\n\n")); // Espacement pour la signature

            // --- DATE ET SIGNATURE ---
            String dateJour = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date());
            Paragraph signature = new Paragraph();
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.add(new Chunk("Fait à Casablanca, le " + dateJour + "\n", bodyFont));
            signature.add(new Chunk("La Direction Générale\n", boldBodyFont));
            signature.add(new Chunk("(Signature et Cachet)", headerFont));
            doc.add(signature);

            // --- BAS DE PAGE ---
            Paragraph footer = new Paragraph("WASILA GESTION - Excellence en Ressources Humaines", new Font(bfReg, 9, Font.ITALIC, BaseColor.LIGHT_GRAY));
            footer.setSpacingBefore(150f);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();

            // Ouverture automatique du fichier
            openFile(file);
            showInfo("Génération réussie", "Le certificat de " + s.getNom() + " a été généré avec succès.");

        } catch (Exception e) {
            showError("Erreur PDF", "Impossible de générer le certificat : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NAVIGATION ---
    private void navigate(String path) {
        try {
            Stage stage = (Stage) tableEmployes.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            stage.getScene().setRoot(loader.load());
        } catch (IOException e) { showError("Navigation", "Vue introuvable."); }
    }

    @FXML private void goToDashboard() { navigate("/com/example/javaproject/dashboard-view.fxml"); }
    @FXML private void goToFactures() { navigate("/com/example/javaproject/facture-view.fxml"); }
    @FXML private void goToProduits() { navigate("/com/example/javaproject/produit-view.fxml"); }
    @FXML private void goToProjets() { navigate("/com/example/javaproject/projet-view.fxml"); }
    @FXML private void goToEmployes() { navigate("/com/example/javaproject/employe-view.fxml"); }
    @FXML private void onLogout() { navigate("/com/example/javaproject/hello-view.fxml"); }
    @FXML
    private void goToChatbot() {
        navigate("/com/example/javaproject/chatbot-page-view.fxml");
    }
    private void openFile(File file) {
        try { if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file); } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearFields() {
        nomField.clear(); prenomField.clear(); emailField.clear(); posteField.clear();
        selectedPhotoPath = "";
    }

    private void showInfo(String title, String content) { new Alert(Alert.AlertType.INFORMATION, content).showAndWait(); }
    private void showError(String title, String content) { new Alert(Alert.AlertType.ERROR, content).showAndWait(); }
}