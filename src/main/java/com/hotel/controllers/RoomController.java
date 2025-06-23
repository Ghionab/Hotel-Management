package com.hotel.controllers;

import com.hotel.model.Room;
import com.hotel.dao.impl.RoomDAOImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
// Unused imports removed

public class RoomController implements Initializable {
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> roomNumberColumn;
    @FXML private TableColumn<Room, String> typeColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, Integer> floorColumn;
    @FXML private TableColumn<Room, BigDecimal> priceColumn;

    // Detail view controls
    @FXML private Label roomNumberLabel;
    @FXML private Label typeLabel;
    @FXML private Label floorLabel;
    @FXML private Label priceLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Button updateStatusButton;
    @FXML private Label messageLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    
    // Pagination controls
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> itemsPerPageCombo;

    private RoomDAOImpl roomDAO;
    
    // Pagination
    private static final int ITEMS_PER_PAGE = 30;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;
    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
        "Available", "Booked", "Cleaning", "Out of Service"
    );

    private final ObservableList<String> roomTypes = FXCollections.observableArrayList(
        "Single", "Double", "Suite", "Deluxe"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Initialize the DAO
            roomDAO = new RoomDAOImpl();

            // Initialize the status options
            statusComboBox.setItems(statusOptions);
            filterStatusComboBox.setItems(statusOptions);
            filterTypeComboBox.setItems(roomTypes);

            // Set up table columns
            roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            floorColumn.setCellValueFactory(new PropertyValueFactory<>("floor"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

            // Set up items per page combo
            itemsPerPageCombo.getItems().addAll(10, 25, 50, 100);
            itemsPerPageCombo.setValue(ITEMS_PER_PAGE);
            itemsPerPageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentPage = 1; // Reset to first page when changing items per page
                    loadRoomData();
                }
            });

            // Set up search field listener
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                loadRoomData();
            });

            // Set up filter combo box listeners
            filterTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                loadRoomData();
            });

            filterStatusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentPage = 1;
                loadRoomData();
            });

            // Set up pagination button actions
            firstPageButton.setOnAction(e -> handleFirstPage());
            prevPageButton.setOnAction(e -> handlePrevPage());
            nextPageButton.setOnAction(e -> handleNextPage());
            lastPageButton.setOnAction(e -> handleLastPage());

            // Add selection listener to table
            roomTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showRoomDetails(newValue));

            // Set up pagination controls
            setupPaginationControls();
            
            // Load initial data
            loadRoomData();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error initializing room controller: " + e.getMessage());
        }
    }

    private void setupPaginationControls() {
        // Set up items per page combo box
        itemsPerPageCombo.getItems().addAll(10, 20, 30, 50, 100);
        itemsPerPageCombo.setValue(ITEMS_PER_PAGE);
        
        // Add listener for items per page changes
        itemsPerPageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1; // Reset to first page when changing items per page
                loadRoomData();
            }
        });
        
        // Set up page navigation buttons
        firstPageButton.setOnAction(e -> handleFirstPage());
        prevPageButton.setOnAction(e -> handlePrevPage());
        nextPageButton.setOnAction(e -> handleNextPage());
        lastPageButton.setOnAction(e -> handleLastPage());
    }
    
    @FXML
    private void handleFirstPage() {
        if (currentPage != 1 && totalPages > 0) {
            currentPage = 1;
            loadRoomData();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadRoomData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadRoomData();
        }
    }

    @FXML
    private void handleLastPage() {
        if (currentPage != totalPages && totalPages > 0) {
            currentPage = totalPages;
            loadRoomData();
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
    }
    


    private void updateStatusLabel() {
        int fromItem = Math.min((currentPage - 1) * itemsPerPageCombo.getValue() + 1, totalItems);
        int toItem = Math.min(currentPage * itemsPerPageCombo.getValue(), totalItems);
        
        if (totalItems > 0) {
            statusLabel.setText(String.format("Showing %d to %d of %d entries", 
                fromItem, toItem, totalItems));
        } else {
            statusLabel.setText("No matching records found");
        }
    }
    
    private void loadRoomData() {
        try {
            // Get all rooms from the database
            List<Room> allRooms = roomDAO.findAll();
            
            // Apply filters
            List<Room> filteredRooms = filterRooms(allRooms);
            
            // Update total items and pages
            totalItems = filteredRooms.size();
            totalPages = (int) Math.ceil((double) totalItems / itemsPerPageCombo.getValue());
            
            // Ensure current page is within bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            } else if (currentPage < 1) {
                currentPage = 1;
            }
            
            // Calculate pagination
            int fromIndex = (currentPage - 1) * itemsPerPageCombo.getValue();
            int toIndex = Math.min(fromIndex + itemsPerPageCombo.getValue(), filteredRooms.size());
            
            // Get the sublist for the current page
            List<Room> pagedRooms = filteredRooms.subList(
                Math.min(fromIndex, filteredRooms.size()),
                Math.min(toIndex, filteredRooms.size())
            );
            
            // Update the table with paginated data
            roomTable.setItems(FXCollections.observableArrayList(pagedRooms));
            
            // Update pagination controls
            updatePaginationControls();
            
            // Update status label
            updateStatusLabel();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading room data: " + e.getMessage());
        }
    }

    private void showRoomDetails(Room room) {
        if (room != null) {
            roomNumberLabel.setText(String.valueOf(room.getRoomNumber()));
            typeLabel.setText(room.getType());
            floorLabel.setText(String.valueOf(room.getFloor()));
            priceLabel.setText(String.format("$%.2f", room.getPrice()));
            statusComboBox.setValue(room.getStatus());
            updateStatusButton.setDisable(false);
        } else {
            roomNumberLabel.setText("");
            typeLabel.setText("");
            floorLabel.setText("");
            priceLabel.setText("");
            statusComboBox.setValue(null);
            updateStatusButton.setDisable(true);
        }
    }

    @FXML
    private void handleUpdateStatusButton() {
        Room selectedRoom = roomTable.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showError("Please select a room first");
            return;
        }

        String newStatus = statusComboBox.getValue();
        if (newStatus == null || newStatus.trim().isEmpty()) {
            showError("Please select a status");
            return;
        }

        try {
            if (roomDAO.updateRoomStatus(selectedRoom.getRoomNumber(), newStatus)) {
                loadRoomData(); // Refresh the table
                showSuccess("Room status updated successfully");
            } else {
                showError("Failed to update room status");
            }
        } catch (SQLException e) {
            showError("Error updating room status: " + e.getMessage());
        }
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
        filterTypeComboBox.setValue(null);
        filterStatusComboBox.setValue(null);
        loadRoomData();
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1; // Reset to first page on refresh
        loadRoomData();
    }

    private List<Room> filterRooms(List<Room> rooms) {
        String searchText = searchField.getText().toLowerCase();
        String typeFilter = filterTypeComboBox.getValue();
        String statusFilter = filterStatusComboBox.getValue();
        
        if (searchText.isEmpty() && typeFilter == null && statusFilter == null) {
            return rooms;
        }
        
        List<Room> filteredList = new ArrayList<>();
        
        for (Room room : rooms) {
            boolean matchesSearch = searchText.isEmpty() ||
                    room.getRoomNumber().toLowerCase().contains(searchText) ||
                    room.getType().toLowerCase().contains(searchText) ||
                    String.valueOf(room.getFloor()).contains(searchText) ||
                    room.getStatus().toLowerCase().contains(searchText);

            boolean matchesType = typeFilter == null || typeFilter.equals(room.getType());
            boolean matchesStatus = statusFilter == null || statusFilter.equals(room.getStatus());

            if (matchesSearch && matchesType && matchesStatus) {
                filteredList.add(room);
            }
        }
        
        return filteredList;
    }
    

}