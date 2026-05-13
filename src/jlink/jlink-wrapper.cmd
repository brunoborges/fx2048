@echo off
setlocal EnableExtensions EnableDelayedExpansion

if defined REAL_JLINK (
    set "JLINK_BIN=%REAL_JLINK%"
) else if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jlink.exe" (
    set "JLINK_BIN=%JAVA_HOME%\bin\jlink.exe"
) else (
    set "JLINK_BIN=jlink"
)

set "OUTPUT_DIR="
set "ARGS="

:args_loop
if "%~1"=="" goto args_done
if "%~1"=="--compress" (
    shift
    if "%~1"=="2" (
        set "ARGS=!ARGS! "--compress" "zip-6""
    ) else (
        set "ARGS=!ARGS! "--compress" "%~1""
    )
    shift
    goto args_loop
)
set "ARG=%~1"
if "!ARG!"=="--compress=2" (
    set "ARGS=!ARGS! "--compress=zip-6""
    shift
    goto args_loop
)
if "%~1"=="--output" (
    set "ARGS=!ARGS! "--output""
    shift
    set "OUTPUT_DIR=%~1"
    set "ARGS=!ARGS! "%~1""
    shift
    goto args_loop
)
if "!ARG:~0,9!"=="--output=" set "OUTPUT_DIR=!ARG:~9!"
set "ARGS=!ARGS! "%~1""
shift
goto args_loop

:args_done
"%JLINK_BIN%" %ARGS%
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" exit /b %EXIT_CODE%

if defined OUTPUT_DIR (
    del /f /q "%OUTPUT_DIR%\bin\keytool.exe" 2>nul
    del /f /q "%OUTPUT_DIR%\bin\keytool" 2>nul
)

exit /b 0
