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
}
