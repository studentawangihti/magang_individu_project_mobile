package com.tmfw.inventory_app.model;

import com.google.gson.annotations.SerializedName;

public class DashboardStatsResponse {
    @SerializedName("status")
    private int status;

    @SerializedName("data")
    private StatsData data;

    public int getStatus() { return status; }
    public StatsData getData() { return data; }

    // Class internal untuk menampung objek "data"
    public class StatsData {
        @SerializedName("total_asset")
        private int totalAsset;

        @SerializedName("total_rusak")
        private int totalRusak;

        @SerializedName("my_ticket")
        private int myTicket;

        public int getTotalAsset() { return totalAsset; }
        public int getTotalRusak() { return totalRusak; }
        public int getMyTicket() { return myTicket; }
    }
}