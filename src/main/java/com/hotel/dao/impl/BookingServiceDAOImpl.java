package com.hotel.dao.impl;

import com.hotel.dao.BookingServiceDAO;
import com.hotel.model.BookingService;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingServiceDAOImpl implements BookingServiceDAO {

    @Override
    public BookingService addBookingService(BookingService bookingService) throws SQLException {
        String sql = "INSERT INTO booking_services (booking_id, service_id, quantity, service_date) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, bookingService.getBookingId());
            stmt.setInt(2, bookingService.getServiceId());
            stmt.setInt(3, bookingService.getQuantity());
            stmt.setDate(4, bookingService.getServiceDate());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating booking service failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    bookingService.setBookingServiceId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating booking service failed, no ID obtained.");
                }
            }
            
            return bookingService;
        }
    }

    @Override
    public void updateBookingService(BookingService bookingService) throws SQLException {
        String sql = "UPDATE booking_services SET booking_id = ?, service_id = ?, quantity = ?, " +
                     "service_date = ? WHERE booking_service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingService.getBookingId());
            stmt.setInt(2, bookingService.getServiceId());
            stmt.setInt(3, bookingService.getQuantity());
            stmt.setDate(4, bookingService.getServiceDate());
            stmt.setInt(5, bookingService.getBookingServiceId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating booking service failed, no rows affected.");
            }
        }
    }

    @Override
    public void deleteBookingService(int bookingServiceId) throws SQLException {
        String sql = "DELETE FROM booking_services WHERE booking_service_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingServiceId);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Deleting booking service failed, no rows affected.");
            }
        }
    }

    @Override
    public List<BookingService> getAllBookingServices() throws SQLException {
        List<BookingService> bookingServices = new ArrayList<>();
        String sql = "SELECT * FROM booking_services";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                BookingService bookingService = mapRowToBookingService(rs);
                bookingServices.add(bookingService);
            }
        }
        
        return bookingServices;
    }

    @Override
    public List<BookingService> getBookingServicesByBookingId(int bookingId) throws SQLException {
        List<BookingService> bookingServices = new ArrayList<>();
        String sql = "SELECT * FROM booking_services WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingService bookingService = mapRowToBookingService(rs);
                    bookingServices.add(bookingService);
                }
            }
        }
        
        return bookingServices;
    }

    @Override
    public List<BookingService> getAllBookingServicesWithDetails() throws SQLException {
        List<BookingService> bookingServices = new ArrayList<>();
        String sql = "SELECT bs.*, " +
                     "b.booking_id, " +
                     "c.first_name, c.last_name, " +
                     "r.room_number, " +
                     "s.service_name, s.price " +
                     "FROM booking_services bs " +
                     "JOIN bookings b ON bs.booking_id = b.booking_id " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "JOIN services s ON bs.service_id = s.service_id " +
                     "ORDER BY bs.service_date DESC, c.last_name, c.first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                BookingService bookingService = mapRowToBookingServiceWithDetails(rs);
                bookingServices.add(bookingService);
            }
        }
        
        return bookingServices;
    }
    
    private BookingService mapRowToBookingService(ResultSet rs) throws SQLException {
        BookingService bookingService = new BookingService();
        bookingService.setBookingServiceId(rs.getInt("booking_service_id"));
        bookingService.setBookingId(rs.getInt("booking_id"));
        bookingService.setServiceId(rs.getInt("service_id"));
        bookingService.setQuantity(rs.getInt("quantity"));
        bookingService.setServiceDate(rs.getDate("service_date"));
        bookingService.setCreatedAt(rs.getTimestamp("created_at"));
        return bookingService;
    }
    
    private BookingService mapRowToBookingServiceWithDetails(ResultSet rs) throws SQLException {
        BookingService bookingService = mapRowToBookingService(rs);
        
        // Set related data from joined tables
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        bookingService.setCustomerName(firstName + " " + lastName);
        
        bookingService.setRoomNumber(rs.getString("room_number"));
        bookingService.setServiceName(rs.getString("service_name"));
        bookingService.setServicePrice(rs.getBigDecimal("price"));
        
        return bookingService;
    }
}
