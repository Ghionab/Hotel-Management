# Hotel Management System

A comprehensive Java-based Hotel Management System built with JavaFX for the frontend and MySQL for the database. 
This application provides a complete solution for managing hotel operations including room bookings, customer management, staff management, and billing.

## Features

- **User Authentication**
  - Secure login system for staff and administrators
  - Role-based access control

- **Room Management**
  - Add, edit, and delete rooms
  - Room status tracking (Available, Occupied, Maintenance)
  - Room type categorization

- **Booking System**
  - Room reservation and check-in/check-out
  - Booking history
  - Availability calendar

- **Customer Management**
  - Customer registration and profiles
  - Booking history
  - Contact information management

- **Billing & Invoicing**
  - Automated invoice generation
  - Payment processing
  - Receipt printing

- **Staff Management**
  - Staff profiles and roles
  - Shift management
  - Performance tracking

- **Services**
  - Additional services booking (laundry, room service, etc.)
  - Service charge management

## Technologies Used

- **Frontend**: JavaFX
- **Backend**: Java 8+
- **Database**: MySQL
- **Build Tool**: Maven
- **Architecture**: MVC (Model-View-Controller) Pattern

## Prerequisites

Before running the application, ensure you have the following installed:

- Java Development Kit (JDK) 8 or later
- MySQL Server
- Maven
- Git (for cloning the repository)

## Installation

1. **Clone the repository**
   ```bash
   git clone [repository-url]
   cd Java-Assign
   ```

2. **Database Setup**
   - Create a new MySQL database named `hotel_management`
   - Import the database schema from `database/hotel_management.sql` (if available)
   - Update database credentials in `src/main/resources/application.properties`

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn javafx:run
   ```
   or run the `MainApp` class from your IDE
