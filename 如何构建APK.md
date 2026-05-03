# 如何构建 Android APK - 详细步骤

## 方法一：使用 Android Studio（最简单，推荐新手）

### 第一步：下载并安装 Android Studio

1. 打开浏览器访问：
   ```
   https://developer.android.com/studio
   ```

2. 点击 **"Download Android Studio"** 按钮

3. 下载完成后，双击安装程序
   - 按照提示一路点击 "Next" 或 "我接受"
   - 建议安装在 C 盘默认位置

### 第二步：打开项目

1. 启动 Android Studio

2. 在欢迎界面选择：
   - **"Open an existing project"**（打开已有项目）

3. 浏览并选择项目中的文件夹：
   ```
   y:\Github\audio\android-app
   ```

4. 点击 "OK" 打开项目

### 第三步：等待 Gradle 同步

1. 首次打开项目，Android Studio 会自动下载依赖
   - 可以看到底部进度条
   - 等待出现 "Gradle sync finished" 提示

2. 如果提示错误，检查：
   - 网络连接是否正常
   - Gradle 版本是否兼容

### 第四步：构建 APK

**方式 A：使用菜单**
1. 点击顶部菜单：**Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. 等待构建完成
3. 右下角出现 "Build completed successfully" 提示

**方式 B：使用快捷键**
1. 按 `Ctrl + F9`（构建项目）
2. 按 `Ctrl + F12`（生成 APK）

### 第五步：找到 APK 文件

APK 文件位置：
```
y:\Github\audio\android-app\app\build\outputs\apk\debug\app-debug.apk
```

或者在 Android Studio 中：
1. 点击底部 "Build" 标签
2. 点击 "locate" 链接直接打开文件夹

---

## 方法二：使用命令行（适合高级用户）

### 第一步：安装必要工具

#### 1. 安装 Java JDK

Android 开发需要 Java 开发工具包：

**下载地址：**
- https://adoptium.net/temurin/releases/ （推荐，免费）
- 或 https://www.oracle.com/java/technologies/downloads/

**选择版本：** JDK 11 或 JDK 17

**安装后验证：**
打开命令提示符，输入：
```bash
java -version
```
应该显示类似：
```
openjdk version "17.x.x"
```

#### 2. 安装 Android SDK

如果 Android Studio 已安装，SDK 位置通常在：
```
C:\Users\你的用户名\AppData\Local\Android\Sdk
```

#### 3. 安装 Gradle

**使用 Chocolatey（推荐）：**
```bash
choco install gradle -y
```

**或手动安装：**
1. 下载：https://gradle.org/releases/
2. 选择版本：Gradle 7.5 或更高版本
3. 解压到：C:\Gradle\gradle-7.5
4. 添加到系统环境变量 PATH

**验证安装：**
```bash
gradle -v
```

### 第二步：配置环境变量

1. 右键 "此电脑" → 属性 → 高级系统设置 → 环境变量

2. 在 "系统变量" 中新建/编辑：
   ```
   变量名：ANDROID_HOME
   变量值：C:\Users\你的用户名\AppData\Local\Android\Sdk
   ```

3. 编辑 "Path"，添加：
   ```
   %ANDROID_HOME%\platform-tools
   %ANDROID_HOME%\tools
   ```

### 第三步：构建 APK

打开命令提示符（或 PowerShell）：

```bash
# 进入项目目录
cd y:\Github\audio\android-app

# 首次构建，下载依赖并编译
./gradlew assembleDebug

# 或使用完整路径
gradlew.bat assembleDebug
```

等待构建完成...

### 第四步：找到 APK

构建成功后，APK 位于：
```
y:\Github\audio\android-app\app\build\outputs\apk\debug\app-debug.apk
```

---

## 方法三：使用批处理脚本快速构建

我为您创建了自动构建脚本：

1. 在项目目录中创建 `build-apk.bat` 文件

2. 双击运行，自动完成所有步骤

---

## 安装 APK 到手机

### 方式 1：通过 USB 安装

1. 用 USB 线连接手机和电脑

2. 在手机上开启开发者模式和 USB 调试：
   - 设置 → 关于手机 → 连续点击"版本号"7次
   - 设置 → 开发者选项 → 开启 "USB 调试"

3. 在命令提示符中运行：
   ```bash
   adb install y:\Github\audio\android-app\app\build\outputs\apk\debug\app-debug.apk
   ```

### 方式 2：通过局域网安装（无线）

1. 手机和电脑连接同一 WiFi

2. 手机开启 USB 调试并连接电脑

3. 在命令提示符中：
   ```bash
   adb tcpip 5555
   adb connect 手机IP地址:5555
   adb install y:\Github\audio\android-app\app\build\outputs\apk\debug\app-debug.apk
   ```

### 方式 3：手动传输

1. 将 APK 文件复制到手机存储

2. 在手机上打开文件管理器

3. 点击 APK 文件安装

4. 如果提示 "禁止安装未知应用"：
   - 设置 → 安全 → 开启 "未知来源"
   - 或设置 → 应用 → 特殊访问 → 安装未知应用

---

## 常见问题解决

### 问题 1：Gradle sync failed

**解决方案：**
- 检查网络连接
- 打开 VPN（如果需要）
- 修改 `build.gradle` 中的镜像源

### 问题 2：Java 版本不兼容

**解决方案：**
- 确保使用 JDK 11 或 17
- 不要使用 JDK 8 或 JDK 20+

### 问题 3：SDK 找不到

**解决方案：**
1. 打开 Android Studio
2. 点击菜单：Tools → SDK Manager
3. 安装必要的 SDK 组件：
   - Android SDK Platform 33
   - Build-Tools 33.0.0
   - Platform-tools

### 问题 4：构建超时

**解决方案：**
- 增加超时时间
- 使用代理或 VPN
- 清理缓存后重试：
  ```bash
  ./gradlew clean
  ./gradlew --stop
  ./gradlew assembleDebug
  ```

---

## 快速开始推荐

如果您是第一次做 Android 开发：

1. ✅ **下载 Android Studio**（约 1GB）
2. ✅ **安装并打开项目**（`android-app` 文件夹）
3. ✅ **等待同步完成**（可能需要 5-10 分钟）
4. ✅ **点击 Build → Build APK**（构建 APK）
5. ✅ **找到 APK 并安装到手机**

整个过程大约需要 20-30 分钟（包括下载和安装）。

祝您构建成功！🚀
