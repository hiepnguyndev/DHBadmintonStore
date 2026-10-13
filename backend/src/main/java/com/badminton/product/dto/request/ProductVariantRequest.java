package com.badminton.product.dto.request;
import jakarta.validation.constraints.*;
public record ProductVariantRequest(@NotBlank @Size(max=100) String sku,@NotBlank @Size(max=80) String color,@NotBlank @Size(max=50) String size,@NotNull @Min(0) Integer stock,@Pattern(regexp="ACTIVE|INACTIVE|OUT_OF_STOCK") String status) {}
