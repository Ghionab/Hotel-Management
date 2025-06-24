package com.hotel.dao.impl;

import com.hotel.dao.ServiceDAO;
import com.hotel.model.Service;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAOImpl implements ServiceDAO {
    @Override
    public List<Service> getAllServices() throws SQLException {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT service_id, service_name, price, description, created_at, updated_at FROM services ORDER BY service_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Service service = new Service(
                    rs.getInt("service_id"),
                    rs.getString("service_name"),
                    rs.getBigDecimal("price"),
                    rs.getString("description"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                );
                services.add(service);
            }
        }
        return services;
    }

    @Override
    public Service addService(Service service) throws SQLException {
        String sql = "INSERT INTO services (service_name, price, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, service.getServiceName());
            pstmt.setBigDecimal(2, service.getPrice());
            pstmt.setString(3, service.getDescription());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating service failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    service.setServiceId(generatedKeys.getInt(1));
                    return service;
                } else {
                    throw new SQLException("Creating service failed, no ID obtained.");
                }
            }
        }
    }

    @Override
    public Service updateService(Service service) throws SQLException {
        String sql = "UPDATE services SET service_name = ?, price = ?, description = ? WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, service.getServiceName());
            pstmt.setBigDecimal(2, service.getPrice());
            pstmt.setString(3, service.getDescription());
            pstmt.setInt(4, service.getServiceId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating service failed, no rows affected.");
            }
            return service;
        }
    }

    @Override
    public void deleteService(int serviceId) throws SQLException {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting service failed, no rows affected.");
            }
        }
    }

    @Override
    public Service getServiceById(int serviceId) throws SQLException {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Service(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getBigDecimal("price"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                    );
                }
                return null;
            }
        }
    }
}
