package com.hotel.controllers;

import com.hotel.dao.InvoiceDAO;
import com.hotel.dao.impl.InvoiceDAOImpl;
import com.hotel.model.Invoice;
import com.hotel.model.BookingService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class InvoiceController implements Initializable {

    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Integer> invoiceIdColumn;
    @FXML private TableColumn<Invoice, String> customerNameColumn;
    @FXML private TableColumn<Invoice, String> roomNumberColumn;
    @FXML private TableColumn<Invoice, java.sql.Date> issueDateColumn;
    @FXML private TableColumn<Invoice, java.sql.Date> dueDateColumn;
    @FXML private TableColumn<Invoice, BigDecimal> roomCostColumn;
    @FXML private TableColumn<Invoice, BigDecimal> serviceCostColumn;
    @FXML private TableColumn<Invoice, BigDecimal> totalAmountColumn;
    @FXML private TableColumn<Invoice, BigDecimal> paidAmountColumn;
    @FXML private TableColumn<Invoice, String> invoiceStatusColumn;
    
    @FXML private Button btnExportToPdf;
    @FXML private Button btnRefresh;
    @FXML private Button btnClearFilters;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Label statusLabel;
    
    private final InvoiceDAO invoiceDAO = new InvoiceDAOImpl();
    private ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private FilteredList<Invoice> filteredInvoices;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupFilters();
        setupTableSelectionListener();
        loadInvoiceData();
    }
    
    private void setupTableColumns() {
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        issueDateColumn.setCellValueFactory(new PropertyValueFactory<>("issueDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        roomCostColumn.setCellValueFactory(new PropertyValueFactory<>("roomCost"));
        serviceCostColumn.setCellValueFactory(new PropertyValueFactory<>("serviceCost"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        paidAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        invoiceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceStatus"));
        
        // Format currency columns
        roomCostColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });
        
        serviceCostColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });
        
        totalAmountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });
        
        paidAmountColumn.setCellFactory(column -> new TableCell<Invoice, BigDecimal>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(amount));
                }
            }
        });
        
        // Format date columns
        issueDateColumn.setCellFactory(column -> new TableCell<Invoice, java.sql.Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            @Override
            protected void updateItem(java.sql.Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
        
        dueDateColumn.setCellFactory(column -> new TableCell<Invoice, java.sql.Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            @Override
            protected void updateItem(java.sql.Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(date));
                }
            }
        });
    }
    
    private void setupFilters() {
        // Initialize status filter options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            "All", "Pending", "Paid", "Partially Paid", "Overdue"
        );
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue("All");
        
        // Setup search and filter listeners
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterInvoices());
        statusFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> filterInvoices());
    }
    
    private void setupTableSelectionListener() {
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> btnExportToPdf.setDisable(newValue == null)
        );
    }
    
    private void loadInvoiceData() {
        try {
            List<Invoice> invoices = invoiceDAO.getAllInvoices();
            invoiceList.setAll(invoices);
            
            // Initialize filtered list
            filteredInvoices = new FilteredList<>(invoiceList, p -> true);
            invoiceTable.setItems(filteredInvoices);
            
            statusLabel.setText("Loaded " + invoices.size() + " invoices");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to load invoices", e.getMessage());
            statusLabel.setText("Error loading invoices");
        }
    }
    
    private void filterInvoices() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = statusFilterComboBox.getValue();
        
        filteredInvoices.setPredicate(invoice -> {
            // Filter by search text
            boolean matchesSearch = searchText.isEmpty() ||
                    (invoice.getCustomerName() != null && 
                     invoice.getCustomerName().toLowerCase().contains(searchText)) ||
                    (invoice.getInvoiceStatus() != null && 
                     invoice.getInvoiceStatus().toLowerCase().contains(searchText));
            
            // Filter by status
            boolean matchesStatus = "All".equals(statusFilter) || 
                    (invoice.getInvoiceStatus() != null && 
                     invoice.getInvoiceStatus().equals(statusFilter));
            
            return matchesSearch && matchesStatus;
        });
        
        statusLabel.setText("Showing " + filteredInvoices.size() + " of " + invoiceList.size() + " invoices");
    }
    
    @FXML
    private void handleRefresh() {
        loadInvoiceData();
    }
    
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("All");
        filterInvoices();
    }
    
    @FXML
    private void handleExportToPdf() {
        Invoice selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                    "No Invoice Selected", "Please select an invoice to export.");
            return;
        }
        
        try {
            // Get detailed invoice data
            Invoice invoiceDetails = invoiceDAO.getInvoiceDetails(selectedInvoice.getInvoiceId());
            List<BookingService> bookingServices = 
                    invoiceDAO.getBookingServicesForBooking(invoiceDetails.getBookingId());
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("Invoice_" + invoiceDetails.getInvoiceId() + ".pdf");
            
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                generatePdf(file, invoiceDetails, bookingServices);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                    "Failed to load invoice details", e.getMessage());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "PDF Generation Error", 
                    "Failed to generate PDF", e.getMessage());
        }
    }
    
    private void generatePdf(File file, Invoice invoice, List<BookingService> services) throws IOException {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float yPosition = yStart;
                int fontSize = 12;
                
                // Hotel Name and Invoice Title
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Hotel Management System");
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Invoice No: " + invoice.getInvoiceId());
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Customer Name: " + invoice.getCustomerName());
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Room Number: " + invoice.getRoomNumber());
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Issue Date: " + formatDate(invoice.getIssueDate()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Due Date: " + formatDate(invoice.getDueDate()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Services:");
                contentStream.endText();
                
                yPosition -= 20;
                
                for (BookingService service : services) {
                    BigDecimal price = service.getServicePrice();
                    BigDecimal total = price.multiply(BigDecimal.valueOf(service.getQuantity()));
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(service.getServiceName() + " x " + service.getQuantity() + " = " + formatCurrency(total));
                    contentStream.endText();
                    
                    yPosition -= 20;
                }
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Room Cost: " + formatCurrency(invoice.getRoomCost()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Service Cost: " + formatCurrency(invoice.getServiceCost()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total Amount: " + formatCurrency(invoice.getTotalAmount()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Paid Amount: " + formatCurrency(invoice.getPaidAmount()));
                contentStream.endText();
                
                yPosition -= 20;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Balance Due: " + formatCurrency(invoice.getBalanceDue()));
                contentStream.endText();
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
        } finally {
            if (document != null) {
                document.save(file);
                document.close();
            }
        }
    }
    
    private String formatDate(java.sql.Date date) {
        if (date == null) return "N/A";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "N/A";
        return NumberFormat.getCurrencyInstance().format(amount);
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
