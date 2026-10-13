$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
$backendScript = Join-Path $root "scripts/start-backend.ps1"
$frontendScript = Join-Path $root "scripts/start-frontend.ps1"

if (!(Test-Path (Join-Path $root "backend/.env"))) {
    throw "Chua co backend/.env. Hay sao chep backend/.env.example thanh backend/.env va cau hinh MySQL/JWT mot lan."
}

$backend = Start-Process -FilePath "powershell.exe" `
    -ArgumentList "-NoExit -ExecutionPolicy Bypass -File `"$backendScript`"" `
    -WorkingDirectory $root -PassThru

try {
    $frontend = Start-Process -FilePath "powershell.exe" `
        -ArgumentList "-NoExit -ExecutionPolicy Bypass -File `"$frontendScript`"" `
        -WorkingDirectory $root -PassThru
} catch {
    Stop-Process -Id $backend.Id -Force -ErrorAction SilentlyContinue
    throw
}

Write-Host "Dang khoi dong DH Badminton Store..."
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend:  http://localhost:8080/api"
Write-Host "Can de MySQL dang chay va backend/.env da duoc cau hinh."
$deadline = (Get-Date).AddMinutes(3)
do {
    Start-Sleep -Seconds 2
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5173" -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            Start-Process "http://localhost:5173"
            exit 0
        }
    } catch {
        if ((Get-Date) -ge $deadline) {
            throw "Frontend chua san sang. Hay kiem tra cua so frontend vua mo."
        }
    }
} while ((Get-Date) -lt $deadline)
