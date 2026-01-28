@echo off
setlocal EnableDelayedExpansion
title VECV Dashboard Installer
mode con: cols=100 lines=30
color 0A

:: ============================================================================
:: HEADER & CONFIG
:: ============================================================================
set "LOGFILE=install_log.txt"
if exist "%LOGFILE%" del "%LOGFILE%"

cls
echo.
:: Safe Base64-encoded ASCII Art Printing
:: This bypasses all "pipe element" and special char issues in batch files
powershell -NoProfile -Command "$g=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('Li4gICAgICAgICAuLmAuLi4uLi4uLiAgICAuLiAgIC4uICAgICAgICAgLi4KIGAuLiAgICAgICBgLi4gYC4uICAgICAgIGAuLiAgIGAuLiBgLi4gICAgICAgYC4uIAogIGAuLiAgICAgYC4uICBgLi4gICAgICBgLi4gICAgICAgICBgLi4gICAgIGAuLiAgCiAgIGAuLiAgIGAuLiAgIGAuLi4uLi4gIGAuLiAgICAgICAgICBgLi4gICBgLi4gICAKICAgIGAuLiBgLi4gICAgYC4uICAgICAgYC4uICAgICAgICAgICBgLi4gYC4uICAgIAogICAgIGAuLi4uICAgICBgLi4gICAgICAgYC4uICAgYC4uICAgICBgLi4uLiAgICAgCiAgICAgIGAuLiAgICAgIGAuLi4uLi4uLiAgIGAuLi4uICAgICAgICBgLi4gICAgICA=')); Write-Host $g -ForegroundColor Green; $w=[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String('ICAgX19fXyAgICAgICAgICAgICAgICAgXyAgICAgICAgICAgICAgICAgICAgICAgICAgICAgXyAgXyAgICAgICAgICAgICAgICBfICBfX19fXyBfX19fICAgICBfX19fICAgICAgICAgICAgICAgICAgICAgICAKICB8ICBfIFwgIF9fX19fICAgX19fX198IHwgX19fICBfIF9fICAgX19fICBfX3wgfCB8IHxfXyAgXyAgIF8gICAgIC8gXHxfICAgXy8gX19ffCAgIC8gX19ffF8gX18gX19fICBfICAgXyBfIF9fICAKICB8IHwgfCB8LyBfIFwgXCAvIC8gXyBcIHwvIF8gXHwgXCdfIFwgLyBfIFwvIF9gIHwgfCBcJ18gXHwgfCB8IHwgICAvIF8gXCB8IHwgXF9fXyBcICB8IHwgIF9fLyBcJ18gXyBcIHwgfCB8IHwgXCdfIFwgCiAgfCB8X3wgfCAgX18vXCBWIC8gIF9fLyB8IChfKSB8IHxfKSB8ICBfXy8gKF98IHwgfCB8XykgfCB8X3wgfCAgLyBfX18gXHwgfCAgX19fKSB8IHwgfF98IHwgfCB8IChfKSB8IHxffCB8IHxfKSB8CiAgfF9fX18vIFxfX198IFxfLyBcX19ffF98XF9fXy98IC5fXy8gXF9fX3xcX18sX3wgfF8uX18vIFxfXywgfCAvXy8gICBcX19cfCB8X19fXy8gICBcX19fX3xffCAgXF9fXy8gXF9fLF98IC5fXy8gCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHxffCAgICAgICAgICAgICAgICAgICAgICAgfF9fXy8gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHxffCAgICA=')); Write-Host $w -ForegroundColor White;"
echo.

:: Progress Tracking
set "TOTAL_STEPS=14"
set "CURRENT_STEP=0"
set "PROGRESS_PCT=0"

:: ============================================================================
:: 1. AUTO-ELEVATION & ADMIN CHECK
:: ============================================================================
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo    Requesting Administrator Privileges...
    :: Use correct quoting for path with spaces
    powershell -Command "Start-Process cmd -ArgumentList '/c, \"\"%~f0\"\"' -Verb RunAs"
    exit /b
)
cd /d "%~dp0"


:: 2. Java Check
call :EXEC_STEP "Checking Java Runtime" "java -version"
if %EXIT_CODE% neq 0 (
    call :EXEC_STEP "Installing Java 17 (Winget)" "winget install Microsoft.OpenJDK.17 --accept-source-agreements --accept-package-agreements"
)

:: 3. Docker Check
call :EXEC_STEP "Checking Docker Engine" "docker --version"
if %EXIT_CODE% neq 0 (
    call :EXEC_STEP "Installing Docker Desktop" "winget install Docker.DockerDesktop --accept-source-agreements --accept-package-agreements"
    echo.
    echo [INFO] Docker installed. Please restart your computer to finish setup.
    pause
    exit
)

:: 4. Docker Service Check
call :EXEC_STEP "Verifying Docker Service" "docker ps"
if %EXIT_CODE% equ 0 goto :DOCKER_READY

