package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class Asset {
    @SerializedName("asset_id")
    private String id;

    @SerializedName("asset_kd")
    private String kode;

    @SerializedName("asset_nm")
    private String nama;

    @SerializedName("asset_kondisi")
    private String kondisi;

    @SerializedName("kategori_nm")
    private String kategori;

    @SerializedName("satuan_nm")
    private String satuan;

    @SerializedName("stok_min_qty")
    private String stok;

    // Getter methods
    public String getNama() { return nama; }
    public String getKode() { return kode; }
    public String getKondisi() { return kondisi; }
    public String getKategori() { return kategori; }
    public String getStok() { return stok; }
}