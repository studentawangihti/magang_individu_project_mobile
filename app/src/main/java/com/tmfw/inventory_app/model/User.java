package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class User {
    // Sesuaikan nama variabel dengan key JSON dari API
    @SerializedName("user_id")
    private String userId;

    @SerializedName("username")
    private String username;

    @SerializedName("nama_lengkap")
    private String namaLengkap;

    @SerializedName("jabatan")
    private String jabatan;

    @SerializedName("nama_role")
    private String roleName;

    // Getter
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getJabatan() { return jabatan; }
    public String getRole() { return roleName; }
}