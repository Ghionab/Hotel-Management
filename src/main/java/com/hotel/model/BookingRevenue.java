package com.hotel.model;

import java.time.LocalDate;
import java.math.BigDecimal;

public class BookingRevenue {
    private int bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String bookingStatus;
    private int invoiceId;
    private BigDecimal roomCost;
    private BigDecimal serviceCost;
    private BigDecimal totalAmount;
    private String invoiceStatus;

    // Default constructor
    public BookingRevenue() {}

    // Full constructor
    public BookingRevenue(int bookingId, LocalDate checkInDate, LocalDate checkOutDate, 
                         String bookingStatus, int invoiceId, BigDecimal roomCost,
                         BigDecimal serviceCost, BigDecimal totalAmount, String invoiceStatus) {
        this.bookingId = bookingId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.invoiceId = invoiceId;
        this.roomCost = roomCost;
        this.serviceCost = serviceCost;
        this.totalAmount = totalAmount;
        this.invoiceStatus = invoiceStatus;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getRoomCost() { return roomCost; }
    public void setRoomCost(BigDecimal roomCost) { this.roomCost = roomCost; }

    public BigDecimal getServiceCost() { return serviceCost; }
    public void setServiceCost(BigDecimal serviceCost) { this.serviceCost = serviceCost; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceStatus() { return invoiceStatus; }
    public void setInvoiceStatus(String invoiceStatus) { this.invoiceStatus = invoiceStatus; }

    @Override
    public String toString() {
        return String.format("Booking #%d - Total: $%.2f, Status: %s", 
                           bookingId, totalAmount, bookingStatus);
    }
}
