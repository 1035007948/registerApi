#!/bin/bash
# ========================================================
# MySQL数据库部署脚本 (Linux/Mac)
# ========================================================

# 配置参数
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS="your_password"
DB_NAME="register_db"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}  用户注册接口数据库部署脚本${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""

# 检查MySQL是否可用
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到MySQL命令${NC}"
    exit 1
fi

# 测试连接
echo -e "${YELLOW}正在连接数据库...${NC}"
if ! mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} -e "SELECT 1" &> /dev/null; then
    echo -e "${RED}错误: 无法连接到数据库，请检查配置${NC}"
    exit 1
fi

echo -e "${GREEN}数据库连接成功!${NC}"
echo ""

# 执行SQL脚本
echo -e "${YELLOW}正在执行数据库脚本...${NC}"
if mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} < schema.sql; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  数据库部署成功!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "数据库名称: ${DB_NAME}"
    echo -e "连接地址: ${DB_HOST}:${DB_PORT}"
    echo ""
    echo -e "测试用户:"
    echo -e "  - admin / 123456"
    echo -e "  - test / 123456"
    echo ""
else
    echo -e "${RED}错误: 数据库部署失败${NC}"
    exit 1
fi
