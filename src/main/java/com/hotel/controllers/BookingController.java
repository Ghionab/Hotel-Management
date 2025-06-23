package com.hotel.controllers;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.model.Customer;
import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.dao.CustomerDAO;
import com.hotel.dao.impl.BookingDAOImpl;
import com.hotel.dao.impl.RoomDAOImpl;
import com.hotel.dao.impl.CustomerDAOImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.SpinnerValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.application.Platform;
//import javafx.scene.control.Alert.AlertType;

public class BookingController {

    @FXML private TextField bookingIdField;
    @FXML private ComboBox<Room> roomNumberComboBox;
    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private Spinner<Integer> numberOfAdultsSpinner;
    @FXML private Spinner<Integer> numberOfKidsSpinner;
    @FXML private Label statusMessageLabel;
    @FXML private TableView<Booking> bookingsTableView;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, Integer> colRoomId;
    @FXML private TableColumn<Booking, Integer> colCustomerId;
    @FXML private TableColumn<Booking, LocalDate> colCheckIn;
    @FXML private TableColumn<Booking, LocalDate> colCheckOut;
    @FXML private TextField searchField;
    @FXML private DatePicker filterStartDate;
    @FXML private DatePicker filterEndDate;
    
    // Pagination controls
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private ComboBox<Integer> itemsPerPageCombo;
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 30;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;

    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private CustomerDAO customerDAO;

    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private ObservableList<Room> availableRoomList = FXCollections.observableArrayList();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public void initialize() {
        try {
            // Instantiate DAOs first
            bookingDAO = new BookingDAOImpl();
            roomDAO = new RoomDAOImpl();
            customerDAO = new CustomerDAOImpl();

            // Initialize UI components
            statusMessageLabel.setText("");
            
            // Initialize spinners
            numberOfAdultsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
            numberOfKidsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

            // Setup TableView columns with proper property names
            colBookingId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
            colRoomId.setCellValueFactory(new PropertyValueFactory<>("roomId"));
            colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
            colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
            colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));

            // Set the items to the table
            bookingsTableView.setItems(bookingList);

            // Setup ComboBoxes
            roomNumberComboBox.setItems(availableRoomList);
            customerComboBox.setItems(customerList);
            
            // Set up cell factories for ComboBoxes
            roomNumberComboBox.setCellFactory(lv -> new ListCell<Room>() {
                @Override
                protected void updateItem(Room room, boolean empty) {
                    super.updateItem(room, empty);
                    setText(empty || room == null ? null : "Room " + room.getRoomNumber());
                }
            });
            
            roomNumberComboBox.setButtonCell(new ListCell<Room>() {
                @Override
                protected void updateItem(Room room, boolean empty) {
                    super.updateItem(room, empty);
                    setText(empty || room == null ? "Select Room" : "Room " + room.getRoomNumber());
                }
            });
            
