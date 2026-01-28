@echo off
title VECV Dashboard Installer
color 0A

echo ===================================================
echo        VECV PULL CHORD DASHBOARD - ONE CLICK SETUP
echo ===================================================
echo.
echo This script will help you:
echo 1. Check for Java 17
echo 2. Build the Application
echo 3. Automatically Setup the SQL Server Database
echo 4. Launch the Dashboard
echo.
echo NOTE: Ensure you have an internet connection for the first run.
echo ===================================================
echo.

:: 1. Check Java
echo [STEP 1/4] Checking Prerequisites...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo [ERROR] Java is NOT installed or not in PATH.
    echo Please install JDK 17 (Java Development Kit) and try again.
    pause
    exit
)
echo [OK] Java is ready.
echo.

:: 2. Build App
echo [STEP 2/4] Building Project (This may take a few minutes)...
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    color 0C
    echo [ERROR] Application Build Failed.
    echo Check your internet connection and try again.
    pause
    exit
)
echo [OK] Application Built Successfully.
echo.

:: 3. Setup Database
echo [STEP 3/4] Database Setup
echo Do you want to try and automatically set up the Local Database?
echo (Requires 'sqlcmd' tool and SQL Server running on localhost with default credentials)
echo.
set /p setup_db="Run Database Setup? (Y/N/Skip): "

if /i "%setup_db%"=="Y" (
    echo.
    echo Attempting to create tables and indexes...
    
    :: Modify these credentials if your SQL Server is different
    set DB_HOST=localhost
    set DB_USER=sa
    set DB_PASS=Ats1234@
    
    sqlcmd -S %DB_HOST% -U %DB_USER% -P "%DB_PASS%" -i "sql/database_setup.sql"
    if %errorlevel% neq 0 (
        echo [WARNING] Could not execute setup. Is 'sqlcmd' installed? Is SQL Server Running?
        echo Skipping DB setup...
    ) else (
        echo [OK] Tables Created.
        echo [INFO] Optimizing Performance...
        sqlcmd -S %DB_HOST% -U %DB_USER% -P "%DB_PASS%" -i "sql/optimize_db.sql"
        sqlcmd -S %DB_HOST% -U %DB_USER% -P "%DB_PASS%" -i "sql/optimize_dimensions.sql"
        echo [OK] Database Fully Optimized.
    )
)
echo.

:: 4. Run App
echo [STEP 4/4] Starting Dashboard...
echo ===================================================
echo Application is starting...
echo Once started, open Chrome and go to: http://localhost:8070
echo ===================================================
echo.

java -jar target/PullChord-Report-0.0.1-SNAPSHOT.jar

pause
