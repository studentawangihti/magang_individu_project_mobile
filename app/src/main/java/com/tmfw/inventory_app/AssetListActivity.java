package com.tmfw.inventory_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.Asset;
import com.tmfw.inventory_app.model.AssetResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssetAdapter adapter;
    private SearchView searchView;
    private Button btnFilter;

    // Data Master
    private List<Asset> fullAssetList = new ArrayList<>();

    // Status Filter Aktif
    private String activeSearch = "";
    private String activeCategory = "Semua";
    private String activeCondition = "Semua";
    private String activeYear = "Semua";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_list);

        recyclerView = findViewById(R.id.recyclerViewAsset);
        searchView = findViewById(R.id.searchViewAsset);
        btnFilter = findViewById(R.id.btnFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDataAsset();

        // SEARCH
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                activeSearch = newText;
                combineFilterAndSearch();
                return true;
            }
        });

        // FILTER BUTTON
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    // =======================
    // FILTER DIALOG (SPINNER)
    // =======================
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        Spinner spKategori = dialogView.findViewById(R.id.spinnerKategori);
        Spinner spKondisi  = dialogView.findViewById(R.id.spinnerKondisi);
        Spinner spTahun    = dialogView.findViewById(R.id.spinnerTahun);
        Button btnTerapkan = dialogView.findViewById(R.id.btnTerapkan);
        Button btnReset    = dialogView.findViewById(R.id.btnReset);

        setupSpinner(spKategori, getUniqueList("kategori"), activeCategory);
        setupSpinner(spKondisi, getUniqueList("kondisi"), activeCondition);
        setupSpinner(spTahun, getUniqueList("tahun"), activeYear);

        btnTerapkan.setOnClickListener(v -> {
            activeCategory = spKategori.getSelectedItem().toString();
            activeCondition = spKondisi.getSelectedItem().toString();
            activeYear = spTahun.getSelectedItem().toString();

            combineFilterAndSearch();
            dialog.dismiss();

            if (!activeCategory.equals("Semua") ||
                    !activeCondition.equals("Semua") ||
                    !activeYear.equals("Semua")) {
                btnFilter.setText("Filter Aktif");
            } else {
                btnFilter.setText("Filter");
            }
        });

        btnReset.setOnClickListener(v -> {
            activeCategory = "Semua";
            activeCondition = "Semua";
            activeYear = "Semua";

            combineFilterAndSearch();
            dialog.dismiss();
            btnFilter.setText("Filter");
        });

        dialog.show();
    }

    // =======================
    // FILTER + SEARCH LOGIC
    // =======================
    private void combineFilterAndSearch() {
        List<Asset> filteredList = new ArrayList<>();

        for (Asset item : fullAssetList) {

            boolean matchSearch =
                    item.getNama().toLowerCase().contains(activeSearch.toLowerCase()) ||
                            item.getKode().toLowerCase().contains(activeSearch.toLowerCase());

            boolean matchCategory =
                    activeCategory.equals("Semua") ||
                            item.getKategori().equalsIgnoreCase(activeCategory);

            boolean matchCondition =
                    activeCondition.equals("Semua") ||
                            item.getKondisi().equalsIgnoreCase(activeCondition);

            boolean matchYear =
                    activeYear.equals("Semua") ||
                            (item.getTahun() != null &&
                                    item.getTahun().equalsIgnoreCase(activeYear));

            if (matchSearch && matchCategory && matchCondition && matchYear) {
                filteredList.add(item);
            }
        }

        sortAssetsByCategory(filteredList);

        if (adapter != null) {
            adapter.updateList(filteredList);
        }
    }

    // =======================
    // API LOAD DATA
    // =======================
    private void loadDataAsset() {
        ApiClient.getApi().getAssets().enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call,
                                   Response<AssetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullAssetList = response.body().getData();
                    sortAssetsByCategory(fullAssetList);
                    adapter = new AssetAdapter(AssetListActivity.this, fullAssetList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(AssetListActivity.this,
                            "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                Toast.makeText(AssetListActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =======================
    // SORTING
    // =======================
    private void sortAssetsByCategory(List<Asset> list) {
        Collections.sort(list, new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                String k1 = o1.getKategori() != null ? o1.getKategori() : "";
                String k2 = o2.getKategori() != null ? o2.getKategori() : "";
                return k1.compareToIgnoreCase(k2);
            }
        });
    }

    // =======================
    // HELPER
    // =======================
    private List<String> getUniqueList(String type) {
        Set<String> set = new HashSet<>();
        set.add("Semua");

        for (Asset item : fullAssetList) {
            if (type.equals("kategori") && item.getKategori() != null)
                set.add(item.getKategori());

            if (type.equals("kondisi") && item.getKondisi() != null)
                set.add(item.getKondisi());

            if (type.equals("tahun") && item.getTahun() != null)
                set.add(item.getTahun());
        }

        List<String> list = new ArrayList<>(set);
        Collections.sort(list);
        list.remove("Semua");
        list.add(0, "Semua");
        return list;
    }

    private void setupSpinner(Spinner spinner,
                              List<String> data,
                              String currentValue) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (data.contains(currentValue)) {
            spinner.setSelection(data.indexOf(currentValue));
        }
    }
}
