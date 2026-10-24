$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '../backend')
Get-Content '.env' | ForEach-Object {
    if ($_.Trim() -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim().Trim([char]34).Trim([char]39), 'Process')
    }
}
# Deployment overrides do not overwrite the user's local .env or merchant secrets.
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:SERVER_ADDRESS = '127.0.0.1'
$env:SERVER_SESSION_COOKIE_SECURE = 'true'
$env:PORT = '8080'
$env:PAYMENT_WEB_URL = 'https://ngdhiep-fithau.id.vn'
$env:PAYMENT_PUBLIC_API_URL = 'https://ngdhiep-fithau.id.vn'
$env:CORS_ALLOWED_ORIGINS = 'https://ngdhiep-fithau.id.vn,http://127.0.0.1:8787,http://localhost:8787,http://localhost:5173'
$env:LOGGING_LEVEL_ROOT = 'INFO'
$env:DEBUG = 'false'
java -jar 'target/badminton-api-0.1.0-SNAPSHOT.jar'
exit $LASTEXITCODE
