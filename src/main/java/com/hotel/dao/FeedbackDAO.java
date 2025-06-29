package com.hotel.dao;

import com.hotel.models.Feedback;
import java.util.List;

public interface FeedbackDAO {
    void addFeedback(Feedback feedback);
    List<Feedback> getAllFeedback();
    Feedback getFeedbackById(int feedbackId);
    List<Feedback> getFeedbackByCustomerId(int customerId);
    List<Feedback> getFeedbackByBookingId(int bookingId);
    double getAverageRating();
}
