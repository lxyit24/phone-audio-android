# 使用 GitHub Actions 自动构建 APK

## 完整步骤

### 第一步：创建 GitHub 仓库

1. **打开 GitHub**
   - 访问：https://github.com
   - 登录您的账号（如果没有，请先注册）

2. **创建新仓库**
   - 点击右上角 "+" → "New repository"
   - 填写信息：
     - **Repository name**: `phone-audio-android`
     - **Description**: 手机音频服务 - 将手机变成电脑的扬声器或麦克风
     - **选择 Private**（私有）或 **Public**（公开）
     - ✅ 勾选 "Add a README file"
     - ✅ 勾选 "Add .gitignore" → 选择 "Android"
   - 点击 "Create repository"

### 第二步：上传代码

**方法 A：使用 Git 命令行**

1. 在本地初始化 Git（如果还没有）
   ```bash
   cd y:\Github\audio\android-app
   git init
   ```

2. 添加所有文件
   ```bash
   git add .
   ```

3. 提交
   ```bash
   git commit -m "手机音频服务 Android 应用"
   ```

4. 关联远程仓库
   ```bash
   git remote add origin https://github.com/您的用户名/phone-audio-android.git
   ```

5. 推送代码
   ```bash
   git branch -M main
   git push -u origin main
   ```

**方法 B：使用 GitHub Desktop**

1. 下载 GitHub Desktop：https://desktop.github.com/
2. 安装并登录
3. File → Clone Repository → 选择您的仓库
4. 将项目文件复制到克隆的文件夹
5. Commit → Push

**方法 C：直接上传文件**

1. 在 GitHub 仓库页面
2. 点击 "Add file" → "Upload files"
3. 将 android-app 文件夹中的所有内容拖入
4. 填写提交信息
5. 点击 "Commit changes"

### 第三步：确认 GitHub Actions 已启用

1. 在仓库页面，点击 **Actions** 标签
2. 应该能看到 "Build Android APK" 工作流
3. 如果没有，点 "New workflow" → 选择 "Set up a workflow yourself"
4. 将 `.github/workflows/build.yml` 的内容复制进去

### 第四步：等待构建

1. 切换到 **Actions** 标签
2. 看到构建任务正在运行（黄色圆点）
3. 点击任务名称查看进度
4. 等待构建完成（通常 3-5 分钟）

### 第五步：下载 APK

构建成功后：

1. 点击构建任务
2. 点击 "Summary"
3. 在 "Artifacts" 部分
4. 点击 "app-debug" 下载 APK

或者：

1. 点击左侧 "Build Android APK"
2. 点击 "Build with Gradle"
3. 查看日志，等待完成
4. 在页面底部找到 APK 下载链接

---

## 详细截图说明

### 1. 创建仓库
```
页面：https://github.com/new
填写：
- Repository name: phone-audio-android
- Description: 手机音频服务
- Public/Private: 选择 Private（如果不想让别人看到）
- ✅ Add a README file
- ✅ Add .gitignore → 选择 Android
```

### 2. 上传代码后
```
仓库根目录应该包含：
- app/
- .github/
- build.gradle
- settings.gradle
- gradlew
- gradlew.bat
- ...
```

### 3. Actions 页面
```
显示：Build Android APK workflow
状态：黄色（运行中）→ 绿色（成功）→ 红色（失败）
```

### 4. 下载 APK
```
Artifacts: app-debug (点击下载)
文件：app-debug.apk
```

---

## 自动构建触发条件

当前配置会在以下情况自动构建：

1. ✅ **每次推送到 main 分支**
2. ✅ **每次创建 Pull Request**
3. ✅ **手动触发**（可选）

---

## 查看构建历史

1. 进入仓库
2. 点击 **Actions** 标签
3. 左侧列表显示所有构建历史
4. 点击任意构建查看详情和日志

---

## 下载历史版本的 APK

1. Actions → 选择要下载的版本
2. 点击进入
3. Summary 页面
4. 下载对应的 APK

---

## 常见问题

### Q1: 构建失败，显示错误

**解决方法**：
- 查看 Actions 日志，找到具体错误
- 常见问题：
  - Java 版本不对 → 检查 build.gradle
  - SDK 组件缺失 → 检查配置
  - 代码语法错误 → 修复代码

### Q2: 没有看到 Actions 标签

**解决方法**：
- 确保仓库名称正确
- 刷新页面
- 检查是否在正确的仓库中

### Q3: 如何重新构建？

**解决方法**：
- 进入 Actions
- 点击 "Build Android APK"
- 右侧点击 "Re-run all jobs"

### Q4: APK 下载链接在哪？

**解决方法**：
- 构建任务完成后
- 点击任务名称
- 向下滚动到 "Artifacts" 部分
- 点击文件名下载

### Q5: 如何删除构建历史？

**解决方法**：
- Settings → Actions → General
- 选择 "Delete all workflow runs"
- 点击保存

---

## 进阶配置

### 添加构建变体

修改 `.github/workflows/build.yml`：

```yaml
jobs:
  build-debug:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./gradlew assembleDebug
  
  build-release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./gradlew assembleRelease
```

### 添加测试

```yaml
- name: Run tests
  run: ./gradlew test
```

### 自动发布

添加 release 配置，每次构建后自动发布：

```yaml
- name: Create Release
  uses: actions/create-release@v1
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
  with:
    tag_name: v1.0.${{ github.run_number }}
    release_name: Release v1.0.${{ github.run_number }}
    draft: false
    prerelease: false
```

---

## 下一步

1. ✅ 按照步骤上传代码到 GitHub
2. ✅ 等待构建完成
3. ✅ 下载 APK 文件
4. ✅ 安装到手机测试
5. ✅ 分享给朋友使用

---

## 分享应用

### 方式 1：直接分享 APK

1. 下载 APK 文件
2. 通过微信、QQ、邮件等发送
3. 朋友安装即可使用

### 方式 2：使用蒲公英平台

1. 注册：https://www.pgyer.com/
2. 上传 APK
3. 生成下载页面和二维码
4. 分享链接或二维码

### 方式 3：使用 Fir.im

1. 注册：https://fir.im/
2. 上传 APK
3. 获取永久下载链接

### 方式 4：发布到应用商店

需要开发者账号：
- **华为应用市场**：https://developer.huawei.com/
- **小米应用商店**：https://dev.mi.com/
- **OPPO 软件商店**：https://open.oppomobile.com/
- **vivo 应用商店**：https://dev.vivo.com.cn/

---

祝您构建成功！🚀
