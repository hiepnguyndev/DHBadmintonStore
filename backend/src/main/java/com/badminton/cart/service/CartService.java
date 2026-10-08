package com.badminton.cart.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.cart.dto.request.*;
import com.badminton.cart.dto.response.CartView;
import com.badminton.cart.entity.*;
import com.badminton.cart.repository.*;
import com.badminton.common.support.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.ProductMapper;
import com.badminton.product.repository.ProductRepository;
import com.badminton.product.service.ProductService;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
  private final CartRepository carts;
  private final CartItemRepository items;
  private final ProductRepository products;
  private final ProductService catalog;
  private final Access access;

  public CartService(
      CartRepository carts,
      CartItemRepository items,
      ProductRepository products,
      ProductService catalog,
      Access access) {
    this.carts = carts;
    this.items = items;
    this.products = products;
    this.catalog = catalog;
    this.access = access;
  }

  public Cart mine() {
    var u = access.lockUser();
    return carts
        .findByUserUserId(u.getUserId())
        .orElseGet(
            () -> {
              Cart c = new Cart();
              c.setUser(u);
              return carts.saveAndFlush(c);
            });
  }

  public CartView get() {
    Cart c = mine();
    var rows =
        items.findByCartCartIdOrderByProductProductIdAsc(c.getCartId()).stream()
            .map(
                i ->
                    new CartView.Item(
                        i.getCartItemId(),
                        catalog.view(i.getProduct()),
                        i.getQuantity(),
                        ProductMapper.effectivePrice(i.getProduct()),
                        ProductMapper.effectivePrice(i.getProduct())
                            .multiply(BigDecimal.valueOf(i.getQuantity()))))
            .toList();
    return new CartView(
        c.getCartId(),
        rows,
        rows.stream().map(CartView.Item::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  public CartView add(CartItemRequest r) {
    Cart c = mine();
    Product p = found(products.findById(r.productId()), "PRODUCT_NOT_FOUND");
    CartItem i =
        items
            .findByCartCartIdAndProductProductId(c.getCartId(), p.getProductId())
            .orElseGet(CartItem::new);
    int total = (i.getQuantity() == null ? 0 : i.getQuantity()) + r.quantity();
    require(
        p.getStatus() == ProductStatus.ACTIVE && p.getStock() >= total,
        "PRODUCT_OUT_OF_STOCK",
        "Sản phẩm không đủ số lượng");
    i.setCart(c);
    i.setProduct(p);
    i.setQuantity(total);
    items.saveAndFlush(i);
    return get();
  }

  public CartView update(Long id, QuantityRequest r) {
    Cart c = mine();
    CartItem i = found(items.findById(id), "CART_ITEM_NOT_FOUND");
    access.owner(i.getCart().getUser().getUserId());
    require(
        i.getProduct().getStatus() == ProductStatus.ACTIVE
            && i.getProduct().getStock() >= r.quantity(),
        "PRODUCT_OUT_OF_STOCK",
        "Sản phẩm không đủ số lượng");
    i.setQuantity(r.quantity());
    return get();
  }

  public void remove(Long id) {
    mine();
    CartItem i = found(items.findById(id), "CART_ITEM_NOT_FOUND");
    access.owner(i.getCart().getUser().getUserId());
    items.delete(i);
  }
}
