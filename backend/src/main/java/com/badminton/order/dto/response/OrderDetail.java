package com.badminton.order.dto.response;

import java.util.List;

public record OrderDetail(OrderResponse order, List<OrderItemResponse> items) {}
