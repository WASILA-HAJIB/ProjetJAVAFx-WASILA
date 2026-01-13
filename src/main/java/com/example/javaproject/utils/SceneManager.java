package com.example.javaproject.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SceneManager {

    /**
     * Méthode statique pour changer de page FXML
     * @param event L'événement du bouton (ActionEvent)
     * @param fxmlFile Le nom du fichier .fxml (ex: "dashboard-view.fxml")
     */
    public static void switchScene(ActionEvent event, String fxmlFile) {
        try {

            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    SceneManager.class.getResource("/com/example/javaproject/" + fxmlFile)
            ));


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("Erreur de navigation vers : " + fxmlFile);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("Fichier FXML introuvable : " + fxmlFile);
        }
    }
}
