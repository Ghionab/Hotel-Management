package com.hotel.dao.impl;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        // TODO: Implement actual database query logic
        System.out.println("UserDAOImpl: findByUsername called (placeholder)");
        String sql = "SELECT user_id, username, password, role FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password")); // Remember: Hash in real app
                user.setRole(rs.getString("role"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
             System.err.println("SQL Error in findByUsername: " + e.getMessage());
             throw e; // Re-throw exception after logging
        }
        return Optional.empty(); // Return empty if not found or error occurred before return
    }

    // TODO: Implement other UserDAO methods if added to the interface (addUser, updateUser, deleteUser)
    @Override
    public boolean addUser(User user) throws SQLException {
        // Ensure password hashing is implemented in a real application before storing
        String sql = "INSERT INTO Users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Store hashed password in production!
            pstmt.setString(3, user.getRole());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in addUser: " + e.getMessage());
            // Consider more specific error handling (e.g., duplicate username)
            throw e;
        }
    }

    @Override
    public Optional<User> getUserWithStaffDetails(int userId) throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.password, u.role, " +
                    "s.first_name, s.last_name, s.phone_number, s.email, s.position " +
                    "FROM Users u " +
                    "LEFT JOIN Staff s ON u.user_id = s.user_id " +
                    "WHERE u.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone_number"),
                    rs.getString("email"),
                    rs.getString("position")
                );
                return Optional.of(user);
            }
            return Optional.empty();

        } catch (SQLException e) {
            System.err.println("SQL Error in getUserWithStaffDetails: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        System.out.println("Updating user with ID: " + user.getUserId());
        System.out.println("New first name: " + user.getFirstName());
        System.out.println("New last name: " + user.getLastName());
        System.out.println("New email: " + user.getEmail());
        System.out.println("New phone: " + user.getPhoneNumber());
        System.out.println("Password changed: " + (!user.getPassword().isEmpty() ? "Yes" : "No"));

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // First verify the user exists
            String checkUserSql = "SELECT COUNT(*) FROM Users WHERE user_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setInt(1, user.getUserId());
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || rs.getInt(1) == 0) {
                    throw new SQLException("User not found with ID: " + user.getUserId());
                }
            }

            // Update Users table (only password if provided)
            if (!user.getPassword().isEmpty()) {
                String userSql = "UPDATE Users SET password = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(userSql)) {
                    pstmt.setString(1, user.getPassword());
                    pstmt.setInt(2, user.getUserId());
                    int updated = pstmt.executeUpdate();
                    System.out.println("Updated password rows: " + updated);
                    if (updated == 0) {
                        throw new SQLException("Failed to update password for user ID: " + user.getUserId());
                    }
                }
            }

            // Check if staff record exists
            String checkSql = "SELECT COUNT(*) FROM Staff WHERE user_id = ?";
            boolean staffExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, user.getUserId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    staffExists = rs.getInt(1) > 0;
                }
            }

            // Update or insert staff record
            if (staffExists) {
                String staffSql = "UPDATE Staff SET first_name = ?, last_name = ?, email = ?, phone_number = ? WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(staffSql)) {
                    pstmt.setString(1, user.getFirstName());
                    pstmt.setString(2, user.getLastName());
                    pstmt.setString(3, user.getEmail());
                    pstmt.setString(4, user.getPhoneNumber());
                    pstmt.setInt(5, user.getUserId());
                    int updated = pstmt.executeUpdate();
                    System.out.println("Updated staff rows: " + updated);
                    if (updated == 0) {
                        throw new SQLException("Failed to update staff record for user ID: " + user.getUserId());
                    }
                }
            } else {
                String insertSql = "INSERT INTO Staff (user_id, first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, user.getUserId());
                    pstmt.setString(2, user.getFirstName());
                    pstmt.setString(3, user.getLastName());
                    pstmt.setString(4, user.getEmail());
                    pstmt.setString(5, user.getPhoneNumber());
                    int inserted = pstmt.executeUpdate();
                    System.out.println("Inserted staff rows: " + inserted);
                    if (inserted == 0) {
                        throw new SQLException("Failed to create staff record for user ID: " + user.getUserId());
                    }
                }
            }

            // If we got here, all operations succeeded
            conn.commit();
            System.out.println("User update successful");
            return true;

        } catch (SQLException e) {
            String errorMessage = "Error updating user profile: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    System.out.println("Rolling back transaction...");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            
            throw new SQLException(errorMessage);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                    System.out.println("Database connection closed");
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error in deleteUser: " + e.getMessage());
            // Handle potential foreign key constraints if users are linked elsewhere
            throw e;
        }
    }
}