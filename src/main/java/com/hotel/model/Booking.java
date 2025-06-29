package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Booking {
    private int bookingId;
    private int roomId;
    private int customerId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;
    private LocalDateTime createdAt;
    private int numberOfAdults;
    private int numberOfKids;
    
    // Additional properties for display purposes
    private String customerName;
    private String roomNumber;

    // Constructors
    public Booking() {}

    public Booking(int bookingId, int roomId, int customerId, LocalDate checkInDate, 
                  LocalDate checkOutDate, String bookingStatus, LocalDateTime createdAt,
                  int numberOfAdults, int numberOfKids) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.customerId = customerId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.createdAt = createdAt;
        this.numberOfAdults = numberOfAdults;
        this.numberOfKids = numberOfKids;
    }

    // Getters and Setters
    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public int getNumberOfKids() {
        return numberOfKids;
    }

    public void setNumberOfKids(int numberOfKids) {
        this.numberOfKids = numberOfKids;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", roomId=" + roomId +
                ", customerId=" + customerId +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", bookingStatus='" + bookingStatus + '\'' +
                ", createdAt=" + createdAt +
                ", numberOfAdults=" + numberOfAdults +
                ", numberOfKids=" + numberOfKids +
                ", customerName='" + customerName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                '}';
    }
}