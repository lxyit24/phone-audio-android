package com.phoneaudio.phoneaudioservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class MicrophoneService extends Service {
    private static final String TAG = "MicrophoneService";
    private static final int AUDIO_PORT = 50002;
    private static final int DISCOVERY_PORT = 50003;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;

    private AudioRecord audioRecord;
    private boolean isRunning = false;
    private Thread recordingThread;
    private Thread discoveryThread;
    private ServerSocket serverSocket;
    private Socket socket;
    private DataOutputStream dataOutput;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "START".equals(intent.getAction())) {
            startForeground(2, createNotification("手机音频服务", "麦克风模式：等待电脑连接..."));
            startRecording();
        }
        return START_STICKY;
    }

    private void startRecording() {
        isRunning = true;
        
        discoveryThread = new Thread(() -> {
            try {
                DatagramSocket discoverySocket = new DatagramSocket(DISCOVERY_PORT);
                discoverySocket.setBroadcast(true);
                
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                Log.d(TAG, "Discovery listener started on port " + DISCOVERY_PORT);
                
                while (isRunning) {
                    try {
                        discoverySocket.receive(packet);
                        String received = new String(packet.getData(), 0, packet.getLength());
                        
                        if (received.contains("discovery") && received.contains("PhoneAudioBridge")) {
                            String response = "{\"type\":\"discovery-response\",\"app\":\"PhoneAudioBridge\",\"mode\":\"mic\",\"name\":\""
                                + Build.MODEL + "\"}";
                            byte[] responseData = response.getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(
                                responseData, responseData.length, packet.getAddress(), DISCOVERY_PORT);
                            discoverySocket.send(responsePacket);
                            
                            Log.d(TAG, "Sent discovery response to: " + packet.getAddress());
                        }
                    } catch (Exception e) {
                        if (isRunning) {
                            Log.e(TAG, "Discovery error: " + e.getMessage());
                        }
                    }
                }
                
                discoverySocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Discovery thread error: " + e.getMessage());
            }
        });
        discoveryThread.start();
        
        recordingThread = new Thread(() -> {
            try {
                initAudioRecord();
                
                serverSocket = new ServerSocket(AUDIO_PORT);
                Log.d(TAG, "Mic server started on port " + AUDIO_PORT);
                
                updateNotification("麦克风模式：等待电脑连接...");
                
                socket = serverSocket.accept();
                Log.d(TAG, "PC connected: " + socket.getInetAddress());
                
                updateNotification("电脑已连接，正在传输音频...");
                
                OutputStream outputStream = socket.getOutputStream();
                dataOutput = new DataOutputStream(outputStream);
                
                byte[] audioBuffer = new byte[BUFFER_SIZE];
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    int bytesRead = audioRecord.read(audioBuffer, 0, BUFFER_SIZE);
                    if (bytesRead > 0 && dataOutput != null) {
                        dataOutput.write(audioBuffer, 0, bytesRead);
                        dataOutput.flush();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Recording error: " + e.getMessage());
            }
        });
        recordingThread.start();
    }

    private void initAudioRecord() {
        if (audioRecord != null) {
            audioRecord.release();
        }

        int bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
            .build();

        audioRecord = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize * 2
        );

        audioRecord.startRecording();
        Log.d(TAG, "AudioRecord initialized");
    }

    private Notification createNotification(String title, String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, "PhoneAudioChannel")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                "PhoneAudioChannel",
                "手机音频服务",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void updateNotification(String message) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        Notification notification = createNotification("手机音频服务", message);
        manager.notify(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (recordingThread != null) {
            recordingThread.interrupt();
        }
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing socket: " + e.getMessage());
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing server: " + e.getMessage());
            }
        }
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
