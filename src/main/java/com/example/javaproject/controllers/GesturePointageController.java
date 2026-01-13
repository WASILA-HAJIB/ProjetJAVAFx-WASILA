package com.example.javaproject.controllers;

import com.example.javaproject.services.PointageService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GesturePointageController {

    @FXML private ImageView videoFrame;
    @FXML private Label statusLabel;

    private VideoCapture capture;
    private ScheduledExecutorService timer;
    private final PointageService pointageService = new PointageService();
    private int employeeId;

    private boolean isActionProcessed = false;
    private boolean detectionReady = false;
    private Mat background = new Mat();
    private int stableFrames = 0;
    private static final int REQUIRED_FRAMES = 8;
    private long startTime;

    private Scalar finalColor = null;
    private String finalText = "";

    static { nu.pattern.OpenCV.loadLocally(); }

    public void initData(int empId) {
        this.employeeId = empId;
        startTime = System.currentTimeMillis();
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            statusLabel.setText("❌ Caméra introuvable");
            return;
        }
        startCamera();
    }

    private void startCamera() {
        timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(() -> {
            Mat frame = new Mat();
            if (!capture.read(frame)) return;
            Core.flip(frame, frame, 1);

            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray, gray);

            if (!detectionReady) {
                Imgproc.putText(frame, "Initialisation environnement...", new Point(30, 50),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 255), 2);
                if (System.currentTimeMillis() - startTime > 2500) {
                    gray.copyTo(background);
                    detectionReady = true;
                }
                Platform.runLater(() -> videoFrame.setImage(matToImage(frame)));
                return;
            }

            if (!isActionProcessed) {
                analyzeFrame(frame, gray);
            } else {
                drawFinalScreen(frame);
            }

            Platform.runLater(() -> videoFrame.setImage(matToImage(frame)));
        }, 0, 80, TimeUnit.MILLISECONDS);
    }

    private void analyzeFrame(Mat frame, Mat grayFrame) {
        Mat diff = new Mat();
        Core.absdiff(background, grayFrame, diff);
        Imgproc.threshold(diff, diff, 45, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(diff, diff, new Mat(), new Point(-1,-1), 2);

        int w = frame.cols();
        int h = frame.rows();
        Rect entreeZone = new Rect(0, h/2, w/4, h/2);
        Rect sortieZone = new Rect(3*w/4, h/2, w/4, h/2);

        Imgproc.rectangle(frame, entreeZone, new Scalar(0, 255, 0), 2);
        Imgproc.rectangle(frame, sortieZone, new Scalar(0, 0, 255), 2);

        String resE = evaluateZone(diff.submat(entreeZone), frame, entreeZone);
        String resS = evaluateZone(diff.submat(sortieZone), frame, sortieZone);

        if (resE.equals("MAIN") || resS.equals("MAIN")) {
            stableFrames++;
            if (stableFrames >= REQUIRED_FRAMES) {
                String type = resE.equals("MAIN") ? "ENTREE" : "SORTIE";
                Platform.runLater(() -> executeAction(type));
            }
        } else if (resE.equals("STYLO") || resS.equals("STYLO")) {
            stableFrames = 0;
            Platform.runLater(() -> {
                statusLabel.setText("⚠️ OBJET DÉTECTÉ : STYLO");
                statusLabel.setStyle("-fx-text-fill: yellow; -fx-font-weight: bold; -fx-background-color: black;");
            });
        } else {
            stableFrames = 0;
            Platform.runLater(() -> statusLabel.setText("Système prêt : Présentez votre main"));
        }
    }

    private String evaluateZone(Mat zoneDiff, Mat frame, Rect rect) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(zoneDiff, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint c : contours) {
            double area = Imgproc.contourArea(c);
            if (area < 2500) continue;

            Rect box = Imgproc.boundingRect(c);
            double ratio = (double) box.width / box.height;

            Point[] pts = c.toArray();
            for (Point p : pts) { p.x += rect.x; p.y += rect.y; }
            MatOfPoint contourGlobal = new MatOfPoint(pts);

            // --- CAS DU STYLO (Effet WOW avec étiquette) ---
            if (ratio < 0.3 || ratio > 3.0) {
                // 1. Dessiner le contour du stylo en jaune
                Imgproc.drawContours(frame, List.of(contourGlobal), -1, new Scalar(0, 255, 255), 2);

                // 2. Créer l'étiquette au-dessus
                String label = "OBJET : STYLO";
                Point textOrg = new Point(box.x + rect.x, box.y + rect.y - 10);

                // Dessiner un petit rectangle plein en jaune pour le fond du texte
                Imgproc.rectangle(frame, new Point(textOrg.x, textOrg.y - 20),
                        new Point(textOrg.x + 150, textOrg.y + 5), new Scalar(0, 255, 255), -1);

                // Ecrire le texte en noir sur le fond jaune
                Imgproc.putText(frame, label, textOrg, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0), 2);

                return "STYLO";
            }
            // --- CAS DE LA MAIN ---
            else if (area > 5500 && ratio > 0.4 && ratio < 2.0) {
                Imgproc.drawContours(frame, List.of(contourGlobal), -1, new Scalar(0, 255, 0), 3);
                Imgproc.putText(frame, "MAIN DETECTEE", new Point(box.x + rect.x, box.y + rect.y - 10),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 0), 2);
                return "MAIN";
            }
        }
        return "NONE";
    }

    private void executeAction(String type) {
        if (isActionProcessed) return;
        isActionProcessed = true;

        if (type.equals("ENTREE")) {
            pointageService.enregistrerEntree(employeeId);
            finalText = "ENTREE ENREGISTREE";
            finalColor = new Scalar(0, 150, 0);
        } else {
            pointageService.enregistrerSortie(employeeId);
            finalText = "SORTIE ENREGISTREE";
            finalColor = new Scalar(0, 0, 150);
        }

        Toolkit.getDefaultToolkit().beep();
        Platform.runLater(() -> statusLabel.setText(finalText));

        new Thread(() -> {
            try { Thread.sleep(3000); } catch (Exception ignored) {}
            Platform.runLater(this::closeAction);
        }).start();
    }

    private void drawFinalScreen(Mat frame) {
        if (finalColor == null) return;
        Mat overlay = new Mat(frame.size(), frame.type(), finalColor);
        Core.addWeighted(overlay, 0.5, frame, 0.5, 0, frame);
        Imgproc.putText(frame, finalText, new Point(frame.cols()/8.0, frame.rows()/2.0),
                Imgproc.FONT_HERSHEY_DUPLEX, 1.4, new Scalar(255, 255, 255), 3);
    }

    @FXML
    public void closeAction() {
        if (timer != null) timer.shutdown();
        if (capture != null) capture.release();
        if (videoFrame.getScene() != null && videoFrame.getScene().getWindow() != null) {
            ((Stage) videoFrame.getScene().getWindow()).close();
        }
    }

    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}