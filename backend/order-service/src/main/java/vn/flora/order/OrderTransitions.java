package vn.flora.order;

import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class OrderTransitions {
    private OrderTransitions() {}

    static void verify(String current, String target, boolean owner, Set<String> roles) {
        boolean isStaffOrAdmin = roles.contains("STAFF") || roles.contains("ADMIN");

        if ("PROCESSING".equals(target)) {
            if (!isStaffOrAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xử lý đơn");
            }
            if (!"PENDING".equals(current)) throw conflict();
            return;
        }

        if ("SHIPPED".equals(target)) {
            if (!isStaffOrAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền cập nhật giao hàng");
            }
            if (!"PROCESSING".equals(current)) throw conflict();
            return;
        }

        if ("COMPLETED".equals(target)) {
            if (!isStaffOrAdmin) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền hoàn tất đơn");
            }
            if (!"SHIPPED".equals(current)) throw conflict();
            return;
        }

        if ("RECEIVED".equals(target)) {
            if (!owner) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ chủ đơn được xác nhận nhận hàng");
            }
            if (!"COMPLETED".equals(current)) throw conflict();
            return;
        }

        if ("CANCELLED".equals(target)) {
            if (isStaffOrAdmin) {
                if (!"PENDING".equals(current) && !"PROCESSING".equals(current)) throw conflict();
                return;
            }
            if (owner) {
                if (!"PENDING".equals(current)) throw conflict();
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền hủy đơn");
        }

        throw conflict();
    }

    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Chuyển trạng thái không hợp lệ");
    }
}
