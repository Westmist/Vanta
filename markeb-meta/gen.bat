@echo off
REM Luban 配置表生成脚本 (Windows)
REM 
REM 使用前请确保：
REM 1. 已安装 .NET 6.0+ 或 Java 8+
REM 2. 已下载 Luban 工具到 luban/ 目录
REM 3. 已配置好 luban.conf 文件

set LUBAN_DLL=luban\Luban.dll
set CONF_FILE=luban\luban.conf
set OUTPUT_CODE_DIR=src\main\java\com\game\vanta\meta\gen
set OUTPUT_DATA_DIR=src\main\resources\meta

REM 检查 Luban 是否存在
if not exist "%LUBAN_DLL%" (
    echo Error: Luban.dll not found at %LUBAN_DLL%
    echo Please download Luban from https://github.com/focus-creative-games/luban
    exit /b 1
)

REM 创建输出目录
if not exist "%OUTPUT_CODE_DIR%" mkdir "%OUTPUT_CODE_DIR%"
if not exist "%OUTPUT_DATA_DIR%" mkdir "%OUTPUT_DATA_DIR%"

REM 生成代码和数据
dotnet "%LUBAN_DLL%" ^
    -t server ^
    -c java-bin ^
    -d bin ^
    --conf "%CONF_FILE%" ^
    -x outputCodeDir=%OUTPUT_CODE_DIR% ^
    -x outputDataDir=%OUTPUT_DATA_DIR%

if %ERRORLEVEL% neq 0 (
    echo Generation failed!
    exit /b 1
)

echo Generation completed successfully!

