package com.numberbook.app;

import com.google.gson.annotations.SerializedName;

public class PhoneEntry {

    @SerializedName("id")
    private int recordId;

    @SerializedName("name")
    private String fullName;

    @SerializedName("phone")
    private String mobileNumber;

    @SerializedName("source")
    private String originTag;

    @SerializedName("created_at")
    private String registeredAt;

    public PhoneEntry() {
    }

    public PhoneEntry(String fullName, String mobileNumber) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
    }

    public int getRecordId() {
        return recordId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getOriginTag() {
        return originTag;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setOriginTag(String originTag) {
        this.originTag = originTag;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }
}
