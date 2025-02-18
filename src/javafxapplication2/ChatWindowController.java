
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
import java.util.Arrays;
import java.util.Scanner;

import java.util.*;
import java.util.regex.*;

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
        chatListView.setOnMouseClicked(event -> loadChat());
    }
    
    
    // Custom method for username-dependent initialization
    private void postInitialize() {
        if (username != null) {
            System.out.println("Initializing with username: " + username);

            // Simulate fetching data from the backend
            fetchContactsForUser();
        }
    }
    
    private void fetchContactsForUser() {
        System.out.println("Fetching contacts for user: " + username);        
        
        // Here you would make the backend call to get the user's contacts
        String response = showContacts("http://localhost:5000/ShowContacts", username);
        String[] stringarray = response.split(":");
        //System.out.println("HERE:\n1: " + stringarray[0] + "\n2: " + stringarray[1] + "\n2: " + stringarray[2]);
        String statusValue = stringarray[2];        
        
        // Extract status inside double quotes
        Pattern quotePattern1 = Pattern.compile("\"(.*?)\"");
        Matcher quoteMatcher1 = quotePattern1.matcher(statusValue);
        String status = quoteMatcher1.find() ? quoteMatcher1.group(1) : "";
        // Print result
        //System.out.println("statusValue: " + status);


        // Extract contacts inside double quotes
        String input = stringarray[1];
        // Extract content inside brackets
        Pattern bracketPattern = Pattern.compile("\\[(.*?)\\]");
        Matcher bracketMatcher = bracketPattern.matcher(input);
        String insideBrackets = bracketMatcher.find() ? bracketMatcher.group(1) : "";
        // Extract words inside double quotes
        Pattern quotePattern = Pattern.compile("\"(.*?)\"");
        Matcher quoteMatcher = quotePattern.matcher(insideBrackets);
        List<String> extractedList = new ArrayList<>();
        while (quoteMatcher.find()) {
            extractedList.add(quoteMatcher.group(1));
        }
        // Convert List to String Array
        String[] Contacts = extractedList.toArray(new String[0]);
        // Print result
        //System.out.println(Arrays.toString(Contacts));
        //System.out.println("Array: " + Arrays.toString(Contacts));
        

        if (status.equals("Success")){
            boolean addAll = chatList.addAll(Arrays.asList(Contacts));
            if (addAll == false){
                System.out.println("Error: Can't put contacts on chatList");
            }
        }else{
            System.out.println("Status: False"); //represents the error message
        }
        //System.out.println("ChatList: " + Arrays.toString(Contacts));
        chatListView.setItems(chatList);
        //chatListView.setOnMouseClicked(event -> loadChat());
    }

    private void loadChat() {
        String selectedChat = chatListView.getSelectionModel().getSelectedItem();
        if (selectedChat != null) {
            chatMessagesContainer.getChildren().clear();
            addMessage("Welcome to your chat with " + selectedChat, "system");
        }else{
            //Load Chats
        }
    }  //After saving messages,build this

    @FXML
    private void handleSendMessage() {
        String message = messageInputField.getText();
        if (message.trim().isEmpty()) return;

        addMessage("You: " + message, "sent");
        messageInputField.clear();

        simulateReply();
    }

    @FXML
    private void handleAddContact() {
        String newContact = addContactField.getText();
        if (newContact == null || newContact.trim().isEmpty()) return;

        if (!chatList.contains(newContact)) {
            chatList.add(newContact);
            addContactField.clear();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Duplicate Contact");
            alert.setHeaderText(null);
            alert.setContentText("This contact is already in your list.");
            alert.showAndWait();
        }
    }

    private void simulateReply() {
        addMessage("Hello! This is a simulated reply.", "received");
    }

    private void addMessage(String text, String type) {
        Label messageLabel = new Label(text);
        messageLabel.getStyleClass().add(type);
        HBox messageContainer = new HBox(messageLabel);
        messageContainer.getStyleClass().add("message-box");
        messageContainer.setAlignment(type.equals("sent") ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);

        chatMessagesContainer.getChildren().add(messageContainer);

        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
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

            //System.out.println("Response from HTTP: " + response.toString());
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error:Unable to connect to the server";
        }
    }
}

//----------------------------------------------------------------------------------------------
//Need to add:
//  - Function that will check if the user that we took from the python and SQL has a chat with his contacts
//      - When we click on a Chat, this function will run and if he has a chat stored IN THE PAST with the contact,it will load the messages in the chat window
//      - after that the sending message part will be used as it is
//      - I will replace the simulate_response() function with a function that :
//          - Will check in real time if the database has been updated with a new received message
//          - If there is a new message, it will print it in the chat window
//  - Function that will send the "sended messages" to the BACKEND (PYTHON) and through it there will be stored in the DATABASE SQL
//----------------------------------------------------------------------------------------------
