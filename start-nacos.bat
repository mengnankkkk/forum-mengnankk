@echo off
chcp 65001 > nul
echo ====================================
echo    Forum微服务 - Nacos启动脚本
echo ====================================

set NACOS_VERSION=2.3.2
set NACOS_DIR=nacos-server
set NACOS_ZIP=nacos-server-%NACOS_VERSION%.zip
set NACOS_URL=https://github.com/alibaba/nacos/releases/download/%NACOS_VERSION%/nacos-server-%NACOS_VERSION%.zip

echo 正在检查Nacos Server状态...

:: 检查Nacos目录是否存在
if not exist "%NACOS_DIR%" (
    echo [INFO] 未找到Nacos Server，开始下载安装...
    
    :: 检查是否有下载的zip文件
    if not exist "%NACOS_ZIP%" (
        echo [INFO] 正在下载 Nacos Server %NACOS_VERSION%...
        echo [INFO] 下载地址: %NACOS_URL%
        echo.
        echo 请手动下载 Nacos Server:
        echo 1. 访问: https://github.com/alibaba/nacos/releases/download/%NACOS_VERSION%/nacos-server-%NACOS_VERSION%.zip
        echo 2. 下载文件到当前目录: %CD%
        echo 3. 重新运行此脚本
        echo.
        pause
        exit /b 1
    )
    
    :: 解压Nacos
    echo [INFO] 正在解压 Nacos Server...
    powershell -Command "Expand-Archive -Path '%NACOS_ZIP%' -DestinationPath '.' -Force"
    ren nacos %NACOS_DIR%
    echo [INFO] Nacos Server 安装完成
)

:: 检查Nacos是否已经运行
echo [INFO] 检查Nacos是否已经运行...
netstat -ano | findstr ":8848" > nul
if %errorlevel% == 0 (
    echo [INFO] Nacos Server 已经在运行 (端口8848)
    echo [INFO] 访问地址: http://localhost:8848/nacos
    echo [INFO] 默认账号: nacos / nacos
) else (
    echo [INFO] 启动 Nacos Server...
    cd %NACOS_DIR%\bin
    echo [INFO] 启动单机模式...
    start "Nacos Server" cmd /k "startup.cmd -m standalone"
    cd ..\..
    
    echo [INFO] 等待Nacos Server启动...
    timeout /t 10 > nul
    
    echo [INFO] Nacos Server 启动完成
    echo [INFO] 访问地址: http://localhost:8848/nacos
    echo [INFO] 默认账号: nacos / nacos
)

echo.
echo ====================================
echo    Nacos Server 准备就绪
echo ====================================
pause
