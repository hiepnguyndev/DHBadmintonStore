package com.badminton.order.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.cart.entity.*;
import com.badminton.cart.repository.*;
import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.order.dto.request.*;
import com.badminton.order.dto.response.*;
import com.badminton.order.entity.*;
import com.badminton.order.mapper.*;
import com.badminton.order.repository.*;
import com.badminton.product.entity.*;
import com.badminton.product.mapper.ProductMapper;
import com.badminton.product.repository.ProductRepository;
import com.badminton.user.repository.AddressRepository;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {
  private final OrderRepository orders;
  private final OrderItemRepository lines;
  private final CartRepository carts;
  private final CartItemRepository cartItems;
  private final ProductRepository products;
  private final AddressRepository addresses;
  private final Access access;

  public OrderService(
      OrderRepository orders,
      OrderItemRepository lines,
      CartRepository carts,
      CartItemRepository cartItems,
      ProductRepository products,
      AddressRepository addresses,
      Access access) {
    this.orders = orders;
    this.lines = lines;
    this.carts = carts;
    this.cartItems = cartItems;
    this.products = products;
    this.addresses = addresses;
    this.access = access;
  }

  @org.springframework.beans.factory.annotation.Value("${app.shop.shipping-fee:0}")
  private BigDecimal shippingFee;

  @Transactional(readOnly = true)
  public OrderDetail detail(Long id) {
    Order o = found(orders.findById(id), "ORDER_NOT_FOUND");
    access.ownerOrAdmin(o.getUser().getUserId());
    return view(o);
  }

  public OrderDetail view(Order o) {
    return new OrderDetail(
        OrderMapper.toResponse(o),
        lines.findByOrderOrderIdOrderByProductProductIdAsc(o.getOrderId()).stream()
            .map(OrderItemMapper::toResponse)
            .toList());
  }

  @Transactional(readOnly = true)
  public PageResponse<OrderResponse> list(boolean admin, OrderStatus status, int page, int size) {
    org.springframework.data.jpa.domain.Specification<Order> filter =
        admin ? all() : eq("user.userId", access.current().id());
    if (status != null) filter = filter.and(eq("orderStatus", status));
    return PageResponse.from(
        orders
            .findAll(filter, page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(OrderMapper::toResponse));
  }

  public OrderDetail checkout(CheckoutRequest r) {
    var u = access.lockUser();
    Cart c = found(carts.findByUserUserId(u.getUserId()), "CART_EMPTY");
    var rows = cartItems.findByCartCartIdOrderByProductProductIdAsc(c.getCartId());
    require(!rows.isEmpty(), "CART_EMPTY", "Giỏ hàng trống");
    var address =
        found(
            addresses.findByAddressIdAndUserUserId(r.addressId(), u.getUserId()),
            "ADDRESS_NOT_FOUND");
    bad(
        r.paymentMethod() == PaymentMethod.COD || r.paymentMethod() == PaymentMethod.BANK_TRANSFER,
        "MVP hỗ trợ COD và BANK_TRANSFER mô phỏng");
    BigDecimal subtotal = BigDecimal.ZERO;
    var locked = new ArrayList<Product>();
    for (var row : rows) {
      Product p = found(products.lock(row.getProduct().getProductId()), "PRODUCT_NOT_FOUND");
      require(
          p.getStatus() == ProductStatus.ACTIVE && p.getStock() >= row.getQuantity(),
          "PRODUCT_OUT_OF_STOCK",
          "Sản phẩm không đủ số lượng");
      locked.add(p);
      subtotal =
          subtotal.add(
              ProductMapper.effectivePrice(p).multiply(BigDecimal.valueOf(row.getQuantity())));
    }
    Order o = new Order();
    o.setUser(u);
    o.setOrderCode(code("ORD"));
    o.setReceiverName(address.getReceiverName());
    o.setReceiverPhone(address.getReceiverPhone());
    o.setShippingAddress(
        address.getDetailAddress()
            + ", "
            + address.getWard()
            + ", "
            + address.getDistrict()
            + ", "
            + address.getProvince());
    o.setSubtotalAmount(money(subtotal));
    o.setShippingFee(money(shippingFee));
    o.setDiscountAmount(BigDecimal.ZERO);
    o.setTotalAmount(money(subtotal.add(shippingFee)));
    o.setPaymentMethod(r.paymentMethod());
    o.setPaymentStatus(
        r.paymentMethod() == PaymentMethod.BANK_TRANSFER
            ? PaymentStatus.PAID
            : PaymentStatus.UNPAID);
    o.setOrderStatus(OrderStatus.PENDING);
    o.setNote(r.note());
    orders.saveAndFlush(o);
    for (int n = 0; n < rows.size(); n++) {
      var row = rows.get(n);
      var p = locked.get(n);
      OrderItem i = new OrderItem();
      i.setOrder(o);
      i.setProduct(p);
      i.setProductName(p.getProductName());
      i.setProductImage(p.getThumbnailUrl());
      i.setQuantity(row.getQuantity());
      i.setUnitPrice(ProductMapper.effectivePrice(p));
      i.setTotalPrice(money(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))));
      lines.save(i);
      p.setStock(p.getStock() - i.getQuantity());
      if (p.getStock() == 0) p.setStatus(ProductStatus.OUT_OF_STOCK);
    }
    cartItems.deleteAll(rows);
    lines.flush();
    return view(o);
  }

  public OrderDetail cancel(Long id) {
    Order o = found(orders.lock(id), "ORDER_NOT_FOUND");
    access.owner(o.getUser().getUserId());
    require(
        o.getOrderStatus() == OrderStatus.PENDING || o.getOrderStatus() == OrderStatus.CONFIRMED,
        "ORDER_INVALID_STATUS",
        "Chỉ hủy trước giai đoạn chuẩn bị hàng");
    for (var i : lines.findByOrderOrderIdOrderByProductProductIdAsc(id)) {
      Product p = found(products.lock(i.getProduct().getProductId()), "PRODUCT_NOT_FOUND");
      p.setStock(p.getStock() + i.getQuantity());
      if (p.getStatus() == ProductStatus.OUT_OF_STOCK) p.setStatus(ProductStatus.ACTIVE);
    }
    if (o.getPaymentStatus() == PaymentStatus.PAID) o.setPaymentStatus(PaymentStatus.REFUNDED);
    o.setOrderStatus(OrderStatus.CANCELLED);
    return view(o);
  }

  public OrderDetail transition(Long id, OrderStatus next) {
    Order o = found(orders.lock(id), "ORDER_NOT_FOUND");
    OrderStatus expected =
        switch (o.getOrderStatus()) {
          case PENDING -> OrderStatus.CONFIRMED;
          case CONFIRMED -> OrderStatus.PREPARING;
          case PREPARING -> OrderStatus.SHIPPING;
          case SHIPPING -> OrderStatus.COMPLETED;
          default -> null;
        };
    require(
        expected == next, "ORDER_INVALID_STATUS", "Không thể bỏ qua hoặc lùi trạng thái đơn hàng");
    o.setOrderStatus(next);
    if (next == OrderStatus.COMPLETED && o.getPaymentStatus() == PaymentStatus.UNPAID)
      o.setPaymentStatus(PaymentStatus.PAID);
    return view(o);
  }
}
