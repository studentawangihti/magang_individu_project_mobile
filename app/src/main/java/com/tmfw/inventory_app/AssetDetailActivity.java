package com.tmfw.inventory_app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;
import com.tmfw.inventory_app.model.LoginResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetDetailActivity extends AppCompatActivity {

    private TextView tvNama, tvKode, tvLabelKondisi;
    private TextView tvKategori, tvTahun, tvHarga, tvStok, tvDeskripsi;
    private Spinner spinnerKondisi;
    private Button btnSimpan, btnKeHalamanLapor;

    private Asset asset;
    private File photoFile;
    private ImageView tempImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        // Inisialisasi Views (SESUAI XML TERBARU)
        tvNama = findViewById(R.id.tvDetailNama);
        tvKode = findViewById(R.id.tvDetailKode);
        tvLabelKondisi = findViewById(R.id.tvLabelKondisi);
        tvKategori = findViewById(R.id.tvDetailKategori);
        tvTahun = findViewById(R.id.tvDetailTahun);
        tvHarga = findViewById(R.id.tvDetailHarga);
        tvStok = findViewById(R.id.tvDetailStok);
        tvDeskripsi = findViewById(R.id.tvDetailDeskripsi);

        spinnerKondisi = findViewById(R.id.spinnerUpdateKondisi);
        btnSimpan = findViewById(R.id.btnSimpanLaporan);
        btnKeHalamanLapor = findViewById(R.id.btnKeHalamanLapor);

        // Ambil Data dari Intent
        if (getIntent().hasExtra("DATA_ASSET")) {
            asset = (Asset) getIntent().getSerializableExtra("DATA_ASSET");
        }

        // Cek null agar tidak Force Close
        if (asset != null) {
            tampilkanData();
            setupSpinner();

            btnSimpan.setOnClickListener(v -> {
                String kondisiBaru = spinnerKondisi.getSelectedItem().toString();
                updateKondisiKeServer(asset.getId(), kondisiBaru);
            });

            btnKeHalamanLapor.setOnClickListener(v -> showDialogLapor());
        } else {
            Toast.makeText(this, "Gagal memuat data aset", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity jika data error
        }
    }

    private void tampilkanData() {
        tvNama.setText(asset.getNama());
        tvKode.setText(asset.getKode());
        tvKategori.setText(asset.getKategori());

        // Safety check untuk string
        tvTahun.setText(asset.getTahun() != null ? asset.getTahun() : "-");

        String stok = asset.getStok() != null ? asset.getStok() : "0";
        String satuan = asset.getSatuan() != null ? asset.getSatuan() : "";
        tvStok.setText(stok + " " + satuan);

        tvHarga.setText("Rp " + (asset.getHarga() != null ? asset.getHarga() : "0"));
        tvDeskripsi.setText(asset.getDeskripsi() != null ? asset.getDeskripsi() : "-");

        // Warna Label
        String kondisi = asset.getKondisi() != null ? asset.getKondisi() : "BAIK";
        tvLabelKondisi.setText(kondisi);

        if (kondisi.equalsIgnoreCase("RUSAK") || kondisi.equalsIgnoreCase("HILANG")) {
            tvLabelKondisi.setBackgroundColor(Color.RED);
        } else if (kondisi.equalsIgnoreCase("PERBAIKAN")) {
            tvLabelKondisi.setBackgroundColor(Color.parseColor("#FF9800"));
        } else {
            tvLabelKondisi.setBackgroundColor(Color.parseColor("#4CAF50"));
        }
    }

    // --- SISA KODE (Setup Spinner, Update Server, Dialog Lapor) TETAP SAMA ---
    // Copy paste bagian bawahnya dari file Anda sebelumnya, karena logika itu sudah benar.
    // Yang penting bagian onCreate dan XML di atas sinkron.

    private void setupSpinner() {
        String[] opsi = {"BAIK", "RUSAK", "HILANG", "PERBAIKAN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opsi);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKondisi.setAdapter(adapter);
    }

    // ... Copy method showDialogLapor, updateKondisiKeServer, onActivityResult dari sebelumnya ...
    private void updateKondisiKeServer(String id, String kondisi) {
        btnSimpan.setEnabled(false);
        btnSimpan.setText("Menyimpan...");
        ApiClient.getApi().updateAsset(id, kondisi).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                if (response.isSuccessful()) {
                    Toast.makeText(AssetDetailActivity.this, "Sukses!", Toast.LENGTH_SHORT).show();
                    tvLabelKondisi.setText(kondisi);
                } else {
                    Toast.makeText(AssetDetailActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnSimpan.setEnabled(true);
                btnSimpan.setText("Simpan");
                Toast.makeText(AssetDetailActivity.this, "Error Koneksi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDialogLapor() {
        // ... (Gunakan kode dialog lapor yang sudah kamu punya)
        // Pastikan Layout dialog_lapor_asset.xml tidak ada error juga
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_lapor_asset, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // ... inisialisasi komponen dialog ...
        // Agar tidak panjang, saya asumsikan bagian ini sudah aman karena errornya di "Buka Menu" (onCreate)

        dialog.show();
    }
}