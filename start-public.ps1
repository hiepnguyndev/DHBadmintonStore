$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
foreach ($port in @(8080, 8787)) {
    if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
        throw "Port $port is in use. Stop the existing application before starting this script."
    }
}
New-Item -ItemType Directory -Force 'backend/var' | Out-Null
Push-Location frontend
try {
    $env:VITE_API_BASE_URL = '/api'
    $env:VITE_BASE_PATH = '/'
    npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed' }
} finally { Pop-Location }
Push-Location backend
try {
    & .\mvnw.cmd -q -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Backend build failed' }
} finally { Pop-Location }
$backend = Start-Process powershell.exe -WindowStyle Hidden -ArgumentList '-NoProfile -ExecutionPolicy Bypass -File "scripts/start-public-backend.ps1"' -WorkingDirectory $PSScriptRoot -RedirectStandardOutput 'backend/var/public-backend.log' -PassThru
$web = Start-Process node.exe -WindowStyle Hidden -ArgumentList 'frontend/scripts/public-server.mjs' -WorkingDirectory $PSScriptRoot -RedirectStandardOutput 'backend/var/public-web.log' -PassThru
Write-Host "Backend launcher PID: $($backend.Id); web PID: $($web.Id)"
Write-Host 'Cloudflare Service URL: http://127.0.0.1:8787'
Write-Host 'Logs: backend/var/public-*.log. Cloudflare route is not created by this script.'
