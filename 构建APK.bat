@echo off
chcp 65001 >nul
title 手机音频服务 - APK 构建脚本

echo.
echo =========================================
echo    手机音频服务 APK 自动构建脚本
echo =========================================
echo.

cd /d "%~dp0"

where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Java JDK！
    echo.
    echo 请先安装 Java JDK：
    echo 1. 访问 https://adoptium.net/temurin/releases/
    echo 2. 下载 JDK 17 (Windows x64)
    echo 3. 安装完成后重新运行此脚本
    echo.
    pause
    exit /b 1
)

echo [OK] 检测到 Java JDK
java -version 2>&1 | findstr /i "version"
echo.

echo [步骤 1/3] 清理旧文件...
if exist "app\build" (
    rmdir /s /q "app\build"
    echo [OK] 清理完成
) else (
    echo [跳过] 无旧文件
)
echo.

echo [步骤 2/3] 检查 Gradle Wrapper...
if exist "gradlew.bat" (
    echo [OK] Gradle Wrapper 已存在
) else (
    echo [提示] 正在创建 Gradle Wrapper...
    where gradle >nul 2>&1
    if %errorlevel% equ 0 (
        gradle wrapper --gradle-version 7.5
        echo [OK] Gradle Wrapper 创建完成
    ) else (
        echo [警告] 未检测到 Gradle，正在下载...
        powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-7.5-bin.zip' -OutFile 'gradle.zip'"
        powershell -command "Expand-Archive -Path 'gradle.zip' -DestinationPath '.' -Force"
        del gradle.zip
        set PATH=%PATH%;%~dp0gradle-7.5\bin
        gradle-7.5\bin\gradle wrapper --gradle-version 7.5
        rmdir /s /q gradle-7.5
        echo [OK] Gradle Wrapper 创建完成
    )
)
echo.

echo [步骤 3/3] 开始构建 APK...
echo.
call gradlew.bat assembleDebug
echo.

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo =========================================
    echo    构建成功！
    echo =========================================
    echo.
    echo APK 文件位置：
    echo %~dp0app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 文件大小：
    for %%A in ("app\build\outputs\apk\debug\app-debug.apk") do (
        set size=%%~zA
        echo %%~zA 字节 ^(%%~zA / 1024 / 1024 MB^)
    )
    echo.
    echo 是否打开所在文件夹？ ^(Y/N^)
    set /p choice=
    if /i "%choice%"=="Y" (
        explorer "app\build\outputs\apk\debug"
    )
) else (
    echo.
    echo [错误] 构建失败！请检查上方错误信息。
    echo.
    echo 常见问题：
    echo - 网络连接是否正常？
    echo - 是否已安装 Android SDK？
    echo - 是否已配置 ANDROID_HOME 环境变量？
    echo.
)

pause
