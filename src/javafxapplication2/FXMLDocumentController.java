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

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {

        String username = usernameField.getText();
        String password = passwordField.getText();
        
        HttpResponse response = makeHttpRequest("http://localhost:5000/login", username, password);

        if (response.getStatus().equals("Success")) {
            try{
                loginMessageLabel.setText("Login successful!");
                loginMessageLabel.setStyle("-fx-text-fill: green;");
                loginMessageLabel.setVisible(true);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafxapplication2/ChatWindow.fxml"));
                Parent chatWindowRoot = loader.load();
                
                ChatWindowController chatWindowController = loader.getController();

                chatWindowController.setUsername(username);

                Scene chatScene = new Scene(chatWindowRoot, 1000, 600);
                chatScene.getStylesheets().add(getClass().getResource("/javafxapplication2/chatStyle.css").toExternalForm());

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(chatScene);
                stage.setTitle("ChatterBox");
                stage.getIcons().add(new Image("/Images/icon.png"));

                stage.setResizable(false);
                
            } catch(Exception e) {
                e.printStackTrace();
                loginMessageLabel.setText("Failed to load the main application.");
                loginMessageLabel.setStyle("-fx-text-fill: red;");
                loginMessageLabel.setVisible(true);
            }
        } else {
            loginMessageLabel.setText("Invalid username or password.");
            loginMessageLabel.setStyle("-fx-text-fill: red;");
            loginMessageLabel.setVisible(true);
        }
    }


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



    @FXML
    private void handleRegisterSubmit(ActionEvent event) {
        String newUsername = newUsernameField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || newUsername.isEmpty() || confirmPassword.isEmpty()){
            
            registrationMessageLabel.setText("Please fill in all fields.");
            registrationMessageLabel.setStyle("-fx-text-fill: red;");
            registrationMessageLabel.setVisible(true);
            
        }else if (newPassword.equals(confirmPassword)) {
            
            // Send HTTP Request
            HttpResponse response = makeHttpRequest("http://localhost:5000/register", newUsername, newPassword);
            
            if (response.getStatus().equals("Success")){
                registrationMessageLabel.setText(response.getMessage());
                registrationMessageLabel.setStyle("-fx-text-fill: green;");
            }else{
                registrationMessageLabel.setText(response.getMessage());
                registrationMessageLabel.setStyle("-fx-text-fill: red;");
            }
            
        }else{
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
            return new HttpResponse(response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse("Error:Unable to connect to the server");
        }
    }
}  