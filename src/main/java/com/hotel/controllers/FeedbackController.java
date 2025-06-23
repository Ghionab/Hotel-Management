package com.hotel.controllers;

import com.hotel.dao.CustomerDAO;
import com.hotel.dao.BookingDAO;
import com.hotel.dao.FeedbackDAO;
import com.hotel.dao.impl.CustomerDAOImpl;
import com.hotel.dao.impl.BookingDAOImpl;
import com.hotel.dao.impl.FeedbackDAOImpl;
import com.hotel.model.Customer;
import com.hotel.model.Booking;
import com.hotel.models.Feedback;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import java.util.stream.Collectors;
import java.util.List;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.util.ResourceBundle;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.SQLException;

public class FeedbackController implements Initializable {
    
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> bookingComboBox;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextArea commentsArea;
    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, Timestamp> dateColumn;
    @FXML private TableColumn<Feedback, String> customerNameColumn;
    @FXML private TableColumn<Feedback, Integer> bookingIdColumn;
    @FXML private TableColumn<Feedback, Integer> ratingColumn;
    @FXML private TableColumn<Feedback, String> commentsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> filterRatingComboBox;
    @FXML private ComboBox<Integer> itemsPerPageCombo;
    @FXML private Button firstPageButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Button lastPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private Label statusLabel;
    
    // ObservableList to hold the filtered feedback
    private ObservableList<Feedback> feedbackList = FXCollections.observableArrayList();
    
    // Pagination variables
    private static final int ITEMS_PER_PAGE = 10;
    private int currentPage = 1;
    private int totalItems = 0;
    private int totalPages = 0;

    private final FeedbackDAO feedbackDAO = new FeedbackDAOImpl();
    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final BookingDAO bookingDAO = new BookingDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupComboBoxes();
        setupPaginationControls();
        loadFeedback();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("feedbackDate"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("comments"));
        
