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
import android.media.AudioTrack;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class SpeakerService extends Service {
    private static final String TAG = "SpeakerService";
    private static final int AUDIO_PORT = 50001;
    private static final int DISCOVERY_PORT = 50003;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;

    private ServerSocket serverSocket;
    private AudioTrack audioTrack;
    private boolean isRunning = false;
    private Thread connectionThread;
    private Thread playbackThread;
    private Thread discoveryThread;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "START".equals(intent.getAction())) {
            startForeground(1, createNotification("手机音频服务", "扬声器模式：等待电脑连接..."));
            startServer();
        }
        return START_STICKY;
    }

    private void startServer() {
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
                            String response = "{\"type\":\"discovery-response\",\"app\":\"PhoneAudioBridge\",\"mode\":\"speaker\",\"name\":\"" 
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
        
        connectionThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(AUDIO_PORT);
                Log.d(TAG, "Speaker server started on port " + AUDIO_PORT);
                
                updateNotification("扬声器模式：等待电脑连接...");
                
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        Log.d(TAG, "PC connected: " + clientSocket.getInetAddress());
                        updateNotification("电脑已连接，正在播放音频...");
                        handleClient(clientSocket);
                    } catch (Exception e) {
                        if (isRunning) {
                            Log.e(TAG, "Accept error: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Server error: " + e.getMessage());
            }
        });
        connectionThread.start();
    }

    private void handleClient(Socket clientSocket) {
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
        }

        playbackThread = new Thread(() -> {
            try {
                initAudioTrack();
                InputStream inputStream = clientSocket.getInputStream();
                DataInputStream dataInput = new DataInputStream(inputStream);

                byte[] buffer = new byte[BUFFER_SIZE];
                while (isRunning && !Thread.currentThread().isInterrupted()) {
                    int bytesRead = dataInput.read(buffer);
                    if (bytesRead > 0) {
                        audioTrack.write(buffer, 0, bytesRead);
                    } else if (bytesRead == -1) {
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Playback error: " + e.getMessage());
            }
        });
        playbackThread.start();
    }

    private void initAudioTrack() {
        if (audioTrack != null) {
            audioTrack.release();
        }

        int bufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        );

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build();

        audioTrack = new AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize * 2,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        );

        audioTrack.play();
        Log.d(TAG, "AudioTrack initialized");
    }

    private Notification createNotification(String title, String content) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, "PhoneAudioChannel")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
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
        manager.notify(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        
        if (playbackThread != null) {
            playbackThread.interrupt();
        }
        if (discoveryThread != null) {
            discoveryThread.interrupt();
        }
        if (connectionThread != null) {
            connectionThread.interrupt();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing server: " + e.getMessage());
            }
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
