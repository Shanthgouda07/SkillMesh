@echo off
title SkillMesh Launcher
echo ===================================================
echo           Launching SkillMesh On-Device App
echo ===================================================

set "EMULATOR_EXE=%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe"
set "ADB_EXE=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "APK_PATH=C:\Users\Shanth gouda\.gemini\antigravity-ide\scratch\SkillMesh\app\build\outputs\apk\debug\app-debug.apk"

echo Starting virtual phone window on your desktop...
start "" "%EMULATOR_EXE%" -avd medium_phone

echo Waiting for virtual phone to connect...
"%ADB_EXE%" wait-for-device

echo Waiting for system boot...
:waitForBoot
for /f "tokens=*" %%a in ('"%ADB_EXE%" shell getprop sys.boot_completed 2^>nul') do (
    if "%%a"=="1" goto bootDone
)
timeout /t 2 >nul
goto waitForBoot

:bootDone
echo Ensuring SkillMesh is installed...
"%ADB_EXE%" install -r "%APK_PATH%" >nul 2>&1

echo Opening SkillMesh on screen...
"%ADB_EXE%" shell am start -n com.example.skillmesh/.MainActivity

echo.
echo ===================================================
echo  SkillMesh is now open on your desktop phone window!
echo ===================================================
timeout /t 5
