@echo off

:START
if exist updater.jar GOTO DELETE
GOTO END_SESSION

:DELETE
del updater.jar
timeout /t 1 /nobreak
GOTO START

:END_SESSION
(goto) 2>nul & del "%~f0"
