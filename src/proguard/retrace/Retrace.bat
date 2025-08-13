@echo off

set PROGUARD=C:\proguard6.0.3

set /p regex=<regex.txt
rem echo "%regex%"
set target=%~dp1/%~n1_deob%~x1
java -jar %PROGUARD%\lib\retrace.jar -regex "%regex%" ../mapas/mapa.map %1 > %target%
echo Fichero %1 desofuscado en %target%

