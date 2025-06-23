package com.hotel.controllers;

import com.hotel.dao.UserDAO;
import com.hotel.dao.impl.UserDAOImpl;
import com.hotel.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;

public class EditProfileController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label messageLabel;

    private Dialog<ButtonType> dialog;
    private User currentUser;
    private final UserDAO userDAO;

    public EditProfileController() {
        userDAO = new UserDAOImpl();
    }

    public void setDialog(Dialog<ButtonType> dialog) {
        this.dialog = dialog;
    }

    public void setUser(User user) {
        this.currentUser = user;
        populateFields();
    }

    private void populateFields() {
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhoneNumber());
            
            // Clear password fields as they're for new passwords only
            passwordField.clear();
            confirmPasswordField.clear();
        }
    }

    public boolean handleSave() {
        System.out.println("Starting handleSave...");
        if (dialog == null) {
            String error = "Dialog not properly initialized";
            System.err.println(error);
            showMessage(error, true);
            return false;
        }
        
        System.out.println("Validating input...");
        String validationError = validateInput();
        if (validationError != null) {
            System.err.println("Input validation failed: " + validationError);
            showMessage(validationError, true);
            return false;
        }

        try {
            // Update user object with new values
            String newFirstName = firstNameField.getText().trim();
            String newLastName = lastNameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();
            
            System.out.println("Updating user with values - Name: " + newFirstName + " " + newLastName + 
                             ", Email: " + newEmail + ", Phone: " + newPhone);
            
            // Create a new User object to avoid modifying the current one until update succeeds
            User updatedUser = new User();
            updatedUser.setUserId(currentUser.getUserId());
            updatedUser.setUsername(currentUser.getUsername());
            updatedUser.setRole(currentUser.getRole());
            updatedUser.setFirstName(newFirstName);
            updatedUser.setLastName(newLastName);
            updatedUser.setEmail(newEmail);
            updatedUser.setPhoneNumber(newPhone);

            // Only update password if a new one was entered
            if (!passwordField.getText().isEmpty()) {
                System.out.println("Password field is not empty, updating password");
                updatedUser.setPassword(passwordField.getText());
            } else {
                System.out.println("No password change requested");
                updatedUser.setPassword(""); // Empty string indicates no password change
            }

            System.out.println("Calling userDAO.updateUser...");
            boolean updateResult = userDAO.updateUser(updatedUser);
            System.out.println("userDAO.updateUser returned: " + updateResult);
            
            if (updateResult) {
                // Update was successful, now update the current user object
                currentUser.setFirstName(updatedUser.getFirstName());
                currentUser.setLastName(updatedUser.getLastName());
                currentUser.setEmail(updatedUser.getEmail());
                currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
                if (!updatedUser.getPassword().isEmpty()) {
                    currentUser.setPassword(updatedUser.getPassword());
                }
                
                String successMsg = "Profile updated successfully!";
                System.out.println(successMsg);
                showMessage(successMsg, false);
                
                // Show success alert
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your profile has been updated successfully!");
                    alert.showAndWait();
                });
                return true;
            } else {
                String errorMsg = "Failed to update profile. Please try again.";
                System.err.println(errorMsg);
                showMessage(errorMsg, true);
                return false;
            }
        } catch (SQLException e) {
            String errorMsg = "Database error: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showMessage(errorMsg, true);
            return false;
        } catch (Exception e) {
            String errorMsg = "Unexpected error: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showMessage(errorMsg, true);
            return false;
        }
    }

    private String validateInput() {
        // Validate required fields
        if (firstNameField.getText().trim().isEmpty()) {
            return "First name is required.";
        }
        if (lastNameField.getText().trim().isEmpty()) {
            return "Last name is required.";
        }
        if (emailField.getText().trim().isEmpty()) {
            return "Email is required.";
        }
        if (phoneField.getText().trim().isEmpty()) {
            return "Phone number is required.";
        }

        // Validate email format
        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Please enter a valid email address.";
        }

        // Validate phone number format (basic validation)
        String phone = phoneField.getText().trim();
        if (!phone.matches("^\\+?[0-9\\s\\(\\)\\-]+")) {
            return "Please enter a valid phone number. Only numbers, spaces, parentheses, and hyphens are allowed.";
        }

        // Validate password if being changed
        if (!passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 6) {
                return "Password must be at least 6 characters long.";
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                return "Passwords do not match.";
            }
        }

        return null; // null means validation passed
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}
