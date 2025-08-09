@echo off
chcp 65001 > nul
echo ====================================
echo   Forum认证服务 - 启动脚本
echo ====================================

:: 切换到认证服务目录
cd /d "e:\github\springaialibaba-example\forum-auth"

echo [INFO] 当前目录: %CD%

:: 检查Nacos是否运行
echo [INFO] 检查Nacos Server状态...
netstat -ano | findstr ":8848" > nul
if %errorlevel% neq 0 (
    echo [警告] Nacos Server 未运行！
    echo [提示] 请先运行 start-nacos.bat 启动Nacos Server
    echo.
    pause
    exit /b 1
)

echo [INFO] Nacos Server 运行正常

:: 检查认证服务端口是否被占用
echo [INFO] 检查端口8080状态...
netstat -ano | findstr ":8080" > nul
if %errorlevel% == 0 (
    echo [警告] 端口8080已被占用，请先停止占用该端口的进程
    echo [提示] 使用以下命令查看占用进程:
    echo         netstat -ano | findstr ":8080"
    echo.
    pause
    exit /b 1
)

echo [INFO] 端口8080可用

:: 清理并编译项目
echo [INFO] 清理并编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo [错误] 项目编译失败！
    pause
    exit /b 1
)

echo [INFO] 项目编译成功

:: 启动认证服务
echo [INFO] 启动Forum认证服务...
echo [INFO] 服务端口: 8080
echo [INFO] 健康检查: http://localhost:8080/actuator/health
echo [INFO] 服务信息: http://localhost:8080/actuator/info
echo [INFO] Nacos状态: http://localhost:8080/actuator/nacos
echo.
echo [提示] 按 Ctrl+C 停止服务
echo.

:: 启动Spring Boot应用
call mvn spring-boot:run

echo.
echo [INFO] 认证服务已停止
pause
