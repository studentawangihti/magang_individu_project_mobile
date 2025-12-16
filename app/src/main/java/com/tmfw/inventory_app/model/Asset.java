package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Tambahkan ini agar object bisa dikirim lewat Intent

public class Asset implements Serializable { // Implement Serializable

    // Sesuaikan nama field dengan JSON dari API
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

    // [BARU] Tambahkan field tambahan untuk detail lengkap
    @SerializedName("asset_tahun") // Pastikan kolom ini ada di query API Asset.php
    private String tahun;

    @SerializedName("asset_lokasi") // Contoh field lain
    private String lokasi;

    // Getter methods
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getKode() { return kode; }
    public String getKondisi() { return kondisi; }
    public String getKategori() { return kategori; }
    public String getSatuan() { return satuan; }
    public String getStok() { return stok; }
    public String getTahun() { return tahun; }
    public String getLokasi() { return lokasi; }
}