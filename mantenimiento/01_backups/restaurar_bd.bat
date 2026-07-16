@echo off

REM ==========================================================================
REM  RESTAURAR_BD.BAT
REM  Restaura la base de datos "Autoservicio" desde un archivo de backup .sql
REM  generado previamente por backup_bd.bat
REM
REM  Como usarlo:
REM    restaurar_bd.bat "C:\Backups\AltoquePago\Autoservicio_20260715_0930.sql"
REM
REM  IMPORTANTE: Esta operacion reemplaza los datos actuales de la base.
REM  El script pide confirmacion explicita antes de ejecutar la restauracion.
REM ==========================================================================

setlocal enabledelayedexpansion

set MYSQL_USER=tu usuario
set MYSQL_PASSWORD=tu contraseña
set DB_NAME=Autoservicio
set MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin

if "%~1"=="" (
    echo Uso: restaurar_bd.bat "C:\Backups\AltoquePago\Autoservicio_20260715_1626.sql"
    exit /b 1
)

if not exist "%~1" (
    echo [ERROR] El archivo "%~1" no existe.
    exit /b 1
)

echo.
echo ============================================================
echo  ADVERTENCIA: esta accion reemplazara TODOS los datos
echo  actuales de la base de datos "%DB_NAME%" con el contenido
echo  del archivo:
echo    %~1
echo ============================================================
set /p CONFIRM=Escriba SI para continuar:

if /I not "%CONFIRM%"=="SI" (
    echo Operacion cancelada por el usuario.
    exit /b 0
)

echo.
echo Restaurando backup...
"%MYSQL_BIN%\mysql.exe" -u %MYSQL_USER% -p%MYSQL_PASSWORD% %DB_NAME% < "%~1"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Base de datos restaurada correctamente desde: %~1
) else (
    echo.
    echo [ERROR] Fallo la restauracion. Verifique que la base "%DB_NAME%"
    echo exista ^(use crear_bd.bat si es la primera vez^) y que el archivo
    echo de backup sea valido.
)

endlocal
