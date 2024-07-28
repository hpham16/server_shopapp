package com.project.shopapp.controllers;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.dtos.ThongKeDanhMucDTO;
import com.project.shopapp.dtos.ThongKeSanPhamDTO;
import com.project.shopapp.dtos.ThongKeThangDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.responses.OrderListResponse;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.IOrderService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final LocalizationUtils localizationUtils;
    @PostMapping("")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            BindingResult result
    ) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            Order orderResponse = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user/{user_id}") // Thêm biến đường dẫn "user_id"
    //GET http://localhost:8088/api/v1/orders/user/4
    public ResponseEntity<?> getOrders(@Valid @PathVariable("user_id") Long userId) {
        try {
            List<Order> orders = orderService.findByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    //GET http://localhost:8088/api/v1/orders/2
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@Valid @PathVariable("id") Long orderId) {
        try {
            Order existingOrder = orderService.getOrder(orderId);
            OrderResponse orderResponse = OrderResponse.fromOrder(existingOrder);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    //PUT http://localhost:8088/api/v1/orders/2
    //công việc của admin
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable long id,
            @Valid @RequestBody OrderDTO orderDTO) {

        try {
            Order order = orderService.updateOrder(id, orderDTO);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@Valid @PathVariable Long id) {
        //xóa mềm => cập nhật trường active = false
        orderService.deleteOrder(id);
        String result = localizationUtils.getLocalizedMessage(
                MessageKeys.DELETE_ORDER_SUCCESSFULLY, id);
        return ResponseEntity.ok().body(result);
    }
    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderListResponse> getOrdersByKeyword(
            @RequestParam(defaultValue = "", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        // Tạo Pageable từ thông tin trang và giới hạn
        PageRequest pageRequest = PageRequest.of(
                page, limit,
                //Sort.by("createdAt").descending()
                Sort.by("id").ascending()
        );
        Page<OrderResponse> orderPage = orderService
                                        .getOrdersByKeyword(keyword, pageRequest)
                                        .map(OrderResponse::fromOrder);
        // Lấy tổng số trang
        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orderResponses = orderPage.getContent();
        return ResponseEntity.ok(OrderListResponse
                .builder()
                .orders(orderResponses)
                .totalPages(totalPages)
                .build());
    }



    @GetMapping("/thong-ke-thang")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ThongKeThangDTO>> getRevenueStatistics(@RequestParam(value = "month", required = false) Integer month) {
        List<Object[]> result = orderService.thongKeDoanhThuTheoThang(month);

        List<ThongKeThangDTO> dtoList = result.stream()
                .map(this::convertToThongKeThangDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    private ThongKeThangDTO convertToThongKeThangDTO(Object[] record) {
        String monthStr = (record[0] != null) ? String.valueOf(((Number) record[0]).intValue()) : "N/A";
        int year = (record[1] != null) ? ((Number) record[1]).intValue() : 0;
        double totalMoney = (record[2] != null) ? ((Number) record[2]).doubleValue() : 0.0;
        Integer numberOfProducts = (record.length > 3 && record[3] != null) ? ((Number) record[3]).intValue() : null;

        return ThongKeThangDTO.builder()
                .totalMoney(totalMoney)
                .numberOfProducts(numberOfProducts)
                .month(monthStr)
                .year(year)
                .build();
    }

    @GetMapping("/thong-ke-san-pham")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> thongKeTheoSanPham(@RequestParam(value = "productName", required = false) String productName) {
        List<Object[]> result = orderService.thongKeDoanhThuTheoSanPham(productName);
        List<ThongKeSanPhamDTO> dtoList = result.stream().map(record -> {
            String productNames = (String) record[0];
            double totalMoney = ((Number) record[1]).doubleValue();
            Integer numberOfProducts = ((Number) record[2]).intValue();
            return ThongKeSanPhamDTO.builder()
                    .productName(productNames)
                    .totalMoney(totalMoney)
                    .numberOfProducts(numberOfProducts)
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/thong-ke-danh-muc")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> thongKeTheoDanhMuc(@RequestParam(value = "categoryName", required = false) String categoryName) {
        List<Object[]> result = orderService.thongKeDoanhThuTheoDanhMuc(categoryName);
        List<ThongKeDanhMucDTO> dtoList = result.stream().map(record -> {
            String categoryNames = (String) record[0];
            double totalMoney = ((Number) record[1]).doubleValue();
            Integer numberOfProducts = ((Number) record[2]).intValue();
            return ThongKeDanhMucDTO.builder()
                    .categoryName(categoryNames)
                    .totalMoney(totalMoney)
                    .numberOfProducts(numberOfProducts)
                    .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}
