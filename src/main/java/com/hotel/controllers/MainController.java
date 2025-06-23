package com.hotel.controllers;

import com.hotel.dao.UserDAO;
import com.hotel.dao.impl.UserDAOImpl;
import com.hotel.model.User;
import com.hotel.util.RoleBasedAccessControl;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;


public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label userRoleLabel;
    
    // Navigation buttons
    @FXML private Button roomsBtn;
    @FXML private Button bookingsBtn;
    @FXML private Button customersBtn;
    @FXML private Button staffBtn;
    @FXML private Button availableServicesBtn;
    @FXML private Button bookingServicesBtn;
    @FXML private Button invoicesBtn;
    @FXML private Button paymentsBtn;

    @FXML private Button dashboardBtn;
    @FXML private Button feedbackBtn;
    
    // User info sidebar components
    @FXML private VBox userInfoSidebar;
    @FXML private Label fullNameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label positionLabel;
    @FXML private Button logoutBtn;

    private int loggedInUserId;
    private UserDAO userDAO;

    public MainController() {
        userDAO = new UserDAOImpl();
    }

    public void setLoggedInUserId(int userId) {
        this.loggedInUserId = userId;
    }

    @FXML private Label avatarLabel; // For displaying user initials
    
    private User currentUser; // Store the current user
    
    /**
     * Sets the current user and updates the UI with user information
     * @param user The User object containing user details
     */
    public void setUser(User user) {
        this.currentUser = user;
        
        // Set the logged in user ID
        if (user != null) {
            this.loggedInUserId = user.getUserId();
            System.out.println("Set loggedInUserId to: " + loggedInUserId);
            
            // Update access to staff tab
            if (staffBtn != null) {
                RoleBasedAccessControl.updateControlAccess(user, staffBtn);
            }
            
            // Update access to financial tabs
            RoleBasedAccessControl.updateFinancialAccess(user, invoicesBtn, paymentsBtn);
            
            // Update CRUD button states
            updateCrudButtonStates();
        }
        
        // Update the UI with user information
        if (user != null) {
            // Set basic user info
            Platform.runLater(() -> {
                try {
                    // Set user initials for avatar
                    if (avatarLabel != null) {
                        String initials = "";
                        if (user.getFirstName() != null && !user.getFirstName().isEmpty()) {
                            initials += user.getFirstName().charAt(0);
                        }
                        if (user.getLastName() != null && !user.getLastName().isEmpty()) {
                            initials += user.getLastName().charAt(0);
                        }
                        avatarLabel.setText(initials.isEmpty() ? "U" : initials);
                    }
                    
                    if (fullNameLabel != null) {
                        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
                        fullNameLabel.setText(fullName.isEmpty() ? "N/A" : fullName);
                    }
                    if (usernameLabel != null) {
                        usernameLabel.setText(user.getUsername() != null ? user.getUsername() : "N/A");
                    }
                    if (roleLabel != null) {
                        roleLabel.setText(user.getRole() != null ? user.getRole() : "N/A");
                    }
                    if (phoneLabel != null) {
                        phoneLabel.setText(user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty() ? 
                                          user.getPhoneNumber() : "N/A");
                    }
                    if (emailLabel != null) {
                        emailLabel.setText(user.getEmail() != null && !user.getEmail().isEmpty() ? 
                                          user.getEmail() : "N/A");
                    }
                    if (positionLabel != null) {
                        positionLabel.setText(user.getPosition() != null && !user.getPosition().isEmpty() ? 
                                             user.getPosition() : "N/A");
                    }
                    
                    // Show the user info sidebar
                    if (userInfoSidebar != null) {
                        userInfoSidebar.setVisible(true);
                        userInfoSidebar.setManaged(true);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating UI with user info: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Fetch additional user details if needed
            loadUserDetails(user.getUserId());
        } else {
            // Hide the user info sidebar if no user is set
            if (userInfoSidebar != null) {
                userInfoSidebar.setVisible(false);
                userInfoSidebar.setManaged(false);
            }
        }
    }
    
    /**
     * Loads additional user details from the database
     * @param userId The ID of the user to load details for
     */
    private void loadUserDetails(int userId) {
        try {
            if (userDAO == null) {
                userDAO = new UserDAOImpl();
            }
            
            // Fetch user details with staff information
            Optional<User> userOptional = userDAO.getUserWithStaffDetails(userId);
            
            if (userOptional.isPresent()) {
                User userWithDetails = userOptional.get();
                
                // Update the UI with the fetched details
                Platform.runLater(() -> {
                    // Update any additional fields that might have been fetched
                    if (emailLabel != null && (currentUser.getEmail() == null || currentUser.getEmail().isEmpty())) {
                        emailLabel.setText(userWithDetails.getEmail() != null && !userWithDetails.getEmail().isEmpty() ? 
                                        userWithDetails.getEmail() : "N/A");
                    }
                    if (phoneLabel != null && (currentUser.getPhoneNumber() == null || currentUser.getPhoneNumber().isEmpty())) {
                        phoneLabel.setText(userWithDetails.getPhoneNumber() != null && !userWithDetails.getPhoneNumber().isEmpty() ? 
                                        userWithDetails.getPhoneNumber() : "N/A");
                    }
                    if (positionLabel != null && (currentUser.getPosition() == null || currentUser.getPosition().isEmpty())) {
                        positionLabel.setText(userWithDetails.getPosition() != null && !userWithDetails.getPosition().isEmpty() ? 
                                           userWithDetails.getPosition() : "N/A");
                    }
                });
                
                // Update the current user with the fetched details
                currentUser = userWithDetails;
            }
        } catch (SQLException e) {
            System.err.println("Error loading user details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Button currentActiveButton;


    public void initialize() {
        System.out.println("MainController initialized.");
        try {
            userDAO = new UserDAOImpl();
            
            // Set Dashboard as default active view
            setActiveButton(dashboardBtn);
            loadView("DashboardTab.fxml");
            
            // Initialize the user info sidebar
            if (userInfoSidebar != null) {
                userInfoSidebar.setVisible(false);
                userInfoSidebar.setManaged(false);
                
                // Apply any additional styling or initialization
                userInfoSidebar.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 0 1;");
            }
            
            // Hide restricted buttons by default until we know user's role
            if (staffBtn != null) staffBtn.setVisible(false);
            if (invoicesBtn != null) invoicesBtn.setVisible(false);
            if (paymentsBtn != null) paymentsBtn.setVisible(false);
            
            // Initially disable all CRUD buttons
            disableAllCrudButtons();
        } catch (Exception e) {
            String errorMsg = "Error initializing MainController: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showErrorAlert(errorMsg);
        }
    }

    // setUser and loadUserDetails methods are defined above with complete implementations
    
    
    // For backward compatibility
    public void setUserRole(String role) {
        if (currentUser != null) {
            currentUser.setRole(role);
            if (userRoleLabel != null) {
                userRoleLabel.setText("[" + role + "]");
            }
        }
        System.out.println("User role set to: " + role);
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Hotel Management System");
        alert.setContentText("Version 1.0\n\nA comprehensive hotel management solution\n© 2025 Hotel Management System");
        alert.showAndWait();
    }
    
    /**
     * Sets the active navigation button style
     * 
     * @param button The button to set as active
     */
    private void setActiveButton(Button button) {
        // Remove active style from previous button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        
        // Add active style to current button
        button.getStyleClass().add("active");
        currentActiveButton = button;
    }
    
    /**
     * Loads a view into the content area
     * 
     * @param fxmlFile The FXML file to load
     */
    private void loadView(String fxmlFile) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/fxml/" + fxmlFile));
            if (loader.getLocation() == null) {
                throw new IOException("FXML file not found: " + fxmlFile);
            }
            
            Node view = loader.load();
            
            // Get the controller and pass the current user if it implements UserAware
            Object controller = loader.getController();
            if (controller instanceof UserAware && currentUser != null) {
                ((UserAware) controller).setUser(currentUser);
            }
            
            // Clear content area and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error loading view: " + fxmlFile + "\nError: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Unexpected error loading view: " + fxmlFile + "\nError: " + e.getMessage());
        }
    }
    
    /**
     * Shows the Rooms view
     */
    @FXML
    public void showRooms(ActionEvent event) {
        setActiveButton(roomsBtn);
        loadView("RoomTab.fxml");
    }
    
    /**
     * Shows the Bookings view
     */
    @FXML
    public void showBookings(ActionEvent event) {
        setActiveButton(bookingsBtn);
        loadView("BookingTab.fxml");
    }
    
    /**
     * Shows the Customers view
     */
    @FXML
    public void showCustomers(ActionEvent event) {
        setActiveButton(customersBtn);
        loadView("CustomerTab.fxml");
    }
    /**
     * Shows the Staff view
     */
    @FXML
    public void showStaff(ActionEvent event) {
        setActiveButton(staffBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/fxml/StaffTab.fxml"));
            Node view = loader.load();
            
            // Get the controller and pass the current user
            StaffController controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setUser(currentUser);
            }
            
            // Clear content area and add the new view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error loading Staff view: " + e.getMessage());
        }
    }
    

    /**
     * Shows the Dashboard view
     */
    @FXML
    public void showDashboard(ActionEvent event) {
        setActiveButton(dashboardBtn);
        loadView("DashboardTab.fxml");
    }
    
    /**
     * Shows the Available Services view
     */
    @FXML
    public void showAvailableServices(ActionEvent event) {
        setActiveButton(availableServicesBtn);
        loadView("AvailableServicesTab.fxml");
    }
    
    /**
     * Shows the Booking Services view
     */
    @FXML
    public void showBookingServices(ActionEvent event) {
        setActiveButton(bookingServicesBtn);
        loadView("BookingServicesTab.fxml");
    }

    /**
     * Shows the Feedback view
     */
    @FXML
    public void showFeedback(ActionEvent event) {
        setActiveButton(feedbackBtn);
        loadView("FeedbackTab.fxml");
    }
    
    /**
     * Shows the Invoices view
     */
    @FXML
    public void showInvoices(ActionEvent event) {
        setActiveButton(invoicesBtn);
        loadView("InvoiceTab.fxml");
    }
    
    /**
     * Shows the Payments view
     */
    @FXML
    private Button editProfileButton;

    @FXML
    public void handleEditProfile(ActionEvent event) {
        System.out.println("Edit profile button clicked");
        try {
            System.out.println("Loading EditProfileDialog.fxml...");
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/fxml/EditProfileDialog.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Failed to locate EditProfileDialog.fxml");
                showError("Failed to load edit profile dialog.");
                return;
            }
            
            DialogPane dialogPane = loader.load();
            System.out.println("Dialog FXML loaded successfully");
            
            EditProfileController controller = loader.getController();
            if (controller == null) {
                System.err.println("Failed to get controller from FXML");
                showError("Failed to initialize edit profile dialog.");
                return;
            }

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Profile");
            System.out.println("Dialog created with title: " + dialog.getTitle());

            // Set the dialog and current user in the controller
            controller.setDialog(dialog);
            
            // Get current user details
            System.out.println("Fetching user details for ID: " + loggedInUserId);
            Optional<User> userOpt = userDAO.getUserWithStaffDetails(loggedInUserId);
            if (userOpt.isPresent()) {
                System.out.println("User found: " + userOpt.get().getUsername());
                controller.setUser(userOpt.get());
                
                // Show the dialog and handle the result
                System.out.println("Showing dialog...");
                Optional<ButtonType> result = dialog.showAndWait();
                System.out.println("Dialog closed with result: " + (result.isPresent() ? result.get() : "empty"));
                
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    if (controller.handleSave()) {
                        // Get the updated user information
                        Optional<User> updatedUserOpt = userDAO.getUserWithStaffDetails(loggedInUserId);
                        if (updatedUserOpt.isPresent()) {
                            // Update the UI with the latest user information
                            updateUserInfo(updatedUserOpt.get());
                            System.out.println("User information updated and refreshed successfully");
                            
                            // Also update the current user in the MainController
                            this.currentUser = updatedUserOpt.get();
                        } else {
                            System.err.println("Failed to fetch updated user details");
                        }
                    } else {
                        System.err.println("Failed to save user changes");
                    }
                }
            } else {
                String errorMsg = "Could not load user details for ID: " + loggedInUserId;
                System.err.println(errorMsg);
                showError(errorMsg);
            }
        } catch (IOException e) {
            System.err.println("IO Error loading dialog: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading dialog: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateUserInfo(User user) {
        // Update the UI elements with new user information
        if (fullNameLabel != null) fullNameLabel.setText(user.getFullName());
        if (usernameLabel != null) usernameLabel.setText(user.getUsername());
        if (emailLabel != null) emailLabel.setText(user.getEmail());
        if (phoneLabel != null) phoneLabel.setText(user.getPhoneNumber());
        if (positionLabel != null) positionLabel.setText(user.getPosition());
    }

    @FXML
    public void showPayments(ActionEvent event) {
        setActiveButton(paymentsBtn);
        loadView("PaymentTab.fxml");
    }



    /**
     * Handles the About action, displaying information about the application.
     *
     * @param event The ActionEvent triggered by the user action.
     */
    @FXML
    public void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Hotel Management System");
        alert.setContentText("Version 1.0\nDeveloped by [Your Name]\n 2025");
        alert.showAndWait();
    }

    /**
     * Handles the Exit action, closing the application.
     *
     * @param event The ActionEvent triggered by the user action.
     */
    @FXML
    public void handleExit(ActionEvent event) {
        Platform.exit();
    }
    
    /**
     * Shows an error alert with the given message
     * 
     * @param message The error message to display
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Disable all CRUD buttons in the current view
     */
    private void disableAllCrudButtons() {
        if (contentArea == null || contentArea.getChildren().isEmpty()) return;
        
        Node currentView = contentArea.getChildren().get(0);
        if (currentView == null) return;
        
        // Find all buttons in the view
        currentView.lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                String id = button.getId();
                if (id != null && (id.toLowerCase().contains("add") ||
                    id.toLowerCase().contains("edit") ||
                    id.toLowerCase().contains("delete") ||
                    id.toLowerCase().contains("update") ||
                    id.toLowerCase().contains("save") ||
                    id.toLowerCase().contains("remove"))) {
                    button.setDisable(true);
                }
            }
        });
    }
    
    /**
     * Update CRUD button states based on user role
     */
    private void updateCrudButtonStates() {
        if (contentArea == null || contentArea.getChildren().isEmpty()) return;
        
        Node currentView = contentArea.getChildren().get(0);
        if (currentView == null) return;
        
        // Enable/disable CRUD buttons based on role
        boolean shouldDisable = RoleBasedAccessControl.isRegularStaff(currentUser);
        
        currentView.lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                String id = button.getId();
                if (id != null && (id.toLowerCase().contains("add") ||
                    id.toLowerCase().contains("edit") ||
                    id.toLowerCase().contains("delete") ||
                    id.toLowerCase().contains("update") ||
                    id.toLowerCase().contains("save") ||
                    id.toLowerCase().contains("remove"))) {
                    button.setDisable(shouldDisable);
                }
            }
        });
    }

    /**
     * Handles the Logout action, returning to the login screen.
     *
     * @param event The ActionEvent triggered by the user action.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/fxml/LoginView.fxml"));
            Parent loginView = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) contentArea.getScene().getWindow();
            
            // Set the login scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Error returning to login screen");
        }
    }
}