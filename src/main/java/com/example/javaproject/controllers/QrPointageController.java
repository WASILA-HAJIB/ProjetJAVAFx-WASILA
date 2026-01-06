package com.example.javaproject.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.example.javaproject.services.PointageService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class QrPointageController {
    @FXML private ImageView webcamView;
    @FXML private Label qrStatusLabel;
    @FXML private VBox successOverlay;

    private Webcam webcam;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private final PointageService service = new PointageService();
    private int empId;
    private String savedNom, savedPrenom;

    public void setUserInfo(int id, String nom, String prenom) {
        this.empId = id;
        this.savedNom = nom;
        this.savedPrenom = prenom;
        System.out.println("[DEBUG] setUserInfo reçu ID: " + id);
        initWebcam();
    }

    private void initWebcam() {
        new Thread(() -> {
            try {
                webcam = Webcam.getDefault();
                if (webcam != null) {
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.open();
                    isRunning.set(true);
                    launchScanningThread();
                    Platform.runLater(() -> qrStatusLabel.setText("Caméra prête. Scannez l'ID: " + empId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void launchScanningThread() {
        Thread thread = new Thread(() -> {
            while (isRunning.get()) {
                if (webcam == null || !webcam.isOpen()) break;
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> webcamView.setImage(fxImage));

                    try {
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                        Result result = new MultiFormatReader().decode(bitmap);

                        if (result != null) {
                            String code = result.getText().trim();
                            // DEBUG CONSOLE CRUCIAL
                            System.out.println("[SCAN] Lu: " + code + " | Attendu: " + empId);

                            if (code.equals(String.valueOf(empId))) {
                                processPointage();
                            }
                        }
                    } catch (NotFoundException ignored) {}
                }
                try { Thread.sleep(150); } catch (InterruptedException e) { break; }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void processPointage() {
        if (!isRunning.get()) return; // Évite les doubles scans
        isRunning.set(false);

        // --- AJOUT DU BIP ---
        // On utilise Toolkit pour un bip système simple et efficace
        java.awt.Toolkit.getDefaultToolkit().beep();

        Platform.runLater(() -> {
            System.out.println("[DEBUG] Déclenchement de l'animation...");
            successOverlay.setVisible(true); // Affiche le check vert
            qrStatusLabel.setText("Scan réussi !");

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> {
                System.out.println("[DEBUG] Appel au service pour l'ID: " + empId);

                // Exécution de la logique automatique (Entrée/Pause/Reprise/Sortie)
                String resultat = service.enregistrerPointageAutomatique(empId);
                System.out.println("[DEBUG] Résultat BDD: " + resultat);

                successOverlay.setVisible(false);
                stopWebcam();
                forceGoBack(); // Retourne à l'accueil
            });
            pause.play();
        });
    }

    private void forceGoBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/javaproject/welcome-view.fxml"));
            Parent root = loader.load();
            WelcomeController welcomeCtrl = loader.getController();
            welcomeCtrl.setUserInfo(this.empId, this.savedNom, this.savedPrenom);
            Stage stage = (Stage) webcamView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goBack(ActionEvent event) {
        stopWebcam();
        forceGoBack();
    }

    private void stopWebcam() {
        isRunning.set(false);
        if (webcam != null) webcam.close();
    }
}