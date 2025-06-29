package com.hotel.dao;

import com.hotel.model.Invoice;
import com.hotel.model.BookingService;
import java.sql.SQLException;
import java.util.List;

public interface InvoiceDAO {
    /**
     * Retrieves all invoices with customer and room details
     * @return List of Invoice objects with populated customer and room information
     * @throws SQLException if a database access error occurs
     */
    List<Invoice> getAllInvoices() throws SQLException;
    
    /**
     * Retrieves a specific invoice by ID with all details
     * @param invoiceId the ID of the invoice to retrieve
     * @return Invoice object with all details including customer and room information
     * @throws SQLException if a database access error occurs
     */
    Invoice getInvoiceDetails(int invoiceId) throws SQLException;
    
    /**
     * Retrieves all booking services for a specific booking
     * @param bookingId the ID of the booking
     * @return List of BookingService objects with service details
     * @throws SQLException if a database access error occurs
     */
    List<BookingService> getBookingServicesForBooking(int bookingId) throws SQLException;
}
