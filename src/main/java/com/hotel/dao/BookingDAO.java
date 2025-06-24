package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingDAO {
    // Basic CRUD operations
    Optional<Booking> findById(int bookingId) throws SQLException;
    List<Booking> findAll() throws SQLException;
    boolean addBooking(Booking booking) throws SQLException;
    boolean updateBooking(Booking booking) throws SQLException;
    boolean deleteBooking(int bookingId) throws SQLException;
    
    // Method for BookingServices tab
    List<Booking> getAllBookings() throws SQLException;
    
    // Booking-specific queries
    List<Booking> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException;
    List<Booking> findByCustomerId(int customerId) throws SQLException;
    List<Booking> findByRoomId(int roomId) throws SQLException;
    
    // UI support methods
    List<Room> getAllAvailableRooms(LocalDate checkIn, LocalDate checkOut) throws SQLException;
    List<Customer> getAllCustomers() throws SQLException;
    List<String> getAllBookingStatuses() throws SQLException;
    
    // Dashboard methods
    int getCheckedInGuestsCount() throws SQLException;
    int getExpectedCheckInsToday() throws SQLException;
    int getExpectedCheckOutsToday() throws SQLException;
    int getNewBookingsToday() throws SQLException;
    double getRevenueToday() throws SQLException;
}