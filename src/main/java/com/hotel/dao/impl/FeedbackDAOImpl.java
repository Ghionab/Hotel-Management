package com.hotel.dao.impl;

import com.hotel.dao.FeedbackDAO;
import com.hotel.models.Feedback;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAOImpl implements FeedbackDAO {

    @Override
    public void addFeedback(Feedback feedback) {
        String sql = "INSERT INTO feedback (customer_id, booking_id, rating, comments) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, feedback.getCustomerId());
            if (feedback.getBookingId() != null) {
                pstmt.setInt(2, feedback.getBookingId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setInt(3, feedback.getRating());
            pstmt.setString(4, feedback.getComments());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    feedback.setFeedbackId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding feedback", e);
        }
    }

    @Override
    public List<Feedback> getAllFeedback() {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, c.first_name, c.last_name FROM feedback f " +
                    "JOIN customers c ON f.customer_id = c.customer_id " +
                    "ORDER BY f.feedback_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                feedbackList.add(extractFeedbackFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving feedback", e);
        }
        return feedbackList;
    }

    @Override
    public Feedback getFeedbackById(int feedbackId) {
        String sql = "SELECT f.*, c.first_name, c.last_name FROM feedback f " +
                    "JOIN customers c ON f.customer_id = c.customer_id " +
                    "WHERE f.feedback_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, feedbackId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractFeedbackFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving feedback by ID", e);
        }
        return null;
    }

    @Override
    public List<Feedback> getFeedbackByCustomerId(int customerId) {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, c.first_name, c.last_name FROM feedback f " +
                    "JOIN customers c ON f.customer_id = c.customer_id " +
                    "WHERE f.customer_id = ? ORDER BY f.feedback_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbackList.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving feedback by customer ID", e);
        }
        return feedbackList;
    }

    @Override
    public List<Feedback> getFeedbackByBookingId(int bookingId) {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, c.first_name, c.last_name FROM feedback f " +
                    "JOIN customers c ON f.customer_id = c.customer_id " +
                    "WHERE f.booking_id = ? ORDER BY f.feedback_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    feedbackList.add(extractFeedbackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving feedback by booking ID", e);
        }
        return feedbackList;
    }

    @Override
    public double getAverageRating() {
        String sql = "SELECT AVG(rating) as avg_rating FROM feedback";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating average rating", e);
        }
        return 0.0;
    }

    private Feedback extractFeedbackFromResultSet(ResultSet rs) throws SQLException {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(rs.getInt("feedback_id"));
        feedback.setCustomerId(rs.getInt("customer_id"));
        feedback.setBookingId(rs.getObject("booking_id") != null ? rs.getInt("booking_id") : null);
        feedback.setRating(rs.getInt("rating"));
        feedback.setComments(rs.getString("comments"));
        feedback.setFeedbackDate(rs.getTimestamp("feedback_date"));
        feedback.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
        return feedback;
    }
}
