package com.hotel.controllers;

import com.hotel.model.Customer;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.impl.CustomerDAOImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SpinnerValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {

    @FXML private TextField customerIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private Spinner<Integer> adultsSpinner;
    @FXML private Spinner<Integer> kidsSpinner;
    @FXML private Label statusMessageLabel;
    @FXML private TableView<Customer> customersTableView;
    @FXML private TableColumn<Customer, Integer> colCustomerId;
    @FXML private TableColumn<Customer, String> colFirstName;
    @FXML private TableColumn<Customer, String> colLastName;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, Integer> colAdults;
    @FXML private TableColumn<Customer, Integer> colKids;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterAdultsComboBox;
    @FXML private ComboBox<String> filterKidsComboBox;
    @FXML private ComboBox<Integer> itemsPerPageCombo;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;

    private CustomerDAO customerDAO;
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;

    public void initialize() {
        customerDAO = new CustomerDAOImpl();
        statusMessageLabel.setText("");
        
        // Initialize pagination controls
        setupPaginationControls();

        // Setup TableView columns
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colAdults.setCellValueFactory(new PropertyValueFactory<>("numberOfAdults"));
        colKids.setCellValueFactory(new PropertyValueFactory<>("numberOfKids"));

        customersTableView.setItems(customerList);

        // Setup Spinners
        SpinnerValueFactory<Integer> adultsValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        SpinnerValueFactory<Integer> kidsValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0);
        adultsSpinner.setValueFactory(adultsValueFactory);
        kidsSpinner.setValueFactory(kidsValueFactory);

        // Add listener to TableView selection
        customersTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showCustomerDetails(newSelection));

        // Setup filter ComboBoxes
        ObservableList<String> adultsOptions = FXCollections.observableArrayList(
            "Any", "1", "2", "3", "4+"
        );
        ObservableList<String> kidsOptions = FXCollections.observableArrayList(
            "Any", "0", "1", "2", "3+"
        );
        filterAdultsComboBox.setItems(adultsOptions);
        filterKidsComboBox.setItems(kidsOptions);
        filterAdultsComboBox.setValue("Any");
        filterKidsComboBox.setValue("Any");

        // Add filter listeners
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterAdultsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        filterKidsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Load initial data
        loadCustomers();
        
        // Set up selection listener for the table
        customersTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showCustomerDetails(newSelection);
                }
            });
    }

    private void loadCustomers() {
        try {
            // Get all customers from the database
            List<Customer> allCustomers = customerDAO.findAll();
            
            // Apply filters to get filtered list
            List<Customer> filteredCustomers = filterCustomers(allCustomers);
            
            // Update total items and pages
            totalItems = filteredCustomers.size();
            int itemsPerPage = itemsPerPageCombo.getValue() != null ? 
                itemsPerPageCombo.getValue() : ITEMS_PER_PAGE;
            totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            
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
            List<Customer> pagedCustomers = filteredCustomers.subList(fromIndex, toIndex);
            
            // Update the table
            customerList.setAll(pagedCustomers);
            customersTableView.setItems(customerList);
            
            // Update pagination controls
            updatePaginationControls();
            
            System.out.println(String.format("Showing items from index %d to %d", fromIndex, toIndex));
            
        } catch (SQLException e) {
            setStatusMessage("Error loading customers: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }
    
    private List<Customer> filterCustomers(List<Customer> customers) {
        String searchTerm = searchField.getText().toLowerCase();
        String adultsFilter = filterAdultsComboBox.getValue() != null ? filterAdultsComboBox.getValue() : "";
        String kidsFilter = filterKidsComboBox.getValue() != null ? filterKidsComboBox.getValue() : "";
        
        return customers.stream()
            .filter(customer -> 
                (searchTerm.isEmpty() || 
                 (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(searchTerm)) ||
                 (customer.getLastName() != null && customer.getLastName().toLowerCase().contains(searchTerm)) ||
                 (customer.getEmail() != null && customer.getEmail().toLowerCase().contains(searchTerm)) ||
                 (customer.getPhoneNumber() != null && customer.getPhoneNumber().toLowerCase().contains(searchTerm))))
            .filter(customer -> 
                adultsFilter.isEmpty() || 
                adultsFilter.equals("Any") ||
                (adultsFilter.equals("4+") && customer.getNumberOfAdults() >= 4) ||
                String.valueOf(customer.getNumberOfAdults()).equals(adultsFilter))
            .filter(customer -> 
                kidsFilter.isEmpty() ||
                kidsFilter.equals("Any") ||
                (kidsFilter.equals("3+") && customer.getNumberOfKids() >= 3) ||
                String.valueOf(customer.getNumberOfKids()).equals(kidsFilter))
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
                loadCustomers();
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
            loadCustomers();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadCustomers();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadCustomers();
        }
    }

    @FXML
    private void handleLastPage() {
        if (currentPage != totalPages && totalPages > 0) {
            currentPage = totalPages;
            loadCustomers();
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

    private void showCustomerDetails(Customer customer) {
        if (customer != null) {
            customerIdField.setText(String.valueOf(customer.getCustomerId()));
            firstNameField.setText(customer.getFirstName());
            lastNameField.setText(customer.getLastName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhoneNumber());
            addressField.setText(customer.getAddress());
            adultsSpinner.getValueFactory().setValue(customer.getNumberOfAdults());
            kidsSpinner.getValueFactory().setValue(customer.getNumberOfKids());
        } else {
            handleClearFields();
        }
    }

    @FXML
    private void handleAddCustomer() {
        if (!validateInput()) return;

        Customer newCustomer = new Customer(
            0, // ID is auto-generated by DB
            firstNameField.getText(),
            lastNameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            addressField.getText(),
            adultsSpinner.getValue(),
            kidsSpinner.getValue()
        );

        try {
            if (customerDAO == null) {
                setStatusMessage("Error: Customer service not available.", false);
                return;
            }
            boolean success = customerDAO.addCustomer(newCustomer);
            if (success) {
                loadCustomers();
                handleClearFields();
                setStatusMessage("Customer added successfully!", true);
            } else {
                setStatusMessage("Failed to add customer.", false);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Duplicate entry
                setStatusMessage("Email address already exists.", false);
            } else {
                setStatusMessage("Database error adding customer: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleUpdateCustomer() {
        Customer selectedCustomer = customersTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to update.");
            return;
        }
        if (!validateInput()) return;

        Customer updatedCustomer = new Customer(
            selectedCustomer.getCustomerId(),
            firstNameField.getText(),
            lastNameField.getText(),
            emailField.getText(),
            phoneField.getText(),
            addressField.getText(),
            adultsSpinner.getValue(),
            kidsSpinner.getValue()
        );

        try {
            if (customerDAO == null) {
                setStatusMessage("Error: Customer service not available.", false);
                return;
            }
            boolean success = customerDAO.updateCustomer(updatedCustomer);
            if (success) {
                loadCustomers();
                setStatusMessage("Customer updated successfully!", true);
            } else {
                setStatusMessage("Failed to update customer.", false);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // Duplicate entry
                setStatusMessage("Email address already exists.", false);
            } else {
                setStatusMessage("Database error updating customer: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer selectedCustomer = customersTableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a customer to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Customer: " + selectedCustomer.getFullName());
        alert.setContentText("Are you sure you want to delete this customer? This will also delete all their bookings.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                if (customerDAO == null) {
                    setStatusMessage("Error: Customer service not available.", false);
                    return;
                }
                boolean success = customerDAO.deleteCustomer(selectedCustomer.getCustomerId());
                if (success) {
                    loadCustomers();
                    handleClearFields();
                    setStatusMessage("Customer deleted successfully!", true);
                } else {
                    setStatusMessage("Failed to delete customer.", false);
                }
            } catch (SQLException e) {
                setStatusMessage("Database error deleting customer: " + e.getMessage(), false);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClearFields() {
        customersTableView.getSelectionModel().clearSelection();
        customerIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        adultsSpinner.getValueFactory().setValue(1);
        kidsSpinner.getValueFactory().setValue(0);
        statusMessageLabel.setText("");
        firstNameField.requestFocus();
    }

    private boolean validateInput() {
        String errorMessage = "";
        
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "First name is required.\n";
        }
        
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "Last name is required.\n";
        }
        
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "Email is required.\n";
        } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessage += "Invalid email format.\n";
        }
        
        if (adultsSpinner.getValue() < 1) {
            errorMessage += "At least 1 adult is required.\n";
        }
        
        if (kidsSpinner.getValue() < 0) {
            errorMessage += "Number of kids cannot be negative.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", errorMessage);
            return false;
        }
    }

    private void setStatusMessage(String message, boolean success) {
         statusMessageLabel.setText(message);
         statusMessageLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1; // Reset to first page when searching
        loadCustomers();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterAdultsComboBox.getSelectionModel().clearSelection();
        filterKidsComboBox.getSelectionModel().clearSelection();
        currentPage = 1; // Reset to first page when clearing filters
        loadCustomers();
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1; // Reset to first page when refreshing
        loadCustomers();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String adultsFilter = filterAdultsComboBox.getValue();
        String kidsFilter = filterKidsComboBox.getValue();

        ObservableList<Customer> filteredList = customerList.filtered(customer -> {
            boolean matchesSearch = searchText.isEmpty() ||
                String.valueOf(customer.getCustomerId()).contains(searchText) ||
                customer.getFirstName().toLowerCase().contains(searchText) ||
                customer.getLastName().toLowerCase().contains(searchText) ||
                customer.getEmail().toLowerCase().contains(searchText) ||
                customer.getPhoneNumber().toLowerCase().contains(searchText);

            boolean matchesAdults = adultsFilter.equals("Any") ||
                (adultsFilter.equals("4+") && customer.getNumberOfAdults() >= 4) ||
                String.valueOf(customer.getNumberOfAdults()).equals(adultsFilter);

            boolean matchesKids = kidsFilter.equals("Any") ||
                (kidsFilter.equals("3+") && customer.getNumberOfKids() >= 3) ||
                String.valueOf(customer.getNumberOfKids()).equals(kidsFilter);

            return matchesSearch && matchesAdults && matchesKids;
        });

        customersTableView.setItems(filteredList);
    }
} 