        // Add custom cell factories for formatting if needed
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toLocalDateTime().toString());
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Setup rating options (1-5)
        ObservableList<Integer> ratingOptions = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        ratingComboBox.setItems(ratingOptions);
        filterRatingComboBox.setItems(ratingOptions);
        
        // Setup search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
        
        // Setup rating filter listener
        filterRatingComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch();
        });
        
        // Load customer data
        try {
            List<Customer> customers = customerDAO.findAll();
            ObservableList<String> customerOptions = FXCollections.observableArrayList();
            customerOptions.add("Select Customer");
            customers.forEach(customer -> 
                customerOptions.add(customer.getFirstName() + " " + customer.getLastName()));
            customerComboBox.setItems(customerOptions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load customers: " + e.getMessage());
        }
        
        // Load booking data
        try {
            List<Booking> bookings = bookingDAO.getAllBookings();
            ObservableList<String> bookingOptions = FXCollections.observableArrayList();
            bookingOptions.add("Select Booking");
            bookings.forEach(booking -> 
                bookingOptions.add("Booking ID: " + booking.getBookingId() + 
                    " (" + booking.getCustomerName() + ", Room: " + booking.getRoomNumber() + ")"));
            bookingComboBox.setItems(bookingOptions);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private void loadFeedback() {
        try {
            // Get all feedback from the database
            List<Feedback> allFeedbacks = feedbackDAO.getAllFeedback();
            
            // Apply filters to get filtered list
            List<Feedback> filteredFeedbacks = filterFeedbacks(allFeedbacks);
            
            // Update total items and pages
            totalItems = filteredFeedbacks.size();
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
            List<Feedback> pagedFeedbacks = filteredFeedbacks.subList(fromIndex, toIndex);
            
            // Update the table
            feedbackList.setAll(pagedFeedbacks);
            feedbackTable.setItems(feedbackList);
            
            // Update pagination controls
            updatePaginationControls();
            
            System.out.println(String.format("Showing items from index %d to %d", fromIndex, toIndex));
            
        } catch (Exception e) {
            showAlert("Error", "Error loading feedback: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private List<Feedback> filterFeedbacks(List<Feedback> feedbacks) {
        String searchTerm = searchField.getText().toLowerCase();
        Integer ratingFilter = filterRatingComboBox.getValue();
        
        return feedbacks.stream()
            .filter(feedback -> 
                searchTerm.isEmpty() || 
                (feedback.getComments() != null && feedback.getComments().toLowerCase().contains(searchTerm)) ||
                (feedback.getCustomerName() != null && feedback.getCustomerName().toLowerCase().contains(searchTerm)))
            .filter(feedback -> 
                ratingFilter == null || 
                feedback.getRating() == ratingFilter)
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
                loadFeedback();
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
            loadFeedback();
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadFeedback();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadFeedback();
        }
    }

    @FXML
    private void handleLastPage() {
        if (currentPage != totalPages && totalPages > 0) {
            currentPage = totalPages;
            loadFeedback();
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

    @FXML
    private void handleSubmitFeedback() {
        // Validate input
        if (customerComboBox.getValue() == null || ratingComboBox.getValue() == null) {
            showAlert("Error", "Please fill in all required fields.");
            return;
        }

        try {
            Feedback feedback = new Feedback();
            // Get customer ID from selected customer
            String selectedCustomer = customerComboBox.getValue();
            if (selectedCustomer != null && !selectedCustomer.equals("Select Customer")) {
                // Extract customer ID from the display string
                List<Customer> customers = customerDAO.findAll();
                Customer selectedCustomerObj = customers.stream()
                    .filter(c -> (c.getFirstName() + " " + c.getLastName()).equals(selectedCustomer))
                    .findFirst().orElse(null);
                if (selectedCustomerObj != null) {
                    feedback.setCustomerId(selectedCustomerObj.getCustomerId());
                }
            }
            
            // Get booking ID if a booking is selected
            if (bookingComboBox.getValue() != null && !bookingComboBox.getValue().equals("Select Booking")) {
                try {
                    // Extract booking ID from the display string
                    // Example format: "Booking ID: 123 (John Doe, Room: 101)"
                    String selectedBooking = bookingComboBox.getValue();
                    // Find the part after "ID: " and before the next space
                    int idStart = selectedBooking.indexOf("ID: ") + 4; // +4 to skip "ID: "
                    if (idStart > 3) { // Make sure we found "ID: "
                        int idEnd = selectedBooking.indexOf(' ', idStart);
                        if (idEnd == -1) {
                            idEnd = selectedBooking.indexOf('(', idStart);
                            if (idEnd == -1) idEnd = selectedBooking.length();
                        }
                        String idStr = selectedBooking.substring(idStart, idEnd).trim();
                        int bookingId = Integer.parseInt(idStr);
                        feedback.setBookingId(bookingId);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing booking ID: " + e.getMessage());
                    // Continue without setting booking ID
                }
            }
            feedback.setRating(ratingComboBox.getValue());
            feedback.setComments(commentsArea.getText());
            feedback.setFeedbackDate(Timestamp.valueOf(LocalDateTime.now()));

            feedbackDAO.addFeedback(feedback);
            
            // Clear form and refresh table
            clearForm();
            loadFeedback();
            
            showAlert("Success", "Feedback submitted successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to submit feedback: " + e.getMessage());
        }
    }

    private void clearForm() {
        customerComboBox.setValue(null);
        bookingComboBox.setValue(null);
        ratingComboBox.setValue(null);
        commentsArea.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1; // Reset to first page when searching
        loadFeedback();
    }
    
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        filterRatingComboBox.getSelectionModel().clearSelection();
        currentPage = 1; // Reset to first page when clearing filters
        loadFeedback();
    }
    
    @FXML
    private void handleRefresh() {
        currentPage = 1; // Reset to first page when refreshing
        loadFeedback();
    }
}
