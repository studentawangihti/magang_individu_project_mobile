package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AssetResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Asset> data;

    public List<Asset> getData() { return data; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}