package com.hotel.dao;

import com.hotel.model.BookingService;
import java.sql.SQLException;
import java.util.List;

public interface BookingServiceDAO {
    /**
     * Adds a new booking service to the database
     * @param bookingService The booking service to add
     * @return The added booking service with generated ID
     * @throws SQLException If a database error occurs
     */
    BookingService addBookingService(BookingService bookingService) throws SQLException;
    
    /**
     * Updates an existing booking service in the database
     * @param bookingService The booking service to update
     * @throws SQLException If a database error occurs
     */
    void updateBookingService(BookingService bookingService) throws SQLException;
    
    /**
     * Deletes a booking service from the database
     * @param bookingServiceId The ID of the booking service to delete
     * @throws SQLException If a database error occurs
     */
    void deleteBookingService(int bookingServiceId) throws SQLException;
    
    /**
     * Retrieves all booking services from the database
     * @return A list of all booking services
     * @throws SQLException If a database error occurs
     */
    List<BookingService> getAllBookingServices() throws SQLException;
    
    /**
     * Retrieves booking services for a specific booking
     * @param bookingId The ID of the booking
     * @return A list of booking services for the specified booking
     * @throws SQLException If a database error occurs
     */
    List<BookingService> getBookingServicesByBookingId(int bookingId) throws SQLException;
    
    /**
     * Retrieves all booking services with details from related tables
     * @return A list of booking services with customer, room, and service details
     * @throws SQLException If a database error occurs
     */
    List<BookingService> getAllBookingServicesWithDetails() throws SQLException;
}
