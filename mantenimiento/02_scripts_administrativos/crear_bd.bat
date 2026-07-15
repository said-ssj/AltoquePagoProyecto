@echo off

REM ==========================================================================
REM  CREAR_BD.BAT
REM  Crea (o reinicia por completo) la base de datos "Autoservicio" a partir
REM  del script fuente del proyecto: database/schema.sql
REM
REM  Uso tipico: instalacion inicial en un equipo nuevo, o para levantar un
REM  entorno de pruebas limpio.
REM
REM  ADVERTENCIA: schema.sql ejecuta "DROP DATABASE IF EXISTS Autoservicio",
REM  es decir, borra la base existente antes de recrearla. Si ya hay datos
REM  en produccion, ejecute antes 01_backups\backup_bd.bat
REM
REM  IMPORTANTE: este script asume que se ejecuta desde
REM  mantenimiento\02_scripts_administrativos\. Ajuste RUTA_SCHEMA si lo
REM  mueve de lugar.
REM ==========================================================================

setlocal

set MYSQL_USER=root
set MYSQL_PASSWORD=root1234
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin
set RUTA_SCHEMA=..\..\database\schema.sql

if not exist "%RUTA_SCHEMA%" (
    echo [ERROR] No se encontro el archivo %RUTA_SCHEMA%
    echo Verifique la ubicacion del script o ajuste la variable RUTA_SCHEMA.
    exit /b 1
)

echo.
echo ============================================================
echo  ADVERTENCIA: esto eliminara la base "Autoservicio" si ya
echo  existe y la volvera a crear vacia con datos base (roles, etc).
echo ============================================================
set /p CONFIRM=Escriba SI para continuar:

if /I not "%CONFIRM%"=="SI" (
    echo Operacion cancelada.
    exit /b 0
)

echo.
echo Ejecutando %RUTA_SCHEMA% ...
"%MYSQL_BIN%\mysql.exe" --default-character-set=utf8mb4 -u %MYSQL_USER% -p%MYSQL_PASSWORD% < "%RUTA_SCHEMA%"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Base de datos "Autoservicio" creada correctamente.
) else (
    echo [ERROR] Fallo la creacion de la base de datos.
)

endlocal
