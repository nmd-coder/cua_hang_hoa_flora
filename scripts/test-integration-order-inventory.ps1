# ==========================================================================================
#  Kiểm thử tích hợp trên MÔI TRƯỜNG THẬT (Docker Compose) – mục 2.4
#  IP-01 Order Service ↔ Inventory Service   (RI-01 .. RI-04)
#  IP-03 Payment Service → Order Service      (RI-05)
#
#  Cách chạy (PowerShell, tại thư mục gốc dự án, sau khi `docker compose up -d --build`):
#     powershell -ExecutionPolicy Bypass -File scripts\test-integration-order-inventory.ps1
#  Kết quả: in ra màn hình + ghi log/CSV vào test-results\integration\
#  Script tự tạo tài khoản thử nghiệm và tự dọn dữ liệu sau khi chạy.
# ==========================================================================================
param([string]$Base = 'http://127.0.0.1:8080')
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch {}

$root = Split-Path -Parent $PSScriptRoot
Push-Location $root
$stamp  = Get-Date -Format 'yyyyMMdd-HHmmss'
$outDir = Join-Path $root 'test-results\integration'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$logFile = Join-Path $outDir "order-inventory-$stamp.log"
$csvFile = Join-Path $outDir "order-inventory-$stamp.csv"
$results = New-Object System.Collections.ArrayList
$userId  = $null

function Log([string]$msg) {
    $line = '[{0}] {1}' -f (Get-Date -Format 'HH:mm:ss'), $msg
    Write-Host $line
    Add-Content -Path $logFile -Value $line -Encoding UTF8
}

function Sql([string]$query) {
    # Truyền SQL qua stdin để không bị PowerShell làm mất dấu nháy kép của schema "order"
    $out = $query | docker compose exec -T postgres psql -U flora -d flora -At -v ON_ERROR_STOP=1
    if ($LASTEXITCODE -ne 0) { throw "SQL lỗi: $query" }
    return ($out | Select-Object -Last 1)
}

function Record([string]$id, [string]$name, [string]$expected, [string]$actual, [bool]$pass) {
    $verdict = if ($pass) { 'PASS' } else { 'FAIL' }
    [void]$results.Add([pscustomobject]@{ ID = $id; TestCase = $name; Expected = $expected; Actual = $actual; Result = $verdict })
    Log ("{0} [{1}] {2}" -f $id, $verdict, $name)
    Log ("      Mong đợi : {0}" -f $expected)
    Log ("      Thực tế  : {0}" -f $actual)
}

function Call([string]$method, [string]$path, $headers, $body) {
    # Trả về @{ Code = <HTTP status>; Body = <object> } kể cả khi lỗi 4xx/5xx
    try {
        $params = @{ Uri = "$Base$path"; Method = $method; Headers = $headers; TimeoutSec = 30; UseBasicParsing = $true }
        if ($null -ne $body) { $params.ContentType = 'application/json'; $params.Body = ($body | ConvertTo-Json -Depth 6) }
        $resp = Invoke-WebRequest @params
        $obj = $null
        if ($resp.Content) { $obj = $resp.Content | ConvertFrom-Json }
        return @{ Code = [int]$resp.StatusCode; Body = $obj }
    } catch {
        if ($_.Exception.Response) { return @{ Code = [int]$_.Exception.Response.StatusCode; Body = $null } }
        throw
    }
}

function New-Quote($auth, [string]$productId, [int]$qty) {
    $tomorrow = [TimeZoneInfo]::ConvertTimeBySystemTimeZoneId([DateTime]::UtcNow, 'SE Asia Standard Time').AddDays(1).ToString('yyyy-MM-dd')
    $req = @{ items = @(@{ productId = $productId; quantity = $qty }); recipientName = 'Integration test'; recipientPhone = '0900000000';
              address = 'Synthetic integration address'; deliveryDate = $tomorrow; timeSlot = '09-12'; cardMessage = 'IT' }
    $r = Call 'Post' '/api/checkout/quotes' $auth $req
    if ($r.Code -ne 201) { throw "Không tạo được báo giá (HTTP $($r.Code))" }
    return $r.Body
}

function Place-Order($auth, [string]$quoteId, [string]$key) {
    $h = @{ Authorization = $auth.Authorization; 'Idempotency-Key' = $key }
    return Call 'Post' '/api/orders' $h @{ quoteId = $quoteId }
}

