@echo off
setlocal EnableExtensions

set "APP_DIR=%~dp0"
if "%APP_DIR:~-1%"=="\" set "APP_DIR=%APP_DIR:~0,-1%"

set "JAVA_EXE=%APP_DIR%\jre\bin\javaw.exe"
if not exist "%JAVA_EXE%" set "JAVA_EXE=%APP_DIR%\jre\bin\java.exe"

set "JAR_FILE=%APP_DIR%\BoxingGame-1.0.0.jar"
if not defined BOXINGGAME_JVM_OPTS set "BOXINGGAME_JVM_OPTS=-Xms256m -Xmx1024m -XX:+UseG1GC"

if not exist "%JAVA_EXE%" (
  echo [Launcher] Missing Java runtime: "%JAVA_EXE%"
  pause
  exit /b 1
)

if not exist "%JAR_FILE%" (
  echo [Launcher] Missing game JAR: "%JAR_FILE%"
  pause
  exit /b 1
)

start "" /D "%APP_DIR%" "%JAVA_EXE%" %BOXINGGAME_JVM_OPTS% -Dfile.encoding=UTF-8 -jar "%JAR_FILE%"
exit /b 0

