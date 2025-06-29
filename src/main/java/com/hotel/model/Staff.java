package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Staff {
    private int userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String position;
    private LocalDate hireDate;
    private double salary;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Staff() {}

    // Constructor with required fields
    public Staff(int userId, String firstName, String lastName, String position) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
    }

    // Full constructor
    public Staff(int userId, String firstName, String lastName, String phoneNumber, 
                String email, String position, LocalDate hireDate, double salary, 
                String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.position = position;
        this.hireDate = hireDate;
        this.salary = salary;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", firstName, lastName, position);
    }
}
