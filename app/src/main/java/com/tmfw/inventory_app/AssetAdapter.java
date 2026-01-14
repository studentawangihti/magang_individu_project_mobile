package com.tmfw.inventory_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tmfw.inventory_app.model.Asset;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

    private List<Asset> assetList;
    private Context context;

    public AssetAdapter(Context context, List<Asset> assetList) {
        this.context = context;
        this.assetList = assetList;
    }

    public void updateList(List<Asset> newList) {
        this.assetList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asset asset = assetList.get(position);

        // 1. Set Data Dasar (Variabel sudah disesuaikan dengan ID XML)
        holder.tvNamaBarang.setText(asset.getNama());
        holder.tvKodeBarang.setText("Kode: " + asset.getKode());

        // 2. Set Data Baru
        holder.tvTahun.setText(asset.getTahun());
        holder.tvStok.setText(asset.getStok() + " Unit");

        // 3. Format Rupiah
        holder.tvHarga.setText(formatRupiah(asset.getHarga()));

        // 4. Logika Warna Kondisi
        holder.tvKondisi.setText(asset.getKondisi());
        if ("RUSAK".equalsIgnoreCase(asset.getKondisi())) {
            holder.tvKondisi.setTextColor(Color.RED);
            holder.tvKondisi.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else {
            holder.tvKondisi.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvKondisi.setBackgroundColor(Color.parseColor("#E8F5E9"));
        }

        // 5. Logika Grouping Header
        String currentCategory = asset.getKategori();
        String previousCategory = "";

        if (position > 0) {
            previousCategory = assetList.get(position - 1).getKategori();
        }

        if (currentCategory != null && (position == 0 || !currentCategory.equals(previousCategory))) {
            holder.tvCategoryHeader.setVisibility(View.VISIBLE);
            holder.tvCategoryHeader.setText(currentCategory);
        } else {
            holder.tvCategoryHeader.setVisibility(View.GONE);
        }

        // 6. event klik
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke AssetDetailActivity
                android.content.Intent intent = new android.content.Intent(context, AssetDetailActivity.class);

                // Bawa ID Aset sebagai 'bekal'
                intent.putExtra("ASSET_ID", asset.getId());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assetList != null ? assetList.size() : 0;
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        // REVISI: Nama variabel disamakan persis dengan ID di item_asset.xml
        TextView tvNamaBarang, tvKodeBarang, tvKondisi, tvCategoryHeader;
        TextView tvTahun, tvStok, tvHarga;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Mapping ID sekarang konsisten (nama variabel == nama ID)
            tvNamaBarang = itemView.findViewById(R.id.tvNamaBarang);
            tvKodeBarang = itemView.findViewById(R.id.tvKodeBarang);
            tvCategoryHeader = itemView.findViewById(R.id.tvCategoryHeader);
            tvKondisi = itemView.findViewById(R.id.tvKondisi);

            tvTahun = itemView.findViewById(R.id.tvTahun);
            tvStok = itemView.findViewById(R.id.tvStok);
            tvHarga = itemView.findViewById(R.id.tvHarga);
        }
    }
}