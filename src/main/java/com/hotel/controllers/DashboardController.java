package com.hotel.controllers;

import com.hotel.dao.DashboardDAO;
import com.hotel.dao.impl.DashboardDAOImpl;
import com.hotel.models.DashboardSummary;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    // Room Status Labels
    @FXML private Label availableRoomsLabel;
    @FXML private Label cleaningRoomsLabel;
    @FXML private Label maintenanceRoomsLabel;
    @FXML private Label outOfServiceRoomsLabel;
    @FXML private Label bookedRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label totalRoomsLabel;
    
    // Staff and Bookings Labels
    @FXML private Label totalStaffLabel;
    @FXML private Label todaysBookingsLabel;
    @FXML private Label todaysCheckInsLabel;
    @FXML private Label todaysCheckOutsLabel;
    
    // Feedback and Revenue Labels
    @FXML private Label avgFeedbackRatingLabel;
    @FXML private Label todaysRevenueLabel;
    @FXML private Label revenueLast30DaysLabel;
    
    // Progress Bars for visual representation
    @FXML private ProgressBar roomOccupancyProgress;
    @FXML private ProgressBar feedbackRatingProgress;
    
    // Last Updated Label
    @FXML private Label lastUpdatedLabel;

    private final DashboardDAO dashboardDAO;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat RATING_FORMAT = new DecimalFormat("#.#");

    public DashboardController() {
        this.dashboardDAO = new DashboardDAOImpl();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshDashboard();
    }

    @FXML
    private void handleRefreshDashboard() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            DashboardSummary summary = dashboardDAO.getDashboardSummary();
            updateDashboardUI(summary);
            lastUpdatedLabel.setText("Last Updated: " + LocalDateTime.now().format(TIME_FORMATTER));
        } catch (Exception e) {
            showError("Dashboard Error", "Failed to refresh dashboard data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDashboardUI(DashboardSummary summary) {
        // Update room status
        availableRoomsLabel.setText(String.valueOf(summary.getAvailableRooms()));
        cleaningRoomsLabel.setText(String.valueOf(summary.getCleaningRooms()));
        maintenanceRoomsLabel.setText(String.valueOf(summary.getMaintenanceRooms()));
        outOfServiceRoomsLabel.setText(String.valueOf(summary.getOutOfServiceRooms()));
        bookedRoomsLabel.setText(String.valueOf(summary.getBookedRooms()));
        occupiedRoomsLabel.setText(String.valueOf(summary.getOccupiedRooms()));
        totalRoomsLabel.setText(String.valueOf(summary.getTotalRooms()));
        
        // Update staff and bookings
        totalStaffLabel.setText(String.valueOf(summary.getTotalStaff()));
        todaysBookingsLabel.setText(String.valueOf(summary.getTodaysBookings()));
        todaysCheckInsLabel.setText(String.valueOf(summary.getTodaysCheckIns()));
        todaysCheckOutsLabel.setText(String.valueOf(summary.getTodaysCheckOuts()));
        
        // Update feedback and revenue
        avgFeedbackRatingLabel.setText(RATING_FORMAT.format(summary.getAvgFeedbackRating30Days()) + " / 5.0");
        todaysRevenueLabel.setText(CURRENCY_FORMAT.format(summary.getTodaysRevenue()));
        revenueLast30DaysLabel.setText(CURRENCY_FORMAT.format(summary.getRevenueLast30Days()));
        
        // Update progress bars
        double occupancyRate = summary.getTotalRooms() > 0 ? 
            (double) (summary.getOccupiedRooms() + summary.getBookedRooms()) / summary.getTotalRooms() : 0;
        roomOccupancyProgress.setProgress(occupancyRate);
        
        double ratingProgress = summary.getAvgFeedbackRating30Days() / 5.0;
        feedbackRatingProgress.setProgress(ratingProgress);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}