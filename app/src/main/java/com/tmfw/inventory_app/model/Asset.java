package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Asset implements Serializable {

    // Field yang sudah ada
    @SerializedName("asset_id")
    private String id;

    @SerializedName("asset_nm")
    private String nama;

    @SerializedName("asset_kd")
    private String kode;

    @SerializedName("kategori_nm")
    private String kategori;

    @SerializedName("stok_min_qty")
    private String stok;

    @SerializedName("asset_thn_beli")
    private String tahun;

    @SerializedName("satuan_nm")
    private String satuan;

    @SerializedName("asset_kondisi")
    private String kondisi;

    @SerializedName("harga_beli")
    private String harga;

    @SerializedName("asset_ket")
    private String deskripsi;

    @SerializedName("image_url")
    private String imageUrl;

    // --- GETTER ---
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getKode() { return kode; }
    public String getKategori() { return kategori; }
    public String getStok() { return stok; }
    public String getTahun() { return tahun; }
    public String getSatuan() { return satuan; }
    public String getKondisi() { return kondisi; }
    public String getHarga() { return harga; }
    public String getDeskripsi() { return deskripsi; }
    public String getImageUrl() { return imageUrl; }
}