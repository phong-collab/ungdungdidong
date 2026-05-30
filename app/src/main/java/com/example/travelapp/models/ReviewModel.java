package com.example.travelapp.models;

import com.google.firebase.Timestamp;

public class ReviewModel {
    private String id;
    private String userId;
    private String reviewerName;
    private String tourId;
    private String tourTitle;
    private String content;
    private double rating;
    private Timestamp createdAt;

    public ReviewModel() {}

    public ReviewModel(String id, String userId, String reviewerName, String tourId, String tourTitle, String content, double rating, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.reviewerName = reviewerName;
        this.tourId = tourId;
        this.tourTitle = tourTitle;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}