@echo off

REM ==========================================================================
REM  INICIAR_CON_JMX.BAT
REM  Inicia AltoquePago habilitando el puerto JMX para poder monitorearlo
REM  con JConsole desde OTRO equipo de la misma red local.
REM
REM  Uso normal (monitoreo en el mismo equipo): NO hace falta este script,
REM  JConsole detecta el proceso local automaticamente. Use este script
REM  unicamente si necesita observar el kiosko desde otra maquina.
REM
REM  ADVERTENCIA DE SEGURIDAD: esta configuracion NO pide usuario/clave y
REM  NO usa SSL. Usela unicamente dentro de una red local de confianza,
REM  jamas en un equipo expuesto a internet.
REM ==========================================================================

setlocal

REM Ir a la raiz del proyecto
cd /d "%~dp0\..\.."

set JMX_PORT=9010

echo.
echo ============================================================
echo  Iniciando AltoquePago con JMX habilitado en el puerto %JMX_PORT%
echo ============================================================
echo Desde otro equipo conectar JConsole a:
echo IP_DE_ESTE_EQUIPO:%JMX_PORT%
echo.

call mvnw.cmd javafx:run ^
-Djavafx.args="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1"

endlocal