package com.badminton.product.dto.response;
public record ProductVariantResponse(Long variantId,Long productId,String sku,String color,String size,Integer stock,String status) {}
