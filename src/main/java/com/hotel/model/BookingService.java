package com.hotel.model;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class BookingService {
    private final IntegerProperty bookingServiceId;
    private final IntegerProperty bookingId;
    private final IntegerProperty serviceId;
    private final IntegerProperty quantity;
    private final ObjectProperty<Date> serviceDate;
    private final ObjectProperty<Timestamp> createdAt;
    
    // Related data from other tables
    private final StringProperty customerName;
    private final StringProperty roomNumber;
    private final StringProperty serviceName;
    private final ObjectProperty<BigDecimal> servicePrice;

    public BookingService() {
        this.bookingServiceId = new SimpleIntegerProperty();
        this.bookingId = new SimpleIntegerProperty();
        this.serviceId = new SimpleIntegerProperty();
        this.quantity = new SimpleIntegerProperty(1); // Default to 1
        this.serviceDate = new SimpleObjectProperty<>();
        this.createdAt = new SimpleObjectProperty<>();
        
        // Related data
        this.customerName = new SimpleStringProperty();
        this.roomNumber = new SimpleStringProperty();
        this.serviceName = new SimpleStringProperty();
        this.servicePrice = new SimpleObjectProperty<>();
    }

    // Getters and setters for direct properties
    public int getBookingServiceId() {
        return bookingServiceId.get();
    }

    public IntegerProperty bookingServiceIdProperty() {
        return bookingServiceId;
    }

    public void setBookingServiceId(int bookingServiceId) {
        this.bookingServiceId.set(bookingServiceId);
    }

    public int getBookingId() {
        return bookingId.get();
    }

    public IntegerProperty bookingIdProperty() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId.set(bookingId);
    }

    public int getServiceId() {
        return serviceId.get();
    }

    public IntegerProperty serviceIdProperty() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId.set(serviceId);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public Date getServiceDate() {
        return serviceDate.get();
    }

    public ObjectProperty<Date> serviceDateProperty() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate.set(serviceDate);
    }

    public Timestamp getCreatedAt() {
        return createdAt.get();
    }

    public ObjectProperty<Timestamp> createdAtProperty() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt.set(createdAt);
    }

    // Getters and setters for related data
    public String getCustomerName() {
        return customerName.get();
    }

    public StringProperty customerNameProperty() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName.set(customerName);
    }

    public String getRoomNumber() {
        return roomNumber.get();
    }

    public StringProperty roomNumberProperty() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber.set(roomNumber);
    }

    public String getServiceName() {
        return serviceName.get();
    }

    public StringProperty serviceNameProperty() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName.set(serviceName);
    }

    public BigDecimal getServicePrice() {
        return servicePrice.get();
    }

    public ObjectProperty<BigDecimal> servicePriceProperty() {
        return servicePrice;
    }

    public void setServicePrice(BigDecimal servicePrice) {
        this.servicePrice.set(servicePrice);
    }
    
    // Helper method to calculate total price (quantity * service price)
    public BigDecimal getTotalPrice() {
        if (getServicePrice() == null || getQuantity() <= 0) {
            return BigDecimal.ZERO;
        }
        return getServicePrice().multiply(new BigDecimal(getQuantity()));
    }
    
    @Override
    public String toString() {
        return "BookingService{" +
                "bookingServiceId=" + getBookingServiceId() +
                ", bookingId=" + getBookingId() +
                ", serviceId=" + getServiceId() +
                ", quantity=" + getQuantity() +
                ", serviceDate=" + getServiceDate() +
                ", customerName=" + getCustomerName() +
                ", roomNumber=" + getRoomNumber() +
                ", serviceName=" + getServiceName() +
                ", servicePrice=" + getServicePrice() +
                '}';
    }
}
