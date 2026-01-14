package com.tmfw.inventory_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.HistoryItem;
import com.tmfw.inventory_app.model.HistoryResponse;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HistoryItem> listHistory = new ArrayList<>();
    private ImageView btnBack;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 1. Ambil User ID dari Login
        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Inisialisasi UI
        rvHistory = findViewById(R.id.rvHistory);
        btnBack = findViewById(R.id.btnBackHistory);

        btnBack.setOnClickListener(v -> finish());

        // 3. Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, listHistory);
        rvHistory.setAdapter(adapter);

        // 4. Load Data
        loadHistoryData(userId);
    }

    private void loadHistoryData(String userId) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat riwayat...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<HistoryResponse> call = api.getHistory(userId);

        call.enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 200) {
                        List<HistoryItem> data = response.body().getData();
                        if (data != null && !data.isEmpty()) {
                            adapter.updateList(data);
                        } else {
                            Toast.makeText(HistoryActivity.this, "Belum ada riwayat laporan", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(HistoryActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(HistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}