echo      [INFO] Waiting for Docker Service to start...
:WAIT_DOCKER
timeout /t 5 >nul
docker ps >nul 2>&1
if %errorlevel% neq 0 goto :WAIT_DOCKER

:DOCKER_READY

:: ============================================================================
:: 5. DATABASE SETUP MENU
:: ============================================================================

:DB_MENU
cls
echo.
echo    DATABASE CONFIGURATION
echo    ======================
echo.
echo    1. New Installation (Create Docker Container & Restore Backup)
echo    2. Existing Installation (Connect to running SQL Server)
echo.
set /p "DB_CHOICE=Select Option (1 or 2): "
if "%DB_CHOICE%"=="1" goto :SETUP_NEW_DB
if "%DB_CHOICE%"=="2" goto :CONNECT_EXISTING_DB
goto :DB_MENU

:: ----------------------------------------------------------------------------
:: OPTION 1: NEW INSTALLATION
:: ----------------------------------------------------------------------------
:SETUP_NEW_DB
echo.
echo    [NEW INSTALLATION]
echo    ------------------
echo    You need to set a 'System Administrator' (sa) password for the new database.
echo    Password requirements: 8+ chars, uppercase, lowercase, numbers (e.g. Vecv@2025).
echo.
set /p "NEW_PASS=Enter Password to Set: "
set "DB_PASSWORD=%NEW_PASS%"
set "DB_USER=sa"
set "DB_HOST=localhost"

:: check for vecv.bak (File or Folder) in current or parent dir
set "BAK_PATH="
set "BAK_IS_DIR=0"

if exist "vecv.bak" (
    set "BAK_PATH=%CD%\vecv.bak"
    if exist "vecv.bak\" set "BAK_IS_DIR=1"
)
if not defined BAK_PATH (
    if exist "..\vecv.bak" (
        set "BAK_PATH=%CD%\..\vecv.bak"
        if exist "..\vecv.bak\" set "BAK_IS_DIR=1"
    )
)

call :EXEC_STEP "Checking SQL Server Container" "docker ps -a --format '{{.Names}}' | findstr sqlserver"
if %EXIT_CODE% equ 0 (
    echo    [WARN] Found existing 'sqlserver' container. Removing it for fresh install...
    docker rm -f sqlserver >nul 2>&1
)

call :EXEC_STEP "Creating SQL Container" "docker run -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=%DB_PASSWORD%' -p 1433:1433 --name sqlserver -d mcr.microsoft.com/mssql/server:2022-latest"
echo    [INFO] Waiting 20s for SQL Server Warmup...
timeout /t 20 >nul

:: Logic split to avoid label-inside-brackets syntax error
if not defined BAK_PATH goto :RUN_SCHEMA_SCRIPTS

:: Handle Folder vs File
set "FINAL_BAK_FILE="
if "%BAK_IS_DIR%"=="1" (
    echo    [INFO] '%BAK_PATH%' is a folder. Searching for .bak file inside...
    for /r "%BAK_PATH%" %%F in (*.bak) do (
        set "FINAL_BAK_FILE=%%F"
        goto :FOUND_BAK
    )
) else (
    set "FINAL_BAK_FILE=%BAK_PATH%"
)

:FOUND_BAK
if not defined FINAL_BAK_FILE (
    echo    [WARN] No .bak file found inside the folder!
    goto :RUN_SCHEMA_SCRIPTS
)

echo    [INFO] Found Backup File: %FINAL_BAK_FILE%
call :EXEC_STEP "Copying Backup to Container" "docker cp \"%FINAL_BAK_FILE%\" sqlserver:/var/opt/mssql/backup.bak"

echo    [INFO] Restoring Database from Backup...
call :EXEC_STEP "Restoring vecv.bak" "docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P %DB_PASSWORD% -Q \"RESTORE DATABASE VECV_Scada_DB FROM DISK = '/var/opt/mssql/backup.bak' WITH MOVE 'VECV_Scada_DB' TO '/var/opt/mssql/data/VECV_Scada_DB.mdf', MOVE 'VECV_Scada_DB_log' TO '/var/opt/mssql/data/VECV_Scada_DB_log.ldf', REPLACE\""

if %EXIT_CODE% neq 0 (
     echo    [ERROR] Restore Failed. Falling back to clean schema scripts...
     goto :RUN_SCHEMA_SCRIPTS
)
goto :DB_OPTIMIZE

:RUN_SCHEMA_SCRIPTS
if not defined BAK_PATH echo    [WARN] 'vecv.bak' not found. Creating clean database structure...
call :EXEC_STEP "DB: Creating Schema & Tables" "type sql\database_setup.sql | docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P %DB_PASSWORD% -d master"
goto :DB_OPTIMIZE

