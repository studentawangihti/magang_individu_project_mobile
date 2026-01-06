package com.tmfw.inventory_app.api;

import com.tmfw.inventory_app.model.AssetResponse;
import com.tmfw.inventory_app.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ApiEndpoint {

    // Endpoint Login yang sudah ada (dari Auth.php)
    @FormUrlEncoded
    @POST("api/auth/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // [BENAR] Sesuaikan dengan nama Modul (api) dan Controller (Asset) di PHP Anda
    @GET("api/asset")
    Call<AssetResponse> getAssets();

    // [PERBAIKAN DETAIL] Tambahkan '/detail' agar tidak memanggil fungsi index() lagi
    @GET("api/asset/detail")
    Call<AssetResponse> getAssetDetail(@Query("id") String assetId);

    // [PERBAIKAN UPDATE] Pastikan path ini juga benar
    @FormUrlEncoded
    @POST("api/asset/update_asset")
    Call<LoginResponse> updateAsset(
            @Field("asset_id") String assetId,
            @Field("kondisi") String kondisi
    );

    @Multipart
    @POST("api/asset/lapor_kerusakan")
    Call<ResponseBody> laporKerusakan(
            @Part("asset_id") RequestBody assetId,
            @Part("kondisi") RequestBody kondisi,
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("api/asset/lapor_kerusakan")
    Call<AssetResponse> laporKerusakan(
            @Part("asset_id") RequestBody assetId,
            @Part("user_id") RequestBody userId,
            @Part("judul") RequestBody judul,
            @Part("deskripsi") RequestBody deskripsi,
            @Part("kondisi_baru") RequestBody kondisiBaru,
            @Part MultipartBody.Part imageFile
    );
}