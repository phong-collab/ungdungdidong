package com.example.travelapp.models;

public class BookingModel {
    private String id;
    private String tourId;
    private String tourTitle;
    private String tourThumbnail;
    private String paymentStatus;
    private String userId;
    private String tourDescription; // ĐÃ THÊM: Khai báo thuộc tính này để hết lỗi gạch đỏ
    private long totalPrice;

    // Constructor trống bắt buộc phải có cho Firebase Firestore
    public BookingModel() {}

    // --- CÁC HÀM GETTER VÀ SETTER ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public String getTourTitle() { return tourTitle; }
    public void setTourTitle(String tourTitle) { this.tourTitle = tourTitle; }

    public String getTourThumbnail() { return tourThumbnail; }
    public void setTourThumbnail(String tourThumbnail) { this.tourThumbnail = tourThumbnail; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTourDescription() { return tourDescription; }
    public void setTourDescription(String tourDescription) { this.tourDescription = tourDescription; }

    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
}