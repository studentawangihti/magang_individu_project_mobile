package com.tmfw.inventory_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.tmfw.inventory_app.api.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity {

    // Komponen UI
    private ImageView ivPreview;
    private Spinner spinnerKondisi;
    private Button btnKamera, btnKirim;
    private TextView tvStatus;

    // Variabel Data
    private String assetId;
    private String currentPhotoPath; // Path foto asli (resolusi penuh)
    private File photoFile; // File object untuk foto

    // Konstanta Request Code
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // 1. Ambil ID Asset dari Intent
        assetId = getIntent().getStringExtra("ASSET_ID");
        if (assetId == null) {
            Toast.makeText(this, "Data Aset Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2. Inisialisasi View
        ivPreview = findViewById(R.id.ivPreview);
        spinnerKondisi = findViewById(R.id.spinnerKondisi);
        btnKamera = findViewById(R.id.btnKamera);
        btnKirim = findViewById(R.id.btnKirimLaporan);

        // Setup Spinner (Pilihan Kondisi)
        setupSpinner();

        // 3. Listener Tombol
        btnKamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bukaKamera();
            }
        });

        btnKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prosesUpload();
            }
        });
    }

    private void setupSpinner() {
        // Opsi kondisi yang bisa dipilih
        String[] options = {"RUSAK", "HILANG", "DIPERBAIKI"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKondisi.setAdapter(adapter);
    }

    // --- BAGIAN 1: LOGIKA KAMERA ---

    private void bukaKamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Pastikan ada aplikasi kamera yang bisa menangani intent ini
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                // Buat file kosong untuk menampung gambar
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Gagal membuat file penyimpanan", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lanjutkan jika file berhasil dibuat
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "Kamera tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
    }

    // Membuat nama file unik berdasarkan waktu
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Menerima hasil dari Kamera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Foto berhasil diambil, simpan path-nya
            currentPhotoPath = photoFile.getAbsolutePath();

            // Tampilkan preview kecil (agar tidak membebani memori UI)
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            ivPreview.setImageBitmap(bitmap);
        }
    }

    // --- BAGIAN 2: LOGIKA UPLOAD & KOMPRESI ---

    private void prosesUpload() {
        if (currentPhotoPath == null) {
            Toast.makeText(this, "Silakan ambil foto bukti terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Matikan tombol agar tidak diklik ganda
        btnKirim.setEnabled(false);
        btnKirim.setText("Mengirim...");

        // 1. Kompresi Gambar (Penting agar upload cepat)
        File finalFile = compressImage(currentPhotoPath);

        // 2. Siapkan Data Multipart untuk Retrofit
        // Bagian Teks: ID Asset & Kondisi
        RequestBody reqId = RequestBody.create(MediaType.parse("text/plain"), assetId);
        String selectedKondisi = spinnerKondisi.getSelectedItem().toString();
        RequestBody reqKondisi = RequestBody.create(MediaType.parse("text/plain"), selectedKondisi);

        // Bagian File Gambar
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), finalFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image_file", finalFile.getName(), reqFile);

        // 3. Kirim ke API
        Call<ResponseBody> call = ApiClient.getApi().laporKerusakan(reqId, reqKondisi, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnKirim.setEnabled(true);
                btnKirim.setText("Kirim Laporan");

                if (response.isSuccessful()) {
                    Toast.makeText(ReportActivity.this, "Laporan Berhasil Terkirim!", Toast.LENGTH_LONG).show();
                    // Tutup halaman report dan kembali ke detail/list
                    finish();
                } else {
                    Toast.makeText(ReportActivity.this, "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnKirim.setEnabled(true);
                btnKirim.setText("Kirim Laporan");
                Log.e("UploadError", t.getMessage());
                Toast.makeText(ReportActivity.this, "Koneksi Bermasalah: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fungsi Helper: Resize & Kompres Gambar
    private File compressImage(String path) {
        // Decode file gambar asli ke Bitmap
        Bitmap original = BitmapFactory.decodeFile(path);

        // Tentukan ukuran maksimal (misal 1024px lebar atau tinggi)
        int maxWidth = 1024;
        int maxHeight = 1024;

        int width = original.getWidth();
        int height = original.getHeight();

        // Hitung skala resize
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) (maxWidth / ratioBitmap);
        }

        // Lakukan Resize
        Bitmap resized = Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);

        // Simpan Bitmap yang sudah di-resize ke file sementara di Cache
        File compressedFile = new File(getCacheDir(), "upload_compressed.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(compressedFile);
            // Kompresi JPEG kualitas 80% (Cukup bagus, ukuran kecil)
            resized.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Jika gagal kompresi, kembalikan file asli
            return new File(path);
        }

        return compressedFile;
    }
}