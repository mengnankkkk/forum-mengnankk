@echo off
chcp 65001 > nul
echo ====================================
echo   Forum微服务 - 一键启动脚本
echo ====================================

echo [步骤1] 启动Nacos Server...
call start-nacos.bat

echo.
echo [步骤2] 等待用户确认Nacos启动完成...
echo 请确认Nacos Server已启动完成，然后按任意键继续...
pause > nul

echo.
echo [步骤3] 启动认证服务...
call start-auth-service.bat

echo.
echo ====================================
echo   所有服务启动完成
echo ====================================
pause
