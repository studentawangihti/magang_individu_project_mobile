# SOP Git Multi-Remote – Project Mobile (Android)

**Tujuan**
Mengelola **1 project Android Studio (lokal)** yang di-*push* dan di-*pull* ke **2 repository GitHub berbeda** (organisasi & pribadi) secara aman, konsisten, dan profesional.

---

## 📁 Informasi Project

**Lokasi Project Lokal**
`C:\Users\acer\AndroidStudioProjects\Inventory_App`

**Repository Tujuan**

1. **Repo Organisasi (UTAMA / TIM)**
   [https://github.com/technomedic-magang/Inventory_App_Mobile.git](https://github.com/technomedic-magang/Inventory_App_Mobile.git)
2. **Repo Pribadi (BACKUP / PORTOFOLIO)**
   [https://github.com/studentawangihti/magang_individu_project_mobile.git](https://github.com/studentawangihti/magang_individu_project_mobile.git)

---

## 🏷️ Standar Nama Remote (WAJIB KONSISTEN)

* `origin`  → repo organisasi (Technomedic)
* `origin2` → repo pribadi

> ⚠️ Repo organisasi adalah **source of truth**.

---

## 🔧 SETUP AWAL (DILAKUKAN SEKALI SAJA)

### 1️⃣ Masuk ke folder project

```bash
cd C:\Users\acer\AndroidStudioProjects\Inventory_App
```

### 2️⃣ Inisialisasi Git (jika belum)

```bash
git init
```

### 3️⃣ Tambahkan remote repository

```bash
git remote add origin https://github.com/technomedic-magang/Inventory_App_Mobile.git
git remote add origin2 https://github.com/studentawangihti/magang_individu_project_mobile.git
```

### 4️⃣ Verifikasi remote

```bash
git remote -v
```

Output yang benar:

```text
origin   https://github.com/technomedic-magang/Inventory_App_Mobile.git (fetch)
origin   https://github.com/technomedic-magang/Inventory_App_Mobile.git (push)
origin2  https://github.com/studentawangihti/magang_individu_project_mobile.git (fetch)
origin2  https://github.com/studentawangihti/magang_individu_project_mobile.git (push)
```

---

## 🚀 SOP PUSH (UPLOAD PERUBAHAN)

### 🧠 Kapan Push?

* Setelah nambah fitur
* Setelah fix bug
* Setelah update UI
* Setelah refactor

---

### 1️⃣ Cek status perubahan

```bash
git status
```

### 2️⃣ Tambahkan file ke staging

```bash
git add .
```

### 3️⃣ Commit perubahan

```bash
git commit -m "Deskripsi perubahan singkat"
```

Contoh:

```bash
git commit -m "Add inventory list screen"
```

---

### 4️⃣ Push ke repo organisasi (WAJIB)

```bash
git push origin main
```

### 5️⃣ Push ke repo pribadi

```bash
git push origin2 main
```

✅ **WAJIB:** push ke **dua repo** agar tetap sinkron.

---

### ⚡ PUSH CEPAT (OPSIONAL)

```bash
git push origin main && git push origin2 main
```

---

## 🔄 SOP PULL (AMBIL UPDATE DARI TIM)

### 🧠 Prinsip Penting

* **Pull hanya dari repo organisasi (`origin`)**
* Repo pribadi **TIDAK BOLEH** dijadikan sumber pull

---

### 1️⃣ Pull dari repo organisasi

```bash
git pull origin main
```

### 2️⃣ Jika tidak ada konflik, sinkronkan ke repo pribadi

```bash
git push origin2 main
```

---

## ❗ ATURAN PENTING (WAJIB DIIKUTI)

1. ❌ Jangan pull dari `origin2`
2. ❌ Jangan force push ke repo organisasi tanpa izin mentor
3. ✅ Repo organisasi = sumber utama
4. ✅ Repo pribadi = backup & portofolio
5. ✅ Commit message harus jelas & relevan

---

## 🧯 PENANGANAN ERROR UMUM

### ❌ `failed to push some refs`

```bash
git pull origin main --rebase
git push origin main
git push origin2 main
```

---

### ❌ Salah branch

```bash
git branch
```

Jika masih `master`:

```bash
git branch -M main
```

---

## 📌 CHEAT SHEET (INGAT INI SAJA)

### 🔼 PUSH

```bash
git add .
git commit -m "update"
git push origin main
git push origin2 main
```

### 🔽 PULL

```bash
git pull origin main
git push origin2 main
```

---

## 🏁 PENUTUP

SOP ini dibuat agar:

* Tidak salah repo saat push/pull
* Aman untuk kerja tim magang (Android)
* Repo pribadi selalu sinkron sebagai backup

**Gunakan SOP ini setiap hari selama magang.**
