# ==========================================================================
#  ST-05 – Đánh giá sản phẩm khi đơn hàng CHƯA được nhận (kiểm thử hệ thống)
#  Chạy: powershell -ExecutionPolicy Bypass -File scripts\st05-danh-gia-khi-chua-nhan-hang.ps1
#  Điều kiện: hệ thống đang chạy; customer@flora.vn có ít nhất 1 đơn ở trạng thái "Chờ tiếp nhận" (PENDING)
# ==========================================================================
$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch {}
$base = 'http://127.0.0.1:8080'

Write-Host '[1] Đăng nhập tài khoản khách hàng customer@flora.vn' -ForegroundColor Cyan
$cred  = @{ email = 'customer@flora.vn'; password = 'Password123@' } | ConvertTo-Json
$login = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $cred
$h     = @{ Authorization = "Bearer $($login.accessToken)" }

Write-Host '[2] Tìm một đơn hàng đang ở trạng thái PENDING (chưa giao, chưa nhận)' -ForegroundColor Cyan
$orders = Invoke-RestMethod "$base/api/orders" -Headers $h      # PS 5.1 tra ve ca mang nhu 1 doi tuong
$order  = $orders | ForEach-Object { $_ } | Where-Object { $_.status -eq 'PENDING' } | Select-Object -First 1
if (-not $order) { throw 'Chưa có đơn PENDING. Hãy đặt 1 đơn COD trên web rồi chạy lại.' }
$detail = Invoke-RestMethod "$base/api/orders/$($order.id)" -Headers $h
$item   = $detail.items | ForEach-Object { $_ } | Select-Object -First 1
# Server khong ghi charset nen PS 5.1 doc nham UTF-8 thanh Latin-1 -> giai ma lai cho dung tieng Viet
$tenSp = [Text.Encoding]::UTF8.GetString([Text.Encoding]::GetEncoding(28591).GetBytes([string]$item.name_snapshot))
Write-Host ("    Mã đơn: {0} | Trạng thái: {1} | Sản phẩm: {2}" -f $detail.order_code, $detail.status, $tenSp)

Write-Host '[3] Gửi đánh giá 5 sao trực tiếp tới API POST /api/reviews' -ForegroundColor Cyan
$body = @{ orderItemId = $item.id; productId = $item.product_id; rating = 5; comment = 'ST-05: danh gia khi don chua nhan hang' } | ConvertTo-Json
try {
    $r = Invoke-WebRequest "$base/api/reviews" -Method Post -ContentType 'application/json' -Headers $h -Body $body -UseBasicParsing
    Write-Host ("    Phản hồi: HTTP {0} {1}" -f [int]$r.StatusCode, $r.Content)
    Write-Host ''
    Write-Host '    Mong đợi : bị từ chối vì đơn chưa ở trạng thái "Đã nhận" (RECEIVED)' -ForegroundColor Yellow
    Write-Host '    Thực tế  : hệ thống CHẤP NHẬN đánh giá' -ForegroundColor Yellow
    Write-Host '    => ST-05 FAIL – phát hiện lỗi BUG-SYS-01' -ForegroundColor Red
} catch {
    $code = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 'không phản hồi' }
    Write-Host "    Phản hồi: HTTP $code"
    if ($code -eq 409) {
        Write-Host '    Món này đã được đánh giá ở lần chạy trước. Hãy đặt 1 đơn COD mới rồi chạy lại.' -ForegroundColor Yellow
    } else {
        Write-Host '    => Hệ thống TỪ CHỐI đánh giá – ST-05 PASS (không có lỗi)' -ForegroundColor Green
    }
}
