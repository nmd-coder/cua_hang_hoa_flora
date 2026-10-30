param([string]$Base='http://127.0.0.1:8080')
$ErrorActionPreference='Stop'
$ProgressPreference='SilentlyContinue'
$root=Split-Path -Parent $PSScriptRoot
Push-Location $root
$userId=$null
try {
 $ready=$false
 for($i=0;$i -lt 60;$i++){
  try { Invoke-WebRequest "$Base/api/orders" -TimeoutSec 3 -UseBasicParsing | Out-Null }
  catch { if($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 401){$ready=$true;break} }
  Start-Sleep -Seconds 2
 }
 if(!$ready){throw 'Order is not ready'}
 $email="cod-$([guid]::NewGuid())@example.test"
 $body=@{email=$email;password='Local-test-password-2026'}|ConvertTo-Json
 Invoke-WebRequest "$Base/api/auth/register" -Method Post -ContentType 'application/json' -Body $body -UseBasicParsing | Out-Null
 $login=Invoke-RestMethod "$Base/api/auth/login" -Method Post -ContentType 'application/json' -Body $body -UseBasicParsing
 $headers=@{Authorization="Bearer $($login.accessToken)"}
 $userId=(Invoke-RestMethod "$Base/api/auth/me" -Headers $headers -UseBasicParsing).id
 $request=@{items=@(@{productId='10000000-0000-0000-0000-000000000001';quantity=2});recipientName='COD test recipient';recipientPhone='0900000000';address='Synthetic test address';deliveryDate=[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow,'SE Asia Standard Time').AddDays(1).ToString('yyyy-MM-dd');timeSlot='09-12';cardMessage='Test'}|ConvertTo-Json -Depth 5
 $quote=Invoke-RestMethod "$Base/api/checkout/quotes" -Method Post -ContentType 'application/json' -Headers $headers -Body $request -UseBasicParsing
 if($quote.total -ne 730000){throw 'Unexpected quote total'}
 $headers['Idempotency-Key']="cod-test-$([guid]::NewGuid())"
 $payload=@{quoteId=$quote.id}|ConvertTo-Json
 $first=Invoke-RestMethod "$Base/api/orders" -Method Post -ContentType 'application/json' -Headers $headers -Body $payload -TimeoutSec 30 -UseBasicParsing
 $second=Invoke-RestMethod "$Base/api/orders" -Method Post -ContentType 'application/json' -Headers $headers -Body $payload -TimeoutSec 30 -UseBasicParsing
 if($first.id -ne $second.id -or $first.status -ne 'PENDING'){throw 'Order replay failed'}
 $detail=Invoke-RestMethod "$Base/api/orders/$($first.id)" -Headers $headers -UseBasicParsing
 if($detail.total -ne 730000 -or $detail.payment_status -ne 'UNPAID' -or $detail.items[0].quantity -ne 2){throw 'Order snapshot mismatch'}
 $orderId=$first.id
 if($orderId -notmatch '^[0-9a-f-]{36}$'){throw 'Invalid response id'}
 $held=docker compose exec -T postgres psql -U flora -d flora -At -v ON_ERROR_STOP=1 -c "SELECT sum(quantity) FROM inventory.stock_reservations WHERE order_id='$orderId' AND status='HELD'"
 if($LASTEXITCODE -ne 0 -or [int]($held|Select-Object -Last 1) -ne 2){throw 'Stock was not held exactly once'}
 $cancel=Invoke-RestMethod "$Base/api/orders/$orderId/cancel" -Method Post -Headers $headers -TimeoutSec 30 -UseBasicParsing
 $cancelAgain=Invoke-RestMethod "$Base/api/orders/$orderId/cancel" -Method Post -Headers $headers -TimeoutSec 30 -UseBasicParsing
 if($cancel.status -ne 'CANCELLED' -or $cancelAgain.status -ne 'CANCELLED'){throw 'Cancellation was not idempotent'}
 $again=Invoke-RestMethod "$Base/api/orders" -Method Post -ContentType 'application/json' -Headers $headers -Body $payload -TimeoutSec 30 -UseBasicParsing
 if($again.id -ne $orderId -or $again.status -ne 'CANCELLED'){throw 'Order replay returned the wrong status after cancellation'}
 $remaining=docker compose exec -T postgres psql -U flora -d flora -At -v ON_ERROR_STOP=1 -c "SELECT count(*) FROM inventory.stock_reservations WHERE order_id='$orderId' AND status='HELD'"
 if($LASTEXITCODE -ne 0 -or [int]($remaining|Select-Object -Last 1) -ne 0){throw 'Stock is still held'}
 'PASS: COD order, replay, cancellation, and stock release.'
} finally {
 if($userId -and $userId -match '^[0-9a-f-]{36}$'){
  $sql=@"
BEGIN;
UPDATE inventory.stock s SET reserved=s.reserved-r.quantity
FROM inventory.stock_reservations r
WHERE s.item_id=r.item_id AND r.status='HELD'
AND r.order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.stock_movements WHERE reference_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.stock_reservations WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.reservation_batches WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM "order".checkout_requests WHERE user_id='$userId';
DELETE FROM "order".order_status_history WHERE order_id IN (SELECT id FROM "order".orders WHERE user_id='$userId');
DELETE FROM "order".order_items WHERE order_id IN (SELECT id FROM "order".orders WHERE user_id='$userId');
DELETE FROM "order".orders WHERE user_id='$userId';
DELETE FROM "order".checkout_intents WHERE user_id='$userId';
DELETE FROM "order".checkout_quotes WHERE user_id='$userId';
DELETE FROM identity.sessions WHERE user_id='$userId';
DELETE FROM identity.user_roles WHERE user_id='$userId';
DELETE FROM identity.credentials WHERE user_id='$userId';
DELETE FROM identity.users WHERE id='$userId';
COMMIT;
"@
  $sql | docker compose exec -T postgres psql -U flora -d flora -v ON_ERROR_STOP=1 | Out-Null
  if($LASTEXITCODE -ne 0){Write-Warning 'Could not clean synthetic data'}
 }
 Pop-Location
}