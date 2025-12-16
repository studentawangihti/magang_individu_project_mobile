package com.tmfw.inventory_app.api;

import com.tmfw.inventory_app.model.AssetResponse;
import com.tmfw.inventory_app.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiEndpoint {

    // Endpoint Login yang sudah ada (dari Auth.php)
    @FormUrlEncoded
    @POST("api/auth/login")
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // [BARU] Endpoint untuk mengambil daftar Asset (dari Asset.php)
    // URL lengkap akan menjadi: BASE_URL + "asset"
    @GET("api/asset")
    Call<AssetResponse> getAssets();
}