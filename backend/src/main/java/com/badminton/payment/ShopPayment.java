package com.badminton.payment;

import com.badminton.common.entity.BaseEntity;
import com.badminton.order.entity.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_payments")
public class ShopPayment extends BaseEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "payment_id")
  private Long paymentId;
  @Column(name = "order_id", nullable = false)
  private Long orderId;
  @Column(nullable = false, length = 50)
  private String reference;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
  private PaymentMethod method;
  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;
  @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
  private PaymentStatus status;
  @Column(name = "checkout_url", columnDefinition = "TEXT")
  private String checkoutUrl;
  @Column(name = "bank_name", length = 100)
  private String bankName;
  @Column(name = "bank_id", length = 30)
  private String bankId;
  @Column(name = "account_number", length = 30)
  private String accountNumber;
  @Column(name = "account_name", length = 100)
  private String accountName;
  @Column(name = "external_key", length = 200)
  private String externalKey;
  @Column(name = "refund_key", length = 200)
  private String refundKey;
  @Column(name = "expires_at")
  private LocalDateTime expiresAt;
  public Long getPaymentId() { return paymentId; }
  public void setPaymentId(Long value) { paymentId = value; }
  public Long getOrderId() { return orderId; }
  public void setOrderId(Long value) { orderId = value; }
  public String getReference() { return reference; }
  public void setReference(String value) { reference = value; }
  public PaymentMethod getMethod() { return method; }
  public void setMethod(PaymentMethod value) { method = value; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal value) { amount = value; }
  public PaymentStatus getStatus() { return status; }
  public void setStatus(PaymentStatus value) { status = value; }
  public String getCheckoutUrl() { return checkoutUrl; }
  public void setCheckoutUrl(String value) { checkoutUrl = value; }
  public String getBankName() { return bankName; }
  public void setBankName(String value) { bankName = value; }
  public String getBankId() { return bankId; }
  public void setBankId(String value) { bankId = value; }
  public String getAccountNumber() { return accountNumber; }
  public void setAccountNumber(String value) { accountNumber = value; }
  public String getAccountName() { return accountName; }
  public void setAccountName(String value) { accountName = value; }
  public String getExternalKey() { return externalKey; }
  public void setExternalKey(String value) { externalKey = value; }
  public String getRefundKey() { return refundKey; }
  public void setRefundKey(String value) { refundKey = value; }
  public LocalDateTime getExpiresAt() { return expiresAt; }
  public void setExpiresAt(LocalDateTime value) { expiresAt = value; }
}
