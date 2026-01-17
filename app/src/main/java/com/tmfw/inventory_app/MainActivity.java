package com.tmfw.inventory_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// --- [PENTING] Import Library Material Design ---
import com.google.android.material.textfield.TextInputEditText;

import com.tmfw.inventory_app.api.ApiClient;
import com.tmfw.inventory_app.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    // Menggunakan TextInputEditText sesuai layout XML baru
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inisialisasi View
        // ID-nya tetap sama seperti di XML (etUsername, etPassword, btnLogin)
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);

        // 2. Event Klik Tombol Login
        btnLogin.setOnClickListener(v -> {
            // Ambil teks dan hapus spasi di awal/akhir (trim)
            String u = etUsername.getText().toString().trim();
            String p = etPassword.getText().toString().trim();

            if(u.isEmpty()) {
                etUsername.setError("Username wajib diisi");
                etUsername.requestFocus();
            } else if (p.isEmpty()) {
                etPassword.setError("Password wajib diisi");
                etPassword.requestFocus();
            } else {
                loginProses(u, p);
            }
        });
    }

    private void loginProses(String username, String password) {
        // Tampilkan loading sederhana (opsional, biar user tau sedang proses)
        btnLogin.setEnabled(false);
        btnLogin.setText("Loading...");

        // Memanggil API Login
        ApiClient.getClient().create(com.tmfw.inventory_app.api.ApiEndpoint.class).login(username, password)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("MASUK");

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse res = response.body();

                            if (res.getStatus() == 200) {
                                // Login BERHASIL
                                // Ambil data dari response (pastikan getter di Model User.java sesuai)
                                String namaUser = res.getData().getNamaLengkap(); // Ambil Nama Asli
                                String jabatan  = res.getData().getJabatan();
                                String role     = res.getData().getRole();     // Misal: "Admin" atau "Staff"
                                String userId   = res.getData().getUserId();

                                Toast.makeText(MainActivity.this, "Selamat Datang, " + namaUser, Toast.LENGTH_SHORT).show();

                                // Pindah ke Dashboard & Bawa Data
                                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);

                                intent.putExtra("NAMA", response.body().getData().getNamaLengkap());
                                intent.putExtra("JABATAN", response.body().getData().getJabatan());
                                intent.putExtra("ROLE", response.body().getData().getRole()); // <--- Kirim Role
                                intent.putExtra("USER_ID", response.body().getData().getUserId());     // ID User (Penting untuk transaksi)

                                startActivity(intent);
                                finish(); // Tutup halaman login agar tidak bisa di-back
                            } else {
                                // Login GAGAL (Password Salah)
                                Toast.makeText(MainActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Gagal menghubungi server", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("MASUK");

                        // Error Koneksi
                        Toast.makeText(MainActivity.this, "Koneksi Bermasalah: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}