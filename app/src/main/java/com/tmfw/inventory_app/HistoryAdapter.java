package com.tmfw.inventory_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tmfw.inventory_app.model.HistoryItem;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<HistoryItem> listHistory;

    public HistoryAdapter(Context context, List<HistoryItem> listHistory) {
        this.context = context;
        this.listHistory = listHistory;
    }

    public void updateList(List<HistoryItem> newList) {
        this.listHistory = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = listHistory.get(position);

        // 1. Set Teks Utama
        holder.tvAsset.setText(item.getAssetName());
        holder.tvDate.setText(item.getTanggal());
        holder.tvDesc.setText(item.getDeskripsi());

        // 2. Logika Status (Warna & Teks)
        // 0=Baru, 1=Proses, 2=Selesai (Sesuai kode PHP Anda)
        String st = item.getStatus();
        if ("0".equals(st)) {
            holder.tvStatus.setText("MENUNGGU");
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F")); // Merah
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else if ("1".equals(st)) {
            holder.tvStatus.setText("DIPROSES");
            holder.tvStatus.setTextColor(Color.parseColor("#F57C00")); // Orange
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
        } else if ("2".equals(st)) {
            holder.tvStatus.setText("SELESAI");
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C")); // Hijau
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            holder.tvStatus.setText("STATUS: " + st);
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        // fitur tampil gambar glide

        if (item.getFotoUrl() != null && !item.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getFotoUrl())                // URL dari JSON
                    .placeholder(R.drawable.ic_launcher_background) // Tampil saat loading
                    .error(R.drawable.ic_launcher_foreground)       // Tampil jika link error/gagal
                    .centerCrop()                           // Potong gambar agar rapi (kotak)
                    .into(holder.imgHistory);
        } else {
            // Jika user tidak upload foto, tampilkan icon default
            holder.imgHistory.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Di dalam onBindViewHolder
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, HistoryDetailActivity.class);
            intent.putExtra("ITEM_HISTORY", item); // Mengirim seluruh objek
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listHistory != null ? listHistory.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAsset, tvDate, tvStatus, tvDesc;
        ImageView imgHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAsset = itemView.findViewById(R.id.tvHistoryAsset);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvDesc = itemView.findViewById(R.id.tvHistoryDesc);
            imgHistory = itemView.findViewById(R.id.imgHistory);
        }
    }
}