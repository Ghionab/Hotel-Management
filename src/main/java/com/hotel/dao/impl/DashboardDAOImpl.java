package com.hotel.dao.impl;

import com.hotel.dao.DashboardDAO;
import com.hotel.models.DashboardSummary;
import com.hotel.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAOImpl implements DashboardDAO {
    
    @Override
    public DashboardSummary getDashboardSummary() {
        String sql = "SELECT * FROM dashboard_summary";
        DashboardSummary summary = new DashboardSummary();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                summary.setAvailableRooms(rs.getInt("available_rooms"));
                summary.setCleaningRooms(rs.getInt("cleaning_rooms"));
                summary.setMaintenanceRooms(rs.getInt("maintenance_rooms"));
                summary.setOutOfServiceRooms(rs.getInt("out_of_service_rooms"));
                summary.setBookedRooms(rs.getInt("booked_rooms"));
                summary.setOccupiedRooms(rs.getInt("occupied_rooms"));
                summary.setTotalRooms(rs.getInt("total_rooms"));
                summary.setTotalStaff(rs.getInt("total_staff"));
                summary.setTodaysBookings(rs.getInt("todays_bookings"));
                summary.setTodaysCheckIns(rs.getInt("todays_check_ins"));
                summary.setTodaysCheckOuts(rs.getInt("todays_check_outs"));
                summary.setAvgFeedbackRating30Days(rs.getDouble("avg_feedback_rating_30_days"));
                summary.setTodaysRevenue(rs.getBigDecimal("todays_revenue"));
                summary.setRevenueLast30Days(rs.getBigDecimal("revenue_last_30_days"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving dashboard summary", e);
        }
        return summary;
    }

    @Override
    public void refreshDashboardData() {
        // The view automatically refreshes when queried
        // This method is included for consistency and potential future use
    }
}
