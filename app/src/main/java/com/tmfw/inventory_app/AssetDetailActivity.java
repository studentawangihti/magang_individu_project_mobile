package com.tmfw.inventory_app;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.SingleAssetResponse;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetDetailActivity extends AppCompatActivity {

    private TextView tvName, tvCode, tvPrice, tvCondition;
    private TextView tvKategori, tvTahun, tvStok, tvSatuan, tvDeskripsi;
    private LinearLayout llSpecsContainer;
    private ImageView btnBack;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_detail);

        // 1. Inisialisasi View
        tvName = findViewById(R.id.tvDetailName);
        tvCode = findViewById(R.id.tvDetailCode);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvCondition = findViewById(R.id.tvDetailCondition);

        tvKategori = findViewById(R.id.tvDetailKategori);
        tvTahun = findViewById(R.id.tvDetailTahun);
        tvStok = findViewById(R.id.tvDetailStok);
        tvSatuan = findViewById(R.id.tvDetailSatuan);
        tvDeskripsi = findViewById(R.id.tvDetailDeskripsi);

        llSpecsContainer = findViewById(R.id.llSpecsContainer);
        btnBack = findViewById(R.id.btnBackDetail);

        // 2. Setup Tombol Kembali
        btnBack.setOnClickListener(v -> finish());

        // 3. Ambil ID dari Intent
        String assetId = getIntent().getStringExtra("ASSET_ID");

        if (assetId != null && !assetId.isEmpty()) {
            loadAssetDetail(assetId);
        } else {
            Toast.makeText(this, "ID Aset tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAssetDetail(String id) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang menghubungi server...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        android.util.Log.d("DEBUG_API", "Memulai Request ke API untuk ID: " + id);

        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<SingleAssetResponse> call = api.getAssetDetail(id);

        call.enqueue(new Callback<SingleAssetResponse>() {
            @Override
            public void onResponse(Call<SingleAssetResponse> call, Response<SingleAssetResponse> response) {
                progressDialog.dismiss();

                // 1. Cek Kode HTTP (Harus 200)
                android.util.Log.d("DEBUG_API", "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    // 2. Cek Status dari JSON (Harus 200)
                    int status = response.body().getStatus();
                    String message = response.body().getMessage();
                    android.util.Log.d("DEBUG_API", "Status JSON: " + status);
                    android.util.Log.d("DEBUG_API", "Pesan Server: " + message);

                    if (status == 200) {
                        Asset asset = response.body().getData();
                        if (asset != null) {
                            android.util.Log.d("DEBUG_API", "Data Aset Diterima. Nama: " + asset.getNama());

                            // Cek Detail Tambahan
                            if (asset.getDetailTambahan() != null) {
                                android.util.Log.d("DEBUG_API", "Jumlah Detail Item: " + asset.getDetailTambahan().size());
                            } else {
                                android.util.Log.e("DEBUG_API", "List Detail Tambahan NULL!");
                            }

                            displayData(asset);
                        } else {
                            android.util.Log.e("DEBUG_API", "Objek Asset NULL dalam Data");
                        }
                    } else {
                        Toast.makeText(AssetDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 3. Jika Error dari Server (404, 500, dll)
                    android.util.Log.e("DEBUG_API", "Response Gagal/Body Null");
                    try {
                        if (response.errorBody() != null) {
                            android.util.Log.e("DEBUG_API", "Error Body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AssetDetailActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SingleAssetResponse> call, Throwable t) {
                progressDialog.dismiss();
                // 4. Jika Gagal Koneksi (Internet mati / Typo URL / Parsing Error)
                android.util.Log.e("DEBUG_API", "ON FAILURE: " + t.getMessage());
                t.printStackTrace(); // Cetak error lengkap
                Toast.makeText(AssetDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayData(Asset asset) {
        // A. Set Data Utama
        tvName.setText(asset.getNama());
        tvCode.setText("Kode: " + asset.getKode());
        tvPrice.setText(formatRupiah(asset.getHarga()));

        tvKategori.setText(asset.getKategori());
        tvTahun.setText(asset.getTahun());
        tvStok.setText(asset.getStok());
        tvSatuan.setText(asset.getSatuan());

        // Cek Deskripsi
        if (asset.getDeskripsi() != null && !asset.getDeskripsi().isEmpty()) {
            tvDeskripsi.setText(asset.getDeskripsi());
        } else {
            tvDeskripsi.setText("-");
        }

        // B. Set Warna Kondisi
        String kondisi = asset.getKondisi();
        tvCondition.setText(kondisi);

        if ("RUSAK".equalsIgnoreCase(kondisi)) {
            tvCondition.setTextColor(Color.RED);
            tvCondition.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else if ("HILANG".equalsIgnoreCase(kondisi)) {
            tvCondition.setTextColor(Color.DKGRAY);
            tvCondition.setBackgroundColor(Color.parseColor("#EEEEEE"));
        } else {
            tvCondition.setTextColor(Color.parseColor("#2E7D32"));
            tvCondition.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }

        // C. Tampilkan Spesifikasi Tambahan & DEBUGGING
        llSpecsContainer.removeAllViews();

        List<Asset.DetailItem> listDetail = asset.getDetailTambahan();

        // --- MULAI DEBUGGING LOG (SAFE VERSION) ---
        if (listDetail == null) {
            android.util.Log.e("CEK_DATA", "DATA GAGAL: listDetail bernilai NULL (Cek Backend/Model)");
        } else if (listDetail.isEmpty()) {
            android.util.Log.w("CEK_DATA", "DATA KOSONG: listDetail size = 0 (Database kosong atau belum diinput)");
        } else {
            android.util.Log.d("CEK_DATA", "DATA SUKSES: Ditemukan " + listDetail.size() + " detail item.");
        }
        // --- SELESAI DEBUGGING LOG ---

        if (listDetail != null && !listDetail.isEmpty()) {
            for (Asset.DetailItem item : listDetail) {
                // Log per item agar tahu apa isinya
                android.util.Log.d("CEK_DATA", "Mencetak Item: " + item.getLabel() + " -> " + item.getValue());

                View specView = LayoutInflater.from(this).inflate(R.layout.item_specification, llSpecsContainer, false);

                TextView tvLabel = specView.findViewById(R.id.tvSpecLabel);
                TextView tvValue = specView.findViewById(R.id.tvSpecValue);

                tvLabel.setText(item.getLabel());
                tvValue.setText(item.getValue());

                llSpecsContainer.addView(specView);
            }
        } else {
            // Tampilan jika kosong
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Tidak ada spesifikasi khusus.");
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setTextSize(12);
            llSpecsContainer.addView(tvEmpty);
        }
    }

    private String formatRupiah(String nominal) {
        try {
            double value = Double.parseDouble(nominal);
            NumberFormat formatRp = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            return formatRp.format(value).replace("Rp", "Rp ");
        } catch (Exception e) {
            return "Rp 0";
        }
    }
}