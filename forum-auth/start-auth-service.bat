@echo off
echo 正在启动 Forum Auth 服务...
echo.

REM 设置环境变量
set JAVA_OPTS=-Xms512m -Xmx1024m -Dspring.profiles.active=dev

REM 切换到项目目录
cd /d "e:\github\springaialibaba-example\forum-auth"

REM 构建项目
echo 正在编译项目...
call mvn compile -q

REM 运行应用
echo 正在启动应用...
call mvn exec:java -Dexec.mainClass="com.mengnankk.auth.ForumAuthApplication" -Dexec.cleanupDaemonThreads=false

pause
