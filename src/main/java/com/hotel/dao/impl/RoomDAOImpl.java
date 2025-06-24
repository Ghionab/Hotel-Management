package com.hotel.dao.impl;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAOImpl implements RoomDAO {

    @Override
    public Optional<Room> findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, room_number, type, price, status, floor FROM Rooms WHERE room_id = ?";
        Optional<Room> room = Optional.empty();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Room foundRoom = mapRowToRoom(rs);
                room = Optional.of(foundRoom);
            }
        }
        return room;
    }

    @Override
    public List<Room> findAll() throws SQLException {
        String sql = "SELECT room_id, room_number, type, price, status, floor FROM Rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        }
        return rooms;
    }
    


    @Override
    public boolean updateRoomStatus(String roomNumber, String newStatus) throws SQLException {
        String sql = "UPDATE Rooms SET status = ? WHERE room_number = ?";
        int affectedRows = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, roomNumber);

            affectedRows = pstmt.executeUpdate();
        }
        return affectedRows > 0;
    }

    @Override
    public boolean addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO Rooms (room_number, type, price, status, floor, description) VALUES (?, ?, ?, ?, ?, ?)";
        int affectedRows = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getType());
            pstmt.setBigDecimal(3, room.getPrice());
            pstmt.setString(4, room.getStatus());
            pstmt.setInt(5, room.getFloor());
            pstmt.setString(6, room.getDescription());

            affectedRows = pstmt.executeUpdate();
        }
        return affectedRows > 0;
    }

    @Override
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE Rooms SET type = ?, price = ?, status = ?, floor = ?, description = ? WHERE room_id = ?";
        int affectedRows = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getType());
            pstmt.setBigDecimal(2, room.getPrice());
            pstmt.setString(3, room.getStatus());
            pstmt.setInt(4, room.getFloor());
            pstmt.setString(5, room.getDescription());
            pstmt.setInt(6, room.getRoomId());

            affectedRows = pstmt.executeUpdate();
        }
        return affectedRows > 0;
    }

    @Override
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM Rooms WHERE room_id = ?";
        int affectedRows = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);

            affectedRows = pstmt.executeUpdate();
        }
        return affectedRows > 0;
    }

    @Override
    public List<Room> findAvailableRooms() throws SQLException {
        String sql = "SELECT room_id, room_number, type, price, status, floor FROM Rooms WHERE status = ?";
        List<Room> rooms = new ArrayList<>();
        String availableStatus = "Available";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, availableStatus);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        }
        return rooms;
    }

    @Override
    public int getTotalRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Rooms";
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
    public int getAvailableRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Rooms WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "Available");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getOccupiedRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Rooms WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "Booked");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getCheckedInGuestsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings b " +
                "JOIN rooms r ON b.room_number = r.room_number " +
                "WHERE b.status = 'CHECKED_IN'";

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
    public int getCheckOutsDueToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings b " +
                "JOIN rooms r ON b.room_number = r.room_number " +
                "WHERE b.status = 'CHECKED_IN' " +
                "AND DATE(b.check_out_date) = CURRENT_DATE";

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
        String sql = "SELECT COUNT(*) FROM bookings b " +
                "JOIN rooms r ON b.room_number = r.room_number " +
                "WHERE b.status = 'CONFIRMED' " +
                "AND DATE(b.check_in_date) = CURRENT_DATE";

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
    public int getReservationsToday() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings b " +
                "WHERE DATE(b.created_at) = CURRENT_DATE";

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
        String sql = "SELECT COUNT(*) FROM bookings b " +
                "WHERE DATE(b.created_at) = CURRENT_DATE " +
                "AND b.status = 'CONFIRMED'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setType(rs.getString("type"));
        room.setPrice(rs.getBigDecimal("price"));
        room.setStatus(rs.getString("status"));
        room.setFloor(rs.getInt("floor"));
            return room;
    }
} 