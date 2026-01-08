package com.tmfw.inventory_app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    // Komponen UI (CardView)
    CardView cardListAsset, cardLapor, cardLainnya, cardLogout;

    // Data User
    String roleUser, namaUser, jabatanUser;

    // Data untuk Spinner Dialog
    private List<Asset> listAsset = new ArrayList<>();
    private List<String> listNamaAsset = new ArrayList<>();

    // Variabel untuk Upload
    private String selectedAssetId = "";
    private File photoFile;
    private ImageView tempImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Ambil Data dari Intent (Kiriman Login)
        if(getIntent() != null) {
            roleUser = getIntent().getStringExtra("ROLE");
            namaUser = getIntent().getStringExtra("NAMA");
            jabatanUser = getIntent().getStringExtra("JABATAN");
        }

        // 2. Inisialisasi CardView
        cardListAsset = findViewById(R.id.cardListAsset);
        cardLapor = findViewById(R.id.cardLapor);
        cardLainnya = findViewById(R.id.cardLainnya);
        cardLogout = findViewById(R.id.cardLogout);

        // 3. Atur Tampilan Menu Berdasarkan Role
        aturMenuSesuaiRole();

        // 4. Ambil data aset secara background
        fetchDataAssets();

        // --- SETUP LISTENERS ---

        // Menu 1: List Aset
        cardListAsset.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, AssetListActivity.class));
        });

        // Menu 2: Lapor Cepat (Dialog)
        cardLapor.setOnClickListener(v -> {
            showDialogLaporDashboard();
        });

        // Menu 3: Lainnya
        cardLainnya.setOnClickListener(v -> {
            Toast.makeText(this, "Profil: " + namaUser + "\nJabatan: " + jabatanUser, Toast.LENGTH_SHORT).show();
        });

        // Menu 4: Logout
        cardLogout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(DashboardActivity.this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        });
    }

    // --- FUNGSI KHUSUS ROLE (LOGIKA BISNIS) ---
    private void aturMenuSesuaiRole() {
        if (roleUser == null) return;

        // Role ID dari SQL:
        // 01.01 = Superadmin
        // 02.01 = Magang

        // Aturan 1: Magang tidak bisa melihat menu "Lainnya" (Contoh)
        if (roleUser.equals("02.01")) {
            cardLainnya.setVisibility(View.GONE);
        }

        // Aturan 2: Hanya Superadmin (01.01) yang bisa melihat List Aset Lengkap
        // Jika user BUKAN superadmin, sembunyikan list asset
        if (!roleUser.equals("01.01")) {
            // cardListAsset.setVisibility(View.GONE); // Uncomment jika ingin mengaktifkan aturan ini
        }

        // Aturan 3: Semua role boleh Lapor Kerusakan (Default visible)
    }

    // --- FUNGSI 1: AMBIL DATA ASET ---
    private void fetchDataAssets() {
        ApiClient.getApi().getAssets().enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listAsset = response.body().getData();
                    listNamaAsset.clear();
                    for (Asset a : listAsset) {
                        listNamaAsset.add(a.getNama() + " (" + a.getKode() + ")");
                    }
                }
            }
            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                // Log error
            }
        });
    }

    // --- FUNGSI 2: TAMPILKAN DIALOG LAPOR ---
    private void showDialogLaporDashboard() {
        if (listAsset.isEmpty()) {
            Toast.makeText(this, "Sedang memuat data aset...", Toast.LENGTH_SHORT).show();
            fetchDataAssets();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_lapor_dashboard, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        Spinner spAsset = dialogView.findViewById(R.id.spinnerPilihAsset);
        Spinner spKondisi = dialogView.findViewById(R.id.spinnerKondisi);
        EditText etJudul = dialogView.findViewById(R.id.etJudul);
        EditText etDeskripsi = dialogView.findViewById(R.id.etDeskripsi);
        Button btnFoto = dialogView.findViewById(R.id.btnPilihFoto);
        Button btnKirim = dialogView.findViewById(R.id.btnKirim);
        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        ImageView ivPreview = dialogView.findViewById(R.id.ivPreview);

        ArrayAdapter<String> adapterAsset = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listNamaAsset);
        adapterAsset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAsset.setAdapter(adapterAsset);

        spAsset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAssetId = listAsset.get(position).getId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String[] opsiKondisi = {"RUSAK", "PERBAIKAN", "HILANG", "BAIK"};
        ArrayAdapter<String> adapterKondisi = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opsiKondisi);
        adapterKondisi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKondisi.setAdapter(adapterKondisi);

        btnFoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 200);
            tempImageView = ivPreview;
        });

        btnKirim.setOnClickListener(v -> {
            String judul = etJudul.getText().toString();
            String deskripsi = etDeskripsi.getText().toString();
            String kondisiBaru = spKondisi.getSelectedItem().toString();

            if (selectedAssetId.isEmpty()) { Toast.makeText(this, "Pilih aset dulu!", Toast.LENGTH_SHORT).show(); return; }
            if (judul.isEmpty()) { Toast.makeText(this, "Isi judul laporan!", Toast.LENGTH_SHORT).show(); return; }

            uploadLaporan(selectedAssetId, judul, deskripsi, kondisiBaru, dialog);
        });

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- FUNGSI 3: HANDLE HASIL GAMBAR ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (tempImageView != null) {
                tempImageView.setVisibility(View.VISIBLE);
                tempImageView.setImageURI(selectedImage);
            }
            String realPath = getRealPathFromURI(selectedImage);
            if (realPath != null) photoFile = new File(realPath);
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    // --- FUNGSI 4: UPLOAD KE SERVER ---
    private void uploadLaporan(String assetId, String judul, String deskripsi, String kondisi, AlertDialog dialog) {
        // ID Pegawai (Idealnya diambil dari LoginResponse juga, disimpan di SharedPreference)
        String userId = "P000000000029"; // Hardcoded sementara

        RequestBody reqAssetId = RequestBody.create(MediaType.parse("text/plain"), assetId);
        RequestBody reqUserId = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody reqJudul = RequestBody.create(MediaType.parse("text/plain"), judul);
        RequestBody reqDesk = RequestBody.create(MediaType.parse("text/plain"), deskripsi);
        RequestBody reqKondisi = RequestBody.create(MediaType.parse("text/plain"), kondisi);

        MultipartBody.Part bodyGambar = null;
        if (photoFile != null) {
            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), photoFile);
            bodyGambar = MultipartBody.Part.createFormData("image_file", photoFile.getName(), reqFile);
        }

        ApiClient.getApi().laporKerusakan(reqAssetId, reqUserId, reqJudul, reqDesk, reqKondisi, bodyGambar)
                .enqueue(new Callback<AssetResponse>() {
                    @Override
                    public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(DashboardActivity.this, "Laporan Terkirim!", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            photoFile = null;
                        } else {
                            Toast.makeText(DashboardActivity.this, "Gagal: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<AssetResponse> call, Throwable t) {
                        Toast.makeText(DashboardActivity.this, "Error Jaringan", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}