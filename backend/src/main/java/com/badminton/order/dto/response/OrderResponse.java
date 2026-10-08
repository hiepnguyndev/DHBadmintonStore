package com.badminton.order.dto.response;

import com.badminton.order.entity.OrderStatus;
import com.badminton.order.entity.PaymentMethod;
import com.badminton.order.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.*;

public record OrderResponse(
    Long orderId,
    Long userId,
    String orderCode,
    String receiverName,
    String receiverPhone,
    String shippingAddress,
    BigDecimal subtotalAmount,
    BigDecimal shippingFee,
    BigDecimal discountAmount,
    BigDecimal totalAmount,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    OrderStatus orderStatus,
    String note,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
