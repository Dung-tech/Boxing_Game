$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-Step {
    param([Parameter(Mandatory = $true)][string]$Message)
    Write-Host "`n[STEP] $Message" -ForegroundColor Cyan
}

function Require-LeafPath {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "$Label not found: $Path"
    }
}

function Invoke-ProcessCapture {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$ArgumentList
    )

    $tempStdOut = [System.IO.Path]::GetTempFileName()
    $tempStdErr = [System.IO.Path]::GetTempFileName()

    try {
        $process = Start-Process -FilePath $FilePath -ArgumentList $ArgumentList -NoNewWindow -PassThru -Wait -RedirectStandardOutput $tempStdOut -RedirectStandardError $tempStdErr
        return [PSCustomObject]@{
            ExitCode = $process.ExitCode
            StdOut   = (Get-Content -LiteralPath $tempStdOut -Raw)
            StdErr   = (Get-Content -LiteralPath $tempStdErr -Raw)
        }
    }
    finally {
        Remove-Item -LiteralPath $tempStdOut -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $tempStdErr -Force -ErrorAction SilentlyContinue
    }
}

function Resolve-IsccPath {
    if ($env:ISCC_PATH) {
        if (Test-Path -LiteralPath $env:ISCC_PATH -PathType Leaf) {
            return (Resolve-Path -LiteralPath $env:ISCC_PATH).Path
        }

        throw "ISCC_PATH is set but invalid: $($env:ISCC_PATH)"
    }

    $fromCommand = Get-Command "ISCC.exe" -ErrorAction SilentlyContinue
    if ($fromCommand -and $fromCommand.Source) {
        return $fromCommand.Source
    }

    $candidates = @(
        (Join-Path $env:ProgramFiles "Inno Setup 6\ISCC.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 6\ISCC.exe"),
        (Join-Path $env:ProgramFiles "Inno Setup 5\ISCC.exe"),
        (Join-Path ${env:ProgramFiles(x86)} "Inno Setup 5\ISCC.exe")
    )

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate -PathType Leaf)) {
            return $candidate
        }
    }

    throw "Cannot find ISCC.exe. Set ISCC_PATH or install Inno Setup (6.x/5.x)."
}

function Test-FileLockedForWrite {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return $false
    }

    try {
        $stream = [System.IO.File]::Open($Path, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
        $stream.Close()
        return $false
    }
    catch {
        return $true
    }
}

function Invoke-IsccBuild {
    param(
        [Parameter(Mandatory = $true)][string]$IsccPath,
        [Parameter(Mandatory = $true)][string]$OutputDir,
        [Parameter(Mandatory = $true)][string]$BaseName,
        [Parameter(Mandatory = $true)][string]$IssPath,
        [int]$MaxAttempts = 3,
        [int]$RetryDelaySeconds = 2
    )

    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        $result = Invoke-ProcessCapture -FilePath $IsccPath -ArgumentList @("/Qp", "/O$outputDir", "/F$BaseName", "$IssPath")
        if ($result.ExitCode -eq 0) {
            return $result
        }

        $combinedOutput = "$($result.StdOut)`n$($result.StdErr)"
        $isLockRelated = $combinedOutput -match "EndUpdateResource failed|Resource update error|\(110\)"
        $hasRemainingAttempts = $attempt -lt $MaxAttempts

        if ($isLockRelated -and $hasRemainingAttempts) {
            Write-Host "ISCC reported a temporary resource lock (attempt $attempt/$MaxAttempts). Retrying in $RetryDelaySeconds seconds..." -ForegroundColor Yellow
            Start-Sleep -Seconds $RetryDelaySeconds
            continue
        }

        return $result
    }
}

function Remove-DirectoryWithRetry {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [int]$MaxAttempts = 6,
        [int]$InitialDelayMs = 400
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Container)) {
        return
    }

    $lastError = $null
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        try {
            Remove-Item -LiteralPath $Path -Recurse -Force
            if (-not (Test-Path -LiteralPath $Path -PathType Container)) {
                return
            }
        }
        catch {
            $lastError = $_
        }

        if ($attempt -lt $MaxAttempts) {
            $delayMs = [Math]::Min(3000, $InitialDelayMs * $attempt)
            Write-Host "Delete retry $attempt/$MaxAttempts for '$Path' in ${delayMs}ms (folder is likely locked)." -ForegroundColor Yellow
            Start-Sleep -Milliseconds $delayMs
            continue
        }
    }

    $detail = if ($lastError) { $lastError.Exception.Message } else { "Unknown error while deleting directory." }
    throw "Could not remove '$Path' after $MaxAttempts attempts. Close running game/installer/Explorer preview or antivirus lock, then retry. Last error: $detail"
}

function Remove-FileWithRetry {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [int]$MaxAttempts = 6,
        [int]$InitialDelayMs = 400
    )

    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        return
    }

    $lastError = $null
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        try {
            Remove-Item -LiteralPath $Path -Force
            if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
                return
            }
        }
        catch {
            $lastError = $_
        }

        if ($attempt -lt $MaxAttempts) {
            $delayMs = [Math]::Min(3000, $InitialDelayMs * $attempt)
            Write-Host "Delete retry $attempt/$MaxAttempts for '$Path' in ${delayMs}ms (file is likely locked)." -ForegroundColor Yellow
            Start-Sleep -Milliseconds $delayMs
            continue
        }
    }

    $detail = if ($lastError) { $lastError.Exception.Message } else { "Unknown error while deleting file." }
    throw "Could not remove '$Path' after $MaxAttempts attempts. Close running installer/game or antivirus lock, then retry. Last error: $detail"
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $scriptDir "..\..")).Path

