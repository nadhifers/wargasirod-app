# 📱 Warga Sirod - Aplikasi Android (APK)

Aplikasi Android resmi untuk portal layanan warga [wargasirod.my.id](https://wargasirod.my.id/). Didesain khusus agar nyaman, ringan, dan cepat diakses melalui smartphone Android.

---

## ✨ Fitur Utama Aplikasi

1. **Bottom Navigation (Menu Inti)**:
   - 🏠 **Beranda**: Halaman utama portal Warga Sirod.
   - 💳 **Bayar (Prioritas)**: Langsung menuju menu pembayaran & transaksi warga.
   - 📄 **Tagihan**: Cek status tagihan iuran / kas warga yang belum lunas.
   - 🕒 **Riwayat**: Bukti transaksi dan riwayat pembayaran terdahulu.
   - 👤 **Profil**: Informasi akun dan data warga.
2. **Swipe-to-Refresh**: Tarik layar ke bawah untuk memuat ulang data terbaru.
3. **Kamera & Upload Bukti Pembayaran**: Mendukung upload foto/struk transfer langsung dari galeri atau kamera HP.
4. **Offline Handling**: Deteksi otomatis saat tidak ada sinyal internet dengan tombol "Coba Lagi".
5. **Mobile View Injection**: Otomatis menyesuaikan tampilan web agar pas dan responsif di layar ponsel.

---

## 🛠️ Cara Membuat / Build File APK

Ada **2 cara mudah** untuk menghasilkan file `.apk` siap pasang di HP:

### Opsi A: Lewat Android Studio (Di Komputer Anda)
1. Buka software **Android Studio**.
2. Pilih menu **File > Open**, lalu arahkan ke folder:
   ```
   C:\Users\MyBook Prime\.gemini\antigravity\scratch\WargaSirodApp
   ```
3. Tunggu hingga proses **Gradle Sync** selesai.
4. Untuk membuat APK:
   - Klik menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Setelah selesai, klik tombol **locate** di notifikasi pojok kanan bawah.
6. File `app-debug.apk` siap dipindahkan dan diinstall ke HP Android!

---

### Opsi B: Build Otomatis di Cloud via GitHub Actions (Tanpa Install Android Studio)
Project ini sudah dilengkapi file workflow `.github/workflows/build-apk.yml`:
1. Buat repository baru di [GitHub](https://github.com/).
2. Push atau upload seluruh isi folder `WargaSirodApp` ke repository tersebut.
3. Buka tab **Actions** di GitHub repository Anda.
4. Workflow **Build Warga Sirod APK** akan berjalan otomatis selama ~2 menit.
5. Klik build yang selesai, lalu download file **WargaSirod-Debug-APK** di bagian *Artifacts*.

---

### Opsi C: Pasang Instan via Browser Chrome (Fitur Web App / PWA)
Jika Anda memiliki akses ke server hosting `wargasirod.my.id`:
1. Upload file `manifest.json` ke root folder website Anda.
2. Masukkan baris berikut di dalam tag `<head>` website:
   ```html
   <link rel="manifest" href="/manifest.json">
   <meta name="theme-color" content="#107C41">
   ```
3. Setiap warga yang membuka web melalui Chrome di HP Android akan langsung muncul tombol **"Tambahkan ke Layar Utama" / "Install Aplikasi"**, dan web akan berubah menjadi aplikasi mandiri di beranda HP warga.
