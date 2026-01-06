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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetDetailActivity extends AppCompatActivity {

    // Deklarasi Komponen UI
    private TextView tvNama, tvKode, tvLabelKondisi;
    private TextView tvKategori, tvTahun, tvHarga, tvStok, tvDeskripsi;
    private Button btnKeHalamanLapor;

    // Variabel Data
    private Asset asset;
    private File photoFile;          // File foto yang siap diupload
    private ImageView tempImageView; // Untuk preview di dalam dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        // 1. Inisialisasi Views sesuai ID di XML terbaru
        tvNama = findViewById(R.id.tvDetailNama);
        tvKode = findViewById(R.id.tvDetailKode);
        tvLabelKondisi = findViewById(R.id.tvLabelKondisi);
        tvKategori = findViewById(R.id.tvDetailKategori);
        tvTahun = findViewById(R.id.tvDetailTahun);
        tvHarga = findViewById(R.id.tvDetailHarga);
        tvStok = findViewById(R.id.tvDetailStok);
        tvDeskripsi = findViewById(R.id.tvDetailDeskripsi);

        btnKeHalamanLapor = findViewById(R.id.btnKeHalamanLapor);

        // 2. Ambil Data dari Intent
        if (getIntent().hasExtra("DATA_ASSET")) {
            asset = (Asset) getIntent().getSerializableExtra("DATA_ASSET");
        }

        // 3. Tampilkan Data jika ada
        if (asset != null) {
            tampilkanData();

            // Event Listener Tombol Lapor
            btnKeHalamanLapor.setOnClickListener(v -> showDialogLapor());
        } else {
            Toast.makeText(this, "Gagal memuat data aset", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void tampilkanData() {
        tvNama.setText(asset.getNama());
        tvKode.setText(asset.getKode());
        tvKategori.setText(asset.getKategori());

        tvTahun.setText(asset.getTahun() != null ? asset.getTahun() : "-");

        String stok = asset.getStok() != null ? asset.getStok() : "0";
        String satuan = asset.getSatuan() != null ? asset.getSatuan() : "";
        tvStok.setText(stok + " " + satuan);

        tvHarga.setText("Rp " + (asset.getHarga() != null ? asset.getHarga() : "0"));
        tvDeskripsi.setText(asset.getDeskripsi() != null ? asset.getDeskripsi() : "-");

        // Set Warna Label Kondisi
        String kondisi = asset.getKondisi() != null ? asset.getKondisi() : "BAIK";
        tvLabelKondisi.setText(kondisi);

        if (kondisi.equalsIgnoreCase("RUSAK") || kondisi.equalsIgnoreCase("HILANG")) {
            tvLabelKondisi.setBackgroundColor(Color.RED);
        } else if (kondisi.equalsIgnoreCase("PERBAIKAN")) {
            tvLabelKondisi.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
        } else {
            tvLabelKondisi.setBackgroundColor(Color.parseColor("#4CAF50")); // Hijau
        }
    }

    // --- LOGIKA DIALOG LAPOR (POPUP) ---
    private void showDialogLapor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_lapor_asset, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // Init Komponen Dialog
        Spinner spKondisi = dialogView.findViewById(R.id.spinnerKondisi);
        EditText etJudul = dialogView.findViewById(R.id.etJudul);
        EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
        Button btnFoto = dialogView.findViewById(R.id.btnPilihFoto);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);
        Button btnKirim = dialogView.findViewById(R.id.btnKirim);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);

        // Setup Spinner Dialog
        String[] statusOptions = {"RUSAK", "PERBAIKAN", "HILANG", "BAIK"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKondisi.setAdapter(adapter);

        // Listener Pilih Foto
        btnFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
            tempImageView = ivPreview; // Simpan referensi view agar bisa diupdate di onActivityResult
        });

        // Listener Kirim
        btnKirim.setOnClickListener(v -> {
            String judul = etJudul.getText().toString();
            String desk = etDeskripsi.getText().toString();
            String kond = spKondisi.getSelectedItem().toString();

            if(judul.isEmpty()) {
                Toast.makeText(this, "Judul Laporan Wajib Diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadLaporanTiket(judul, desk, kond, dialog);
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- LOGIKA UPLOAD KE SERVER ---
    private void uploadLaporanTiket(String judul, String deskripsi, String kondisiBaru, AlertDialog dialog) {
        // TODO: Ganti dengan ID User yang login sesungguhnya (dari Session/SharedPreferences)
        String userId = "P000000000029";

        // Bungkus data text
        RequestBody reqAssetId = RequestBody.create(MediaType.parse("text/plain"), asset.getId());
        RequestBody reqUserId = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody reqJudul = RequestBody.create(MediaType.parse("text/plain"), judul);
        RequestBody reqDesk = RequestBody.create(MediaType.parse("text/plain"), deskripsi);
        RequestBody reqKondisi = RequestBody.create(MediaType.parse("text/plain"), kondisiBaru);

        // Bungkus data file gambar (jika ada)
        MultipartBody.Part bodyGambar = null;
        if (photoFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
            bodyGambar = MultipartBody.Part.createFormData("image_file", photoFile.getName(), reqFile);
        }

        // Panggil Retrofit
        ApiClient.getApi().laporKerusakan(reqAssetId, reqUserId, reqJudul, reqDesk, reqKondisi, bodyGambar)
                .enqueue(new Callback<AssetResponse>() {
                    @Override
                    public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AssetDetailActivity.this, "Laporan Terkirim!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            finish(); // Kembali ke list agar data ter-refresh
                        } else {
                            Toast.makeText(AssetDetailActivity.this, "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<AssetResponse> call, Throwable t) {
                        Toast.makeText(AssetDetailActivity.this, "Error Jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- PERBAIKAN UTAMA: MENANGANI FILE GAMBAR (SCOPED STORAGE) ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            // 1. Tampilkan Preview di Dialog
            if(tempImageView != null) {
                tempImageView.setVisibility(View.VISIBLE);
                tempImageView.setImageURI(selectedImage);
            }

            // 2. Salin URI ke File Cache (Solusi EACCES Permission Denied)
            try {
                photoFile = uriToFile(selectedImage);
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // Fungsi Helper: Menyalin gambar dari Galeri ke Folder Cache Aplikasi
    private File uriToFile(Uri uri) {
        try {
            // Buat file temporary di folder cache
            File file = new File(getCacheDir(), "temp_upload_" + System.currentTimeMillis() + ".jpg");

            // Buka stream dari ContentResolver (Galeri)
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Buka stream ke file cache
            OutputStream outputStream = new FileOutputStream(file);

            // Salin data
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}