package com.hotel.model;

public class RoomStatus {
    private String status;
    private int roomCount;

    // Default constructor
    public RoomStatus() {}

    // Full constructor
    public RoomStatus(String status, int roomCount) {
        this.status = status;
        this.roomCount = roomCount;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getRoomCount() { return roomCount; }
    public void setRoomCount(int roomCount) { this.roomCount = roomCount; }

    @Override
    public String toString() {
        return String.format("%s: %d rooms", status, roomCount);
    }
}
