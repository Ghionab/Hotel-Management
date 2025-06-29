package com.hotel.model;

import java.math.BigDecimal;

public class Room {
    private int roomId;
    private String roomNumber;
    private String type;
    private BigDecimal price;
    private String status;
    private int floor;
    private String description;

    // Constructors
    public Room() {}

    public Room(int roomId, String roomNumber, String type, BigDecimal price, String status, int floor, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.status = status;
        this.floor = floor;
        this.description = description;
    }

    // Getters and Setters
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ", Price: " + price + ", Floor: " + floor + ", Status: " + status + ", Description: " + description + ")";
    }
}