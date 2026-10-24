$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

if (!(Test-Path (Join-Path $root "backend/.env"))) {
    throw "Chua co backend/.env. Hay sao chep backend/.env.example va cau hinh MySQL/JWT."
}

function Test-ProjectListener([int]$Port) {
    $listeners = @(Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
    if (!$listeners.Count) { return $false }
    foreach ($listener in $listeners) {
        $process = Get-CimInstance Win32_Process -Filter "ProcessId=$($listener.OwningProcess)"
        $command = $process.CommandLine
        # Spring Boot puts the project's classpath in a Java argument file.
        if ($command -match '@([^"\s]+\.argfile)' -and (Test-Path -LiteralPath $Matches[1])) {
            $command += Get-Content -LiteralPath $Matches[1] -Raw
        }
        if (!$command -or !$command.Replace('\\', '\').Replace('\', '/').Contains($root.Replace('\', '/'))) {
            throw "Cong $Port dang duoc ung dung khac su dung (PID $($listener.OwningProcess))."
        }
    }
    return $true
}

# Check both ports before starting anything; preserve services already running.
$backendRunning = Test-ProjectListener 8080
$frontendRunning = Test-ProjectListener 5173
$backend = $null
$frontend = $null
function Start-ServiceScript([string]$Name) {
    $script = Join-Path $root "scripts/start-$Name.ps1"
    $logDirectory = Join-Path $root $Name
    Start-Process powershell.exe -WindowStyle Hidden -PassThru `
        -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$script`"" `
        -WorkingDirectory $root `
        -RedirectStandardOutput (Join-Path $logDirectory "start.log")
}
if (!$backendRunning) { $backend = Start-ServiceScript "backend" }
if (!$frontendRunning) { $frontend = Start-ServiceScript "frontend" }

Write-Host "Dang kiem tra DH Badminton Store..."
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend:  http://localhost:8080/api"
if ($frontendRunning) { Write-Host "Dung lai frontend dang chay tren cong 5173." }
if ($backendRunning) { Write-Host "Dung lai backend dang chay tren cong 8080." }
$deadline = (Get-Date).AddMinutes(3)
do {
    if ($backend -and $backend.HasExited) {
        $log = Get-Content (Join-Path $root "backend/start.log") -Raw
        if ($log -match 'Access denied for user') {
            throw "MySQL tu choi tai khoan trong backend/.env. Kiem tra DB_USERNAME/DB_PASSWORD va quyen MySQL. Xem backend/start.log."
        }
        throw "Backend dung. Xem backend/start.log."
    }
    if ($frontend -and $frontend.HasExited) { throw "Frontend dung. Xem frontend/start.log." }
    $ready = $false
    try {
        $web = Invoke-WebRequest "http://localhost:5173" -UseBasicParsing -TimeoutSec 10
        $api = Invoke-WebRequest "http://localhost:8080/api/products?page=0&size=1" -UseBasicParsing -TimeoutSec 10
        $ready = $web.StatusCode -eq 200 -and $api.StatusCode -eq 200
    } catch { }
    if ($ready) {
        Write-Host "Frontend va backend da san sang."
        Start-Process "http://localhost:5173"
        exit 0
    }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $deadline)
throw "Ung dung chua san sang. Xem backend/start.log va frontend/start.log."

