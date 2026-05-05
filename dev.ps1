# =============================================================================
# dev.ps1 - Flight Platform Dev Launcher
# Usage:
#   .\dev.ps1                        -> start all services
#   .\dev.ps1 -Skip ingestion-service -> start all except one
#   .\dev.ps1 -Skip ingestion-service,flight-tracker-service -> skip multiple
#   .\dev.ps1 -TestOnly              -> run all tests, no services started
#   .\dev.ps1 -Build                 -> mvn clean install only
#TODO Script opens in new tabs instead new windows of PS.
# =============================================================================

param(
    [string[]]$Skip = @(),
    [switch]$TestOnly,
    [switch]$Build
)

$Root = $PSScriptRoot

$BuildOrder = @("common")
$ServiceOrder = @("config-server", "discovery-server", "flight-tracker-service", "ingestion-service")

# ---- Helpers ----------------------------------------------------------------

function Write-Header($msg) {
    Write-Host ""
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan
}

function Run-Maven($dir, $mvnArgs) {
    $path = Join-Path $Root $dir
    Write-Host "[MVN] $dir -> mvn $mvnArgs" -ForegroundColor Yellow
    Push-Location $path
    try {
        & mvn $mvnArgs.Split(" ")
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERROR] Maven failed in $dir (exit $LASTEXITCODE)" -ForegroundColor Red
            Pop-Location
            exit $LASTEXITCODE
        }
    } finally {
        Pop-Location
    }
}

function Start-Service($dir) {
    $path = Join-Path $Root $dir
    Write-Host "[START] Launching $dir in new window..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", `
        "Set-Location '$path'; Write-Host 'Starting $dir...' -ForegroundColor Green; mvn spring-boot:run"
}

# ---- Build ------------------------------------------------------------------

function Build-All {
    Write-Header "Building all modules (skipping tests)"
    Run-Maven "common" "clean install -DskipTests"
    Run-Maven "." "clean install -DskipTests"
    Write-Host "[OK] Build complete." -ForegroundColor Green
}

# ---- Tests ------------------------------------------------------------------

function Run-AllTests {
    Write-Header "Running all tests"
    Run-Maven "common" "clean install"
    Run-Maven "." "test"
    Write-Host "[OK] Tests complete." -ForegroundColor Green
}

# ---- Main Logic -------------------------------------------------------------

if ($TestOnly) {
    Run-AllTests
    exit 0
}

if ($Build) {
    Build-All
    exit 0
}

Build-All

Write-Header "Starting services"

foreach ($svc in $ServiceOrder) {
    if ($Skip.Count -gt 0 -and $Skip -contains $svc) {
        Write-Host "[SKIP] Skipping $svc as requested." -ForegroundColor Magenta
        continue
    }
    Start-Service $svc

    if ($svc -eq "config-server" -or $svc -eq "discovery-server") {
        Write-Host "[WAIT] Giving $svc 15s to come up before continuing..." -ForegroundColor DarkYellow
        Start-Sleep -Seconds 15
    }
}

Write-Host ""
Write-Host "All requested services launched in separate windows." -ForegroundColor Green
Write-Host "Tip: close those windows or Ctrl+C inside them to stop individual services."