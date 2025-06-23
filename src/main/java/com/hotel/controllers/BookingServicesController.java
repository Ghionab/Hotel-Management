package com.hotel.controllers;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.BookingServiceDAO;
import com.hotel.dao.ServiceDAO;
import com.hotel.dao.impl.BookingDAOImpl;
import com.hotel.dao.impl.BookingServiceDAOImpl;
import com.hotel.dao.impl.ServiceDAOImpl;
import com.hotel.model.Booking;
import com.hotel.model.BookingService;
import com.hotel.model.Service;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class BookingServicesController implements Initializable {

    @FXML private TableView<BookingService> bookingServicesTable;
    @FXML private TableColumn<BookingService, Integer> idColumn;
    @FXML private TableColumn<BookingService, String> customerNameColumn;
    @FXML private TableColumn<BookingService, String> roomNumberColumn;
    @FXML private TableColumn<BookingService, String> serviceNameColumn;
    @FXML private TableColumn<BookingService, Integer> quantityColumn;
    @FXML private TableColumn<BookingService, Date> serviceDateColumn;
    @FXML private TableColumn<BookingService, BigDecimal> servicePriceColumn;
    @FXML private TableColumn<BookingService, BigDecimal> totalPriceColumn;
    
    @FXML private ComboBox<Booking> bookingComboBox;
    @FXML private ComboBox<Service> serviceComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private DatePicker serviceDatePicker;
    
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    
    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem deleteMenuItem;
    
    private BookingServiceDAO bookingServiceDAO;
    private BookingDAO bookingDAO;
    private ServiceDAO serviceDAO;
    
    private ObservableList<BookingService> bookingServicesData;
    private BookingService selectedBookingService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        bookingServiceDAO = new BookingServiceDAOImpl();
        bookingDAO = new BookingDAOImpl();
        serviceDAO = new ServiceDAOImpl();
        
        // Initialize data lists
        bookingServicesData = FXCollections.observableArrayList();
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingServiceId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        serviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        serviceDateColumn.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        servicePriceColumn.setCellValueFactory(new PropertyValueFactory<>("servicePrice"));
        
        // Custom cell factory for total price (quantity * price)
        totalPriceColumn.setCellValueFactory(cellData -> {
            BookingService bs = cellData.getValue();
            BigDecimal total = bs.getTotalPrice();
            return new SimpleObjectProperty<>(total);
        });
        
        // Set up quantity spinner
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);
        
        // Set up date picker with current date as default
        serviceDatePicker.setValue(LocalDate.now());
        
        // Set up booking combo box
        setupBookingComboBox();
        
        // Set up service combo box
        setupServiceComboBox();
        
        // Set up table selection listener
        bookingServicesTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedBookingService = newSelection;
                    updateButton.setDisable(newSelection == null);
                    deleteButton.setDisable(newSelection == null);
                    editMenuItem.setDisable(newSelection == null);
                    deleteMenuItem.setDisable(newSelection == null);
                    
                    if (newSelection != null) {
                        populateFields(newSelection);
                    }
                });
        
        // Load booking services data
        loadBookingServices();
    }
    
    private void setupBookingComboBox() {
        try {
            List<Booking> bookings = bookingDAO.getAllBookings();
            bookingComboBox.setItems(FXCollections.observableArrayList(bookings));
            
            // Custom cell factory to display booking info
            bookingComboBox.setCellFactory(new Callback<ListView<Booking>, ListCell<Booking>>() {
                @Override
                public ListCell<Booking> call(ListView<Booking> param) {
                    return new ListCell<Booking>() {
                        @Override
                        protected void updateItem(Booking booking, boolean empty) {
                            super.updateItem(booking, empty);
                            if (empty || booking == null) {
                                setText(null);
                            } else {
                                setText("Booking #" + booking.getBookingId() + " - " + 
                                       booking.getCustomerName() + " (Room " + booking.getRoomNumber() + ")");
                            }
                        }
                    };
                }
            });
            
            // Use the same display for the selected item
            bookingComboBox.setConverter(new StringConverter<Booking>() {
                @Override
                public String toString(Booking booking) {
                    if (booking == null) {
                        return null;
                    }
                    return "Booking #" + booking.getBookingId() + " - " + 
                           booking.getCustomerName() + " (Room " + booking.getRoomNumber() + ")";
                }
                
                @Override
                public Booking fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
        } catch (SQLException e) {
            showDatabaseError("Could not load bookings", e);
        }
    }
    
    private void setupServiceComboBox() {
        try {
            List<Service> services = serviceDAO.getAllServices();
            serviceComboBox.setItems(FXCollections.observableArrayList(services));
            
            // Custom cell factory to display service info
            serviceComboBox.setCellFactory(new Callback<ListView<Service>, ListCell<Service>>() {
                @Override
                public ListCell<Service> call(ListView<Service> param) {
                    return new ListCell<Service>() {
                        @Override
                        protected void updateItem(Service service, boolean empty) {
                            super.updateItem(service, empty);
                            if (empty || service == null) {
                                setText(null);
                            } else {
                                setText(service.getServiceName() + " ($" + service.getPrice() + ")");
                            }
                        }
                    };
                }
            });
            
            // Use the same display for the selected item
            serviceComboBox.setConverter(new StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    if (service == null) {
                        return null;
                    }
                    return service.getServiceName() + " ($" + service.getPrice() + ")";
                }
                
                @Override
                public Service fromString(String string) {
                    return null; // Not needed for this use case
                }
            });
        } catch (SQLException e) {
            showDatabaseError("Could not load services", e);
        }
    }
    
    private void loadBookingServices() {
        try {
            List<BookingService> bookingServices = bookingServiceDAO.getAllBookingServicesWithDetails();
            bookingServicesData.clear();
            bookingServicesData.addAll(bookingServices);
            bookingServicesTable.setItems(bookingServicesData);
        } catch (SQLException e) {
            showDatabaseError("Could not load booking services", e);
        }
    }
    
    private void populateFields(BookingService bookingService) {
        // Find and select the booking in the combo box
        for (Booking booking : bookingComboBox.getItems()) {
            if (booking.getBookingId() == bookingService.getBookingId()) {
                bookingComboBox.setValue(booking);
                break;
            }
        }
        
        // Find and select the service in the combo box
        for (Service service : serviceComboBox.getItems()) {
            if (service.getServiceId() == bookingService.getServiceId()) {
                serviceComboBox.setValue(service);
                break;
            }
        }
        
        // Set quantity
        quantitySpinner.getValueFactory().setValue(bookingService.getQuantity());
        
        // Set service date
        if (bookingService.getServiceDate() != null) {
            serviceDatePicker.setValue(bookingService.getServiceDate().toLocalDate());
        } else {
            serviceDatePicker.setValue(LocalDate.now());
        }
    }
    
    @FXML
    private void handleAddBookingService() {
        if (isInputValid()) {
            try {
                BookingService bookingService = new BookingService();
                bookingService.setBookingId(bookingComboBox.getValue().getBookingId());
                bookingService.setServiceId(serviceComboBox.getValue().getServiceId());
                bookingService.setQuantity(quantitySpinner.getValue());
                bookingService.setServiceDate(Date.valueOf(serviceDatePicker.getValue()));
                
                bookingServiceDAO.addBookingService(bookingService);
                handleClearFields();
                loadBookingServices();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Booking Service Added", 
                        "The booking service was successfully added.");
            } catch (SQLException e) {
                showDatabaseError("Could not add booking service", e);
            }
        }
    }
    
    @FXML
    private void handleUpdateBookingService() {
        if (selectedBookingService != null && isInputValid()) {
            try {
                selectedBookingService.setBookingId(bookingComboBox.getValue().getBookingId());
                selectedBookingService.setServiceId(serviceComboBox.getValue().getServiceId());
                selectedBookingService.setQuantity(quantitySpinner.getValue());
                selectedBookingService.setServiceDate(Date.valueOf(serviceDatePicker.getValue()));
                
                bookingServiceDAO.updateBookingService(selectedBookingService);
                handleClearFields();
                loadBookingServices();
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Booking Service Updated", 
                        "The booking service was successfully updated.");
            } catch (SQLException e) {
                showDatabaseError("Could not update booking service", e);
            }
        }
    }
    
    @FXML
    private void handleDeleteBookingService() {
        if (selectedBookingService != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Booking Service");
            alert.setHeaderText("Are you sure you want to delete this booking service?");
            alert.setContentText("This action cannot be undone.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    bookingServiceDAO.deleteBookingService(selectedBookingService.getBookingServiceId());
                    handleClearFields();
                    loadBookingServices();
                    
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "Booking Service Deleted", 
                            "The booking service was successfully deleted.");
                } catch (SQLException e) {
                    showDatabaseError("Could not delete booking service", e);
                }
            }
        }
    }
    
    @FXML
    private void handleClearFields() {
        bookingComboBox.getSelectionModel().clearSelection();
        serviceComboBox.getSelectionModel().clearSelection();
        quantitySpinner.getValueFactory().setValue(1);
        serviceDatePicker.setValue(LocalDate.now());
        selectedBookingService = null;
        bookingServicesTable.getSelectionModel().clearSelection();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
    
    @FXML
    private void handleEditBookingService() {
        // This is called from the context menu
        if (selectedBookingService != null) {
            populateFields(selectedBookingService);
        }
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        
        if (bookingComboBox.getValue() == null) {
            errorMessage += "Please select a booking.\n";
        }
        
        if (serviceComboBox.getValue() == null) {
            errorMessage += "Please select a service.\n";
        }
        
        if (quantitySpinner.getValue() == null || quantitySpinner.getValue() < 1) {
            errorMessage += "Quantity must be at least 1.\n";
        }
        
        if (serviceDatePicker.getValue() == null) {
            errorMessage += "Please select a service date.\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Fields", 
                    "Please correct invalid fields", errorMessage);
            return false;
        }
    }
    
    private void showDatabaseError(String message, Exception e) {
        showAlert(Alert.AlertType.ERROR, "Database Error", message, e.getMessage());
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
