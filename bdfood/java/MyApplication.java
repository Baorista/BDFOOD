package com.example.myapplication;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp Application tùy chỉnh, khởi tạo khi ứng dụng bắt đầu chạy.
 * Cấu hình Cloudinary SDK với cloud_name để sử dụng cho việc tải ảnh đại diện.
 * Khai báo trong AndroidManifest: android:name=".MyApplication"
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo Cloudinary với cloud_name
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "icd");
        MediaManager.init(this, config);
    }
}
