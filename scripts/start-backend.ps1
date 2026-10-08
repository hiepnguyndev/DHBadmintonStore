$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../backend")
if (!(Test-Path ".env")) { throw "Copy backend/.env.example to backend/.env and configure DB/JWT first." }
Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        $value = $Matches[2].Trim().Trim([char]34).Trim([char]39)
        [Environment]::SetEnvironmentVariable($Matches[1], $value, "Process")
    }
}
if (!$env:DB_USERNAME -or !$env:DB_PASSWORD -or !$env:JWT_SECRET) { throw "DB_USERNAME, DB_PASSWORD and JWT_SECRET are required." }
mvn spring-boot:run
exit $LASTEXITCODE
