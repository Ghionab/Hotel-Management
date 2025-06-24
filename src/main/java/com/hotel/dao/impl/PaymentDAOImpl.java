package com.hotel.dao.impl;

import com.hotel.dao.PaymentDAO;
import com.hotel.model.Payment;
import com.hotel.model.Invoice;
import com.hotel.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * Implementation of the PaymentDAO interface for database operations
 */
public class PaymentDAOImpl implements PaymentDAO {

    @Override
    public void addPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (invoice_id, payment_date, amount, payment_method, transaction_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, payment.getInvoiceId());
            stmt.setDate(2, payment.getPaymentDate());
            stmt.setBigDecimal(3, payment.getAmount());
            stmt.setString(4, payment.getPaymentMethod());
            stmt.setString(5, payment.getTransactionId());
            stmt.setString(6, payment.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating payment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setPaymentId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating payment failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        
        String sql = "SELECT p.payment_id, p.invoice_id, p.payment_date, p.amount, p.payment_method, " +
                     "p.transaction_id, p.notes, p.created_at, " +
                     "i.total_amount, i.paid_amount, i.invoice_status, " +
                     "b.booking_id, b.check_in_date, b.check_out_date, " +
                     "c.first_name, c.last_name, " +
                     "r.room_number " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN bookings b ON i.booking_id = b.booking_id " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "ORDER BY p.payment_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setInvoiceId(rs.getInt("invoice_id"));
                payment.setPaymentDate(rs.getDate("payment_date"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setTransactionId(rs.getString("transaction_id"));
                payment.setNotes(rs.getString("notes"));
                payment.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Related data
                payment.setInvoiceTotal(rs.getBigDecimal("total_amount"));
                payment.setPaidAmount(rs.getBigDecimal("paid_amount"));
                payment.setInvoiceStatus(rs.getString("invoice_status"));
                payment.setBookingId(rs.getInt("booking_id"));
                payment.setCheckInDate(rs.getDate("check_in_date"));
                payment.setCheckOutDate(rs.getDate("check_out_date"));
                payment.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                payment.setRoomNumber(rs.getString("room_number"));
                
                // Calculate balance due
                BigDecimal total = rs.getBigDecimal("total_amount");
                BigDecimal paid = rs.getBigDecimal("paid_amount");
                payment.setBalanceDue(total.subtract(paid));
                
                payments.add(payment);
            }
        }
        
        return payments;
    }

    @Override
    public Payment getPaymentDetailsForReceipt(int paymentId) throws SQLException {
        String sql = "SELECT p.*, i.total_amount, i.paid_amount, i.invoice_status, " +
                     "CONCAT(c.first_name, ' ', c.last_name) as customer_name, " +
                     "r.room_number, b.check_in_date, b.check_out_date " +
                     "FROM payments p " +
                     "JOIN invoices i ON p.invoice_id = i.invoice_id " +
                     "JOIN bookings b ON i.booking_id = b.booking_id " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "WHERE p.payment_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, paymentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    // Set payment fields
                    payment.setPaymentId(rs.getInt("payment_id"));
                    payment.setInvoiceId(rs.getInt("invoice_id"));
                    payment.setPaymentDate(rs.getDate("payment_date"));
                    payment.setAmount(rs.getBigDecimal("amount"));
                    payment.setPaymentMethod(rs.getString("payment_method"));
                    payment.setTransactionId(rs.getString("transaction_id"));
                    payment.setNotes(rs.getString("notes"));
                    payment.setCreatedAt(rs.getTimestamp("created_at"));
                    
                    // Set related invoice data
                    payment.setInvoiceTotal(rs.getBigDecimal("total_amount"));
                    payment.setPaidAmount(rs.getBigDecimal("paid_amount"));
                    payment.setInvoiceStatus(rs.getString("invoice_status"));
                    
                    // Set customer and booking data
                    payment.setCustomerName(rs.getString("customer_name"));
                    payment.setRoomNumber(rs.getString("room_number"));
                    payment.setCheckInDate(rs.getDate("check_in_date"));
                    payment.setCheckOutDate(rs.getDate("check_out_date"));
                    
                    // Calculate balance due
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    BigDecimal paid = rs.getBigDecimal("paid_amount");
                    payment.setBalanceDue(total.subtract(paid));
                    
                    return payment;
                }
                return null;
            }
        }
    }

    @Override
    public List<Invoice> getAllInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        
        String sql = "SELECT i.invoice_id, i.booking_id, i.issue_date, i.due_date, " +
                     "i.total_amount, i.paid_amount, i.invoice_status, " +
                     "b.check_in_date, b.check_out_date, " +
                     "c.first_name, c.last_name, " +
                     "r.room_number " +
                     "FROM invoices i " +
                     "JOIN bookings b ON i.booking_id = b.booking_id " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "ORDER BY i.invoice_status, i.due_date";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setInvoiceId(rs.getInt("invoice_id"));
                invoice.setBookingId(rs.getInt("booking_id"));
                invoice.setIssueDate(rs.getDate("issue_date"));
                invoice.setDueDate(rs.getDate("due_date"));
                invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
                invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
                invoice.setInvoiceStatus(rs.getString("invoice_status"));
                
                // Related data
                invoice.setCheckInDate(rs.getDate("check_in_date"));
                invoice.setCheckOutDate(rs.getDate("check_out_date"));
                invoice.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                invoice.setRoomNumber(rs.getString("room_number"));
                
                // Balance due is calculated by getBalanceDue() method
                
                invoices.add(invoice);
            }
        }
        
        return invoices;
    }
}
