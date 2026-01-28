@echo off
setlocal
title Create VECV Dashboard Shortcut

:: Define variables
set "SHORTCUT_NAME=VECV Dashboard.url"
set "TARGET_URL=http://localhost:8070"
set "SHORTCUT_PATH=%USERPROFILE%\Desktop\%SHORTCUT_NAME%"

echo.
echo ==========================================================
echo    Creating Desktop Shortcut for VECV Dashboard
echo ==========================================================
echo.
echo Target URL: %TARGET_URL%
echo Location:   %SHORTCUT_PATH%
echo.

:: Create the .url shortcut using PowerShell
powershell -Command "$s=(New-Object -COM WScript.Shell).CreateShortcut('%SHORTCUT_PATH%'); $s.TargetPath='%TARGET_URL%'; $s.Save()"

if exist "%SHORTCUT_PATH%" (
    echo [SUCCESS] Shortcut created successfully on your Desktop!
) else (
    echo [ERROR] Failed to create shortcut.
)

echo.
pause
