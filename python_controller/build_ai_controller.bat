@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM Build AI_Controller.exe from python_controller\core\main.py with bundled deps/data.
cd /d "%~dp0"

REM Use Python 3.11 because mediapipe wheels may not be available on newer versions.
py -3.11 -m pip install --upgrade pip
py -3.11 -m pip install pyinstaller mediapipe==0.10.14 opencv-python numpy

for /f "usebackq delims=" %%i in (`py -3.11 -c "import mediapipe, os; print(os.path.dirname(mediapipe.__file__))"`) do set "MEDIAPIPE_ROOT=%%i"
set "MEDIAPIPE_MODULES=%MEDIAPIPE_ROOT%\modules"
set "EXTRA_DATA="
if exist "%MEDIAPIPE_MODULES%" (
  set "EXTRA_DATA=--add-data ""%MEDIAPIPE_MODULES%;_internal\mediapipe\modules"""
) else (
  echo [WARN] Could not find mediapipe modules folder at "%MEDIAPIPE_MODULES%".
)

py -3.11 -m PyInstaller ^
  --noconfirm ^
  --clean ^
  --onedir ^
  --noconsole ^
  --name AI_Controller ^
  --collect-all mediapipe ^
  --collect-all cv2 ^
  --hidden-import mediapipe ^
  --hidden-import cv2 ^
  --hidden-import numpy ^
  !EXTRA_DATA! ^
  core\main.py

if errorlevel 1 (
  echo.
  echo Build failed.
  exit /b 1
)

set "DIST_MODULES=%CD%\dist\AI_Controller\_internal\mediapipe\modules"
if exist "%DIST_MODULES%" (
  xcopy /E /Y /I "%MEDIAPIPE_MODULES%" "%DIST_MODULES%" >nul
) else (
  echo [WARN] Could not find packaged mediapipe modules folder at "%DIST_MODULES%".
)

if not exist "%DIST_MODULES%\pose_landmark\pose_landmark_lite.tflite" (
  echo [WARN] pose_landmark_lite.tflite is missing from packaged output.
)

echo.
echo Build complete: %CD%\dist\AI_Controller\AI_Controller.exe
echo Runtime log file (after launch): %%LOCALAPPDATA%%\BoxingGame\ai_controller.log
exit /b 0
