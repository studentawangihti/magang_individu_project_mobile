package com.tmfw.inventory_app.api;

import com.tmfw.inventory_app.model.AssetResponse;
import com.tmfw.inventory_app.model.DashboardStatsResponse;
import com.tmfw.inventory_app.model.GeneralResponse; // Import Model Baru
import com.tmfw.inventory_app.model.LoginResponse;
import com.tmfw.inventory_app.model.SingleAssetResponse;
import com.tmfw.inventory_app.model.HistoryResponse;

import okhttp3.MultipartBody; // Wajib untuk File
import okhttp3.RequestBody;   // Wajib untuk Teks dalam Multipart
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart; // Annotation Multipart
import retrofit2.http.POST;
import retrofit2.http.Part;      // Annotation Part
import retrofit2.http.Query;

public interface ApiEndpoint {

    // ... (Kode Login & GetAssets yang lama biarkan saja) ...
    @FormUrlEncoded
    @POST("api/auth/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("api/asset")
    Call<AssetResponse> getAssets();

    @FormUrlEncoded
    @POST("api/asset/detail")
    Call<SingleAssetResponse> getAssetDetail(@Field("asset_id") String assetId);

    // --- TAMBAHAN BARU: FITUR LAPOR KERUSAKAN (MULTIPART) ---
    @Multipart
    @POST("api/asset/lapor_kerusakan")
    Call<GeneralResponse> laporKerusakan(
            @Part("asset_id") RequestBody assetId,
            @Part("user_id") RequestBody userId,
            @Part("deskripsi") RequestBody deskripsi,
            @Part MultipartBody.Part imageFile
    );

    @GET("api/asset/history")
    Call<HistoryResponse> getHistory(@Query("user_id") String userId);

    @GET("api/asset/dashboard_stats")
    Call<DashboardStatsResponse> getDashboardStats(@Query("user_id") String userId);
}