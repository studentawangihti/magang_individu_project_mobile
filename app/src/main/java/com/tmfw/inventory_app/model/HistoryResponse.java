package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HistoryResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<HistoryItem> data;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public List<HistoryItem> getData() { return data; }
}