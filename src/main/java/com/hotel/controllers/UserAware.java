package com.hotel.controllers;

import com.hotel.model.User;

/**
 * Interface for controllers that need access to the current user.
 * Implement this interface in any controller that needs to perform
 * role-based access control.
 */
public interface UserAware {
    void setUser(User user);
}
