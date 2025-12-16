package com.tmfw.inventory_app.model

import com.google.gson.annotations.SerializedName

data class Asset(
    @SerializedName("asset_id")
    val id: String, // ID biasanya string/int, aman pakai String di JSON

    @SerializedName("asset_kd")
    val kode: String,

    @SerializedName("asset_nm")
    val nama: String,

    @SerializedName("asset_kondisi")
    val kondisi: String,

    @SerializedName("kategori_nm") // Sesuai alias di query SQL tadi
    val kategori: String?,

    @SerializedName("satuan_nm") // Sesuai alias di query SQL tadi
    val satuan: String?
)

data class AssetResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Asset>
)