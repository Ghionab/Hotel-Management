package com.hotel.dao;

import com.hotel.model.User;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    /**
     * Finds a user by username.
     * Used for login verification.
     *
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    Optional<User> findByUsername(String username) throws SQLException;
    
    /**
     * Retrieves a user with their staff details by user ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return An Optional containing the User with staff details if found, otherwise empty.
     * @throws SQLException if a database access error occurs.
     */
    Optional<User> getUserWithStaffDetails(int userId) throws SQLException;

    // User management methods
    boolean addUser(User user) throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(int userId) throws SQLException;
}