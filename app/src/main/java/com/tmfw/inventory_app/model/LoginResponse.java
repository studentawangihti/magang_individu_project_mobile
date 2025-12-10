package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private User data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public User getData() { return data; }
}