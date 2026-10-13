param(
    [string]$Email = "admin@gmail.com",
    [switch]$Check
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"
$envFile = Join-Path $backend ".env"
$classpathFile = Join-Path $backend "target/admin-tool.classpath"
$hashSource = Join-Path $PSScriptRoot "HashAdminPassword.java"

$Email = $Email.Trim().ToLowerInvariant()
try { $address = [System.Net.Mail.MailAddress]::new($Email) }
catch { throw "Email khong hop le." }
if ($address.Address -ne $Email -or $Email.Length -gt 150) { throw "Email khong hop le." }

if (!(Test-Path -LiteralPath $envFile)) { throw "Chua co backend/.env." }
$settings = @{}
Get-Content -LiteralPath $envFile | ForEach-Object {
    if ($_.Trim() -match '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        $settings[$Matches[1]] = $Matches[2].Trim().Trim([char]34).Trim([char]39)
    }
}
foreach ($name in @("DB_USERNAME", "DB_PASSWORD")) {
    if (!$settings[$name]) { throw "Thieu $name trong backend/.env." }
}
$url = $settings["DB_URL"]
if (!$url) { $url = "jdbc:mysql://localhost:3306/badminton_ecommerce" }
if ($url -notmatch '^jdbc:mysql://') { throw "DB_URL phai la ket noi MySQL." }
$db = [Uri]$url.Substring(5)
$schema = $db.AbsolutePath.Trim('/')
if ($db.Host -notin @("localhost", "127.0.0.1", "::1") -or $schema -ne "badminton_ecommerce") {
    throw "Script nay chi dung cho CSDL badminton_ecommerce tren may local."
}
$port = if ($db.Port -gt 0) { $db.Port } else { 3306 }
$java = (Get-Command java -ErrorAction Stop).Source
$maven = (Get-Command mvn -ErrorAction Stop).Source
$mysqlCommand = Get-Command mysql -ErrorAction SilentlyContinue
$mysql = if ($mysqlCommand) { $mysqlCommand.Source } else { Join-Path $env:ProgramFiles "MySQL/MySQL Server 8.0/bin/mysql.exe" }
if (!(Test-Path -LiteralPath $mysql)) { throw "Khong tim thay mysql.exe. Them MySQL bin vao PATH." }

Push-Location $backend
try {
    & $maven -q dependency:build-classpath "-Dmdep.outputFile=$classpathFile" "-DincludeScope=runtime"
    if ($LASTEXITCODE -ne 0) { throw "Khong tai duoc classpath Maven." }
} finally { Pop-Location }
$classpath = (Get-Content -LiteralPath $classpathFile -Raw).Trim()

$password = $null
$confirmation = $null
$plain = $null
$confirmed = $null
$encoded = $null
$hash = $null
$oldMysqlPassword = [Environment]::GetEnvironmentVariable("MYSQL_PWD", "Process")
try {
    Write-Host "CSDL local: $($db.Host):$port/$schema"
    Write-Host "Email admin: $Email"
    if ($Check) {
        $plain = "AdminToolConnectionCheck123!"
    } else {
        Write-Host "Tai khoan chua co se duoc tao; neu da co, mat khau va quyen ADMIN se duoc cap nhat."
        $password = Read-Host "Nhap mat khau admin (an, it nhat 8 ky tu)" -AsSecureString
        $confirmation = Read-Host "Nhap lai mat khau" -AsSecureString
        $credential = [System.Net.NetworkCredential]::new("", $password)
        $plain = $credential.Password
        $credential = $null
        $credential = [System.Net.NetworkCredential]::new("", $confirmation)
        $confirmed = $credential.Password
        $credential = $null
        if (![String]::Equals($plain, $confirmed, [StringComparison]::Ordinal)) { throw "Mat khau xac nhan khong khop." }
    }

    # Only stdin carries the password; command arguments and SQL never contain it.
    $encoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($plain))
    $hashOutput = $encoded | & $java --class-path $classpath $hashSource
    if ($LASTEXITCODE -ne 0) { throw "Mat khau phai co it nhat 8 ky tu va toi da 72 byte UTF-8." }
    $hash = ($hashOutput -join "").Trim()
    if ($hash -notmatch '^\$2a\$12\$[./A-Za-z0-9]{53}$') { throw "Hash BCrypt khong hop le." }
    $plain = $null
    $confirmed = $null
    $encoded = $null

    $emailHex = ([BitConverter]::ToString([Text.Encoding]::UTF8.GetBytes($Email))).Replace("-", "")
    $sqlEmail = "CONVERT(0x$emailHex USING utf8mb4)"
    $mysqlArgs = @("--protocol=TCP", "--host=$($db.Host)", "--port=$port", "--user=$($settings['DB_USERNAME'])", "--database=$schema", "--default-character-set=utf8mb4", "--batch", "--skip-column-names")
    $env:MYSQL_PWD = $settings["DB_PASSWORD"]
    if ($Check) {
        $sql = "SELECT COUNT(*) FROM users WHERE email=$sqlEmail;"
    } else {
        $sql = @"
SET @admin_email = $sqlEmail;
SET @admin_hash = '$hash';
START TRANSACTION;
INSERT INTO users (full_name, email, password_hash, role, status, created_at, updated_at)
VALUES ('Admin', @admin_email, @admin_hash, 'ADMIN', 'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE password_hash=@admin_hash, role='ADMIN', status='ACTIVE', updated_at=UTC_TIMESTAMP(6);
COMMIT;
SELECT email, role, status FROM users WHERE email=@admin_email;
"@
    }
    $result = $sql | & $mysql @mysqlArgs
    if ($LASTEXITCODE -ne 0) { throw "MySQL bao loi; kiem tra ket noi va migration cua backend." }
    if ($Check) {
        Write-Host "BCrypt va ket noi MySQL: OK. Tai khoan trung email: $result. Khong ghi du lieu."
    } else {
        Write-Host "Da luu tai khoan: $result"
        Write-Host "Dang nhap tai http://localhost:5173/login bang email tren va mat khau ban vua nhap."
    }
} finally {
    [Environment]::SetEnvironmentVariable("MYSQL_PWD", $oldMysqlPassword, "Process")
    $plain = $null
    $confirmed = $null
    $encoded = $null
    $hash = $null
    if ($password) { $password.Dispose() }
    if ($confirmation) { $confirmation.Dispose() }
}
