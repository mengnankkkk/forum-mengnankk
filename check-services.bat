@echo off
chcp 65001 > nul
echo ====================================
echo   Forum微服务 - 服务状态检查
echo ====================================

echo [检查1] Nacos Server 状态...
netstat -ano | findstr ":8848" > nul
if %errorlevel% == 0 (
    echo ✅ Nacos Server 运行正常 (端口8848)
    echo    访问地址: http://localhost:8848/nacos
) else (
    echo ❌ Nacos Server 未运行
)

echo.
echo [检查2] 认证服务状态...
netstat -ano | findstr ":8080" > nul
if %errorlevel% == 0 (
    echo ✅ 认证服务运行正常 (端口8080)
    echo    健康检查: http://localhost:8080/actuator/health
) else (
    echo ❌ 认证服务未运行
)

echo.
echo [检查3] 测试健康检查端点...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% == 0 (
    echo ✅ 健康检查端点响应正常
    echo.
    echo 健康检查结果:
    curl -s http://localhost:8080/actuator/health
) else (
    echo ❌ 健康检查端点无响应
)

echo.
echo ====================================
echo   检查完成
echo ====================================
pause