            customerComboBox.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer customer, boolean empty) {
                    super.updateItem(customer, empty);
                    if (empty || customer == null) {
                        setText(null);
                    } else {
                        setText(String.format("%s %s (ID: %d)", 
                            customer.getFirstName(), customer.getLastName(), customer.getCustomerId()));
                    }
                }
            });
            
            customerComboBox.setButtonCell(new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer customer, boolean empty) {
                    super.updateItem(customer, empty);
                    setText(empty || customer == null ? "Select Customer" : 
                        String.format("%s %s", customer.getFirstName(), customer.getLastName()));
                }
            });

            // Setup pagination controls
            setupPaginationControls();

            // Add listener to TableView selection
            bookingsTableView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> showBookingDetails(newSelection));

            // Add filter listeners
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                applyFilters();
            });
            
            filterStartDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                applyFilters();
            });
            
            filterEndDate.valueProperty().addListener((observable, oldValue, newValue) -> {
                currentPage = 1;
                applyFilters();
            });
            
            // Add listeners to date pickers to refresh available rooms
            checkInDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> loadAvailableRooms());
            checkOutDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> loadAvailableRooms());

            // Set default dates
            checkInDatePicker.setValue(LocalDate.now());
            checkOutDatePicker.setValue(LocalDate.now().plusDays(1));

            // Load initial data
            loadInitialData();
            
        } catch (Exception e) {
            showError("Error initializing booking controller: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadInitialData() {
        try {
            loadBookings();
            loadAvailableRooms();
            loadCustomers();
        } catch (Exception e) {
            showError("Error loading initial data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadAvailableRooms() {
        try {
            System.out.println("Loading available rooms...");
            
            // Clear the current list
            availableRoomList.clear();
            
            // Get dates from date pickers
            LocalDate checkIn = checkInDatePicker.getValue();
            LocalDate checkOut = checkOutDatePicker.getValue();
            
            // If dates aren't selected, use current date and next day
            if (checkIn == null) checkIn = LocalDate.now();
            if (checkOut == null) checkOut = checkIn.plusDays(1);
            
            // Ensure check-out is after check-in
            if (checkOut.isBefore(checkIn.plusDays(1))) {
                checkOut = checkIn.plusDays(1);
                checkOutDatePicker.setValue(checkOut);
            }
            
            System.out.println("Checking availability from " + checkIn + " to " + checkOut);
            
            // Get available rooms from the database
            List<Room> availableRooms = bookingDAO.getAllAvailableRooms(checkIn, checkOut);
            System.out.println("Found " + availableRooms.size() + " available rooms");
            
            // Update the available rooms list on the JavaFX Application Thread
            Room selectedRoom = roomNumberComboBox.getValue();
            Platform.runLater(() -> {
                try {
                    availableRoomList.setAll(availableRooms);
                    
                    // Update the selected room if it's no longer available
                    if (selectedRoom != null && !availableRoomList.contains(selectedRoom)) {
                        roomNumberComboBox.setValue(null);
                    }
                    
                    if (availableRooms.isEmpty()) {
                        showStatusMessage("No available rooms found for selected dates.", false);
                    } else {
                        showStatusMessage(availableRooms.size() + " rooms available", true);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating available rooms UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (SQLException e) {
            String errorMsg = "Error loading available rooms: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showError(errorMsg);
        }
    }

    private void loadCustomers() {
        try {
            System.out.println("Loading customers...");
            
            // Clear the current list
            customerList.clear();
            
            // Get customers from DAO
            List<Customer> customers = customerDAO.findAll();
            System.out.println("Found " + customers.size() + " customers");
            
            // Update the customers list on the JavaFX Application Thread
            Platform.runLater(() -> {
                try {
                    customerList.setAll(customers);
                    
                    // Update the selected customer if it's no longer in the list
                    Customer selectedCustomer = customerComboBox.getValue();
                    if (selectedCustomer != null && !customerList.contains(selectedCustomer)) {
                        customerComboBox.setValue(null);
                    }
                    
                    if (customers.isEmpty()) {
                        showStatusMessage("No customers found.", false);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating customers UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (SQLException e) {
            String errorMsg = "Error loading customers: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            Platform.runLater(() -> showError(errorMsg));
        }
    }

    private void showBookingDetails(Booking booking) {
        if (booking != null) {
            bookingIdField.setText(String.valueOf(booking.getBookingId()));
            try {
                // Find and select the room
                List<Booking> roomBookings = bookingDAO.findByRoomId(booking.getRoomId());
                if (!roomBookings.isEmpty()) {
                    Room room = roomDAO.findById(booking.getRoomId()).orElse(null);
                    if (room != null) {
                        roomNumberComboBox.setValue(room);
                    }
                }
                
                // Find and select the customer
                List<Booking> customerBookings = bookingDAO.findByCustomerId(booking.getCustomerId());
                if (!customerBookings.isEmpty()) {
                    Customer customer = customerDAO.findById(booking.getCustomerId()).orElse(null);
                    if (customer != null) {
                        customerComboBox.setValue(customer);
                    }
                }
                
                checkInDatePicker.setValue(booking.getCheckInDate());
                checkOutDatePicker.setValue(booking.getCheckOutDate());
                numberOfAdultsSpinner.getValueFactory().setValue(booking.getNumberOfAdults());
                numberOfKidsSpinner.getValueFactory().setValue(booking.getNumberOfKids());
            } catch (SQLException e) {
                showStatusMessage("Error loading booking details: " + e.getMessage(), false);
            }
        } else {
            handleClearFields();
        }
    }

    @FXML
    private void handleAddBooking() {
        if (!validateInput()) {
            return;
        }

        try {
            Booking booking = new Booking();
            booking.setRoomId(roomNumberComboBox.getValue().getRoomId());
            booking.setCustomerId(customerComboBox.getValue().getCustomerId());
            booking.setCheckInDate(checkInDatePicker.getValue());
            booking.setCheckOutDate(checkOutDatePicker.getValue());
            booking.setNumberOfAdults(numberOfAdultsSpinner.getValue());
            booking.setNumberOfKids(numberOfKidsSpinner.getValue());

            if (bookingDAO.addBooking(booking)) {
                showSuccess("Booking added successfully!");
                
                // Reload data and stay on the current page
                loadBookings();
                handleClearFields();
            } else {
                showError("Failed to add booking");
            }
        } catch (SQLException e) {
            showError("Error adding booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateBooking() {
        Booking selectedBooking = bookingsTableView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to update.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            selectedBooking.setRoomId(roomNumberComboBox.getValue().getRoomId());
            selectedBooking.setCustomerId(customerComboBox.getValue().getCustomerId());
            selectedBooking.setCheckInDate(checkInDatePicker.getValue());
            selectedBooking.setCheckOutDate(checkOutDatePicker.getValue());
            selectedBooking.setNumberOfAdults(numberOfAdultsSpinner.getValue());
            selectedBooking.setNumberOfKids(numberOfKidsSpinner.getValue());

            if (bookingDAO.updateBooking(selectedBooking)) {
                showSuccess("Booking updated successfully!");
                
                // Reload data and stay on the current page
                loadBookings();
            } else {
                showError("Failed to update booking");
            }
        } catch (SQLException e) {
            showError("Error updating booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearFields() {
        bookingsTableView.getSelectionModel().clearSelection();
        bookingIdField.clear();
        roomNumberComboBox.getSelectionModel().clearSelection();
        customerComboBox.getSelectionModel().clearSelection();
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
        numberOfAdultsSpinner.getValueFactory().setValue(1);
        numberOfKidsSpinner.getValueFactory().setValue(0);
        statusMessageLabel.setText("");
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (roomNumberComboBox.getValue() == null) {
            errorMessage += "Room must be selected.\n";
        }
        if (customerComboBox.getValue() == null) {
            errorMessage += "Customer must be selected.\n";
        }
        if (checkInDatePicker.getValue() == null) {
            errorMessage += "Check-in date is required.\n";
        }
        if (checkOutDatePicker.getValue() == null) {
            errorMessage += "Check-out date is required.\n";
        }
        if (checkInDatePicker.getValue() != null && checkOutDatePicker.getValue() != null) {
            if (checkOutDatePicker.getValue().isBefore(checkInDatePicker.getValue()) || checkOutDatePicker.getValue().isEqual(checkInDatePicker.getValue())) {
                errorMessage += "Check-out date must be after check-in date.\n";
            }
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", errorMessage);
            return false;
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        });
    }
    
    private void showSuccess(String message) {
        Platform.runLater(() -> {
            statusMessageLabel.setText(message);
            statusMessageLabel.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
        });
    }
    
    private void showStatusMessage(String message, boolean isSuccess) {
        if (isSuccess) {
            showSuccess(message);
        } else {
            showError(message);
        }
    }
    
    private void setupPaginationControls() {
        // Set up items per page combo box
        itemsPerPageCombo.getItems().addAll(10, 25, 50, 100);
        itemsPerPageCombo.setValue(ITEMS_PER_PAGE);
        
        // Add listener for items per page changes
        itemsPerPageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentPage = 1; // Reset to first page when changing items per page
                loadBookings();
            }
        });
        
        // Set up page navigation buttons
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
            loadBookings();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadBookings();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadBookings();
        }
    }

    @FXML
    private void handleLastPage() {
        if (currentPage != totalPages && totalPages > 0) {
            currentPage = totalPages;
            loadBookings();
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
        int itemsPerPage = itemsPerPageCombo != null ? itemsPerPageCombo.getValue() : ITEMS_PER_PAGE;
        int fromItem = Math.min((currentPage - 1) * itemsPerPage + 1, totalItems);
        int toItem = Math.min(currentPage * itemsPerPage, totalItems);
        
        if (totalItems > 0) {
            statusLabel.setText(String.format("Showing %d to %d of %d entries", 
                fromItem, toItem, totalItems));
        } else {
            statusLabel.setText("No matching records found");
        }
    }
    
    private List<Booking> filterBookings(List<Booking> allBookings) {
        String searchText = searchField.getText().toLowerCase();
        LocalDate startDate = filterStartDate.getValue();
        LocalDate endDate = filterEndDate.getValue();
        
        return allBookings.stream()
            .filter(booking -> {
                // Apply search filter
                boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(booking.getBookingId()).contains(searchText) ||
                    String.valueOf(booking.getRoomId()).toLowerCase().contains(searchText) ||
                    String.valueOf(booking.getCustomerId()).toLowerCase().contains(searchText);
                
                // Apply date range filter
                boolean matchesDate = true;
                if (startDate != null) {
                    matchesDate = !booking.getCheckInDate().isBefore(startDate);
                }
                if (endDate != null) {
                    matchesDate = matchesDate && !booking.getCheckInDate().isAfter(endDate);
                }
                
                return matchesSearch && matchesDate;
            })
            .collect(Collectors.toList());
    }
    
    private void loadBookings() {
        try {
            System.out.println("Loading bookings...");
            
            // Get all bookings from the database
            List<Booking> allBookings = bookingDAO.findAll();
            System.out.println("Found " + allBookings.size() + " bookings in database");
            
            if (allBookings.isEmpty()) {
                System.out.println("No bookings found in the database");
            }
            
            // Apply any active filters
            List<Booking> filteredBookings = filterBookings(allBookings);
            System.out.println("After filtering: " + filteredBookings.size() + " bookings");
            
            // Update total items and pages
            totalItems = filteredBookings.size();
            int itemsPerPage = itemsPerPageCombo != null ? itemsPerPageCombo.getValue() : ITEMS_PER_PAGE;
            totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            
            System.out.println("Total items: " + totalItems + ", items per page: " + itemsPerPage + ", total pages: " + totalPages);
            
            // Ensure current page is within bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            } else if (currentPage < 1) {
                currentPage = 1;
            }
            
            // Calculate pagination
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, filteredBookings.size());
            
            System.out.println("Showing items from index " + fromIndex + " to " + toIndex);
            
            // Get the sublist for the current page
            List<Booking> pagedBookings = filteredBookings.subList(
                Math.min(fromIndex, filteredBookings.size()),
                Math.min(toIndex, filteredBookings.size())
            );
            
            // Update the table with paginated data
            Platform.runLater(() -> {
                try {
                    bookingList.setAll(pagedBookings);
                    System.out.println("Updated table with " + pagedBookings.size() + " bookings");
                    
                    // Update pagination controls
                    updatePaginationControls();
                    
                    // Update status label
                    updateStatusLabel();
                } catch (Exception e) {
                    System.err.println("Error updating UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (SQLException e) {
            String errorMsg = "Error loading bookings: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showError(errorMsg);
        }
    }
    
    @FXML
    private void handleDeleteBooking() {
        Booking selectedBooking = bookingsTableView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Booking");
        alert.setContentText("Are you sure you want to delete this booking?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (bookingDAO.deleteBooking(selectedBooking.getBookingId())) {
                    showSuccess("Booking deleted successfully!");
                    
                    // Check if we need to go back a page if this was the last item on the current page
                    if (bookingList.size() == 1 && currentPage > 1) {
                        currentPage--;
                    }
                    
                    // Reload data
                    loadBookings();
                    handleClearFields();
                } else {
                    showError("Failed to delete booking");
                }
            } catch (SQLException e) {
                showError("Error deleting booking: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterStartDate.setValue(null);
        filterEndDate.setValue(null);
        loadBookings();
    }

    @FXML
    private void handleRefresh() {
        currentPage = 1; // Reset to first page when refreshing
        loadBookings();
    }
    


    private void applyFilters() {
        try {
            // Get all bookings from the database
            List<Booking> allBookings = bookingDAO.findAll();
            
            // Apply filters to get filtered list
            List<Booking> filteredBookings = filterBookings(allBookings);
            
            // Update total items and pages
            totalItems = filteredBookings.size();
            int itemsPerPage = itemsPerPageCombo.getValue();
            totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            
            // Ensure current page is within bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            } else if (currentPage < 1) {
                currentPage = 1;
            }
            
            // Calculate pagination
            int fromIndex = (currentPage - 1) * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, filteredBookings.size());
            
            // Get the sublist for the current page
            List<Booking> pagedBookings = filteredBookings.subList(
                Math.min(fromIndex, filteredBookings.size()),
                Math.min(toIndex, filteredBookings.size())
            );
            
            // Update the table with paginated data
            bookingList.setAll(pagedBookings);
            
            // Update pagination controls
            updatePaginationControls();
            
        } catch (SQLException e) {
            showError("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 