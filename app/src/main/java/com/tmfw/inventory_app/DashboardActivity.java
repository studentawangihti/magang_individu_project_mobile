package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button btnListAsset = findViewById(R.id.btnListAsset);
        Button btnTest1 = findViewById(R.id.btnTest1);
        Button btnTest2 = findViewById(R.id.btnTest2);

        // Menu 1: Pindah ke halaman List Asset
        btnListAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AssetListActivity.class);
                startActivity(intent);
            }
        });

        // Menu 2
        btnTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Menu Test 1 dipilih", Toast.LENGTH_SHORT).show();
            }
        });

        // Menu 3
        btnTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "Menu Test 2 dipilih", Toast.LENGTH_SHORT).show();
            }
        });
    }
}