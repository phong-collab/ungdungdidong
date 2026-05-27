package com.example.travelapp.models;

public class BookingModel {
    private String id, tourId, tourTitle, tourThumbnail, paymentStatus;
    private long totalPrice;

    public BookingModel() {}
    // Getters và Setters
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getTourId() { return tourId; } public void setTourId(String tourId) { this.tourId = tourId; }
    public String getTourTitle() { return tourTitle; } public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }
    public String getTourThumbnail() { return tourThumbnail; } public void setTourThumbnail(String tourThumbnail) { this.tourThumbnail = tourThumbnail; }
    public String getPaymentStatus() { return paymentStatus; } public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public long getTotalPrice() { return totalPrice; } public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
}