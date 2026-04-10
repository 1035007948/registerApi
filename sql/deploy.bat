@echo off
chcp 65001 >nul
REM ========================================================
REM MySQL数据库部署脚本 (Windows)
REM ========================================================

REM 配置参数
set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=root
set DB_PASS=your_password
set DB_NAME=register_db

echo ========================================
echo   用户注册接口数据库部署脚本
echo ========================================
echo.

REM 检查MySQL是否可用
mysql --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到MySQL命令，请确保MySQL已安装并添加到环境变量
    pause
    exit /b 1
)

REM 测试连接
echo [信息] 正在连接数据库...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo [错误] 无法连接到数据库，请检查配置
    pause
    exit /b 1
)

echo [成功] 数据库连接成功!
echo.

REM 执行SQL脚本
echo [信息] 正在执行数据库脚本...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < schema.sql
if errorlevel 1 (
    echo [错误] 数据库部署失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo   数据库部署成功!
echo ========================================
echo.
echo 数据库名称: %DB_NAME%
echo 连接地址: %DB_HOST%:%DB_PORT%
echo.
echo 测试用户:
echo   - admin / 123456
echo   - test / 123456
echo.
pause