:: ----------------------------------------------------------------------------
:: OPTION 2: EXISTING INSTALLATION
:: ----------------------------------------------------------------------------
:CONNECT_EXISTING_DB
echo.
echo    [EXISTING INSTALLATION]
echo    -----------------------
set "DB_HOST=localhost"
set "DB_USER=sa"
set /p "DB_HOST=Enter Hostname (Default: localhost): "
set /p "DB_USER=Enter Username (Default: sa): "

:ENTER_PASS
echo.
set /p "DB_PASSWORD=Enter SQL Password: "

:CHECK_CONN
call :EXEC_STEP "Verifying Connection..." "docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S %DB_HOST% -U %DB_USER% -P %DB_PASSWORD% -Q 'SELECT 1'"
if %EXIT_CODE% neq 0 (
    echo.
    echo    [ERROR] Connection Failed! Check Password or Hostname.
    echo    1. Retry Password
    echo    2. Back to Menu
    set /p "RETRY_CHOICE=(1/2): "
    if "!RETRY_CHOICE!"=="2" goto :DB_MENU
    goto :ENTER_PASS
)

:: ----------------------------------------------------------------------------
:: COMMON: OPTIMIZATION & PROCS
:: ----------------------------------------------------------------------------
:DB_OPTIMIZE
:: Ensure DB context is correct for these scripts
call :EXEC_STEP "DB: Applying Optimizations" "type sql\optimize_db.sql | docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S %DB_HOST% -U %DB_USER% -P %DB_PASSWORD% -d VECV_Scada_DB"
call :EXEC_STEP "DB: Configuring Dimensions" "type sql\optimize_dimensions.sql | docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S %DB_HOST% -U %DB_USER% -P %DB_PASSWORD% -d VECV_Scada_DB"

:: 8. Database Procedures
call :EXEC_STEP "DB: Compiling Core Procedures" "type sql\sp_setup.sql | docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S %DB_HOST% -U %DB_USER% -P %DB_PASSWORD% -d VECV_Scada_DB"
call :EXEC_STEP "DB: Compiling Calculation Logic" "type sql\sp_CalcDowntime.sql | docker exec -i sqlserver /opt/mssql-tools/bin/sqlcmd -S %DB_HOST% -U %DB_USER% -P %DB_PASSWORD% -d VECV_Scada_DB"

:: 9. Application Build
if not exist "VECV-Dashboard-App.jar" (
    call :EXEC_STEP "Building Application (Maven)" "call mvnw.cmd clean package -DskipTests"
    copy target\PullChord-Report-0.0.1-SNAPSHOT.jar VECV-Dashboard-App.jar >nul
) else (
    call :EXEC_STEP "Verifying Application Jar" "dir VECV-Dashboard-App.jar"
)

:: 10. Shortcut
call :EXEC_STEP "Creating Desktop Shortcut" "powershell -Command \"$s=(New-Object -COM WScript.Shell).CreateShortcut('%USERPROFILE%\Desktop\VECV Dashboard.url'); $s.TargetPath='http://localhost:8070'; $s.Save()\""

:: 11. Launch
set "PROGRESS_PCT=100"
call :DRAW_PROGRESS "Installation Complete"

echo.
echo    [SUCCESS] DASHBOARD PREPARED AND READY.
echo.
echo    Opening Dashboard: http://localhost:8070 ...
start http://localhost:8070

echo    Starting Server...
java -jar VECV-Dashboard-App.jar

pause
exit /b


exit /b


:: ============================================================================
:: FUNCTIONS
:: ============================================================================

:EXEC_STEP <TaskName> <Command>
    set "TASK_NAME=%~1"
    set "CMD_STR=%~2"
    
    :: Update Progress
    set /a CURRENT_STEP+=1
    set /a PROGRESS_PCT=CURRENT_STEP*100/TOTAL_STEPS
    if !PROGRESS_PCT! gtr 100 set PROGRESS_PCT=100
    
    call :DRAW_PROGRESS "%TASK_NAME%"
    
    :: Execute Command Synchronously (No Background Jobs)
    cmd /c "%CMD_STR% >> %LOGFILE% 2>&1"
    
    if !errorlevel! equ 0 (
         set "EXIT_CODE=0"
    ) else (
         set "EXIT_CODE=1"
         echo [FAILED] See %LOGFILE% for details.
    )
    exit /b

:DRAW_PROGRESS <TaskName>
    :: Calculate Time Remaining (Simple Linear Estimate)
    :: Assume 240 seconds total.
    if not defined START_TIME (
        set "START_TIME=%time%"
        set "EST_TOTAL_SEC=240"
    )
    
    :: Decimal Percentage Logic
    set /a "dec=PROGRESS_PCT*10/100"
    if !dec! gtr 100 set dec=100
    set "DISP_PCT=!PROGRESS_PCT!.0"

    echo.
    echo ========================================================================================================================
    echo    Current Step: %~1
    echo    Progress:     !DISP_PCT!%%
    echo    Status:       Working...
    echo ========================================================================================================================
    echo.
    exit /b

