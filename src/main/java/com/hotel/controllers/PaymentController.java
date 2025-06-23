package com.hotel.controllers;

import com.hotel.dao.PaymentDAO;
import com.hotel.dao.impl.PaymentDAOImpl;
import com.hotel.model.Invoice;
import com.hotel.model.Payment;
import com.hotel.model.User;
import com.hotel.util.AlertUtil;
import com.hotel.util.RoleBasedAccessControl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentController implements Initializable, UserAware {
    private User currentUser;

    // DAO
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    
    // Form fields
    @FXML private ComboBox<Invoice> invoiceComboBox;
    @FXML private Label invoiceStatusLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label roomNumberLabel;
    @FXML private Label invoiceTotalLabel;
    @FXML private Label balanceDueLabel;
    @FXML private DatePicker paymentDatePicker;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField transactionIdField;
    @FXML private TextArea notesArea;
    @FXML private Button clearButton;
    @FXML private Button recordPaymentButton;
    
    // Table and columns
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> paymentIdColumn;
    @FXML private TableColumn<Payment, Integer> invoiceIdColumn;
    @FXML private TableColumn<Payment, String> customerNameColumn;
    @FXML private TableColumn<Payment, String> roomNumberColumn;
    @FXML private TableColumn<Payment, Date> paymentDateColumn;
    @FXML private TableColumn<Payment, BigDecimal> amountColumn;
    @FXML private TableColumn<Payment, String> paymentMethodColumn;
    @FXML private TableColumn<Payment, String> transactionIdColumn;
    @FXML private TableColumn<Payment, String> invoiceStatusColumn;
    
    // Export buttons
    @FXML private Button exportToExcelButton;
    @FXML private Button generateReceiptButton;
    
    // Data
    private ObservableList<Payment> payments = FXCollections.observableArrayList();
    private ObservableList<Invoice> invoices = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializePaymentMethodComboBox();
        initializeInvoiceComboBox();
        initializeTableColumns();
        setupListeners();
        
        // Set default date to today
        paymentDatePicker.setValue(LocalDate.now());
        
        // Load data
        loadInvoices();
        loadPayments();
        
        // Initially disable all CRUD operations until we know the user's role
        disableCrudOperations(true);
    }
    
    @Override
    public void setUser(User user) {
        this.currentUser = user;
        
        // Update CRUD operations based on user role
        boolean isRegularStaff = RoleBasedAccessControl.isRegularStaff(user);
        disableCrudOperations(isRegularStaff);
    }
    
    private void disableCrudOperations(boolean disable) {
        // Disable/enable all CRUD buttons
        if (recordPaymentButton != null) recordPaymentButton.setDisable(disable);
        if (clearButton != null) clearButton.setDisable(disable);
        if (exportToExcelButton != null) exportToExcelButton.setDisable(disable);
        if (generateReceiptButton != null) generateReceiptButton.setDisable(disable);
        
        // Disable/enable input fields
        if (paymentDatePicker != null) paymentDatePicker.setDisable(disable);
        if (amountField != null) amountField.setDisable(disable);
        if (paymentMethodComboBox != null) paymentMethodComboBox.setDisable(disable);
        if (transactionIdField != null) transactionIdField.setDisable(disable);
        if (notesArea != null) notesArea.setDisable(disable);
        if (invoiceComboBox != null) invoiceComboBox.setDisable(disable);
    }
    
    private void initializePaymentMethodComboBox() {
        // Add common payment methods
        ObservableList<String> paymentMethods = FXCollections.observableArrayList(
                "Cash", "Credit Card", "Debit Card", "Bank Transfer", "Check", "Mobile Payment", "Other"
        );
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.getSelectionModel().selectFirst();
    }
    
    private void initializeInvoiceComboBox() {
        // Setup invoice combo box with custom cell factory to display relevant info
        invoiceComboBox.setCellFactory(param -> new ListCell<Invoice>() {
            @Override
            protected void updateItem(Invoice invoice, boolean empty) {
                super.updateItem(invoice, empty);
                if (empty || invoice == null) {
                    setText(null);
                } else {
                    setText(String.format("Invoice #%d - %s - $%.2f", 
                            invoice.getInvoiceId(), 
                            invoice.getCustomerName(),
                            invoice.getBalanceDue().doubleValue()));
                }
            }
        });
        
        // Set the same converter for the button cell
        invoiceComboBox.setButtonCell(new ListCell<Invoice>() {
            @Override
            protected void updateItem(Invoice invoice, boolean empty) {
                super.updateItem(invoice, empty);
                if (empty || invoice == null) {
                    setText(null);
                } else {
                    setText(String.format("Invoice #%d - %s", 
                            invoice.getInvoiceId(), 
                            invoice.getCustomerName()));
                }
            }
        });
        
        // Set converter for value
        invoiceComboBox.setConverter(new StringConverter<Invoice>() {
            @Override
            public String toString(Invoice invoice) {
                if (invoice == null) {
                    return null;
                }
                return String.format("Invoice #%d - %s", 
                        invoice.getInvoiceId(), 
                        invoice.getCustomerName());
            }
            
            @Override
            public Invoice fromString(String string) {
                return null; // Not needed for this use case
            }
        });
    }
    
    private void initializeTableColumns() {
        paymentIdColumn.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        invoiceIdColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        paymentDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        transactionIdColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        invoiceStatusColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceStatus"));
        
        // Format currency column
        amountColumn.setCellFactory(column -> new TableCell<Payment, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });
        
        // Format date column
        paymentDateColumn.setCellFactory(column -> new TableCell<Payment, Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.toLocalDate().format(formatter));
                }
            }
        });
        
        // Set table items
        paymentTable.setItems(payments);
    }
    
    private void setupListeners() {
        // Invoice selection listener
        invoiceComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateInvoiceDetails(newVal);
                
                // Set default payment amount to balance due
                amountField.setText(String.format("%.2f", newVal.getBalanceDue().doubleValue()));
            } else {
                clearInvoiceDetails();
            }
        });
        
        // Table selection listener for enabling/disabling receipt button
        paymentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            generateReceiptButton.setDisable(newVal == null);
        });
    }
    
    private void updateInvoiceDetails(Invoice invoice) {
        customerNameLabel.setText(invoice.getCustomerName());
        roomNumberLabel.setText(invoice.getRoomNumber());
        invoiceStatusLabel.setText(invoice.getInvoiceStatus());
        invoiceTotalLabel.setText(String.format("$%.2f", invoice.getTotalAmount().doubleValue()));
        balanceDueLabel.setText(String.format("$%.2f", invoice.getBalanceDue().doubleValue()));
        
        // Set text color based on invoice status
        String status = invoice.getInvoiceStatus();
        if ("Paid".equals(status)) {
            invoiceStatusLabel.setStyle("-fx-text-fill: green;");
        } else if ("Partially Paid".equals(status)) {
            invoiceStatusLabel.setStyle("-fx-text-fill: orange;");
        } else {
            invoiceStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void clearInvoiceDetails() {
        customerNameLabel.setText("N/A");
        roomNumberLabel.setText("N/A");
        invoiceStatusLabel.setText("N/A");
        invoiceTotalLabel.setText("N/A");
        balanceDueLabel.setText("N/A");
        invoiceStatusLabel.setStyle("");
    }
    
    private void loadInvoices() {
        try {
            List<Invoice> invoiceList = paymentDAO.getAllInvoices();
            invoices.clear();
            invoices.addAll(invoiceList);
            invoiceComboBox.setItems(invoices);
            
            if (!invoices.isEmpty()) {
                invoiceComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            AlertUtil.showErrorAlert("Database Error", "Failed to load invoices", e.getMessage());
        }
    }
    
    private void loadPayments() {
        try {
            List<Payment> paymentList = paymentDAO.getAllPayments();
            payments.clear();
            payments.addAll(paymentList);
        } catch (SQLException e) {
            AlertUtil.showErrorAlert("Database Error", "Failed to load payments", e.getMessage());
        }
    }
    
    @FXML
    private void handleRecordPayment() {
        try {
            // Validate input
            Invoice selectedInvoice = invoiceComboBox.getValue();
            if (selectedInvoice == null) {
                AlertUtil.showWarningAlert("Validation Error", "No Invoice Selected", "Please select an invoice for this payment.");
                return;
            }
            
            if (paymentDatePicker.getValue() == null) {
                AlertUtil.showWarningAlert("Validation Error", "No Date Selected", "Please select a payment date.");
                return;
            }
            
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                AlertUtil.showWarningAlert("Validation Error", "Amount Required", "Please enter a payment amount.");
                return;
            }
            
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    AlertUtil.showWarningAlert("Validation Error", "Invalid Amount", "Payment amount must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtil.showWarningAlert("Validation Error", "Invalid Amount", "Please enter a valid number for the amount.");
                return;
            }
            
            String paymentMethod = paymentMethodComboBox.getValue();
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                AlertUtil.showWarningAlert("Validation Error", "Payment Method Required", "Please select a payment method.");
                return;
            }
            
            // Create payment object
            Payment payment = new Payment();
            payment.setInvoiceId(selectedInvoice.getInvoiceId());
            payment.setPaymentDate(Date.valueOf(paymentDatePicker.getValue()));
            payment.setAmount(amount);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId(transactionIdField.getText().trim());
            payment.setNotes(notesArea.getText().trim());
            
            // Save to database
            paymentDAO.addPayment(payment);
            
            // Show success message
            AlertUtil.showInformationAlert("Success", "Payment Recorded", 
                    "Payment has been successfully recorded.\nInvoice status will be updated automatically.");
            
            // Refresh data
            loadInvoices();
            loadPayments();
            
            // Clear form
            handleClearFields();
            
        } catch (SQLException e) {
            AlertUtil.showErrorAlert("Database Error", "Failed to record payment", e.getMessage());
        }
    }
    
    @FXML
    private void handleClearFields() {
        if (!invoices.isEmpty()) {
            invoiceComboBox.getSelectionModel().selectFirst();
        } else {
            invoiceComboBox.getSelectionModel().clearSelection();
        }
        paymentDatePicker.setValue(LocalDate.now());
        amountField.clear();
        paymentMethodComboBox.getSelectionModel().selectFirst();
        transactionIdField.clear();
        notesArea.clear();
    }
    
    @FXML
    private void handleExportToExcel() {
        if (payments.isEmpty()) {
            AlertUtil.showWarningAlert("No Data", "No Payments to Export", 
                    "There are no payment records to export to Excel.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Payment_Records.xlsx");
        
        File file = fileChooser.showSaveDialog(paymentTable.getScene().getWindow());
        if (file != null) {
            try {
                generateExcel(file);
                AlertUtil.showInformationAlert("Success", "Excel File Generated", 
                        "Payment records have been exported to Excel successfully.");
            } catch (IOException e) {
                AlertUtil.showErrorAlert("Export Error", "Failed to generate Excel file", e.getMessage());
            }
        }
    }
    
    private void generateExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payment Records");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Payment ID", "Invoice ID", "Customer", "Room", "Payment Date", 
                "Amount", "Method", "Transaction ID", "Notes", "Invoice Status"
            };
            
            // Create header cell style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Add headers
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(payment.getPaymentId());
                row.createCell(1).setCellValue(payment.getInvoiceId());
                row.createCell(2).setCellValue(payment.getCustomerName());
                row.createCell(3).setCellValue(payment.getRoomNumber());
                
                org.apache.poi.ss.usermodel.Cell dateCell = row.createCell(4);
                if (payment.getPaymentDate() != null) {
                    dateCell.setCellValue(payment.getPaymentDate().toString());
                }
                
                org.apache.poi.ss.usermodel.Cell amountCell = row.createCell(5);
                if (payment.getAmount() != null) {
                    amountCell.setCellValue(payment.getAmount().doubleValue());
                }
                
                row.createCell(6).setCellValue(payment.getPaymentMethod());
                row.createCell(7).setCellValue(payment.getTransactionId());
                row.createCell(8).setCellValue(payment.getNotes());
                row.createCell(9).setCellValue(payment.getInvoiceStatus());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to file
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
            }
        }
    }
    
    @FXML
    private void handleGenerateReceipt() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            AlertUtil.showWarningAlert("No Selection", "No Payment Selected", 
                    "Please select a payment to generate a receipt.");
            return;
        }
        
        try {
            // Get the payment details directly from the selected payment
            // No need for additional database query since we already have all the data
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Receipt PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("Receipt_" + selectedPayment.getPaymentId() + ".pdf");
            
            File file = fileChooser.showSaveDialog(paymentTable.getScene().getWindow());
            if (file != null) {
                generatePdf(file, selectedPayment);
                AlertUtil.showInformationAlert("Success", "PDF Generated", 
                        "Receipt has been generated successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showErrorAlert("Error", "Failed to generate PDF", e.getMessage());
        }
    }
    
    private void generatePdf(File file, Payment payment) throws IOException {
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
                
                // Header
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("HOTEL MANAGEMENT SYSTEM");
                contentStream.endText();
                
                yPosition -= 25;
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PAYMENT RECEIPT");
                contentStream.endText();
                
                yPosition -= 30;
                
                // Draw line after header
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                
                yPosition -= 20;
                
                // Payment Information Section
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("PAYMENT DETAILS");
                contentStream.endText();
                yPosition -= 20;
                
                addLabelValue(contentStream, "Payment ID:", String.valueOf(payment.getPaymentId()), margin, yPosition);
                yPosition -= 20;
                
                addLabelValue(contentStream, "Invoice ID:", String.valueOf(payment.getInvoiceId()), margin, yPosition);
                yPosition -= 20;
                
                addLabelValue(contentStream, "Date:", formatDate(payment.getPaymentDate()), margin, yPosition);
                yPosition -= 20;
                
                addLabelValue(contentStream, "Amount:", formatCurrency(payment.getAmount()), margin, yPosition);
                yPosition -= 20;
                
                String paymentMethod = payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "N/A";
                addLabelValue(contentStream, "Payment Method:", paymentMethod, margin, yPosition);
                yPosition -= 20;
                
                if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
                    addLabelValue(contentStream, "Transaction ID:", payment.getTransactionId(), margin, yPosition);
                    yPosition -= 20;
                }
                
                // Customer Information Section
                yPosition -= 10;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("CUSTOMER INFORMATION");
                contentStream.endText();
                yPosition -= 20;
                
                String customerName = payment.getCustomerName() != null ? payment.getCustomerName() : "N/A";
                addLabelValue(contentStream, "Customer Name:", customerName, margin, yPosition);
                yPosition -= 20;
                
                String roomNumber = payment.getRoomNumber() != null ? payment.getRoomNumber() : "N/A";
                addLabelValue(contentStream, "Room Number:", roomNumber, margin, yPosition);
                
                // Invoice Status Section
                yPosition -= 30;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("INVOICE STATUS");
                contentStream.endText();
                yPosition -= 20;
                
                String invoiceStatus = payment.getInvoiceStatus() != null ? payment.getInvoiceStatus() : "N/A";
                addLabelValue(contentStream, "Status:", invoiceStatus, margin, yPosition);
                yPosition -= 20;
                
                if (payment.getInvoiceTotal() != null) {
                    addLabelValue(contentStream, "Invoice Total:", formatCurrency(payment.getInvoiceTotal()), margin, yPosition);
                    yPosition -= 20;
                }
                
                if (payment.getPaidAmount() != null) {
                    addLabelValue(contentStream, "Amount Paid:", formatCurrency(payment.getPaidAmount()), margin, yPosition);
                    yPosition -= 20;
                }
                
                if (payment.getBalanceDue() != null) {
                    addLabelValue(contentStream, "Balance Due:", formatCurrency(payment.getBalanceDue()), margin, yPosition);
                    yPosition -= 20;
                }
                
                // Notes Section
                if (payment.getNotes() != null && !payment.getNotes().trim().isEmpty()) {
                    yPosition -= 10;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("NOTES");
                    contentStream.endText();
                    yPosition -= 20;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, fontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(payment.getNotes());
                    contentStream.endText();
                }
                
                // Footer
                yPosition = 100;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(margin, 70);
                contentStream.showText("Thank you for your payment. This is an official receipt.");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(margin, 50);
                contentStream.showText("Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                contentStream.endText();
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            
            document.save(file);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }
    
    private void addLabelValue(PDPageContentStream contentStream, String label, String value, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();
        
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(x + 120, y);
        contentStream.showText(value);
        contentStream.endText();
    }
    
    private String formatDate(Date date) {
        if (date == null) {
            return "N/A";
        }
        return date.toLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%.2f", amount);
    }
}
