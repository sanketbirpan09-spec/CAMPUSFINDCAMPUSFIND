package com.campusfind.dto;

import com.campusfind.model.Item;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    private Long id;
    private Long userId;
    private String title;
    private String type;
    private String category;
    private String description;
    private LocalDate itemDate;
    private String location;
    private String contactInfo;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private String reporterName;
    private String reporterEmail;
    private boolean isOwner;

    public ItemResponse() {}

    public static ItemResponse fromItem(Item item, Long currentUserId) {
        if (item == null) return null;
        ItemResponse resp = new ItemResponse();
        resp.setId(item.getId());
        resp.setUserId(item.getUserId());
        resp.setTitle(item.getTitle());
        resp.setType(item.getType());
        resp.setCategory(item.getCategory());
        resp.setDescription(item.getDescription());
        resp.setItemDate(item.getItemDate());
        resp.setLocation(item.getLocation());
        resp.setContactInfo(item.getContactInfo());
        resp.setImageUrl(item.getImageUrl());
        resp.setStatus(item.getStatus());
        resp.setCreatedAt(item.getCreatedAt());
        resp.setReporterName(item.getReporterName());
        resp.setReporterEmail(item.getReporterEmail());
        resp.setOwner(currentUserId != null && currentUserId.equals(item.getUserId()));
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
