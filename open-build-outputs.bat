@echo off
echo ✅ Opening APK and AAR build output folders...

REM -- APKs
echo 🔍 Dev Release APK
start "" "%~dp0app\build\outputs\apk\dev\release"

echo 🔍 Prod Release APK
start "" "%~dp0app\build\outputs\apk\prod\release"

REM -- AAB (if built)
echo 🔍 App Bundle (AAB)
start "" "%~dp0app\build\outputs\bundle\prodRelease"

REM -- AARs (library modules)
echo 📦 Core AAR
start "" "%~dp0core\build\outputs\aar"

echo 📦 Data AAR
start "" "%~dp0data\build\outputs\aar"

echo ✅ Done!
pause
