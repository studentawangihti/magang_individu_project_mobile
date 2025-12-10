package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Pastikan nama layout XML sesuai

        // Inisialisasi View
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Event Klik Tombol Login
        btnLogin.setOnClickListener(v -> {
            String u = etUsername.getText().toString();
            String p = etPassword.getText().toString();

            if(u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Username dan Password harus diisi!", Toast.LENGTH_SHORT).show();
            } else {
                loginProses(u, p);
            }
        });
    }

    private void loginProses(String username, String password) {
        // Memanggil API Login
        ApiClient.getApi().login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();

                    if (res.getStatus() == 200) {
                        // Login BERHASIL
                        String namaUser = res.getData().getNamaLengkap();
                        String jabatan = res.getData().getJabatan();

                        Toast.makeText(MainActivity.this, "Login Sukses!", Toast.LENGTH_SHORT).show();

                        // Pindah ke Dashboard & Bawa Data
                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                        intent.putExtra("NAMA", namaUser);     // Kirim Nama
                        intent.putExtra("JABATAN", jabatan);   // Kirim Jabatan
                        startActivity(intent);
                        finish(); // Agar user tidak bisa kembali ke login pakai tombol back
                    } else {
                        // Login GAGAL (Password Salah / User Tidak Ditemukan)
                        Toast.makeText(MainActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Gagal menghubungi server: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Error Koneksi (WiFi mati, IP salah, Server mati)
                Toast.makeText(MainActivity.this, "Error Koneksi: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}