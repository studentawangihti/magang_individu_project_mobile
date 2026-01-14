package com.tmfw.inventory_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssetAdapter adapter;

    // Data Utama (Master) & Data Tampil
    private List<Asset> masterList = new ArrayList<>();
    private List<Asset> displayedList = new ArrayList<>();

    private EditText etSearch;
    private ImageView btnBack, btnFilter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_list);

        // 1. Inisialisasi View
        recyclerView = findViewById(R.id.rvAsset);
        etSearch = findViewById(R.id.etSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFilter = findViewById(R.id.btnFilter); // Tombol Baru

        // 2. Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssetAdapter(this, displayedList);
        recyclerView.setAdapter(adapter);

        // 3. Setup Listeners
        btnBack.setOnClickListener(v -> finish());

        // Listener Search Live
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString()); // Filter Search
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener Tombol Filter
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // 4. Panggil Data
        loadDataAsset();
    }

    private void loadDataAsset() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengambil data aset...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<AssetResponse> call = api.getAssets();

        call.enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus() == 200) {
                        masterList = response.body().getData(); // Simpan ke Master
                        displayedList.clear();
                        displayedList.addAll(masterList); // Tampilkan Semua Awalnya
                        adapter.updateList(displayedList);
                    } else {
                        Toast.makeText(AssetListActivity.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AssetListActivity.this, "Gagal ambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(AssetListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- LOGIKA FILTER UTAMA ---

    // 1. Fungsi Search Text
    private void filterData(String query) {
        displayedList.clear();
        if (query.isEmpty()) {
            displayedList.addAll(masterList);
        } else {
            for (Asset item : masterList) {
                if (item.getNama().toLowerCase().contains(query.toLowerCase()) ||
                        item.getKode().toLowerCase().contains(query.toLowerCase())) {
                    displayedList.add(item);
                }
            }
        }
        adapter.updateList(displayedList);
    }

    // 2. Fungsi Filter Dialog
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        Spinner spinKategori = view.findViewById(R.id.spinKategori);
        Spinner spinKondisi = view.findViewById(R.id.spinKondisi);
        Spinner spinTahun = view.findViewById(R.id.spinTahun);
        Button btnTerapkan = view.findViewById(R.id.btnTerapkan);
        Button btnReset = view.findViewById(R.id.btnReset);

        // --- SIAPKAN DATA SPINNER SECARA OTOMATIS ---

        // A. Spinner Kategori (Ambil Unik dari Data Master)
        Set<String> catSet = new HashSet<>();
        for (Asset a : masterList) { if(a.getKategori() != null) catSet.add(a.getKategori()); }
        List<String> listKategori = new ArrayList<>(catSet);
        Collections.sort(listKategori);
        listKategori.add(0, "Semua Kategori"); // Pilihan Default
        ArrayAdapter<String> adapterKat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listKategori);
        spinKategori.setAdapter(adapterKat);

        // B. Spinner Kondisi (Manual)
        String[] listKondisi = {"Semua Kondisi", "BAIK", "RUSAK", "HILANG", "DIPERBAIKI"};
        ArrayAdapter<String> adapterKon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listKondisi);
        spinKondisi.setAdapter(adapterKon);

        // C. Spinner Tahun (Ambil Unik dari Data Master)
        Set<String> yearSet = new HashSet<>();
        for (Asset a : masterList) { if(a.getTahun() != null) yearSet.add(a.getTahun()); }
        List<String> listTahun = new ArrayList<>(yearSet);
        Collections.sort(listTahun, Collections.reverseOrder()); // Tahun terbaru diatas
        listTahun.add(0, "Semua Tahun");
        ArrayAdapter<String> adapterThn = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listTahun);
        spinTahun.setAdapter(adapterThn);

        // --- AKSI TOMBOL ---

        btnTerapkan.setOnClickListener(v -> {
            String selKat = spinKategori.getSelectedItem().toString();
            String selKon = spinKondisi.getSelectedItem().toString();
            String selThn = spinTahun.getSelectedItem().toString();

            terapkanFilter(selKat, selKon, selThn);
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            // Reset ke awal
            displayedList.clear();
            displayedList.addAll(masterList);
            adapter.updateList(displayedList);
            dialog.dismiss();
            Toast.makeText(this, "Filter Direset", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void terapkanFilter(String kategori, String kondisi, String tahun) {
        displayedList.clear();

        for (Asset item : masterList) {
            boolean matchKategori = kategori.equals("Semua Kategori") || item.getKategori().equalsIgnoreCase(kategori);
            boolean matchKondisi = kondisi.equals("Semua Kondisi") || item.getKondisi().equalsIgnoreCase(kondisi);
            boolean matchTahun = tahun.equals("Semua Tahun") || item.getTahun().equalsIgnoreCase(tahun);

            // Item harus cocok dengan SEMUA kriteria (AND logic)
            if (matchKategori && matchKondisi && matchTahun) {
                displayedList.add(item);
            }
        }

        adapter.updateList(displayedList);

        if (displayedList.isEmpty()) {
            Toast.makeText(this, "Tidak ada aset yang cocok", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Menampilkan " + displayedList.size() + " aset", Toast.LENGTH_SHORT).show();
        }
    }
}