package com.hotel.dao;

import com.hotel.model.Staff;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface StaffDAO {
    List<Staff> findAll() throws SQLException;
    Optional<Staff> findById(int userId) throws SQLException;
    boolean addStaff(Staff staff) throws SQLException;
    boolean updateStaff(Staff staff) throws SQLException;
    boolean deleteStaff(int userId) throws SQLException;
    List<Staff> findByPosition(String position) throws SQLException;
}
