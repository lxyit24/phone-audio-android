# 📱 手机音频服务 - Android 应用

将 Android 手机变成电脑的无线扬声器或麦克风

## 🚀 功能特点

- 🔊 **扬声器模式**：电脑音频通过手机播放
- 🎤 **麦克风模式**：手机麦克风作为电脑麦克风
- 🌐 **无线连接**：无需数据线，WiFi 连接
- 📱 **中文界面**：完整的中文界面
- 🔄 **自动发现**：自动发现局域网内的设备

## 🔧 技术规格

- **音频格式**：PCM 16-bit, 44.1kHz, 立体声
- **传输协议**：TCP 流媒体
- **设备发现**：UDP 广播 (端口 50003)
- **音频端口**：扬声器 50001，麦克风 50002
- **最低 Android 版本**：7.0 (API 24)

## 📥 下载 APK

GitHub Actions 自动构建：
- 每次代码更新自动构建
- 在仓库的 Actions 页面下载最新 APK

## 🔨 构建说明

### GitHub Actions（推荐）
1. 代码推送后自动构建
2. 在 Actions 页面下载 APK

### Android Studio
1. 打开项目
2. 点击 Build > Build Bundle(s) / APK(s) > Build APK(s)

### Gradle 命令行
```bash
./gradlew assembleDebug
```

## 📄 许可证

MIT License

## 👨‍💻 开发者

lxyit24
