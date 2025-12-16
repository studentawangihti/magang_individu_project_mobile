package com.tmfw.inventory_app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetDetailActivity extends AppCompatActivity {

    private TextView tvNama, tvKode, tvKategori, tvStok, tvTahun, tvSatuan;
    private Spinner spinnerKondisi;
    private Button btnSimpan;
    private Asset asset; // Object untuk menampung data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        // 1. Inisialisasi View
        tvNama = findViewById(R.id.tvDetailNama);
        tvKode = findViewById(R.id.tvDetailKode);
        tvKategori = findViewById(R.id.tvDetailKategori);
        tvStok = findViewById(R.id.tvDetailStok);
        tvTahun = findViewById(R.id.tvDetailTahun);
        tvSatuan = findViewById(R.id.tvDetailSatuan);
        spinnerKondisi = findViewById(R.id.spinnerUpdateKondisi);
        btnSimpan = findViewById(R.id.btnSimpanLaporan);

        // 2. Ambil Data dari Intent (Dikirim dari Adapter)
        asset = (Asset) getIntent().getSerializableExtra("DATA_ASSET");

        if (asset != null) {
            tampilkanData(asset);
        }

        // 3. Setup Spinner Kondisi
        setupSpinner();

        // 4. Tombol Simpan
        btnSimpan.setOnClickListener(v -> {
            String kondisiBaru = spinnerKondisi.getSelectedItem().toString();
            updateKondisi(asset.getId(), kondisiBaru);
        });
    }

    private void tampilkanData(Asset a) {
        tvNama.setText(a.getNama());
        tvKode.setText(a.getKode());
        tvKategori.setText(a.getKategori());
        tvStok.setText(a.getStok());
        tvTahun.setText(a.getTahun() != null ? a.getTahun() : "-");
        tvSatuan.setText(a.getSatuan());
    }

    private void setupSpinner() {
        String[] opsi = {"BAIK", "RUSAK", "HILANG", "DIPERBAIKI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opsi);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKondisi.setAdapter(adapter);

        // Set seleksi awal sesuai kondisi aset saat ini
        if (asset != null && asset.getKondisi() != null) {
            for (int i = 0; i < opsi.length; i++) {
                if (opsi[i].equalsIgnoreCase(asset.getKondisi())) {
                    spinnerKondisi.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateKondisi(String id, String kondisi) {
        btnSimpan.setText("Menyimpan...");
        btnSimpan.setEnabled(false);

        ApiClient.getApi().updateAsset(id, kondisi).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan Perubahan");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AssetDetailActivity.this, "Sukses Update Kondisi!", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup activity dan kembali ke list
                } else {
                    Toast.makeText(AssetDetailActivity.this, "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan Perubahan");
                Toast.makeText(AssetDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}