package com.example.travelapp.models;
import java.util.List;

public class TourModel {
    private String id, title, thumbnail, description, categoryId;
    private long priceAdult, totalReviews;

    private long  priceChild;
    private double ratingAverage;
    private boolean isFeatured;
    private List<ItineraryInner> itinerary;

    public TourModel() {}

    public static class ItineraryInner {
        private int day; private String content;
        public ItineraryInner() {}
        public int getDay() { return day; } public void setDay(int day) { this.day = day; }
        public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    }

    // Getters và Setters cho các trường chính
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getThumbnail() { return thumbnail; } public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public long getPriceAdult() { return priceAdult; } public void setPriceAdult(long priceAdult) { this.priceAdult = priceAdult; }
    public long getPriceChild() {
        return priceChild;
    }

    // 3. Thêm hàm Setter để Firebase có thể tự động đổ dữ liệu vào
    public void setPriceChild(long priceChild) {
        this.priceChild = priceChild;
    }
    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    public boolean getIsFeatured() { return isFeatured; }
    public void setIsFeatured(boolean featured) { isFeatured = featured; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public List<ItineraryInner> getItinerary() { return itinerary; } public void setItinerary(List<ItineraryInner> itinerary) { this.itinerary = itinerary; }
    public long getTotalReviews() { return totalReviews; } public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
    public double getRatingAverage() { return ratingAverage; } public void setRatingAverage(double ratingAverage) { this.ratingAverage = ratingAverage; }
    public String getCategoryId() { return categoryId; } public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}