package com.numberbook.app;

import com.google.gson.annotations.SerializedName;

public class ServerResult {

    @SerializedName("success")
    private boolean operationSucceeded;

    @SerializedName("message")
    private String statusMessage;

    public boolean isOperationSucceeded() {
        return operationSucceeded;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
