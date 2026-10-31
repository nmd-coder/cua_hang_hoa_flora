export function formatOrderStatus(status?: string | null): string {
  if (!status) return "Chờ xử lý";
  switch (status.toUpperCase()) {
    case "PENDING":
      return "Chờ xử lý";
    case "PROCESSING":
      return "Đang chuẩn bị hoa";
    case "SHIPPED":
      return "Đang giao hàng";
    case "DELIVERED":
      return "Đã giao hàng";
    case "RECEIVED":
      return "Khách đã nhận";
    case "COMPLETED":
      return "Hoàn thành";
    case "CANCELLED":
      return "Đã hủy";
    case "CANCEL_PENDING":
      return "Đang xử lý hủy";
    case "REFUNDED":
      return "Đã hoàn tiền";
    default:
      return status;
  }
}

export function formatPaymentStatus(status?: string | null): string {
  if (!status) return "Chưa thu tiền";
  switch (status.toUpperCase()) {
    case "UNPAID":
      return "Chưa thu tiền";
    case "PAID":
      return "Đã thu tiền";
    case "REFUNDED":
      return "Đã hoàn tiền";
    case "FAILED":
      return "Thất bại";
    default:
      return status;
  }
}

