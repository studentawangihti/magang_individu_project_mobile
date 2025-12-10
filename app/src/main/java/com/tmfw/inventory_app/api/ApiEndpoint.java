package com.tmfw.inventory_app.api;

import com.tmfw.inventory_app.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiEndpoint {
    @FormUrlEncoded
    @POST("api/auth/login") // Sesuaikan path ini jika berbeda di server
    Call<LoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );
}