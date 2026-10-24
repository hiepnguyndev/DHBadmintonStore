package com.badminton.recommendation.entity;

import com.badminton.common.enums.PlayStyle;
import com.badminton.common.enums.PlayType;
import com.badminton.common.enums.SkillLevel;
import com.badminton.common.enums.WristStrength;
import com.badminton.user.entity.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "player_profiles")
public class PlayerProfile extends com.badminton.common.entity.BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "profile_id")
  private Long profileId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "skill_level", nullable = false, length = 30)
  private SkillLevel skillLevel;

  @Enumerated(EnumType.STRING)
  @Column(name = "play_style", nullable = false, length = 30)
  private PlayStyle playStyle;

  @Enumerated(EnumType.STRING)
  @Column(name = "play_type", nullable = false, length = 30)
  private PlayType playType;

  @Enumerated(EnumType.STRING)
  @Column(name = "wrist_strength", nullable = false, length = 30)
  private WristStrength wristStrength;

  @Column(name = "preferred_weight", nullable = false, length = 10)
  private String preferredWeight;

  @Column(name = "min_budget", nullable = false, precision = 15, scale = 2)
  private BigDecimal minBudget;

  @Column(name = "max_budget", nullable = false, precision = 15, scale = 2)
  private BigDecimal maxBudget;

  @Column(name = "dominant_hand", length = 10) private String dominantHand;
  @Column(name = "balance_type", length = 20) private String balanceType;
  @Column(name = "shaft_flexibility", length = 20) private String shaftFlexibility;
  @Column(name = "shirt_size", length = 12) private String shirtSize;
  @Column(name = "pants_size", length = 12) private String pantsSize;
  @Column(name = "shoe_size", length = 12) private String shoeSize;
  @Column(name = "favorite_colors", length = 255) private String favoriteColors;
  @Column(name = "favorite_brands", length = 255) private String favoriteBrands;

  public Long getProfileId() {
    return profileId;
  }

  public void setProfileId(Long value) {
    profileId = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User value) {
    user = value;
  }

  public SkillLevel getSkillLevel() {
    return skillLevel;
  }

  public void setSkillLevel(SkillLevel value) {
    skillLevel = value;
  }

  public PlayStyle getPlayStyle() {
    return playStyle;
  }

  public void setPlayStyle(PlayStyle value) {
    playStyle = value;
  }

  public PlayType getPlayType() {
    return playType;
  }

  public void setPlayType(PlayType value) {
    playType = value;
  }

  public WristStrength getWristStrength() {
    return wristStrength;
  }

  public void setWristStrength(WristStrength value) {
    wristStrength = value;
  }

  public String getPreferredWeight() {
    return preferredWeight;
  }

  public void setPreferredWeight(String value) {
    preferredWeight = value;
  }

  public BigDecimal getMinBudget() {
    return minBudget;
  }

  public void setMinBudget(BigDecimal value) {
    minBudget = value;
  }

  public BigDecimal getMaxBudget() {
    return maxBudget;
  }

  public void setMaxBudget(BigDecimal value) {
    maxBudget = value;
  }

  public String getDominantHand() { return dominantHand; }
  public void setDominantHand(String value) { dominantHand = value; }
  public String getBalanceType() { return balanceType; }
  public void setBalanceType(String value) { balanceType = value; }
  public String getShaftFlexibility() { return shaftFlexibility; }
  public void setShaftFlexibility(String value) { shaftFlexibility = value; }
  public String getShirtSize() { return shirtSize; }
  public void setShirtSize(String value) { shirtSize = value; }
  public String getPantsSize() { return pantsSize; }
  public void setPantsSize(String value) { pantsSize = value; }
  public String getShoeSize() { return shoeSize; }
  public void setShoeSize(String value) { shoeSize = value; }
  public String getFavoriteColors() { return favoriteColors; }
  public void setFavoriteColors(String value) { favoriteColors = value; }
  public String getFavoriteBrands() { return favoriteBrands; }
  public void setFavoriteBrands(String value) { favoriteBrands = value; }
}
