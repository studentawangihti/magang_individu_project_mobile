package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.DashboardStatsResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    // Komponen UI
    private CardView cardListAsset, cardReportAsset, cardLogout, cardHistory;
    private TextView tvGreeting, tvJabatan;

    // Komponen UI Statistik (Angka-angka di Dashboard)
    private TextView tvStatTotal, tvStatRusak, tvStatTiket;

    // Data User
    private String namaUser, jabatanUser, roleUser, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Ambil Data dari Intent (Dikirim dari MainActivity saat Login)
        if(getIntent() != null) {
            namaUser    = getIntent().getStringExtra("NAMA");
            jabatanUser = getIntent().getStringExtra("JABATAN");
            roleUser    = getIntent().getStringExtra("ROLE"); // Ambil Data Role sebagai cadangan
            userId      = getIntent().getStringExtra("USER_ID");
        }

        // 2. Inisialisasi UI (Hubungkan dengan ID di XML)
        tvGreeting      = findViewById(R.id.tvGreeting);
        tvJabatan       = findViewById(R.id.tvJabatan);

        // Menu Cards
        cardListAsset   = findViewById(R.id.cardListAsset);
        cardReportAsset = findViewById(R.id.cardReportAsset);
        cardHistory     = findViewById(R.id.cardHistory);
        cardLogout      = findViewById(R.id.cardLogout);

        // Statistik Cards
        tvStatTotal     = findViewById(R.id.tvStatTotal);
        tvStatRusak     = findViewById(R.id.tvStatRusak);
        tvStatTiket     = findViewById(R.id.tvStatTiket);

        // 3. Set Teks Header (Nama & Jabatan)
        tvGreeting.setText("Halo, " + (namaUser != null ? namaUser : "User"));

        // LOGIKA PENTING: Cek Jabatan vs Role
        if (jabatanUser != null && !jabatanUser.isEmpty() && !jabatanUser.equals("null")) {
            // Jika Jabatan ada, pakai Jabatan
            tvJabatan.setText(jabatanUser);
        } else if (roleUser != null && !roleUser.isEmpty() && !roleUser.equals("null")) {
            // Jika Jabatan kosong, pakai Role (Admin/User)
            tvJabatan.setText(roleUser);
        } else {
            // Jika keduanya kosong
            tvJabatan.setText("-");
        }

        // 4. Setup Listener (Aksi Klik Menu)

        // Menu 1: Daftar Aset
        cardListAsset.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AssetListActivity.class);
            startActivity(intent);
        });

        // Menu 2: Lapor Kerusakan
        cardReportAsset.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(DashboardActivity.this, "Sesi User Hilang, Silahkan Login Ulang", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(DashboardActivity.this, ReportAssetActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Menu 3: Riwayat Laporan
        cardHistory.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Menu 4: Logout
        cardLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            // Clear history stack agar user tidak bisa back ke dashboard setelah logout
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(DashboardActivity.this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        });
    }

    // 5. Lifecycle: Dipanggil saat aplikasi dibuka kembali (Resume)
    // Gunanya agar angka statistik ter-update setelah kita melakukan pelaporan baru
    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            loadDashboardStats();
        }
    }

    // 6. Fungsi Memanggil API Statistik
    private void loadDashboardStats() {
        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<DashboardStatsResponse> call = api.getDashboardStats(userId);

        call.enqueue(new Callback<DashboardStatsResponse>() {
            @Override
            public void onResponse(Call<DashboardStatsResponse> call, Response<DashboardStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 200) {
                        DashboardStatsResponse.StatsData data = response.body().getData();
                        if (data != null) {
                            // Update Angka di Layar dengan data dari Server
                            tvStatTotal.setText(String.valueOf(data.getTotalAsset()));
                            tvStatRusak.setText(String.valueOf(data.getTotalRusak()));
                            tvStatTiket.setText(String.valueOf(data.getMyTicket()));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DashboardStatsResponse> call, Throwable t) {
                // Jika gagal koneksi, set strip "-" agar user tahu data belum masuk
                tvStatTotal.setText("-");
                tvStatRusak.setText("-");
                tvStatTiket.setText("-");
            }
        });
    }
}