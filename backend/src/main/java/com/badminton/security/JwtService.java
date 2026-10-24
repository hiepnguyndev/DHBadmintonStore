package com.badminton.security;

import com.badminton.user.entity.User;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  private final String issuer;
  private final long ttl;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.ttl-seconds}") long ttl) {
    byte[] bytes = Base64.getDecoder().decode(secret);
    if (bytes.length < 32 || ttl <= 0)
      throw new IllegalArgumentException(
          "JWT requires >=32 random secret bytes and a positive TTL");
    var key = new SecretKeySpec(bytes, "HmacSHA256");
    this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
    var d = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    d.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    this.decoder = d;
    this.issuer = issuer;
    this.ttl = ttl;
  }

  public String issue(User user) {
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer(issuer)
            .subject(user.getUserId().toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl))
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  public Long userId(String token) {
    var jwt = decoder.decode(token);
    if (jwt.getExpiresAt() == null || !jwt.getExpiresAt().isAfter(Instant.now()))
      throw new BadJwtException("Expired token");
    return Long.valueOf(jwt.getSubject());
  }
}
