@echo off
echo 💥 Force killing Gradle + Android Studio...
taskkill /F /IM studio64.exe >nul 2>&1
taskkill /F /IM java.exe >nul 2>&1
taskkill /F /IM gradle* >nul 2>&1

echo 🧹 Deleting all /build folders...
for /d /r %%d in (build) do (
    if exist "%%d" (
        echo   deleting %%d
        rd /s /q "%%d"
    )
)

echo ✅ Done. You can now run ./gradlew clean or build safely.
pause