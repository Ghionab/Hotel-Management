package com.hotel.dao;

import com.hotel.model.Payment;
import com.hotel.model.Invoice;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Payment-related database operations
 */
public interface PaymentDAO {
    
    /**
     * Adds a new payment record to the database
     * Note: Invoice updates are handled by database triggers
     * 
     * @param payment The payment object to be added
     * @throws SQLException If a database error occurs
     */
    void addPayment(Payment payment) throws SQLException;
    
    /**
     * Retrieves all payment records with related data
     * 
     * @return List of Payment objects with associated invoice, booking, customer, and room data
     * @throws SQLException If a database error occurs
     */
    List<Payment> getAllPayments() throws SQLException;
    
    /**
     * Retrieves detailed information about a specific payment for receipt generation
     * 
     * @param paymentId The ID of the payment to retrieve
     * @return Payment object with all associated details
     * @throws SQLException If a database error occurs
     */
    Payment getPaymentDetailsForReceipt(int paymentId) throws SQLException;
    
    /**
     * Retrieves all invoices for payment selection
     * 
     * @return List of Invoice objects with necessary details for display
     * @throws SQLException If a database error occurs
     */
    List<Invoice> getAllInvoices() throws SQLException;
}