$gradlewPath = Join-Path $projectRoot "gradlew.bat"
$jarPath = Join-Path $projectRoot "lwjgl3\build\libs\BoxingGame-1.0.0.jar"
$aiBuildScript = Join-Path $projectRoot "python_controller\build_ai_controller.bat"
$aiExePath = Join-Path $projectRoot "python_controller\dist\AI_Controller\AI_Controller.exe"
$issPath = Join-Path $scriptDir "BoxingGame.iss"
$jreOutputDir = Join-Path $scriptDir "jre"
$outputDir = Join-Path $scriptDir "Output"
$installerBaseName = "BoxingGame_Installer"
$installerFileName = "$installerBaseName.exe"
$installerPath = Join-Path $outputDir $installerFileName

Write-Step "Step 1/5 - Build shadow JAR"
Require-LeafPath -Path $gradlewPath -Label "gradlew.bat"
Push-Location -LiteralPath $projectRoot
try {
    & "$gradlewPath" ":lwjgl3:shadowJar"
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}
Require-LeafPath -Path $jarPath -Label "Built JAR"

Write-Step "Step 2/5 - Build AI_Controller.exe"
Require-LeafPath -Path $aiBuildScript -Label "AI controller build script"
$aiBuildResult = Invoke-ProcessCapture -FilePath "cmd.exe" -ArgumentList @("/c", "$aiBuildScript")
if ($aiBuildResult.ExitCode -ne 0) {
    if ($aiBuildResult.StdOut) { Write-Host $aiBuildResult.StdOut }
    if ($aiBuildResult.StdErr) { Write-Host $aiBuildResult.StdErr }
    throw "AI_Controller.exe build failed with exit code $($aiBuildResult.ExitCode)"
}
Require-LeafPath -Path $aiExePath -Label "AI_Controller.exe"

Write-Step "Step 3/5 - Build minimal JRE with jdeps + jlink"
if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME is not set."
}

$javaBinDir = Join-Path $env:JAVA_HOME "bin"
$jmodsDir = Join-Path $env:JAVA_HOME "jmods"
$jdepsPath = Join-Path $javaBinDir "jdeps.exe"
$jlinkPath = Join-Path $javaBinDir "jlink.exe"

Require-LeafPath -Path $jdepsPath -Label "jdeps.exe"
Require-LeafPath -Path $jlinkPath -Label "jlink.exe"
if (-not (Test-Path -LiteralPath $jmodsDir -PathType Container)) {
    throw "JDK jmods folder not found: $jmodsDir"
}
$defaultModules = @("java.base", "java.desktop", "java.management", "java.logging", "jdk.unsupported")

# jdeps on fat JARs can emit stderr/module-resolution noise; capture it safely and fallback.
$detectedModules = @()
$jdepsResult = Invoke-ProcessCapture -FilePath $jdepsPath -ArgumentList @("--ignore-missing-deps", "--multi-release", "17", "--print-module-deps", "$jarPath")
if ($jdepsResult.ExitCode -eq 0) {
    $detectedModulesText = ($jdepsResult.StdOut | Out-String).Trim()
    if ($detectedModulesText) {
        $detectedModules = $detectedModulesText.Split(",") | ForEach-Object { $_.Trim() } | Where-Object { $_ }
    }
}
else {
    Write-Host "jdeps could not resolve modules for fat-jar, fallback to default modules." -ForegroundColor Cyan
}

$allModules = @($defaultModules + $detectedModules | Sort-Object -Unique)
$moduleList = $allModules -join ","
Write-Host "Modules: $moduleList" -ForegroundColor Cyan

if (Test-Path -LiteralPath $jreOutputDir -PathType Container) {
    Write-Host "Removing old JRE: $jreOutputDir" -ForegroundColor Cyan
    Remove-DirectoryWithRetry -Path $jreOutputDir
}

& "$jlinkPath" "--module-path" "$jmodsDir" "--add-modules" "$moduleList" "--output" "$jreOutputDir" "--strip-debug" "--no-header-files" "--no-man-pages" "--compress=2"
if ($LASTEXITCODE -ne 0) {
    throw "jlink failed with exit code $LASTEXITCODE"
}
if (-not (Test-Path -LiteralPath $jreOutputDir -PathType Container)) {
    throw "JRE output folder was not created: $jreOutputDir"
}

Write-Step "Step 4/5 - Locate Inno Setup compiler (ISCC.exe)"
$isccPath = Resolve-IsccPath
Write-Host "Using ISCC: $isccPath" -ForegroundColor Cyan

Write-Step "Step 5/5 - Build installer"
Require-LeafPath -Path $issPath -Label "Inno Setup script"

if (-not (Test-Path -LiteralPath $outputDir -PathType Container)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$canonicalInstallerPath = Join-Path $outputDir ("$installerBaseName.exe")
if (Test-Path -LiteralPath $canonicalInstallerPath -PathType Leaf) {
    Remove-FileWithRetry -Path $canonicalInstallerPath
}

$isccResult = Invoke-IsccBuild -IsccPath $isccPath -OutputDir $outputDir -BaseName $installerBaseName -IssPath $issPath
if ($isccResult.ExitCode -ne 0) {
    if ($isccResult.StdOut) {
        Write-Host $isccResult.StdOut
    }
    if ($isccResult.StdErr) {
        Write-Host $isccResult.StdErr
    }
    throw "ISCC failed with exit code $($isccResult.ExitCode)"
}

if (-not (Test-Path -LiteralPath $installerPath -PathType Leaf)) {
    throw "Expected installer was not found: $installerPath"
}

Write-Host "`nDone: $installerPath" -ForegroundColor Green
