package com.badminton.user.entity;

import com.badminton.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "address_id")
  private Long addressId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "receiver_name", nullable = false, length = 100)
  private String receiverName;

  @Column(name = "receiver_phone", nullable = false, length = 20)
  private String receiverPhone;

  @Column(nullable = false, length = 100)
  private String province;

  @Column(nullable = false, length = 100)
  private String district;

  @Column(nullable = false, length = 100)
  private String ward;

  @Column(name = "detail_address", nullable = false, length = 255)
  private String detailAddress;

  @Column(name = "is_default", nullable = false)
  private boolean defaultAddress;

  protected Address() {}

  public Address(User user) {
    this.user = user;
  }

  public void update(
      String name,
      String phone,
      String province,
      String district,
      String ward,
      String detail,
      boolean isDefault) {
    receiverName = name;
    receiverPhone = phone;
    this.province = province;
    this.district = district;
    this.ward = ward;
    detailAddress = detail;
    defaultAddress = isDefault;
  }

  public void clearDefault() {
    defaultAddress = false;
  }

  public Long getAddressId() {
    return addressId;
  }

  public User getUser() {
    return user;
  }

  public String getReceiverName() {
    return receiverName;
  }

  public String getReceiverPhone() {
    return receiverPhone;
  }

  public String getProvince() {
    return province;
  }

  public String getDistrict() {
    return district;
  }

  public String getWard() {
    return ward;
  }

  public String getDetailAddress() {
    return detailAddress;
  }

  public boolean isDefaultAddress() {
    return defaultAddress;
  }
}
