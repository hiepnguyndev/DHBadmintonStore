package com.badminton.security;

import static org.assertj.core.api.Assertions.*;

import com.badminton.user.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {
  final String secret =
      Base64.getEncoder().encodeToString(new byte[48]); // Test-only key, never application config.
  JwtService service = new JwtService(secret, "test-issuer", 1800);

  @Test
  void roundTripValidToken() {
    User u = User.register("Name", "a@example.com", "hash", null);
    ReflectionTestUtils.setField(u, "userId", 42L);
    assertThat(service.userId(service.issue(u))).isEqualTo(42L);
  }

  @Test
  void invalidTokenRejected() {
    assertThatThrownBy(() -> service.userId("bad-token")).isInstanceOf(JwtException.class);
  }

  String token(String issuer, Instant expires) {
    var key = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
    var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject("1")
            .issuedAt(Instant.now().minusSeconds(600))
            .expiresAt(expires)
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  @Test
  void expiredTokenRejected() {
    assertThatThrownBy(() -> service.userId(token("test-issuer", Instant.now().minusSeconds(120))))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void wrongIssuerRejected() {
    assertThatThrownBy(() -> service.userId(token("other-issuer", Instant.now().plusSeconds(120))))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void wrongSignatureRejected() {
    String other = Base64.getEncoder().encodeToString("a".repeat(48).getBytes());
    assertThatThrownBy(
            () ->
                new JwtService(other, "test-issuer", 1800)
                    .userId(token("test-issuer", Instant.now().plusSeconds(120))))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void weakSecretRejected() {
    assertThatThrownBy(
            () -> new JwtService(Base64.getEncoder().encodeToString(new byte[8]), "test", 1800))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
