package com.badminton.order.dto.request;

import com.badminton.common.enums.*;
import com.badminton.order.entity.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record CheckoutRequest(
    @NotNull Long addressId, @NotNull PaymentMethod paymentMethod, @Size(max = 2000) String note) {}
