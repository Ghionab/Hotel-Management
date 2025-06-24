package com.hotel.dao;

import com.hotel.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CustomerDAO {
    Optional<Customer> findById(int customerId) throws SQLException;
    List<Customer> findAll() throws SQLException;
    boolean addCustomer(Customer customer) throws SQLException;
    boolean updateCustomer(Customer customer) throws SQLException;
    boolean deleteCustomer(int customerId) throws SQLException;
    List<Customer> findByName(String name) throws SQLException; // Example specific query
} 