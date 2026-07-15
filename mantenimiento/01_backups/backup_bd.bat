@echo off
REM ==========================================================================
REM  BACKUP_BD.BAT
REM  Respaldo (backup) de la base de datos "Autoservicio" (AltoquePago)
REM
REM  Que hace:
REM    1. Genera un dump completo de la BD con mysqldump.
REM    2. Nombra el archivo con fecha y hora (evita sobrescribir backups).
REM    3. Guarda el archivo en una carpeta externa al proyecto (BACKUP_DIR).
REM    4. Elimina automaticamente backups con mas de 30 dias de antiguedad.
REM
REM  Como usarlo:
REM    - Doble clic, o ejecutar desde una consola: backup_bd.bat
REM    - Recomendado: programarlo en el "Programador de tareas" de Windows
REM      para que corra automaticamente todos los dias (ver GUIA en README).
REM ==========================================================================

setlocal enabledelayedexpansion

REM ---------- CONFIGURACION (ajustar segun el entorno) ----------
set MYSQL_USER=tu usuario
set MYSQL_PASSWORD=tu contraseña
set DB_NAME=Autoservicio
set BACKUP_DIR=C:\Backups\AltoquePago
set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin"
REM ----------------------------------------------------------------

REM Genera un identificador de fecha/hora AAAAMMDD_HHMM independiente del
REM idioma/formato regional de Windows (evita el problema de %date% variable
REM segun configuracion regional del equipo).
for /f "usebackq" %%I in (`powershell -NoProfile -Command "Get-Date -Format 'yyyyMMdd_HHmm'"`) do set FECHA=%%I

if not exist "%BACKUP_DIR%" (
    echo Creando carpeta de backups: %BACKUP_DIR%
    mkdir "%BACKUP_DIR%"
)

set ARCHIVO_SALIDA=%BACKUP_DIR%\Autoservicio_%FECHA%.sql

echo.
echo ============================================================
echo  Generando respaldo de la base de datos "%DB_NAME%"...
echo  Destino: %ARCHIVO_SALIDA%
echo ============================================================

%MYSQL_BIN%\mysqldump.exe -u %MYSQL_USER% -p%MYSQL_PASSWORD% --routines --triggers --single-transaction %DB_NAME% > "%ARCHIVO_SALIDA%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Respaldo generado correctamente: %ARCHIVO_SALIDA%
) else (
    echo.
    echo [ERROR] No se pudo generar el respaldo.
    echo Verifique que el servicio de MySQL este activo y que las
    echo credenciales/ruta de MYSQL_BIN configuradas al inicio de este
    echo script sean correctas.
    exit /b 1
)

REM ---------- Limpieza de backups antiguos (mayores a 30 dias) ----------
echo.
echo Eliminando backups con mas de 30 dias de antiguedad...
forfiles /p "%BACKUP_DIR%" /s /m *.sql /d -30 /c "cmd /c del @path" 2>nul

echo.
echo Proceso de backup finalizado.
endlocal
