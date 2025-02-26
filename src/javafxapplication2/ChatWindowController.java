package javafxapplication2;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Scanner;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;


public class ChatWindowController {
    @FXML
    private ListView<String> chatListView;
    @FXML
    private VBox chatMessagesContainer;
    @FXML
    private TextField messageInputField;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private TextField addContactField;

    private ObservableList<String> chatList = FXCollections.observableArrayList();
    
    private String username;
    
    private String selectedChat;
    
    private Timeline chatReloadTimer, contactReloadTimer;
        
    // Added to keep track of how many messages and contacts have already been loaded
    private int lastMessageCount = 0, lastContactCount = 0;
    
    // Flag for first load of a chat
    private boolean isFirstLoad = false;

    public ChatWindowController() {
        this.chatList = FXCollections.observableArrayList();
    }
    
    public void setUsername(String usernam) {
        this.username = usernam;
        postInitialize();
    }
    
    public String getUsername() {
        return username;
    }
    

    public void initialize() {
        messageInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSendMessage();
            }
        });
        addContactField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleAddContact();
            }
        });
        chatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // When a chat is selected, start the reload timer.
            if (newVal != null) {
                // Clear previous messages if needed and set the selected chat.
                selectedChat = newVal;
                chatMessagesContainer.getChildren().clear();
                lastMessageCount = 0;
                isFirstLoad = true;
                addMessage("Welcome to your chat with " + selectedChat, "system");
                startChatReloadTimer();
            } else {
                // If no chat is selected, stop the timer.
                stopChatReloadTimer();
            }
        });
    }
    
    // Start the timer to reload chat messages every second.
    private void startChatReloadTimer() {
        // If a timer is already running, stop it first.
        if (chatReloadTimer != null) {
            chatReloadTimer.stop();
        }
        chatReloadTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            loadChat();  // Refresh the chat messages every second.
        }));
        chatReloadTimer.setCycleCount(Timeline.INDEFINITE);
        chatReloadTimer.play();
    }
    
    // Stop the auto-refresh timer.
    private void stopChatReloadTimer() {
        if (chatReloadTimer != null) {
            chatReloadTimer.stop();
        }
    }
    
    private void startContactReloadTimer() {
        if (contactReloadTimer != null) {
            contactReloadTimer.stop();  // Contact reload timer stopped
        }
        // Starting contact reload timer
        contactReloadTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            fetchContactsForUser();  // Reloading contacts, Refresh contacts every 1 second
        }));
        contactReloadTimer.setCycleCount(Timeline.INDEFINITE);
        contactReloadTimer.play();
    }

    // Custom method for username-dependent initialization
    private void postInitialize() {
        if (username != null) {
            fetchContactsForUser();
        }
    }
    
    private void fetchContactsForUser() {
        String response = showContacts("http://localhost:5000/ShowContacts", username);

        try {
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            JsonArray contactsArray = jsonObject.getAsJsonArray("contacts");
            String status = jsonObject.get("status").getAsString();

            if ("Success".equals(status)) {
                List<String> contactsList = new ArrayList<>();
                for (int i = 0; i < contactsArray.size(); i++) {
                    contactsList.add(contactsArray.get(i).getAsString());
                }
                int newContactCount = contactsList.size();
                if (newContactCount > lastContactCount) { // Check if a new contact was added
                    chatList.setAll(contactsList);
                    lastContactCount = newContactCount; // Update counter
                }
            } else {
                System.out.println("Error: Unable to fetch contacts.");
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing JSON response: " + e.getMessage());
        }

        chatListView.setItems(chatList);
        Platform.runLater(() -> {
            startContactReloadTimer();
        });
    }

    private class ChatMessage {
        String message;
        String sender;
    }
    
    private void loadChat() {
        if (selectedChat != null) {
            String response = loadMessages("http://localhost:5000/loadMessages", username, selectedChat);
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            JsonArray messagesArray = jsonObject.getAsJsonArray("messages");

            int newCount = messagesArray.size();
            if (newCount > lastMessageCount) {
                for (int i = lastMessageCount; i < newCount; i++) {
                    ChatMessage msg = new Gson().fromJson(messagesArray.get(i), ChatMessage.class);
                    if (!isFirstLoad && lastMessageCount > 0  && msg.sender.equals("received")) {
                        AudioClip newMessageSound = new AudioClip(getClass().getResource("/Sounds/notification-2-269292.mp3").toExternalForm());
                        newMessageSound.play();
                    }
                    addMessage(msg.message, msg.sender);
                }
                lastMessageCount = newCount;
            }
            Platform.runLater(() -> {
                // On first load, always scroll to bottom
                if (isFirstLoad) {
                    chatScrollPane.setVvalue(1.0);
                    isFirstLoad = false;
                } else {
                    // On subsequent reloads, scroll only if user is near the bottom
                    if (chatScrollPane.getVvalue() > 0.9) {
                        chatScrollPane.setVvalue(1.0);
                    }
                }
            });
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInputField.getText();
        if (message.trim().isEmpty()) return;
        String response = saveMessage("http://localhost:5000/SaveMessage", username,selectedChat,message);        
        messageInputField.clear();
    }

    @FXML
    private void handleAddContact() {
        String newContact = addContactField.getText();
        if (newContact == null || newContact.trim().isEmpty()) return;
        if (!chatList.contains(newContact)) {
            String response = loadMessages("http://localhost:5000/AddContacts", username, newContact);
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            String status = jsonObject.get("status").getAsString();
            String messagesArray = jsonObject.get("message").getAsString();
            if ("Success".equals(status)){
                AudioClip alertSound = new AudioClip(getClass().getResource("/Sounds/success-1-6297.mp3").toExternalForm());
                alertSound.play();
                chatList.add(newContact);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle( newContact + " added as a new contact!");
                alert.setHeaderText(null);
                alert.setContentText(messagesArray);
                alert.showAndWait();
                addContactField.clear();
            }else{
                AudioClip alertSound = new AudioClip(getClass().getResource("/Sounds/message-alert-190042.mp3").toExternalForm());
                alertSound.play();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("User not found");
                alert.setHeaderText(null);
                alert.setContentText(messagesArray);
                alert.showAndWait();
            }
        } else {
            AudioClip alertSound = new AudioClip(getClass().getResource("/Sounds/message-alert-190042.mp3").toExternalForm());
            alertSound.play();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Duplicate Contact");
            alert.setHeaderText(null);
            alert.setContentText("This contact is already in your list.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleExitChat() {
        // Stop the auto-refresh timer.
        stopChatReloadTimer();

        // Clear the selected chat and the chat messages.
        selectedChat = null;
        chatMessagesContainer.getChildren().clear();
        lastMessageCount = 0;
        chatListView.getSelectionModel().clearSelection();
    }

    private void addMessage(String text, String type) {
        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add(type);
        HBox messageContainer = new HBox(messageLabel);
        messageContainer.getStyleClass().add("message-box");
        messageContainer.setAlignment(type.equals("sent") ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
        chatMessagesContainer.getChildren().add(messageContainer);
    }
    
    private String showContacts(String urlString, String username) {
        try {
            // Create URL and Connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure Connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON Payload
            String jsonPayload = String.format("{\"username\": \"%s\"}", username);

            // Write Payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "Error: Server responded with " + responseCode;
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error:Unable to connect to the server";
        }
    }
     
    private String saveMessage(String urlString, String username, String contact, String message){
        try {
            // Create URL and Connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure Connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON Payload
            String jsonPayload = String.format("{\"username\": \"%s\", \"contact\": \"%s\", \"message\": \"%s\"}", username, contact, message);

            // Write Payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "Error: Server responded with " + responseCode;
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error:Unable to connect to the server";
        }
    }
    
    //Also used for adding contacts through handleAddContact() by sending the username and the name of the new contact to the Back-End
    private String loadMessages(String urlString, String username, String contact){
        try {
            // Create URL and Connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure Connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON Payload
            String jsonPayload = String.format("{\"username\": \"%s\", \"contact\": \"%s\"}", username, contact);

            // Write Payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return "Error: Server responded with " + responseCode;
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error:Unable to connect to the server";
        }
    }
}



/*
|    To-Do List:
|   -> 
|   ->
|   ->
|   ->
*/