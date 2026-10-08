package com.badminton.escrow.entity;

import com.badminton.marketorder.entity.MarketplaceOrder;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "escrow_transactions")
public class EscrowTransaction extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "escrow_id")
  private Long escrowId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "market_order_id", nullable = false)
  private MarketplaceOrder marketOrder;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private EscrowStatus status;

  @Column(name = "payment_reference", nullable = true, length = 255)
  private String paymentReference;

  @Column(name = "paid_at", nullable = true)
  private LocalDateTime paidAt;

  @Column(name = "held_at", nullable = true)
  private LocalDateTime heldAt;

  @Column(name = "released_at", nullable = true)
  private LocalDateTime releasedAt;

  @Column(name = "refunded_at", nullable = true)
  private LocalDateTime refundedAt;

  public Long getEscrowId() {
    return escrowId;
  }

  public void setEscrowId(Long value) {
    escrowId = value;
  }

  public MarketplaceOrder getMarketOrder() {
    return marketOrder;
  }

  public void setMarketOrder(MarketplaceOrder value) {
    marketOrder = value;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal value) {
    amount = value;
  }

  public EscrowStatus getStatus() {
    return status;
  }

  public void setStatus(EscrowStatus value) {
    status = value;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public void setPaymentReference(String value) {
    paymentReference = value;
  }

  public LocalDateTime getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(LocalDateTime value) {
    paidAt = value;
  }

  public LocalDateTime getHeldAt() {
    return heldAt;
  }

  public void setHeldAt(LocalDateTime value) {
    heldAt = value;
  }

  public LocalDateTime getReleasedAt() {
    return releasedAt;
  }

  public void setReleasedAt(LocalDateTime value) {
    releasedAt = value;
  }

  public LocalDateTime getRefundedAt() {
    return refundedAt;
  }

  public void setRefundedAt(LocalDateTime value) {
    refundedAt = value;
  }
}
