package com.example.demo.web.order;

import com.example.demo.application.service.CreateOrderService;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand;
import com.example.demo.application.service.CreateOrderService.CreateOrderResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for order creation.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CreateOrderController {

    private final CreateOrderService createOrderService;

    public record CreateOrderRequest(
            @NotNull(message = "商家ID不能为空") String merchantId,
            @NotNull(message = "订单项不能为空") @Size(min = 1, message = "订单至少包含一个餐品") List<@Valid OrderItemRequest> items,
            @NotNull(message = "配送信息不能为空") @Valid DeliveryInfoRequest deliveryInfo,
            @Size(max = 200, message = "备注长度不能超过200字符") String remark) {
        public record OrderItemRequest(
                @NotNull(message = "餐品ID不能为空") String dishId,
                @NotNull(message = "餐品名称不能为空") String dishName,
                @NotNull(message = "数量不能为空") @Min(value = 1, message = "数量必须大于0") Integer quantity,
                @NotNull(message = "价格不能为空") BigDecimal price) {}

        public record DeliveryInfoRequest(
                @NotNull(message = "收货人姓名不能为空") String recipientName,
                @NotNull(message = "收货人手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
                        String recipientPhone,
                @NotNull(message = "收货地址不能为空") @Size(max = 500, message = "地址长度不能超过500字符") String address) {}
    }

    public record CreateOrderResponse(int code, String message, OrderData data) {
        public record OrderData(
                String orderId, String orderNumber, String status, PricingData pricing, String createdAt) {}

        public record PricingData(
                BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {}
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(
            @RequestBody @Valid CreateOrderRequest request, @AuthenticationPrincipal User user) {
        // Convert request to command
        CreateOrderCommand command = toCommand(request, user);

        // Execute the command
        CreateOrderResult result = createOrderService.createOrder(command);

        // Convert result to response
        return toResponse(result);
    }

    private CreateOrderCommand toCommand(CreateOrderRequest request, User user) {
        List<CreateOrderCommand.OrderItemDto> itemDtos = request.items().stream()
                .map(item -> new CreateOrderCommand.OrderItemDto(
                        item.dishId(), item.dishName(), item.quantity(), item.price()))
                .toList();

        CreateOrderCommand.DeliveryInfoDto deliveryInfoDto = new CreateOrderCommand.DeliveryInfoDto(
                request.deliveryInfo().recipientName(),
                request.deliveryInfo().recipientPhone(),
                request.deliveryInfo().address());

        return new CreateOrderCommand(
                user.getUsername(), request.merchantId(), itemDtos, deliveryInfoDto, request.remark());
    }

    private CreateOrderResponse toResponse(CreateOrderResult result) {
        CreateOrderResponse.PricingData pricingData = new CreateOrderResponse.PricingData(
                result.pricing().itemsTotal(),
                result.pricing().packagingFee(),
                result.pricing().deliveryFee(),
                result.pricing().finalAmount());

        CreateOrderResponse.OrderData orderData = new CreateOrderResponse.OrderData(
                result.orderId(),
                result.orderNumber(),
                result.status(),
                pricingData,
                result.createdAt().toString());

        return new CreateOrderResponse(0, "订单创建成功", orderData);
    }
}
