package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class HistoryItem {
    @SerializedName("service_id")
    private String serviceId;

    @SerializedName("asset_nm")
    private String assetName;

    @SerializedName("keluhan_deskripsi")
    private String deskripsi;

    @SerializedName("tanggal_fmt") // Kita pakai tanggal yang sudah diformat PHP
    private String tanggal;

    @SerializedName("status_tiket")
    private String status; // 0=Baru, 1=Proses, 2=Selesai

    @SerializedName("foto_url")
    private String fotoUrl;

    // Getter
    public String getServiceId() { return serviceId; }
    public String getAssetName() { return assetName; }
    public String getDeskripsi() { return deskripsi; }
    public String getTanggal() { return tanggal; }
    public String getStatus() { return status; }
    public String getFotoUrl() { return fotoUrl; }
}