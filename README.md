# 📱 手机音频服务 - Android 应用

将 Android 手机变成电脑的无线扬声器或麦克风

## 🚀 使用 GitHub Actions 自动构建 APK

### 快速开始（5分钟）

#### 第一步：上传代码到 GitHub

1. **访问 GitHub**
   - 打开：https://github.com
   - 登录或注册账号

2. **创建仓库**
   - 点击右上角 "+" → "New repository"
   - 名称填写：`phone-audio-android`
   - 选择 Private（私有）
   - ✅ 勾选 "Add a README file"
   - ✅ 勾选 "Add .gitignore" → 选择 "Android"
   - 点击 "Create repository"

3. **上传代码**
   - 在仓库页面，点击 "Add file" → "Upload files"
   - 将本文件夹（android-app）内的**所有内容**拖入上传区域
   - 包含：app/、.github/、build.gradle、settings.gradle 等
   - **不要上传 build 文件夹**
   - 点击 "Commit changes"

#### 第二步：等待自动构建

1. 点击仓库顶部的 **"Actions"** 标签
2. 看到 "Build Android APK" 任务
3. 等待 3-5 分钟（自动构建中）

#### 第三步：下载 APK

1. 构建完成后，点击任务名称
2. 向下滚动到 "Artifacts" 部分
3. 点击 "app-debug" 下载 APK

#### 第四步：安装到手机

1. 将 APK 传到手机
2. 点击安装
3. 如果提示"禁止安装未知应用"：
   - 设置 → 安全 → 开启"未知来源"
   - 或设置 → 应用 → 特殊访问 → 安装未知应用

## 📋 项目文件结构

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/phoneaudio/phoneaudioservice/
│   │   │   ├── MainActivity.java      # 主界面
│   │   │   ├── SpeakerService.java     # 扬声器服务
│   │   │   ├── MicrophoneService.java # 麦克风服务
│   │   │   └── NetworkReceiver.java   # 网络监听
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml
│   │   │   └── values/
│   │   │       ├── strings.xml        # 中文文本
│   │   │       └── themes.xml         # 主题
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/
│   └── workflows/
│       └── build.yml                  # GitHub Actions 配置
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## ✨ 功能特点

- 🔊 **扬声器模式**：电脑音频通过手机播放
- 🎤 **麦克风模式**：手机麦克风作为电脑麦克风
- 🌐 **无线连接**：无需数据线，WiFi 连接
- 📱 **中文界面**：完整的中文界面
- 🔄 **自动发现**：自动发现局域网内的设备

## 📖 使用说明

### 电脑端

1. 运行 `windows-app-cs` 文件夹中的 `PhoneAudioBridge.exe`
2. 选择手机设备
3. 选择模式

### 手机端

1. 安装并打开本应用
2. 点击"扬声器模式"或"麦克风模式"
3. 等待电脑连接

### 前提条件

- ✅ 手机和电脑连接到同一 WiFi 网络
- ✅ 手机应用已打开
- ✅ 电脑应用已运行

## 🔧 技术规格

- **音频格式**：PCM 16-bit, 44.1kHz, 立体声
- **传输协议**：TCP 流媒体
- **设备发现**：UDP 广播 (端口 50003)
- **音频端口**：扬声器 50001，麦克风 50002
- **最低 Android 版本**：7.0 (API 24)

## 📥 下载地址

GitHub Actions 构建产物：
- 每次推送到 main 分支自动构建
- 在仓库的 Actions 页面下载最新 APK

## 🤝 参与贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License

---

**作者**：Phone Audio Team  
**版本**：v1.0.0  
**更新**：2024
