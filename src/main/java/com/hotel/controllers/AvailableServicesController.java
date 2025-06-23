package com.hotel.controllers;

import com.hotel.dao.ServiceDAO;
import com.hotel.dao.impl.ServiceDAOImpl;
import com.hotel.model.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AvailableServicesController implements Initializable {
    @FXML private TableView<Service> servicesTable;
    @FXML private TableColumn<Service, Integer> serviceIdColumn;
    @FXML private TableColumn<Service, String> serviceNameColumn;
    @FXML private TableColumn<Service, String> descriptionColumn;
    @FXML private TableColumn<Service, Number> priceColumn;
    @FXML private Button addServiceBtn;
    @FXML private Button editServiceBtn;
    @FXML private Button deleteServiceBtn;
    @FXML private Button clearBtn;
    @FXML private TextField serviceNameField;
    @FXML private TextField priceField;
    @FXML private TextArea descriptionArea;
    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem deleteMenuItem;
    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> itemsPerPageCombo;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;

    private ServiceDAO serviceDAO = new ServiceDAOImpl();
    private ObservableList<Service> servicesData;
    private Service selectedService;
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupPaginationControls();
        loadServices();
        
        // Set up selection model
        servicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedService = newSelection;
            editServiceBtn.setDisable(newSelection == null);
            deleteServiceBtn.setDisable(newSelection == null);
            editMenuItem.setDisable(newSelection == null);
            deleteMenuItem.setDisable(newSelection == null);
            
            if (newSelection != null) {
                populateFields(newSelection);
            } else {
                clearFields();
            }
        });
        
        // Set up context menu actions
        editMenuItem.setOnAction(e -> handleEditService());
        deleteMenuItem.setOnAction(e -> handleDeleteService());
        
        // Set up button actions
        addServiceBtn.setOnAction(e -> handleAddService());
        editServiceBtn.setOnAction(e -> handleEditService());
        deleteServiceBtn.setOnAction(e -> handleDeleteService());
        clearBtn.setOnAction(e -> clearFields());
    }

    private void setupTableColumns() {
        serviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        servicesData = FXCollections.observableArrayList();
        servicesTable.setItems(servicesData);
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
                loadServices();
            }
        });
        
        // Set up pagination button actions
        firstPageButton.setOnAction(e -> handleFirstPage());
        prevPageButton.setOnAction(e -> handlePrevPage());
        nextPageButton.setOnAction(e -> handleNextPage());
        lastPageButton.setOnAction(e -> handleLastPage());
        
        // Set up search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadServices();
        });
    }
    
    private void loadServices() {
        try {
            // Get all services from the database
            List<Service> allServices = serviceDAO.getAllServices();
            
            // Apply filters to get filtered list
            List<Service> filteredServices = filterServices(allServices);
            
            // Update total items and pages
            totalItems = filteredServices.size();
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
            List<Service> pagedServices = filteredServices.subList(fromIndex, toIndex);
            
            // Update the table
            servicesData.setAll(pagedServices);
            
            // Update pagination controls
            updatePaginationControls();
            
        } catch (SQLException e) {
            showError("Error loading services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Service> filterServices(List<Service> services) {
        String searchTerm = searchField.getText().toLowerCase();
        
        return services.stream()
            .filter(service -> 
                searchTerm.isEmpty() || 
                (service.getServiceName() != null && service.getServiceName().toLowerCase().contains(searchTerm)) ||
                (service.getDescription() != null && service.getDescription().toLowerCase().contains(searchTerm)) ||
                String.valueOf(service.getPrice()).contains(searchTerm) ||
                String.valueOf(service.getServiceId()).contains(searchTerm))
            .collect(Collectors.toList());
    }

    @FXML
    private void handleAddService() {
        if (isInputValid()) {
            try {
                Service service = new Service();
                service.setServiceName(serviceNameField.getText().trim());
                service.setPrice(new BigDecimal(priceField.getText().trim()));
                service.setDescription(descriptionArea.getText().trim());
                
                serviceDAO.addService(service);
                clearFields();
                loadServices();
                showSuccess("Service added successfully");
            } catch (SQLException e) {
                showError("Could not add service: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditService() {
        if (selectedService != null && isInputValid()) {
            try {
                String serviceName = serviceNameField.getText().trim();
                String priceText = this.priceField.getText().trim();
                String description = descriptionArea.getText().trim();
                
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showError("Price must be greater than zero");
                    return;
                }

                selectedService.setServiceName(serviceName);
                selectedService.setPrice(BigDecimal.valueOf(price));
                selectedService.setDescription(description);

                serviceDAO.updateService(selectedService);
                loadServices();
                clearFields();
                showSuccess("Service updated successfully");
                
                // Reset the add button to its original state
                addServiceBtn.setText("Add Service");
                addServiceBtn.setOnAction(e -> handleAddService());
                
            } catch (NumberFormatException e) {
                showError("Please enter a valid price");
            } catch (SQLException e) {
                showError("Error updating service: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleDeleteService() {
        if (selectedService != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this service?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        serviceDAO.deleteService(selectedService.getServiceId());
                        loadServices();
                        clearFields();
                        showSuccess("Service deleted successfully");
                    } catch (SQLException e) {
                        showError("Error deleting service: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void populateFields(Service service) {
        serviceNameField.setText(service.getServiceName());
        priceField.setText(service.getPrice().toString());
        descriptionArea.setText(service.getDescription());
    }

    private void clearFields() {
        serviceNameField.clear();
        priceField.clear();
        descriptionArea.clear();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (serviceNameField.getText() == null || serviceNameField.getText().trim().isEmpty()) {
            errorMessage += "Service Name is required!\n";
        }
        
        if (priceField.getText() == null || priceField.getText().trim().isEmpty()) {
            errorMessage += "Price is required!\n";
        } else {
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                if (price <= 0) {
                    errorMessage += "Price must be greater than zero!\n";
                }
            } catch (NumberFormatException e) {
                errorMessage += "Price must be a valid number!\n";
            }
        }

        if (errorMessage.length() > 0) {
            showError(errorMessage);
            return false;
        }
        return true;
    }

    @FXML
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
    
    @FXML
    private void handleFirstPage() {
        currentPage = 1;
        loadServices();
    }
    
    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadServices();
        }
    }
    
    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadServices();
        }
    }
    
    @FXML
    private void handleLastPage() {
        currentPage = totalPages;
        loadServices();
    }
    
    @FXML
    private void handleSearch() {
        currentPage = 1; // Reset to first page when searching
        loadServices();
    }
    
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        currentPage = 1;
        loadServices();
    }
    
    @FXML
    private void handleRefresh() {
        currentPage = 1;
        searchField.clear();
        loadServices();
    }
}
