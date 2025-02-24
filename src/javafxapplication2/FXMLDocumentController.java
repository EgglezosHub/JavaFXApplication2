package javafxapplication2;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import static javafx.application.Application.launch;



class HttpResponse {
    private String status;
    private String message;

    public HttpResponse(String response) {
        String[] stringarray = response.split(":");
        String statusValue = stringarray[0];
        String messageValue = stringarray[1];
        
        this.status = statusValue;
        this.message = messageValue;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
}

public class FXMLDocumentController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginMessageLabel; // Label for login messages
    @FXML
    private Label registrationMessageLabel; // Label for registration messages
 
    // Registration Form Fields
    @FXML
    private TextField newUsernameField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button submitRegisterButton;
  
    // Form Containers
    @FXML
    private VBox loginForm;
    @FXML
    private VBox registrationForm;
   
    // Label for page title (Login/Register)
    @FXML
    private Label pageTitle;

    // This method will handle the "Login" button click
    @FXML
    private void handleLoginButtonAction(ActionEvent event) {

        String username = usernameField.getText();
        String password = passwordField.getText();
        
        HttpResponse response = makeHttpRequest("http://localhost:5000/login", username, password);
        
        System.out.println(response.toString());
        System.out.println("Status: " + response.getStatus());
        System.out.println("Message: " + response.getMessage());
        System.out.println("Hello from Under");        

        if (response.getStatus().equals("Success")) {
            try{
                System.out.println("Login successful!");
                loginMessageLabel.setText("Login successful!");
                loginMessageLabel.setStyle("-fx-text-fill: green;");
                loginMessageLabel.setVisible(true);

                // Load the ChatWindow.fxml file (main app)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafxapplication2/ChatWindow.fxml"));
                Parent chatWindowRoot = loader.load();
                
                // Get the ChatWindowController
                ChatWindowController chatWindowController = loader.getController();

                // Pass the username to the ChatWindowController
                chatWindowController.setUsername(username);

                // Create a new scene for the chat window
                Scene chatScene = new Scene(chatWindowRoot, 1000, 600);
                chatScene.getStylesheets().add(getClass().getResource("/javafxapplication2/chatStyle.css").toExternalForm());

                // Get the current stage and set the new scene
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(chatScene);
                stage.setTitle("Cross-Platform Messaging App - Chat Window");
                stage.getIcons().add(new Image("/Images/icon.png"));

                stage.setResizable(false);
                
            } catch(Exception e) {
                System.out.println("Hello from inside");
                e.printStackTrace();
                loginMessageLabel.setText("Failed to load the main application.");
                loginMessageLabel.setStyle("-fx-text-fill: red;");
                loginMessageLabel.setVisible(true);
            }
        } else {
            System.out.println("Invalid username or password.");
            loginMessageLabel.setText("Invalid username or password.");
            loginMessageLabel.setStyle("-fx-text-fill: red;");
            loginMessageLabel.setVisible(true);
        }
    }


    // This method will handle the "Register" button click (Show registration form)
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        loginForm.setVisible(false);  // Hide login form
        loginForm.setManaged(false); // Ensures it does not take space
        registrationForm.setVisible(true);  // Show registration form
        registrationForm.setManaged(true); // Ensures it takes space
        registrationMessageLabel.setVisible(false);
        pageTitle.setText("Register");
        registrationMessageLabel.setText(""); // Clear any previous registration message
    }



    // This method will handle the "Back to Login" button click (Back to login form)
    @FXML
    private void handleBackToLoginButtonAction(ActionEvent event) {
        registrationForm.setVisible(false);  // Hide registration form
        registrationForm.setManaged(false); // Ensures it does not take space
        loginForm.setVisible(true);  // Show login form
        loginForm.setManaged(true); // Ensures it takes space
        loginMessageLabel.setVisible(false);
        pageTitle.setText("Login");
        loginMessageLabel.setText(""); // Clear any previous login message
    }



    // This method will handle the registration form submission
    @FXML
    private void handleRegisterSubmit(ActionEvent event) {
        String newUsername = newUsernameField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || newUsername.isEmpty() || confirmPassword.isEmpty()){
            
            System.out.println("Empty Inputs.");
            registrationMessageLabel.setText("Please fill in all fields.");
            registrationMessageLabel.setStyle("-fx-text-fill: red;");
            registrationMessageLabel.setVisible(true);
            
        }else if (newPassword.equals(confirmPassword)) {
            
            // Send HTTP Request
            HttpResponse response = makeHttpRequest("http://localhost:5000/register", newUsername, newPassword); 
            
            //For debuging
            System.out.println("From Register: ");
            System.out.println("Status: " + response.getStatus());
            System.out.println("Message: " + response.getMessage());
            
            if (response.getStatus().equals("Success")){
                System.out.println("Registration successful for user: " + newUsername);
                //registrationMessageLabel.setText("Registration successful for user: " + newUsername);
                registrationMessageLabel.setText(response.getMessage());
                registrationMessageLabel.setStyle("-fx-text-fill: green;");
            }else{
                registrationMessageLabel.setText(response.getMessage());
                registrationMessageLabel.setStyle("-fx-text-fill: red;");
            }
            
        }else{
            
            System.out.println("Passwords do not match.");
            registrationMessageLabel.setText("Passwords do not match.");
            registrationMessageLabel.setStyle("-fx-text-fill: red;");
            
        }
        registrationMessageLabel.setVisible(true);
    }
    
    
    private HttpResponse makeHttpRequest(String urlString, String username, String password) {
        try {
            // Create URL and Connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure Connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create JSON Payload
            String jsonPayload = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

            // Write Payload
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            // Read Response
            int responseCode = conn.getResponseCode();
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            System.out.println(response);
            //return responseCode == 200 ? response.toString() : "Error: " + response.toString();
            System.out.println("From HTTP: " + response);

            return new HttpResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse("Error:Unable to connect to the server");
        }
    }

}  