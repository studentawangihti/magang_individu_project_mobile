package com.tmfw.inventory_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_list); // Menggunakan layout baru

        recyclerView = findViewById(R.id.recyclerViewAsset);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDataAsset();
    }

    private void loadDataAsset() {
        ApiEndpoint apiService = ApiClient.getApi();
        Call<AssetResponse> call = apiService.getAssets();

        call.enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Asset> assets = response.body().getData();
                    adapter = new AssetAdapter(AssetListActivity.this, assets);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(AssetListActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                Toast.makeText(AssetListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}