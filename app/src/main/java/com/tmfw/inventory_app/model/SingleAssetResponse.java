package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;



public class SingleAssetResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Asset data; // Perhatikan: Ini Single Object, bukan List<Asset>

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Asset getData() { return data; }
}