try {
    Log "=== KIỂM THỬ TÍCH HỢP Order ↔ Inventory, Payment → Order | Gateway: $Base ==="
    # ---- Chờ hệ thống sẵn sàng (Order trả 401 khi chưa đăng nhập nghĩa là đã chạy) ----
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        try { Invoke-WebRequest "$Base/api/orders" -TimeoutSec 3 -UseBasicParsing | Out-Null }
        catch { if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 401) { $ready = $true; break } }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw 'Order Service chưa sẵn sàng. Hãy chạy: docker compose up -d --build' }

    # ---- Chuẩn bị: tài khoản thử nghiệm ----
    $email = "it-$([guid]::NewGuid())@example.test"
    $cred  = @{ email = $email; password = 'Local-test-password-2026' }
    Call 'Post' '/api/auth/register' @{} $cred | Out-Null
    $login = Call 'Post' '/api/auth/login' @{} $cred
    $auth  = @{ Authorization = "Bearer $($login.Body.accessToken)" }
    $userId = (Call 'Get' '/api/auth/me' $auth $null).Body.id
    Log "Tài khoản thử nghiệm: $email (id=$userId)"

    $P  = '10000000-0000-0000-0000-000000000003'   # FL-003, tồn ban đầu 20
    $P2 = '10000000-0000-0000-0000-000000000009'   # FL-009, tồn ban đầu 10 (dùng cho ca hết hàng)

    # ======================= RI-01: Đặt đơn -> giữ tồn thật =======================
    $reservedBefore = [int](Sql "SELECT reserved FROM inventory.stock WHERE item_id='$P';")
    Log "Tồn FL-003 trước khi đặt: reserved=$reservedBefore"
    $q1  = New-Quote $auth $P 2
    $key = "it-key-$([guid]::NewGuid())"
    $o1  = Place-Order $auth $q1.id $key
    $orderId = $o1.Body.id
    $held = Sql "SELECT COALESCE(SUM(quantity),0) FROM inventory.stock_reservations WHERE order_id='$orderId' AND status='HELD';"
    $reservedAfter = [int](Sql "SELECT reserved FROM inventory.stock WHERE item_id='$P';")
    Record 'RI-01' 'Đặt đơn COD 2 x FL-003 -> Inventory giữ tồn' `
        'HTTP 200, status=PENDING; 1 reservation HELD qty=2; stock.reserved tăng 2' `
        "HTTP $($o1.Code), status=$($o1.Body.status); HELD qty=$held; reserved $reservedBefore -> $reservedAfter" `
        ($o1.Code -eq 200 -and $o1.Body.status -eq 'PENDING' -and [int]$held -eq 2 -and $reservedAfter -eq $reservedBefore + 2)

    # ======================= RI-02: Gửi lại cùng Idempotency-Key =======================
    $o1b = Place-Order $auth $q1.id $key
    $reservedReplay = [int](Sql "SELECT reserved FROM inventory.stock WHERE item_id='$P';")
    $orderCount = Sql "SELECT count(*) FROM ""order"".orders WHERE user_id='$userId';"
    Record 'RI-02' 'Gửi lại yêu cầu đặt đơn cùng Idempotency-Key (retry mạng)' `
        'Trả lại cùng orderId; không giữ tồn lần 2; chỉ 1 đơn' `
        "orderId trùng=$($o1b.Body.id -eq $orderId); reserved=$reservedReplay; số đơn=$orderCount" `
        ($o1b.Body.id -eq $orderId -and $reservedReplay -eq $reservedAfter -and [int]$orderCount -eq 1)

    # ======================= RI-03: Hủy đơn -> nhả tồn thật =======================
    $c1 = Call 'Post' "/api/orders/$orderId/cancel" $auth $null
    $released = Sql "SELECT count(*) FROM inventory.stock_reservations WHERE order_id='$orderId' AND status='RELEASED';"
    $reservedCancel = [int](Sql "SELECT reserved FROM inventory.stock WHERE item_id='$P';")
    Record 'RI-03' 'Hủy đơn PENDING -> Inventory nhả tồn' `
        "status=CANCELLED; reservation RELEASED; reserved về $reservedBefore" `
        "HTTP $($c1.Code), status=$($c1.Body.status); RELEASED rows=$released; reserved=$reservedCancel" `
        ($c1.Body.status -eq 'CANCELLED' -and [int]$released -ge 1 -and $reservedCancel -eq $reservedBefore)

    # ======================= RI-04: Đặt vượt tồn -> 409 =======================
    $avail = [int](Sql "SELECT on_hand - reserved FROM inventory.stock WHERE item_id='$P2';")
    $qty = $avail + 1
    if ($qty -gt 99) {
        Log "RI-04 bỏ qua: tồn khả dụng của FL-009 là $avail (> 98), không tạo được yêu cầu vượt tồn hợp lệ"
    } else {
        $ordersBefore = [int](Sql "SELECT count(*) FROM ""order"".orders WHERE user_id='$userId';")
        $q4 = New-Quote $auth $P2 $qty
        $o4 = Place-Order $auth $q4.id "it-key-$([guid]::NewGuid())"
        $ordersAfter = [int](Sql "SELECT count(*) FROM ""order"".orders WHERE user_id='$userId';")
        $intent = Sql "SELECT status FROM ""order"".checkout_intents WHERE quote_id='$($q4.id)';"
        $availAfter = [int](Sql "SELECT on_hand - reserved FROM inventory.stock WHERE item_id='$P2';")
        Record 'RI-04' "Đặt $qty x FL-009 khi chỉ còn $avail -> Inventory từ chối" `
            'HTTP 409; không tạo đơn; checkout_intent=FAILED; tồn khả dụng không đổi' `
            "HTTP $($o4.Code); số đơn $ordersBefore -> $ordersAfter; intent=$intent; khả dụng $avail -> $availAfter" `
            ($o4.Code -eq 409 -and $ordersAfter -eq $ordersBefore -and $intent -eq 'FAILED' -and $availAfter -eq $avail)
    }

    # ======================= RI-05: Payment báo PAID cho Order =======================
    $q5 = New-Quote $auth $P 1
    $o5 = Place-Order $auth $q5.id "it-key-$([guid]::NewGuid())"
    $order5 = $o5.Body.id
    $total5 = (Call 'Get' "/api/orders/$order5" $auth $null).Body.total
    $pay = Call 'Post' '/api/payments/create' @{} @{ orderId = $order5; amount = $total5; provider = 'MOMO'; method = 'MOMO_WALLET' }
    $sim = Call 'Post' '/api/payments/momo/simulate' @{} @{ paymentId = $pay.Body.paymentId }
    Start-Sleep -Seconds 1
    $payStatus   = Sql "SELECT status FROM payment.payment_transactions WHERE id='$($pay.Body.paymentId)';"
    $orderPaySts = (Call 'Get' "/api/orders/$order5" $auth $null).Body.payment_status
    Record 'RI-05' 'Thanh toán MoMo (sandbox) thành công -> Order cập nhật payment_status' `
        'payment_transactions=PAID và orders.payment_status=PAID' `
        "payment=$payStatus (simulate HTTP $($sim.Code), $($sim.Body.status)); orders.payment_status=$orderPaySts" `
        ($payStatus -eq 'PAID' -and $orderPaySts -eq 'PAID')
    Call 'Post' "/api/orders/$order5/cancel" $auth $null | Out-Null   # nhả tồn của đơn RI-05
}
catch {
    Log "LỖI KHI CHẠY SCRIPT: $($_.Exception.Message)"
}
finally {
    if ($results.Count -gt 0) {
        $table = $results | Format-Table -AutoSize -Wrap | Out-String -Width 400
        Write-Host $table
        Add-Content -Path $logFile -Value $table -Encoding UTF8
        $results | Export-Csv -Path $csvFile -NoTypeInformation -Encoding UTF8
        $passed = @($results | Where-Object { $_.Result -eq 'PASS' }).Count
        Log ("TỔNG KẾT: {0}/{1} PASS. Log: {2} | CSV: {3}" -f $passed, $results.Count, $logFile, $csvFile)
    }
    # ---- Dọn dữ liệu thử nghiệm ----
    if ($userId -and $userId -match '^[0-9a-f-]{36}$') {
        $sql = @"
BEGIN;
UPDATE inventory.stock s SET reserved=s.reserved-r.quantity
FROM inventory.stock_reservations r
WHERE s.item_id=r.item_id AND r.status='HELD'
AND r.order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.stock_movements WHERE reference_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.stock_reservations WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM inventory.reservation_batches WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
DELETE FROM payment.payment_transactions WHERE order_id IN (SELECT id FROM "order".checkout_intents WHERE user_id='$userId');
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
        if ($LASTEXITCODE -ne 0) { Write-Warning 'Không dọn được dữ liệu thử nghiệm' } else { Log 'Đã dọn dữ liệu thử nghiệm.' }
    }
    Pop-Location
}
