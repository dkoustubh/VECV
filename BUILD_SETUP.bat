@echo off
echo Building Setup.exe...

:: Find C# Compiler (CSC)
set "CSC="
:: Check standard 64-bit .NET locations
for /d %%d in (%WINDIR%\Microsoft.NET\Framework64\v*) do if exist "%%d\csc.exe" set "CSC=%%d\csc.exe"
:: Fallback to 32-bit if needed or if 64-bit not found
if not defined CSC (
    for /d %%d in (%WINDIR%\Microsoft.NET\Framework\v*) do if exist "%%d\csc.exe" set "CSC=%%d\csc.exe"
)

if not defined CSC (
    echo [ERROR] C# Compiler (csc.exe) not found on this system.
    echo Please ensure .NET Framework is installed (standard on Windows).
    pause
    exit /b
)

echo Found Compiler: %CSC%
echo Compiling VECV_Dashboard_Setup.exe...

"%CSC%" /target:exe /out:VECV_Dashboard_Setup.exe Setup.cs

if %errorlevel% neq 0 (
    echo.
    echo [FAILED] Compilation Error.
    pause
) else (
    echo.
    echo [SUCCESS] VECV_Dashboard_Setup.exe created successfully!
    echo.
    echo You can now delete Setup.cs and BUILD_SETUP.bat if you wish.
    echo The 'VECV_Dashboard_Setup.exe' is ready to use.
    echo.
    del Setup.cs
    pause
)
