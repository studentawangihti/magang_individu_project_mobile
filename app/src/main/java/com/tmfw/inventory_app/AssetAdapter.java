package com.tmfw.inventory_app; // Sesuaikan package Anda

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tmfw.inventory_app.model.Asset;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

    private List<Asset> assetList;
    private Context context;

    public AssetAdapter(Context context, List<Asset> assetList) {
        this.context = context;
        this.assetList = assetList;
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

        holder.tvNama.setText(asset.getNama());
        holder.tvKode.setText("Kode: " + asset.getKode());
        holder.tvKondisi.setText(asset.getKondisi());

        // Ubah warna teks berdasarkan kondisi
        if ("RUSAK".equalsIgnoreCase(asset.getKondisi())) {
            holder.tvKondisi.setTextColor(Color.RED);
        } else {
            holder.tvKondisi.setTextColor(Color.GREEN); // Atau warna hijau tua
        }
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKode, tvKondisi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaBarang);
            tvKode = itemView.findViewById(R.id.tvKodeBarang);
            tvKondisi = itemView.findViewById(R.id.tvKondisi);
        }
    }
}