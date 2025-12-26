#!/bin/bash
# Luban 配置表生成脚本 (Linux/Mac)
#
# 使用前请确保：
# 1. 已安装 .NET 6.0+ 或 Java 8+
# 2. 已下载 Luban 工具到 luban/ 目录
# 3. 已配置好 luban.conf 文件

LUBAN_DLL="luban/Luban.dll"
CONF_FILE="luban/luban.conf"
OUTPUT_CODE_DIR="src/main/java/com/game/vanta/meta/gen"
OUTPUT_DATA_DIR="src/main/resources/meta"

# 检查 Luban 是否存在
if [ ! -f "$LUBAN_DLL" ]; then
    echo "Error: Luban.dll not found at $LUBAN_DLL"
    echo "Please download Luban from https://github.com/focus-creative-games/luban"
    exit 1
fi

# 创建输出目录
mkdir -p "$OUTPUT_CODE_DIR"
mkdir -p "$OUTPUT_DATA_DIR"

# 生成代码和数据
dotnet "$LUBAN_DLL" \
    -t server \
    -c java-bin \
    -d bin \
    --conf "$CONF_FILE" \
    -x outputCodeDir="$OUTPUT_CODE_DIR" \
    -x outputDataDir="$OUTPUT_DATA_DIR"

if [ $? -ne 0 ]; then
    echo "Generation failed!"
    exit 1
fi

echo "Generation completed successfully!"

