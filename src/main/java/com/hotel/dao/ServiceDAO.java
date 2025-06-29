package com.hotel.dao;

import com.hotel.model.Service;
import java.sql.SQLException;
import java.util.List;

public interface ServiceDAO {
    List<Service> getAllServices() throws SQLException;
    Service addService(Service service) throws SQLException;
    Service updateService(Service service) throws SQLException;
    void deleteService(int serviceId) throws SQLException;
    Service getServiceById(int serviceId) throws SQLException;
}
