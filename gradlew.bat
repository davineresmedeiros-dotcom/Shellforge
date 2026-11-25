@echo off
setlocal
set GRADLE_VERSION=8.9
set DIST_NAME=gradle-%GRADLE_VERSION%-bin
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\%DIST_NAME%
set GRADLE_BIN=%DIST_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if exist "%GRADLE_BIN%" goto run
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
set ZIP_FILE=%DIST_DIR%\%DIST_NAME%.zip
set URL=https://services.gradle.org/distributions/%DIST_NAME%.zip
echo Downloading Gradle %GRADLE_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%URL%' -OutFile '%ZIP_FILE%'"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%DIST_DIR%' -Force"
if errorlevel 1 exit /b 1
del "%ZIP_FILE%"
:run
call "%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
