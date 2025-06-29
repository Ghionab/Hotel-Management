package com.hotel.dao;

import com.hotel.models.DashboardSummary;

public interface DashboardDAO {
    DashboardSummary getDashboardSummary();
    void refreshDashboardData(); // Optional method to force refresh the data
}
