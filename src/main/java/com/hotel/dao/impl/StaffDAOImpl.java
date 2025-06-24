package com.hotel.dao.impl;

import com.hotel.dao.StaffDAO;
import com.hotel.model.Staff;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffDAOImpl implements StaffDAO {

    @Override
    public List<Staff> findAll() throws SQLException {
        String sql = "SELECT * FROM staff ORDER BY last_name, first_name";
        List<Staff> staffList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                staffList.add(mapRowToStaff(rs));
            }
        }
        return staffList;
    }

    @Override
    public Optional<Staff> findById(int userId) throws SQLException {
        String sql = "SELECT * FROM staff WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToStaff(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addStaff(Staff staff) throws SQLException {
        String sql = "INSERT INTO staff (user_id, first_name, last_name, phone_number, " +
                    "email, position, hire_date, salary, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStaffParameters(pstmt, staff);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStaff(Staff staff) throws SQLException {
        String sql = "UPDATE staff SET first_name = ?, last_name = ?, phone_number = ?, " +
                    "email = ?, position = ?, hire_date = ?, salary = ?, address = ? " +
                    "WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters in correct order matching the SQL query
            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getPhoneNumber());
            pstmt.setString(4, staff.getEmail());
            pstmt.setString(5, staff.getPosition());
            pstmt.setDate(6, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : null);
            pstmt.setDouble(7, staff.getSalary());
            pstmt.setString(8, staff.getAddress());
            pstmt.setInt(9, staff.getUserId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteStaff(int userId) throws SQLException {
        String sql = "DELETE FROM staff WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Staff> findByPosition(String position) throws SQLException {
        String sql = "SELECT * FROM staff WHERE position = ? ORDER BY last_name, first_name";
        List<Staff> staffList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, position);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                staffList.add(mapRowToStaff(rs));
            }
        }
        return staffList;
    }

    private Staff mapRowToStaff(ResultSet rs) throws SQLException {
        Staff staff = new Staff();
        staff.setUserId(rs.getInt("user_id"));
        staff.setFirstName(rs.getString("first_name"));
        staff.setLastName(rs.getString("last_name"));
        staff.setPhoneNumber(rs.getString("phone_number"));
        staff.setEmail(rs.getString("email"));
        staff.setPosition(rs.getString("position"));
        staff.setHireDate(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toLocalDate() : null);
        staff.setSalary(rs.getDouble("salary"));
        staff.setAddress(rs.getString("address"));
        staff.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        staff.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return staff;
    }

    private void setStaffParameters(PreparedStatement pstmt, Staff staff) throws SQLException {
        pstmt.setInt(1, staff.getUserId());
        pstmt.setString(2, staff.getFirstName());
        pstmt.setString(3, staff.getLastName());
        pstmt.setString(4, staff.getPhoneNumber());
        pstmt.setString(5, staff.getEmail());
        pstmt.setString(6, staff.getPosition());
        pstmt.setDate(7, staff.getHireDate() != null ? Date.valueOf(staff.getHireDate()) : null);
        pstmt.setDouble(8, staff.getSalary());
        pstmt.setString(9, staff.getAddress());
    }
}
