package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class GeneralResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
}