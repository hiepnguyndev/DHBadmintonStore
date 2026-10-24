$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "../backend")
if (!(Test-Path ".env")) { throw "Copy backend/.env.example to backend/.env and configure DB/JWT first." }
$envValues = @{}
Get-Content ".env" | ForEach-Object {
    $line = $_.Trim()
    if ($line -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        $value = $Matches[2].Trim().Trim([char]34).Trim([char]39)
        $envValues[$Matches[1]] = $value
        [Environment]::SetEnvironmentVariable($Matches[1], $value, "Process")
    }
}
if (!$envValues.ContainsKey("DB_USERNAME") -or [string]::IsNullOrWhiteSpace($envValues["DB_USERNAME"]) -or !$envValues.ContainsKey("DB_PASSWORD") -or !$envValues.ContainsKey("JWT_SECRET") -or [string]::IsNullOrWhiteSpace($envValues["JWT_SECRET"])) { throw "DB_USERNAME, DB_PASSWORD (may be empty) and JWT_SECRET must be configured in backend/.env." }
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
