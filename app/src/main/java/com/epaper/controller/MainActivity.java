package com.epaper.controller;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends Activity {
    private LocalHttpServer server;
    private String localUrl;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        startLocalSite();
    }

    private void buildUi() {
        int padding = dp(24);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(Color.rgb(245, 247, 251));

        TextView title = new TextView(this);
        title.setText("Điều khiển màn hình E-Paper");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(25, 35, 55));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(12));

        status = new TextView(this);
        status.setText("Đang khởi động giao diện…");
        status.setTextSize(16);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 0, 0, dp(20));

        Button openButton = new Button(this);
        openButton.setText("MỞ GIAO DIỆN ĐIỀU KHIỂN");
        openButton.setAllCaps(false);
        openButton.setOnClickListener(v -> openInBrowser());

        TextView note = new TextView(this);
        note.setText("Ứng dụng mở giao diện bằng Chrome tại localhost để Web Bluetooth hoạt động. Khi Chrome hỏi quyền, hãy cho phép Bluetooth và thiết bị ở gần.");
        note.setTextSize(14);
        note.setTextColor(Color.GRAY);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(18), 0, 0);

        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(status, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(openButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));
        root.addView(note, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    private void startLocalSite() {
        server = new LocalHttpServer(getAssets());
        try {
            int port = server.start();
            localUrl = "http://127.0.0.1:" + port + "/";
            status.setText("Giao diện đã sẵn sàng");
            status.postDelayed(this::openInBrowser, 350);
        } catch (IOException e) {
            status.setText("Không thể khởi động giao diện nội bộ");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openInBrowser() {
        if (localUrl == null) {
            Toast.makeText(this, "Giao diện chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(localUrl));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Hãy cài Google Chrome hoặc trình duyệt hỗ trợ Web Bluetooth", Toast.LENGTH_LONG).show();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (server != null) server.stop();
        super.onDestroy();
    }
}
