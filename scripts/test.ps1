$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
docker compose --env-file "$root/.env.example" -f "$root/docker-compose.yml" config --quiet
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
foreach ($service in @('api-gateway','identity-service','catalog-service','inventory-service','order-service','payment-service','customer-service')) {
    Push-Location "$root/backend/$service"
    try { mvn -B test; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } } finally { Pop-Location }
}
Push-Location "$root/frontend"
try { npm install; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; npm run build; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } } finally { Pop-Location }