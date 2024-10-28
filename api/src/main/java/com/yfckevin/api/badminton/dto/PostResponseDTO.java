package com.yfckevin.api.badminton.dto;


import com.yfckevin.common.enums.AirConditionerType;

public class PostResponseDTO {
    private String id;
    private String userId;
    private String name;
    private String place;
    private String startTime;
    private String endTime;
    private String level;
    private int fee;
    private double duration;
    private String brand;
    private String contact;
    private String parkInfo;
    private String type;
    private AirConditionerType airConditioner;
    private String dayOfWeek;
    private String creationDate;
    private String modificationDate;
    private String deletionDate;
    private boolean labelCourt; //是否已加入球館內，預設是無

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getParkInfo() {
        return parkInfo;
    }

    public void setParkInfo(String parkInfo) {
        this.parkInfo = parkInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AirConditionerType getAirConditioner() {
        return airConditioner;
    }

    public void setAirConditioner(AirConditionerType airConditioner) {
        this.airConditioner = airConditioner;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(String deletionDate) {
        this.deletionDate = deletionDate;
    }

    public boolean isLabelCourt() {
        return labelCourt;
    }

    public void setLabelCourt(boolean labelCourt) {
        this.labelCourt = labelCourt;
    }

    @Override
    public String toString() {
        return "PostResponseDTO{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", place='" + place + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", level='" + level + '\'' +
                ", fee=" + fee +
                ", duration=" + duration +
                ", brand='" + brand + '\'' +
                ", contact='" + contact + '\'' +
                ", parkInfo='" + parkInfo + '\'' +
                ", type='" + type + '\'' +
                ", airConditioner=" + airConditioner +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", modificationDate='" + modificationDate + '\'' +
                ", deletionDate='" + deletionDate + '\'' +
                ", labelCourt=" + labelCourt +
                '}';
    }
}