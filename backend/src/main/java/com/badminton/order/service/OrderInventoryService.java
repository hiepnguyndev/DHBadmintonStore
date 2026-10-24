package com.badminton.order.service;

import static com.badminton.common.support.Checks.*;

import com.badminton.order.entity.Order;
import com.badminton.order.entity.OrderItem;
import com.badminton.product.entity.ProductStatus;
import com.badminton.order.repository.OrderItemRepository;
import com.badminton.product.entity.Product;
import com.badminton.product.entity.ProductVariant;
import com.badminton.product.repository.ProductRepository;
import com.badminton.product.repository.ProductVariantRepository;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class OrderInventoryService {
  private final OrderItemRepository lines;
  private final ProductRepository products;
  private final ProductVariantRepository variants;

  public OrderInventoryService(OrderItemRepository lines, ProductRepository products, ProductVariantRepository variants) {
    this.lines = lines; this.products = products; this.variants = variants;
  }

  public void commit(Order order) {
    if (Boolean.TRUE.equals(order.getInventoryCommitted())) return;
    var items = lines.findByOrderOrderIdOrderByProductProductIdAsc(order.getOrderId());
    var demand = new LinkedHashMap<Long, Integer>();
    for (var item : items) demand.merge(item.getProduct().getProductId(), item.getQuantity(), Integer::sum);
    var lockedProducts = new LinkedHashMap<Long, Product>();
    for (var entry : demand.entrySet()) {
      var product = found(products.lock(entry.getKey()), "PRODUCT_NOT_FOUND");
      require(product.getStatus() == ProductStatus.ACTIVE && product.getStock() >= entry.getValue(),
          "PRODUCT_OUT_OF_STOCK", "Sản phẩm không đủ số lượng");
      lockedProducts.put(entry.getKey(), product);
    }
    for (var item : items) if (item.getVariant() != null) {
      var variant = found(variants.lock(item.getVariant().getVariantId()), "VARIANT_NOT_FOUND");
      require("ACTIVE".equals(variant.getStatus()) && variant.getStock() >= item.getQuantity(),
          "VARIANT_OUT_OF_STOCK", "Biến thể không đủ tồn kho");
      variant.setStock(variant.getStock() - item.getQuantity());
      if (variant.getStock() == 0) variant.setStatus("OUT_OF_STOCK");
    }
    for (var entry : demand.entrySet()) {
      var product = lockedProducts.get(entry.getKey());
      product.setStock(product.getStock() - entry.getValue());
      if (product.getStock() == 0) product.setStatus(ProductStatus.OUT_OF_STOCK);
    }
    order.setInventoryCommitted(true);
  }

  public void release(Order order) {
    if (!Boolean.TRUE.equals(order.getInventoryCommitted())) return;
    var restored = new LinkedHashSet<Product>();
    for (OrderItem item : lines.findByOrderOrderIdOrderByProductProductIdAsc(order.getOrderId())) {
      var product = found(products.lock(item.getProduct().getProductId()), "PRODUCT_NOT_FOUND");
      product.setStock(product.getStock() + item.getQuantity());
      if (product.getStatus() == ProductStatus.OUT_OF_STOCK) product.setStatus(ProductStatus.ACTIVE);
      restored.add(product);
      if (item.getVariant() != null) {
        var variant = found(variants.lock(item.getVariant().getVariantId()), "VARIANT_NOT_FOUND");
        variant.setStock(variant.getStock() + item.getQuantity());
        if ("OUT_OF_STOCK".equals(variant.getStatus())) variant.setStatus("ACTIVE");
      }
    }
    syncVariantProducts(restored);
    order.setInventoryCommitted(false);
  }

  private void syncVariantProducts(Collection<Product> productsToSync) {
    for (var product : productsToSync) {
      if (!variants.existsByProductProductId(product.getProductId())) continue;
      int total = variants.findByProductProductIdOrderByColorAscSizeAsc(product.getProductId()).stream()
          .mapToInt(ProductVariant::getStock).sum();
      product.setStock(total);
      if (product.getStatus() != ProductStatus.INACTIVE)
        product.setStatus(total == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);
    }
  }
}
