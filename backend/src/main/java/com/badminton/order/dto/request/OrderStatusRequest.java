package com.badminton.order.dto.request;

import com.badminton.common.enums.*;
import com.badminton.order.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record OrderStatusRequest(@NotNull OrderStatus status) {}
