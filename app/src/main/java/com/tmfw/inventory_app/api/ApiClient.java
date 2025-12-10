package com.tmfw.inventory_app.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // ⚠️ GANTI IP INI SESUAI IP LAPTOP ANDA ⚠️
    // Contoh: "http://192.168.1.10/nama_folder_api/index.php/"
    // Pastikan diakhiri tanda '/'
    private static final String BASE_URL = "http://192.168.1.98/Project_Magang_API/index.php/";

    private static Retrofit retrofit;

    public static ApiEndpoint getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiEndpoint.class);
    }
}