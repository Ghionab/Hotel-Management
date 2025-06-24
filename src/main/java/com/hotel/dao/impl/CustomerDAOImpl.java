package com.hotel.dao.impl;

import com.hotel.dao.CustomerDAO;
import com.hotel.model.Customer;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {

    public CustomerDAOImpl() {
        // Empty constructor
    }

    @Override
    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("address"),
                    rs.getInt("number_of_adults"),
                    rs.getInt("number_of_kids")
                ));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("address"),
                    rs.getInt("number_of_adults"),
                    rs.getInt("number_of_kids")
                ));
            }
        }
        return customers;
    }

    @Override
    public boolean addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (first_name, last_name, email, phone_number, address, number_of_adults, number_of_kids) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getAddress());
            stmt.setInt(6, customer.getNumberOfAdults());
            stmt.setInt(7, customer.getNumberOfKids());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, phone_number = ?, address = ?, number_of_adults = ?, number_of_kids = ? WHERE customer_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getAddress());
            stmt.setInt(6, customer.getNumberOfAdults());
            stmt.setInt(7, customer.getNumberOfKids());
            stmt.setInt(8, customer.getCustomerId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Customer> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM customers WHERE CONCAT(first_name, ' ', last_name) LIKE ?";
        List<Customer> customers = new ArrayList<>();
        try (var conn = DatabaseConnection.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            var rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("address"),
                    rs.getInt("number_of_adults"),
                    rs.getInt("number_of_kids")
                ));
            }
        }
        return customers;
    }
}