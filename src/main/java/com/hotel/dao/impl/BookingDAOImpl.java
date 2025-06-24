package com.hotel.dao.impl;

import com.hotel.dao.BookingDAO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BookingDAOImpl implements BookingDAO {

    @Override
    public Optional<Booking> findById(int bookingId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                return Optional.of(booking);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY check_in_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                bookings.add(booking);
            }
        }
        return bookings;
    }

    @Override
    public boolean addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (room_id, customer_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, booking.getRoomId());
            stmt.setInt(2, booking.getCustomerId());
            stmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            stmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    booking.setBookingId(generatedKeys.getInt(1));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET room_id = ?, customer_id = ?, check_in_date = ?, check_out_date = ? WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, booking.getRoomId());
            stmt.setInt(2, booking.getCustomerId());
            stmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            stmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            stmt.setInt(5, booking.getBookingId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteBooking(int bookingId) throws SQLException {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Booking> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        // Find all bookings that overlap with the specified date range
        String sql = "SELECT * FROM bookings WHERE " +
                   "(check_in_date <= ? AND check_out_date >= ?) OR " +  // Booking spans the entire range
                   "(check_in_date >= ? AND check_in_date <= ?) OR " +   // Booking starts within range
                   "(check_out_date >= ? AND check_out_date <= ?)";      // Booking ends within range
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set parameters for the query
            stmt.setDate(1, Date.valueOf(endDate));
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(startDate));
            stmt.setDate(4, Date.valueOf(endDate));
            stmt.setDate(5, Date.valueOf(startDate));
            stmt.setDate(6, Date.valueOf(endDate));
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                booking.setBookingStatus(rs.getString("booking_status"));
                bookings.add(booking);
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> findByCustomerId(int customerId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY check_in_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                bookings.add(booking);
            }
        }
        return bookings;
    }

    @Override
    public List<Booking> findByRoomId(int roomId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE room_id = ? ORDER BY check_in_date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                booking.setBookingStatus(rs.getString("booking_status"));
                booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                booking.setNumberOfAdults(rs.getInt("number_of_adults"));
                booking.setNumberOfKids(rs.getInt("number_of_kids"));
                bookings.add(booking);
            }
        }
        return bookings;
    }

    @Override
    public List<Room> getAllAvailableRooms(LocalDate checkIn, LocalDate checkOut) throws SQLException {
        List<Room> availableRooms = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.type, r.price, r.status, r.floor FROM rooms r WHERE r.status = 'Available' " +
                "AND r.room_id NOT IN (" +
                "SELECT b.room_id FROM bookings b " +
                "WHERE (? BETWEEN b.check_in_date AND b.check_out_date " +
                "OR ? BETWEEN b.check_in_date AND b.check_out_date " +
                "OR (b.check_in_date BETWEEN ? AND ? )) " +
                "AND b.booking_status NOT IN ('Cancelled', 'Checked-out'))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(checkIn));
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setDate(3, Date.valueOf(checkIn));
            stmt.setDate(4, Date.valueOf(checkOut));
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Room room = new Room();
                room.setRoomId(rs.getInt("room_id"));
                room.setRoomNumber(rs.getString("room_number"));
                room.setType(rs.getString("type"));
                room.setPrice(rs.getBigDecimal("price"));
                room.setStatus(rs.getString("status"));
                room.setFloor(rs.getInt("floor"));
                    availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    @Override
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, c.first_name, c.last_name, r.room_number " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "JOIN rooms r ON b.room_id = r.room_id " +
                     "ORDER BY b.check_in_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setBookingId(rs.getInt("booking_id"));
                booking.setRoomId(rs.getInt("room_id"));
                booking.setCustomerId(rs.getInt("customer_id"));
                booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
                booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
                booking.setBookingStatus(rs.getString("booking_status"));
                
                if (rs.getTimestamp("created_at") != null) {
                    booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                
                booking.setNumberOfAdults(rs.getInt("number_of_adults"));
                booking.setNumberOfKids(rs.getInt("number_of_kids"));
                
                // Set the additional properties for display
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                booking.setCustomerName(firstName + " " + lastName);
                booking.setRoomNumber(rs.getString("room_number"));
                
                bookings.add(booking);
            }
        }
        return bookings;
    }

    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Customer customer = new Customer();
                customer.setCustomerId(rs.getInt("customer_id"));
                customer.setFirstName(rs.getString("first_name"));
                customer.setLastName(rs.getString("last_name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhoneNumber(rs.getString("phone_number"));
                customer.setAddress(rs.getString("address"));
                customers.add(customer);
            }
        }
        return customers;
    }

    @Override
    public List<String> getAllBookingStatuses() throws SQLException {
        return Arrays.asList("Confirmed", "Checked-in", "Checked-out", "Cancelled");
    }

    @Override
    public int getCheckedInGuestsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE booking_status = 'Checked-in'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getExpectedCheckInsToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_in_date = CURRENT_DATE AND booking_status = 'Confirmed'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getExpectedCheckOutsToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE check_out_date = CURRENT_DATE AND booking_status = 'Checked-in'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getNewBookingsToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public double getRevenueToday() throws SQLException {
        // Assuming there's a payment or invoice table linked to bookings
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE DATE(payment_date) = CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
}