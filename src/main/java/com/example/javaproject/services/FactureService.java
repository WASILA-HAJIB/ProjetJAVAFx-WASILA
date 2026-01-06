package com.example.javaproject.services;

import com.example.javaproject.dao.FactureDAO;
import com.example.javaproject.models.Facture;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class FactureService {
    private final FactureDAO dao = new FactureDAO();

    public List<Facture> recupererToutesLesFactures() {
        return dao.getAll();
    }

    public void creerFacture(String client, String montantStr, String statut, LocalDate date) throws Exception {
        if (montantStr == null || montantStr.isEmpty()) throw new IllegalArgumentException("Le montant est obligatoire.");
        double montant = Double.parseDouble(montantStr);

        // Validation : Le montant ne doit pas être négatif
        if (montant < 0) {
            throw new IllegalArgumentException("Le montant de la facture ne peut pas être négatif.");
        }
        dao.add(client, montant, statut, date);
    }

    public void supprimerFacture(int id) throws Exception {
        dao.delete(id);
    }

    public double calculerTotalPaye(List<Facture> list) {
        return list.stream()
                .filter(f -> "Payé".equalsIgnoreCase(f.getStatut()))
                .mapToDouble(Facture::getMontant)
                .sum();
    }

    public long compterNonPaye(List<Facture> list) {
        return list.stream()
                .filter(f -> "Non Payé".equalsIgnoreCase(f.getStatut()))
                .count();
    }

    public double calculerTauxRecouvrement(List<Facture> list) {
        double total = list.stream().mapToDouble(Facture::getMontant).sum();
        return (total == 0) ? 0 : (calculerTotalPaye(list) / total) * 100;
    }

    // --- GÉNÉRATION PDF (CONFORME À L'IMAGE DEMANDÉE) ---
    public String genererPdfFactureDetaillee(Facture f) throws Exception {
        String path = System.getProperty("java.io.tmpdir") + File.separator + "Facture_WASILA_" + f.getId() + ".pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(path));
        document.open();

        // En-tête : WASILAGestion en bleu
        Font fontBleu = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.BLUE);
        document.add(new Paragraph("WASILAGestion", fontBleu));
        document.add(new Paragraph("Services de Gestion Digitale\nCasablanca, Maroc\nwww.wasilagestion.ma"));
        document.add(new com.itextpdf.text.pdf.draw.LineSeparator());

        // Titre et Date alignés à droite
        Paragraph pTitre = new Paragraph("\nFACTURE OFFICIELLE N°2026-" + f.getId(),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
        pTitre.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTitre);

        Paragraph pDate = new Paragraph("Émise le : " + LocalDate.now());
        pDate.setAlignment(Element.ALIGN_RIGHT);
        document.add(pDate);

        // Corps de la facture
        document.add(new Paragraph("\nDESTINATAIRE : " + f.getClientNom().toUpperCase()));
        document.add(new Paragraph("\nLa présente facture atteste de la réalisation des prestations de conseil et de gestion RH pour la période en cours."));

        // Montant Total aligné à droite
        Paragraph pTotal = new Paragraph("\n\nTOTAL À RÉGLER : " + String.format("%.2f", f.getMontant()) + " €",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
        pTotal.setAlignment(Element.ALIGN_RIGHT);
        document.add(pTotal);

        // Cachet de statut (Vert pour Payé, Rouge pour Non Payé)
        String texteCachet = f.getStatut().equalsIgnoreCase("Payé") ? "PAYÉ - MERCI" : "À RÉGLER";
        BaseColor couleurCachet = f.getStatut().equalsIgnoreCase("Payé") ? BaseColor.GREEN : BaseColor.RED;
        Paragraph cachet = new Paragraph("\n\n" + texteCachet,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, couleurCachet));
        document.add(cachet);

        document.close();
        ouvrirFichierAuto(path);
        return path;
    }

    // --- GÉNÉRATION EXCEL CORRIGÉE ---
    public String genererExcelFacture(Facture f) throws Exception {
        String nomNettoye = f.getClientNom().replaceAll("[^a-zA-Z0-9]", "_");
        String path = System.getProperty("java.io.tmpdir") + File.separator + "Facture_" + nomNettoye + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Détails Facture");

            // Styles
            CellStyle styleLabel = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fontBold = workbook.createFont();
            fontBold.setBold(true);
            styleLabel.setFont(fontBold);

            // Données
            Object[][] data = {
                    {"WASILAGestion", "FACTURE CLIENT"},
                    {"ID Facture", f.getId()},
                    {"Client", f.getClientNom()},
                    {"Montant", f.getMontant() + " €"},
                    {"Statut", f.getStatut().toUpperCase()},
                    {"Échéance", f.getDateEcheance().toString()}
            };

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(data[i][0].toString());
                cell0.setCellStyle(styleLabel);
                row.createCell(1).setCellValue(data[i][1].toString());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }
        }
        ouvrirFichierAuto(path);
        return path;
    }

    private void ouvrirFichierAuto(String path) {
        try {
            File file = new File(path);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            System.err.println("Erreur d'ouverture : " + e.getMessage());
        }
    }
}