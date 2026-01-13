package com.example.javaproject.controllers;

import com.example.javaproject.models.ChatMessage;
import com.example.javaproject.services.ChatbotService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import com.example.javaproject.utils.SceneManager;
public class ChatbotController {
    @FXML private VBox chatContainer;
    @FXML private TextField questionField;
    @FXML private TextField searchField;
    @FXML private ScrollPane chatScrollPane;
    @FXML private ListView<Integer> sessionListView;

    // ARCHITECTURE RESPECTÉE : Uniquement le Service est appelé
    private final ChatbotService service = new ChatbotService();
    private int currentSessionId = 1;
    private boolean showingFavorites = false;

    //scroll automatique
    @FXML
    public void initialize() {
        chatContainer.heightProperty().addListener((obs, oldVal, newVal) -> chatScrollPane.setVvalue(1.0));

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> loadView());
        }

        if (sessionListView != null) {
            sessionListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentSessionId = newVal;
                    loadView();
                }
            });
            setupCellFactory();
        }
        refreshSessions();
        loadView();
    }

    private void loadView() {
        chatContainer.getChildren().clear();
        List<ChatMessage> list;
        if (showingFavorites) {
            list = service.getFavoritesOnly(currentSessionId);
        } else {
            list = service.getFullHistory(currentSessionId, searchField != null ? searchField.getText() : "");
        }
        list.forEach(this::displayMessage);
    }

    private void refreshSessions() {
        if (sessionListView != null) {
            sessionListView.getItems().setAll(service.getAvailableSessions());
        }
    }

    @FXML
    public void handleSend() {
        String query = questionField.getText().trim();
        if (query.isEmpty()) return;

        // 1. UI + Sauvegarde USER via Service
        service.saveNewMessage("USER", query, currentSessionId);
        questionField.clear();
        loadView();

        // 2. IA via Service (Thread séparé)
        new Thread(() -> {
            String response = service.generateAIResponse(query);
            Platform.runLater(() -> {
                service.saveNewMessage("AI", response, currentSessionId);
                refreshSessions();
                loadView();
            });
        }).start();
    }

    @FXML
    public void handleNewChat() {
        List<Integer> sessions = service.getAvailableSessions();
        currentSessionId = sessions.isEmpty() ? 1 : sessions.get(0) + 1;
        chatContainer.getChildren().clear();
        refreshSessions();
    }

    @FXML
    private void toggleFavoritesFilter(ActionEvent event) {
        showingFavorites = !showingFavorites;
        loadView();
    }

    private void displayMessage(ChatMessage msg) {
        Label label = new Label(msg.getContent());
        label.setWrapText(true);
        label.setMaxWidth(400);

        boolean isUser = msg.getSender().equalsIgnoreCase("USER");
        label.setStyle(isUser ? "-fx-background-color: #2D6A4F; -fx-text-fill: white; -fx-background-radius: 15;"
                : "-fx-background-color: #E8F5E9; -fx-text-fill: #1B4332; -fx-background-radius: 15;");

        Button favBtn = new Button(msg.isFavorite() ? "❤️" : "🤍");
        favBtn.setOnAction(e -> {
            service.updateFavoriteStatus(msg.getId(), !msg.isFavorite());
            loadView();
        });

        HBox line = new HBox(10, isUser ? favBtn : label, isUser ? label : favBtn);
        line.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        line.setPadding(new Insets(5));
        chatContainer.getChildren().add(line);
    }

    @FXML
    private void clearHistory(ActionEvent event) {
        service.deleteEverything();
        refreshSessions();
        loadView();
    }

    private void setupCellFactory() {
        sessionListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : "💬 Chat n° " + item);
            }
        });
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        // Plus besoin de "throws IOException" ni de try-catch ici, le manager s'en occupe
        SceneManager.switchScene(event, "dashboard-view.fxml");
    }
}