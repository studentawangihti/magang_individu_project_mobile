package com.tmfw.inventory_app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.tmfw.inventory_app.model.HistoryItem;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        // 1. Ambil Data dari Intent
        HistoryItem item = (HistoryItem) getIntent().getSerializableExtra("ITEM_HISTORY");

        // 2. Setup View
        ImageView btnBack = findViewById(R.id.btnBackDetail);
        TextView tvStatus = findViewById(R.id.tvStatusTiket);
        TextView tvNama = findViewById(R.id.tvNamaAset);
        TextView tvTanggal = findViewById(R.id.tvTanggal);
        TextView tvDesc = findViewById(R.id.tvDeskripsi);
        ImageView imgBukti = findViewById(R.id.imgBuktiFull);

        btnBack.setOnClickListener(v -> finish());

        if (item != null) {
            tvNama.setText(item.getAssetName());
            tvTanggal.setText(item.getTanggal());
            tvDesc.setText(item.getDeskripsi());

            // Set Warna Status
            String st = item.getStatus();
            if ("0".equals(st)) {
                tvStatus.setText("MENUNGGU VERIFIKASI");
                tvStatus.setTextColor(Color.parseColor("#D32F2F"));
                tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
            } else if ("1".equals(st)) {
                tvStatus.setText("SEDANG DIPROSES");
                tvStatus.setTextColor(Color.parseColor("#F57C00"));
                tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
            } else if ("2".equals(st)) {
                tvStatus.setText("SELESAI DIPERBAIKI");
                tvStatus.setTextColor(Color.parseColor("#388E3C"));
                tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
            }

            // Load Foto Besar
            if (item.getFotoUrl() != null && !item.getFotoUrl().isEmpty()) {
                Glide.with(this)
                        .load(item.getFotoUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imgBukti);
            }
        }
    }
}