# Điều khiển E‑Paper Android

Dự án đã được chuẩn bị để GitHub Actions tự tạo APK cài trực tiếp.

## Cấu trúc đúng ở thư mục gốc repository

```text
.github/workflows/build-apk.yml
app/
build.gradle
gradle.properties
settings.gradle
README.md
```

Không đặt toàn bộ các mục trên bên trong thư mục `EpaperControllerAndroid`.

## Tạo APK

1. Mở tab **Actions**.
2. Chọn **Tao APK Android**.
3. Nhấn **Run workflow** → **Run workflow**.
4. Đợi dấu kiểm màu xanh.
5. Mở lần chạy, kéo xuống **Artifacts**.
6. Tải `Epaper-VietNam-APK`, giải nén và cài `Epaper-VietNam.apk`.

## Lưu ý Bluetooth

Phiên bản này mở giao diện nội bộ bằng Chrome để Web Bluetooth hoạt động. Điện thoại cần cài Chrome, bật Bluetooth và cấp quyền Thiết bị ở gần.

Việc chuyển hoàn toàn sang Bluetooth Android gốc cần kiểm thử với đúng thiết bị E‑Paper và xác nhận UUID/gói lệnh cho từng mẫu phần cứng.
