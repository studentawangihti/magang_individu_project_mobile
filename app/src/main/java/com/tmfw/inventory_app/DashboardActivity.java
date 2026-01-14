package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {

    // Komponen UI
    private CardView cardListAsset, cardReportAsset, cardLogout;
    private TextView tvGreeting, tvJabatan;

    // Data User
    private String namaUser, jabatanUser, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Ambil Data dari Intent (Kiriman dari Login)
        if(getIntent() != null) {
            namaUser    = getIntent().getStringExtra("NAMA");
            jabatanUser = getIntent().getStringExtra("JABATAN");
            userId      = getIntent().getStringExtra("USER_ID"); // [PENTING] Ambil User ID
        }

        // 2. Inisialisasi UI
        tvGreeting      = findViewById(R.id.tvGreeting);
        tvJabatan       = findViewById(R.id.tvJabatan);
        cardListAsset   = findViewById(R.id.cardListAsset);
        cardReportAsset = findViewById(R.id.cardReportAsset); // Tombol Baru
        cardLogout      = findViewById(R.id.cardLogout);

        // 3. Set Teks
        tvGreeting.setText("Halo, " + (namaUser != null ? namaUser : "User"));
        tvJabatan.setText(jabatanUser != null ? jabatanUser : "-");

        // --- SETUP LISTENER MENU ---

        // MENU 1: LIST DATA ASET
        cardListAsset.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AssetListActivity.class);
            startActivity(intent);
        });

        // MENU 2: LAPOR KERUSAKAN (BARU)
        cardReportAsset.setOnClickListener(v -> {
            if (userId == null) {
                Toast.makeText(DashboardActivity.this, "Sesi User Hilang, Silahkan Login Ulang", Toast.LENGTH_SHORT).show();
                return;
            }
            // Pindah ke Halaman Lapor & Bawa ID User
            Intent intent = new Intent(DashboardActivity.this, ReportAssetActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // MENU 3: LOGOUT
        cardLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            // Bersihkan stack agar tidak bisa back ke dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(DashboardActivity.this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        });
    }
}