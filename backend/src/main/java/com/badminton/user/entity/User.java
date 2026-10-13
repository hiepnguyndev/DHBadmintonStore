package com.badminton.user.entity;

import com.badminton.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Column(nullable = true, unique = true, length = 150)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(length = 20, unique = true)
  private String phone;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(length = 12)
  private String gender;

  @Column(name = "height_cm")
  private Integer heightCm;

  @Column(name = "weight_kg", precision = 5, scale = 2)
  private BigDecimal weightKg;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private UserStatus status;

  protected User() {}

  public static User register(String fullName, String email, String passwordHash, String phone) {
    User u = new User();
    u.fullName = fullName;
    u.email = email;
    u.passwordHash = passwordHash;
    u.phone = phone;
    u.role = Role.USER;
    u.status = UserStatus.ACTIVE;
    return u;
  }

  public void updateProfile(String name, String phone, String avatarUrl, LocalDate dateOfBirth, String gender, Integer heightCm, BigDecimal weightKg) {
    this.fullName = name;
    this.phone = phone;
    this.avatarUrl = avatarUrl;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.heightCm = heightCm;
    this.weightKg = weightKg;
  }

  public void changePassword(String hash) {
    passwordHash = hash;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public Long getUserId() {
    return userId;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getPhone() {
    return phone;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public LocalDate getDateOfBirth() { return dateOfBirth; }
  public String getGender() { return gender; }
  public Integer getHeightCm() { return heightCm; }
  public BigDecimal getWeightKg() { return weightKg; }

  public Role getRole() {
    return role;
  }

  public UserStatus getStatus() {
    return status;
  }
}
