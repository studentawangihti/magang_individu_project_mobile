package com.tmfw.inventory_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.api.ApiEndpoint;
import com.tmfw.inventory_app.model.HistoryItem;
import com.tmfw.inventory_app.model.HistoryResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;

    // Data Management
    private List<HistoryItem> masterList = new ArrayList<>();
    private List<HistoryItem> displayedList = new ArrayList<>();

    private ImageView btnBack, btnFilter;
    private EditText etSearch;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        String userId = getIntent().getStringExtra("USER_ID");
        if (userId == null) { finish(); return; }

        // Init View
        rvHistory = findViewById(R.id.rvHistory);
        btnBack = findViewById(R.id.btnBackHistory);
        btnFilter = findViewById(R.id.btnFilterHistory);
        etSearch = findViewById(R.id.etSearchHistory);

        // Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(this, displayedList);
        rvHistory.setAdapter(adapter);

        // Listeners
        btnBack.setOnClickListener(v -> finish());

        // Search Live Logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter Dialog
        btnFilter.setOnClickListener(v -> showFilterDialog());

        loadHistoryData(userId);
    }

    private void loadHistoryData(String userId) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat riwayat...");
        progressDialog.show();

        ApiEndpoint api = ApiClient.getClient().create(ApiEndpoint.class);
        Call<HistoryResponse> call = api.getHistory(userId);

        call.enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().getStatus() == 200) {
                    List<HistoryItem> data = response.body().getData();
                    if (data != null) {
                        masterList.clear();
                        masterList.addAll(data);

                        // Reset Display
                        displayedList.clear();
                        displayedList.addAll(masterList);
                        adapter.updateList(displayedList);
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Data Kosong", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(HistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- FITUR SEARCH ---
    private void filterSearch(String query) {
        displayedList.clear();
        if (query.isEmpty()) {
            displayedList.addAll(masterList);
        } else {
            for (HistoryItem item : masterList) {
                // Cari berdasarkan Nama Aset atau Deskripsi
                if (item.getAssetName().toLowerCase().contains(query.toLowerCase()) ||
                        item.getDeskripsi().toLowerCase().contains(query.toLowerCase())) {
                    displayedList.add(item);
                }
            }
        }
        adapter.updateList(displayedList);
    }

    // --- FITUR FILTER DIALOG ---
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Kita gunakan layout dialog yang sama (dialog_filter.xml)
        // karena isinya mirip (Kategori, Kondisi/Status).
        View view = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        // Mapping View Dialog
        Spinner spinKategori = view.findViewById(R.id.spinKategori);
        Spinner spinStatus = view.findViewById(R.id.spinKondisi); // Kita pakai slot Kondisi untuk Status Tiket
        Spinner spinTahun = view.findViewById(R.id.spinTahun); // Slot ini kita disable/hide saja jika tidak butuh

        TextView tvLabelStatus = view.findViewById(R.id.spinKondisi).getRootView().findViewWithTag("lblKondisi"); // (Opsional kalau mau ganti text label via code)

        // Sembunyikan Filter Tahun (Tidak relevan untuk history tiket)
        spinTahun.setVisibility(View.GONE);
        view.findViewById(R.id.spinTahun).setVisibility(View.GONE); // Labelnya mungkin masih ada manual di XML

        Button btnTerapkan = view.findViewById(R.id.btnTerapkan);
        Button btnReset = view.findViewById(R.id.btnReset);

        // 1. Setup Spinner Kategori (Ambil Unik dari Data Master)
        Set<String> catSet = new HashSet<>();
        for (HistoryItem h : masterList) {
            if(h.getKategori() != null) catSet.add(h.getKategori());
        }
        List<String> listKat = new ArrayList<>(catSet);
        Collections.sort(listKat);
        listKat.add(0, "Semua Kategori");
        ArrayAdapter<String> adapKat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listKat);
        spinKategori.setAdapter(adapKat);

        // 2. Setup Spinner Status (Manual mapping)
        // 0=Menunggu, 1=Proses, 2=Selesai
        String[] listStatus = {"Semua Status", "MENUNGGU", "DIPROSES", "SELESAI"};
        ArrayAdapter<String> adapStat = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listStatus);
        spinStatus.setAdapter(adapStat);

        // 3. Action Buttons
        btnTerapkan.setOnClickListener(v -> {
            String selKat = spinKategori.getSelectedItem().toString();
            String selStat = spinStatus.getSelectedItem().toString();

            terapkanFilter(selKat, selStat);
            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            filterSearch(""); // Reset Search
            etSearch.setText("");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void terapkanFilter(String kategori, String statusLabel) {
        displayedList.clear();

        // Konversi Label Status ke Kode (0, 1, 2)
        String statusCode = "";
        if(statusLabel.equals("MENUNGGU")) statusCode = "0";
        else if(statusLabel.equals("DIPROSES")) statusCode = "1";
        else if(statusLabel.equals("SELESAI")) statusCode = "2";

        for (HistoryItem item : masterList) {
            // Cek Kategori
            boolean matchKat = kategori.equals("Semua Kategori") ||
                    (item.getKategori() != null && item.getKategori().equalsIgnoreCase(kategori));

            // Cek Status
            boolean matchStat = statusLabel.equals("Semua Status") || item.getStatus().equals(statusCode);

            if (matchKat && matchStat) {
                displayedList.add(item);
            }
        }
        adapter.updateList(displayedList);
        Toast.makeText(this, "Hasil: " + displayedList.size() + " Laporan", Toast.LENGTH_SHORT).show();
    }
}