package com.hotel.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Invoice {
    private int invoiceId;
    private int bookingId;
    private Date issueDate;
    private Date dueDate;
    private BigDecimal roomCost;
    private BigDecimal serviceCost;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String invoiceStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Additional properties for display and PDF generation
    private String customerName;
    private String roomNumber;
    private Date checkInDate;
    private Date checkOutDate;
    
    // Constructors
    public Invoice() {
        this.roomCost = BigDecimal.ZERO;
        this.serviceCost = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.invoiceStatus = "Pending";
    }
    
    public Invoice(int invoiceId, int bookingId, Date issueDate, Date dueDate, 
                  BigDecimal roomCost, BigDecimal serviceCost, BigDecimal totalAmount, 
                  BigDecimal paidAmount, String invoiceStatus) {
        this.invoiceId = invoiceId;
        this.bookingId = bookingId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.roomCost = roomCost;
        this.serviceCost = serviceCost;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.invoiceStatus = invoiceStatus;
    }
    
    // Getters and Setters
    public int getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public int getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }
    
    public Date getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public BigDecimal getRoomCost() {
        return roomCost;
    }
    
    public void setRoomCost(BigDecimal roomCost) {
        this.roomCost = roomCost;
    }
    
    public BigDecimal getServiceCost() {
        return serviceCost;
    }
    
    public void setServiceCost(BigDecimal serviceCost) {
        this.serviceCost = serviceCost;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getPaidAmount() {
        return paidAmount;
    }
    
    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }
    
    public String getInvoiceStatus() {
        return invoiceStatus;
    }
    
    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Additional getters and setters for display and PDF generation
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
    
    public Date getCheckInDate() {
        return checkInDate;
    }
    
    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }
    
    public Date getCheckOutDate() {
        return checkOutDate;
    }
    
    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    
    // Utility method to calculate balance due
    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(paidAmount);
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "invoiceId=" + invoiceId +
                ", bookingId=" + bookingId +
                ", customerName='" + customerName + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", totalAmount=" + totalAmount +
                ", paidAmount=" + paidAmount +
                ", invoiceStatus='" + invoiceStatus + '\'' +
                '}';
    }
}
