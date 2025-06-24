package com.hotel.dao.impl;

import com.hotel.dao.InvoiceDAO;
import com.hotel.model.Invoice;
import com.hotel.model.BookingService;
import com.hotel.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAOImpl implements InvoiceDAO {

    @Override
    public List<Invoice> getAllInvoices() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT i.*, " +
                "b.check_in_date, b.check_out_date, " +
                "CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                "r.room_number " +
                "FROM invoices i " +
                "JOIN bookings b ON i.booking_id = b.booking_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "ORDER BY i.invoice_id DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Invoice invoice = mapInvoiceFromResultSet(rs);
                invoices.add(invoice);
            }
        }
        
        return invoices;
    }

    @Override
    public Invoice getInvoiceDetails(int invoiceId) throws SQLException {
        Invoice invoice = null;
        String sql = "SELECT i.*, " +
                "b.check_in_date, b.check_out_date, " +
                "CONCAT(c.first_name, ' ', c.last_name) AS customer_name, " +
                "c.email, c.phone_number, c.address, " +
                "r.room_number, r.type AS room_type, r.price AS room_price " +
                "FROM invoices i " +
                "JOIN bookings b ON i.booking_id = b.booking_id " +
                "JOIN customers c ON b.customer_id = c.customer_id " +
                "JOIN Rooms r ON b.room_id = r.room_id " +
                "WHERE i.invoice_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, invoiceId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    invoice = mapInvoiceFromResultSet(rs);
                    // Additional fields for PDF generation
                    invoice.setCheckInDate(rs.getDate("check_in_date"));
                    invoice.setCheckOutDate(rs.getDate("check_out_date"));
                }
            }
        }
        
        return invoice;
    }

    @Override
    public List<BookingService> getBookingServicesForBooking(int bookingId) throws SQLException {
        List<BookingService> bookingServices = new ArrayList<>();
        String sql = "SELECT bs.*, s.service_name, s.price " +
                "FROM booking_services bs " +
                "JOIN services s ON bs.service_id = s.service_id " +
                "WHERE bs.booking_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingService bookingService = new BookingService();
                    bookingService.setBookingServiceId(rs.getInt("booking_service_id"));
                    bookingService.setBookingId(rs.getInt("booking_id"));
                    bookingService.setServiceId(rs.getInt("service_id"));
                    bookingService.setQuantity(rs.getInt("quantity"));
                    bookingService.setServiceDate(rs.getDate("service_date"));
                    
                    // Set service details
                    bookingService.setServiceName(rs.getString("service_name"));
                    bookingService.setServicePrice(rs.getBigDecimal("price"));
                    
                    bookingServices.add(bookingService);
                }
            }
        }
        
        return bookingServices;
    }
    
    /**
     * Helper method to map a ResultSet to an Invoice object
     */
    private Invoice mapInvoiceFromResultSet(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setInvoiceId(rs.getInt("invoice_id"));
        invoice.setBookingId(rs.getInt("booking_id"));
        invoice.setIssueDate(rs.getDate("issue_date"));
        invoice.setDueDate(rs.getDate("due_date"));
        invoice.setRoomCost(rs.getBigDecimal("room_cost"));
        invoice.setServiceCost(rs.getBigDecimal("service_cost"));
        invoice.setTotalAmount(rs.getBigDecimal("total_amount"));
        invoice.setPaidAmount(rs.getBigDecimal("paid_amount"));
        invoice.setInvoiceStatus(rs.getString("invoice_status"));
        invoice.setCreatedAt(rs.getTimestamp("created_at"));
        invoice.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // Set additional properties for display and PDF generation
        invoice.setCustomerName(rs.getString("customer_name"));
        invoice.setRoomNumber(rs.getString("room_number"));
        
        return invoice;
    }
}
