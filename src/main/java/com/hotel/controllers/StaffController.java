package com.hotel.controllers;

import com.hotel.model.Staff;
import com.hotel.model.User;
import com.hotel.dao.impl.StaffDAOImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StaffController implements Initializable {
    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, Integer> colUserId;
    @FXML private TableColumn<Staff, String> colFirstName;
    @FXML private TableColumn<Staff, String> colLastName;
    @FXML private TableColumn<Staff, String> colPosition;
    @FXML private TableColumn<Staff, String> colPhone;
    @FXML private TableColumn<Staff, String> colEmail;
    @FXML private TableColumn<Staff, LocalDate> colHireDate;
    @FXML private TableColumn<Staff, Double> colSalary;
    
    private User currentUser;  // Store the logged-in user

    @FXML private TextField userIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private ComboBox<String> positionComboBox;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker hireDatePicker;
    @FXML private TextField salaryField;
    @FXML private TextArea addressField;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterPositionComboBox;
    @FXML private ComboBox<Integer> itemsPerPageCombo;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;

    private StaffDAOImpl staffDAO;
    private ObservableList<Staff> staffList;
    
    /**
     * Sets the current user and updates UI elements accordingly
     * @param user The logged-in user
     */
    public void setUser(User user) {
        this.currentUser = user;
        
        // Update UI elements based on user role
        boolean isManager = user != null && "manager".equalsIgnoreCase(user.getRole());
        positionComboBox.setDisable(!isManager);
    }
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;
    private final ObservableList<String> positions = FXCollections.observableArrayList(
        "Admin", "Manager", "Receptionist", "Housekeeper", "Maintenance", "Chef"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        staffDAO = new StaffDAOImpl();

        // Initialize position combo boxes
        positionComboBox.setItems(positions);
        filterPositionComboBox.setItems(positions);

        // Set up table columns
        setupTableColumns();
        
        // Set up pagination controls
        setupPaginationControls();

        // Load initial data
        loadStaffData();
        
        // Initially disable position combo box until we know user's role
        positionComboBox.setDisable(true);
    }

    private void setupTableColumns() {
        // Initialize table columns
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colHireDate.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));

        // Add selection listener
        staffTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showStaffDetails(newValue));

        // Load initial data
        loadStaffData();
    }

    @FXML
    private void handleAddStaff() {
        if (!validateInputFields()) {
            return;
        }

        try {
            Staff staff = new Staff();
            populateStaffFromFields(staff);

            if (staffDAO.addStaff(staff)) {
                loadStaffData();
                clearFields();
                showSuccess("Staff member added successfully");
            } else {
                showError("Failed to add staff member");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for User ID and Salary");
        }
    }

    @FXML
    private void handleUpdateStaff() {
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff == null) {
            showError("Please select a staff member to update");
            return;
        }

        if (!validateInputFields()) {
            return;
        }

        try {
            // Check if position is being changed
            String newPosition = positionComboBox.getValue();
            String currentPosition = selectedStaff.getPosition();
            boolean isPositionChange = !newPosition.equals(currentPosition);

            // If position is being changed, verify user is a manager
            if (isPositionChange) {
                if (currentUser == null || !"manager".equalsIgnoreCase(currentUser.getRole())) {
                    showError("Only managers can update staff positions");
                    return;
                }
            }

            populateStaffFromFields(selectedStaff);

            if (staffDAO.updateStaff(selectedStaff)) {
                loadStaffData();
                clearFields();
                showSuccess("Staff member updated successfully");
            } else {
                showError("Failed to update staff member");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for User ID and Salary");
        }
    }

    @FXML
    private void handleDeleteStaff() {
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff == null) {
            showError("Please select a staff member to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Staff");
        alert.setHeaderText("Delete Staff Member");
        alert.setContentText("Are you sure you want to delete this staff member?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                if (staffDAO.deleteStaff(selectedStaff.getUserId())) {
                    loadStaffData();
                    clearFields();
                    showSuccess("Staff member deleted successfully");
                } else {
                    showError("Failed to delete staff member");
                }
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClearFields() {
        clearFields();
    }

    private void loadStaffData() {
        try {
            // Get all staff from the database
            List<Staff> allStaff = staffDAO.findAll();
            
            // Apply filters to get filtered list
            List<Staff> filteredStaff = filterStaff(allStaff);
            
            // Update total items and pages
            totalItems = filteredStaff.size();
            int itemsPerPage = itemsPerPageCombo.getValue() != null ? 
                itemsPerPageCombo.getValue() : ITEMS_PER_PAGE;
            totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            
            // Update position combo box state based on user role
            boolean isManager = currentUser != null && "manager".equalsIgnoreCase(currentUser.getRole());
            positionComboBox.setDisable(!isManager);
            
            // Ensure current page is within bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            } else if (currentPage < 1) {
                currentPage = 1;
            }
            
            // Calculate pagination
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);
            
            // Get sublist for current page
            List<Staff> pagedStaff = filteredStaff.subList(fromIndex, toIndex);
            
            // Update the table
            staffList = FXCollections.observableArrayList(pagedStaff);
            staffTable.setItems(staffList);
            
            // Update pagination controls
            updatePaginationControls();
            
            System.out.println(String.format("Showing items from index %d to %d", fromIndex, toIndex));
            
        } catch (SQLException e) {
            showError("Error loading staff data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Staff> filterStaff(List<Staff> staff) {
        String searchTerm = searchField.getText().toLowerCase();
        String positionFilter = filterPositionComboBox.getValue();
        
        return staff.stream()
            .filter(s -> 
                searchTerm.isEmpty() || 
                (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(searchTerm)) ||
                (s.getLastName() != null && s.getLastName().toLowerCase().contains(searchTerm)) ||
                (s.getEmail() != null && s.getEmail().toLowerCase().contains(searchTerm)) ||
                (s.getPhoneNumber() != null && s.getPhoneNumber().contains(searchTerm)))
            .filter(s -> 
                positionFilter == null || positionFilter.isEmpty() ||
                (s.getPosition() != null && s.getPosition().equals(positionFilter)))
            .collect(Collectors.toList());
    }
    
    private void setupPaginationControls() {
        // Initialize items per page combo box
        ObservableList<Integer> pageSizes = FXCollections.observableArrayList(5, 10, 25, 50, 100);
        itemsPerPageCombo.setItems(pageSizes);
        itemsPerPageCombo.setValue(ITEMS_PER_PAGE);
        
        // Add listener to items per page combo box
        itemsPerPageCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                currentPage = 1; // Reset to first page when changing page size
                loadStaffData();
            }
        });
        
        // Set up pagination button actions
        firstPageButton.setOnAction(e -> handleFirstPage());
        prevPageButton.setOnAction(e -> handlePrevPage());
        nextPageButton.setOnAction(e -> handleNextPage());
        lastPageButton.setOnAction(e -> handleLastPage());
        
        // Initialize pagination controls
        updatePaginationControls();
    }
    
    @FXML
    private void handleFirstPage() {
        if (currentPage != 1 && totalPages > 0) {
            currentPage = 1;
            loadStaffData();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadStaffData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadStaffData();
        }
    }

    @FXML
    private void handleLastPage() {
        if (currentPage != totalPages && totalPages > 0) {
            currentPage = totalPages;
            loadStaffData();
        }
    }
    
    private void updatePaginationControls() {
        // Update pagination buttons state
        firstPageButton.setDisable(currentPage == 1 || totalPages == 0);
        prevPageButton.setDisable(currentPage == 1 || totalPages == 0);
        nextPageButton.setDisable(currentPage == totalPages || totalPages == 0);
        lastPageButton.setDisable(currentPage == totalPages || totalPages == 0);
        
        // Update page info label
        pageInfoLabel.setText(totalPages > 0 
            ? String.format("Page %d of %d", currentPage, totalPages)
            : "No data available");
            
        // Update status label
        updateStatusLabel();
    }
    
    private void updateStatusLabel() {
        int itemsPerPage = itemsPerPageCombo.getValue() != null ? 
            itemsPerPageCombo.getValue() : ITEMS_PER_PAGE;
        int fromItem = Math.min((currentPage - 1) * itemsPerPage + 1, totalItems);
        int toItem = Math.min(currentPage * itemsPerPage, totalItems);
        
        if (totalItems > 0) {
            statusLabel.setText(String.format("Showing %d to %d of %d entries", 
                fromItem, toItem, totalItems));
        } else {
            statusLabel.setText("No entries to display");
        }
    }

    private void showStaffDetails(Staff staff) {
        if (staff != null) {
            userIdField.setText(String.valueOf(staff.getUserId()));
            firstNameField.setText(staff.getFirstName());
            lastNameField.setText(staff.getLastName());
            positionComboBox.setValue(staff.getPosition());
            
            // Disable position combo box for non-managers
            boolean isManager = currentUser != null && "manager".equalsIgnoreCase(currentUser.getRole());
            positionComboBox.setDisable(!isManager);
            
            phoneField.setText(staff.getPhoneNumber());
            emailField.setText(staff.getEmail());
            hireDatePicker.setValue(staff.getHireDate());
            salaryField.setText(String.valueOf(staff.getSalary()));
            addressField.setText(staff.getAddress());
        } else {
            clearFields();
        }
    }

    private boolean validateInputFields() {
        if (userIdField.getText().trim().isEmpty() ||
            firstNameField.getText().trim().isEmpty() ||
            lastNameField.getText().trim().isEmpty() ||
            positionComboBox.getValue() == null) {
            showError("Required fields: User ID, First Name, Last Name, and Position");
            return false;
        }
        return true;
    }

    private void populateStaffFromFields(Staff staff) {
        staff.setUserId(Integer.parseInt(userIdField.getText().trim()));
        staff.setFirstName(firstNameField.getText().trim());
        staff.setLastName(lastNameField.getText().trim());
        staff.setPosition(positionComboBox.getValue());
        staff.setPhoneNumber(phoneField.getText().trim());
        staff.setEmail(emailField.getText().trim());
        staff.setHireDate(hireDatePicker.getValue());
        staff.setSalary(Double.parseDouble(salaryField.getText().trim()));
        staff.setAddress(addressField.getText().trim());
    }

    private void clearFields() {
        userIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        positionComboBox.setValue(null);
        
        // Update position combo box state
        boolean isManager = currentUser != null && "manager".equalsIgnoreCase(currentUser.getRole());
        positionComboBox.setDisable(!isManager);
        
        phoneField.clear();
        emailField.clear();
        hireDatePicker.setValue(null);
        salaryField.clear();
        addressField.clear();
        messageLabel.setText("");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterPositionComboBox.getSelectionModel().clearSelection();
        currentPage = 1; // Reset to first page when clearing filters
        loadStaffData();
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1; // Reset to first page when refreshing
        loadStaffData();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1; // Reset to first page when searching
        loadStaffData();
    }
}
