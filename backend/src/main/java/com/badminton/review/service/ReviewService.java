package com.badminton.review.service;

import static com.badminton.common.support.Checks.*;
import static com.badminton.common.support.Queries.*;

import com.badminton.common.dto.PageResponse;
import com.badminton.common.support.*;
import com.badminton.order.entity.OrderStatus;
import com.badminton.order.repository.OrderItemRepository;
import com.badminton.review.dto.request.*;
import com.badminton.review.dto.response.*;
import com.badminton.review.entity.*;
import com.badminton.review.mapper.*;
import com.badminton.review.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {
  private final ReviewRepository reviews;
  private final OrderItemRepository items;
  private final Access access;

  public ReviewService(ReviewRepository reviews, OrderItemRepository items, Access access) {
    this.reviews = reviews;
    this.items = items;
    this.access = access;
  }

  public ReviewResponse create(ReviewRequest r) {
    var u = access.lockUser();
    var i = found(items.findById(r.orderItemId()), "ORDER_ITEM_NOT_FOUND");
    access.owner(i.getOrder().getUser().getUserId());
    require(
        i.getOrder().getOrderStatus() == OrderStatus.COMPLETED,
        "ORDER_INVALID_STATUS",
        "Chỉ đánh giá đơn đã hoàn thành");
    require(
        !reviews.existsByUserUserIdAndOrderItemOrderItemId(u.getUserId(), r.orderItemId()),
        "REVIEW_EXISTS",
        "Đã đánh giá lần mua này");
    Review v = new Review();
    v.setUser(u);
    v.setProduct(i.getProduct());
    v.setOrderItem(i);
    v.setRating(r.rating().byteValue());
    v.setComment(r.comment());
    return ReviewMapper.toResponse(reviews.saveAndFlush(v));
  }

  @Transactional(readOnly = true)
  public PageResponse<ReviewResponse> list(Long productId, int page, int size) {
    return PageResponse.from(
        reviews
            .findAll(
                eq("product.productId", productId),
                page(page, size, null, "createdAt", Set.of("createdAt")))
            .map(ReviewMapper::toResponse));
  }
}
