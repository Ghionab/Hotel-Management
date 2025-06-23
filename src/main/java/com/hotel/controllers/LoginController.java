package com.hotel.controllers;

import com.hotel.MainApp;
import com.hotel.dao.UserDAO; // Assuming you'll create an implementation like UserDAOImpl
import com.hotel.dao.impl.UserDAOImpl;
import com.hotel.model.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private UserDAO userDAO; // Use the interface

    public LoginController() {
        try {
            userDAO = new UserDAOImpl();
        } catch (Exception e) {
            System.err.println("Error initializing LoginController: " + e.getMessage());
        }
    }

    public void initialize() {
        statusLabel.setText(""); // Clear status on init
        System.out.println("LoginController initialized."); // Debug message
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        statusLabel.setText(""); // Clear previous messages

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty.");
            return;
        }

        System.out.println("Attempting login for user: " + username); // Debug message

        // --- Placeholder Logic - Replace with DAO Call ---
        // Commenting out placeholder logic
        /*
        if ("admin".equals(username) && "password".equals(password)) {
             loginSuccess("admin");
        } else if ("reception".equals(username) && "password".equals(password)) {
            loginSuccess("receptionist");
        } else {
            statusLabel.setText("Invalid username or password.");
            System.out.println("Login failed for user: " + username);
        }
        */
        // --- End Placeholder Logic ---

        // --- DAO Logic (Now active) ---
        try {
            if (userDAO == null) {
                userDAO = new UserDAOImpl(); // Initialize if null
            }
            Optional<User> userOptional = userDAO.findByUsername(username);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // IMPORTANT: Use password hashing and verification in a real app!
                // Example: if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                if (user.getPassword().equals(password)) { // Simple comparison (INSECURE)
                    System.out.println("Login successful for user: " + username + " with role: " + user.getRole());
                    loginSuccess(user.getRole());
                } else {
                    statusLabel.setText("Invalid username or password.");
                     System.out.println("Incorrect password for user: " + username);
                }
            } else {
                statusLabel.setText("Invalid username or password.");
                System.out.println("User not found: " + username);
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error during login.");
            System.err.println("SQL Error during login: " + e.getMessage());
            e.printStackTrace(); // Log the full error
        } catch (Exception e) {
            statusLabel.setText("An unexpected error occurred.");
            System.err.println("Unexpected Error during login: " + e.getMessage());
            e.printStackTrace(); // Log the full error
        }
        // --- End DAO Logic ---
    }

    private void loginSuccess(String userRole) {
        try {
            // Get the user object with full details
            Optional<User> userOptional = userDAO.findByUsername(usernameField.getText());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Fetch additional user details if needed
                Optional<User> detailedUser = userDAO.getUserWithStaffDetails(user.getUserId());
                if (detailedUser.isPresent()) {
                    user = detailedUser.get();
                }
                // Switch to the main application window with the user object
                MainApp.showMainInterface(user);
            } else {
                // This should theoretically not happen since we just logged in
                statusLabel.setText("Error: User data not found.");
                System.err.println("User data not found after login for: " + usernameField.getText());
            }
        } catch (Exception e) {
            statusLabel.setText("Error loading main application window.");
            System.err.println("Error when loading MainInterface: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 