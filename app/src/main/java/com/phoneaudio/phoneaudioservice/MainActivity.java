package com.phoneaudio.phoneaudioservice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private TextView statusText;
    private Button speakerModeBtn;
    private Button micModeBtn;
    private Button stopServiceBtn;

    private boolean isSpeakerActive = false;
    private boolean isMicActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        speakerModeBtn = findViewById(R.id.speakerModeBtn);
        micModeBtn = findViewById(R.id.micModeBtn);
        stopServiceBtn = findViewById(R.id.stopServiceBtn);
    }

    private void setupListeners() {
        speakerModeBtn.setOnClickListener(v -> {
            if (!isSpeakerActive) {
                startSpeakerMode();
            } else {
                Toast.makeText(this, "扬声器模式已在运行", Toast.LENGTH_SHORT).show();
            }
        });

        micModeBtn.setOnClickListener(v -> {
            if (!isMicActive) {
                startMicMode();
            } else {
                Toast.makeText(this, "麦克风模式已在运行", Toast.LENGTH_SHORT).show();
            }
        });

        stopServiceBtn.setOnClickListener(v -> stopAllServices());
    }

    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.POST_NOTIFICATIONS
        };

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            updateStatus("准备就绪，请选择模式");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                updateStatus("准备就绪，请选择模式");
            } else {
                updateStatus("需要音频权限才能使用此应用");
            }
        }
    }

    private void startSpeakerMode() {
        Intent serviceIntent = new Intent(this, SpeakerService.class);
        serviceIntent.setAction("START");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        isSpeakerActive = true;
        speakerModeBtn.setText("🔊 扬声器模式：已启动");
        updateStatus("扬声器模式已启动，正在等待电脑连接...");
    }

    private void startMicMode() {
        Intent serviceIntent = new Intent(this, MicrophoneService.class);
        serviceIntent.setAction("START");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        isMicActive = true;
        micModeBtn.setText("🎤 麦克风模式：已启动");
        updateStatus("麦克风模式已启动，正在等待电脑连接...");
    }

    private void stopAllServices() {
        stopService(new Intent(this, SpeakerService.class));
        stopService(new Intent(this, MicrophoneService.class));

        isSpeakerActive = false;
        isMicActive = false;
        speakerModeBtn.setText("🔊 扬声器模式");
        micModeBtn.setText("🎤 麦克风模式");
        updateStatus("所有服务已停止");
    }

    private void updateStatus(String message) {
        statusText.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllServices();
    }
}
