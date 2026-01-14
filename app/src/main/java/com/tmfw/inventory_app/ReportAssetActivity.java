package com.tmfw.inventory_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;
import com.tmfw.inventory_app.model.GeneralResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportAssetActivity extends AppCompatActivity {

    // UI Components
    private Spinner spinAsset;
    private EditText etDeskripsi;
    private Button btnPilihFoto, btnKirim;
    private ImageView imgPreview, btnBack;
    private TextView tvNamaFoto;

    // Data
    private List<Asset> assetList = new ArrayList<>(); // Menyimpan objek aset asli
    private File photoFile = null; // Menyimpan file foto yang dipilih
    private String userId; // ID User yang sedang login

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_asset);

        // 1. Ambil User ID dari Intent (Dikirim dari Dashboard)
        userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) {
            Toast.makeText(this, "Sesi User Invalid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Inisialisasi View
        spinAsset = findViewById(R.id.spinAsset);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        btnPilihFoto = findViewById(R.id.btnPilihFoto);
        btnKirim = findViewById(R.id.btnKirimLaporan);
        imgPreview = findViewById(R.id.imgPreview);
        btnBack = findViewById(R.id.btnBackReport);
        tvNamaFoto = findViewById(R.id.tvNamaFoto);

        // 3. Setup Tombol
        btnBack.setOnClickListener(v -> finish());

        btnPilihFoto.setOnClickListener(v -> {
            // Buka Galeri
            pickImageLauncher.launch("image/*");
        });

        btnKirim.setOnClickListener(v -> prosesKirimLaporan());

        // 4. Load Data Aset ke Spinner
        loadAssetsToSpinner();
    }

    // --- BAGIAN 1: LOAD DATA ASET ---
    private void loadAssetsToSpinner() {
        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<AssetResponse> call = api.getAssets();

        call.enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if(response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                    assetList = response.body().getData();

                    // Buat List String untuk Tampilan Spinner
                    List<String> assetNames = new ArrayList<>();
                    for(Asset a : assetList) {
                        assetNames.add("[" + a.getKode() + "] " + a.getNama());
                    }

                    // Pasang ke Adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ReportAssetActivity.this, android.R.layout.simple_spinner_dropdown_item, assetNames);
                    spinAsset.setAdapter(adapter);
                } else {
                    Toast.makeText(ReportAssetActivity.this, "Gagal memuat aset", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                Toast.makeText(ReportAssetActivity.this, "Error Koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- BAGIAN 2: PILIH FOTO & KONVERSI ---

    // Launcher untuk menangkap hasil pemilihan foto
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Tampilkan Preview
                    imgPreview.setVisibility(View.VISIBLE);
                    imgPreview.setImageURI(uri);

                    // Konversi URI ke File agar bisa diupload
                    photoFile = uriToFile(uri);
                    tvNamaFoto.setText(photoFile.getName());
                }
            }
    );

    // Fungsi Pembantu: Mengubah URI Galeri menjadi File Asli di Cache Aplikasi
    // Ini wajib dilakukan karena Android modern membatasi akses langsung ke path file galeri
    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- BAGIAN 3: KIRIM DATA (MULTIPART) ---
    private void prosesKirimLaporan() {
        // 1. Validasi Input
        if (assetList.isEmpty()) {
            Toast.makeText(this, "Data Aset belum dimuat", Toast.LENGTH_SHORT).show();
            return;
        }

        String deskripsi = etDeskripsi.getText().toString();
        if (deskripsi.isEmpty()) {
            etDeskripsi.setError("Wajib diisi!");
            return;
        }

        if (photoFile == null) {
            Toast.makeText(this, "Mohon pilih foto bukti!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Ambil ID Aset yang dipilih dari Spinner
        int selectedPosition = spinAsset.getSelectedItemPosition();
        String assetId = assetList.get(selectedPosition).getId();

        // 3. Siapkan Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengirim Laporan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 4. Siapkan Data Multipart
        // A. Data Teks (Dibungkus RequestBody)
        RequestBody reqAssetId = RequestBody.create(MediaType.parse("text/plain"), assetId);
        RequestBody reqUserId = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody reqDeskripsi = RequestBody.create(MediaType.parse("text/plain"), deskripsi);

        // B. Data File (Dibungkus MultipartBody.Part)
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
        MultipartBody.Part bodyImage = MultipartBody.Part.createFormData("image_file", photoFile.getName(), reqFile);

        // 5. Panggil API
        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<GeneralResponse> call = api.laporKerusakan(reqAssetId, reqUserId, reqDeskripsi, bodyImage);

        call.enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 200) {
                        Toast.makeText(ReportAssetActivity.this, "Laporan Terkirim!", Toast.LENGTH_LONG).show();
                        finish(); // Tutup halaman ini
                    } else {
                        Toast.makeText(ReportAssetActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReportAssetActivity.this, "Gagal mengirim: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ReportAssetActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("UPLOAD_ERR", t.getMessage());
            }
        });
    }
}