package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    TextView tvNama, tvJabatan;
    Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvNama = findViewById(R.id.tvNamaUser);
        tvJabatan = findViewById(R.id.tvJabatan);
        btnLogout = findViewById(R.id.btnLogout);

        // Ambil data dari Intent
        String nama = getIntent().getStringExtra("NAMA");
        String jabatan = getIntent().getStringExtra("JABATAN");

        // Set Text (Tambahkan pengecekan null biar aman)
        tvNama.setText(nama != null ? nama : "User");
        tvJabatan.setText(jabatan != null ? jabatan : "-");

        // REVISI: Logika Logout yang lebih bersih
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            // Flag ini membersihkan tumpukan activity agar tombol Back tidak kembali ke Dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}