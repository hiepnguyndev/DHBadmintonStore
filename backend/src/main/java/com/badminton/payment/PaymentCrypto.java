package com.badminton.payment;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class PaymentCrypto {
  private PaymentCrypto() {}
  public static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
  public static String hmac(String algorithm, String secret, String data) {
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
      return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.GeneralSecurityException e) { throw new IllegalStateException("Cannot sign payment", e); }
  }
  public static boolean matches(String expected, String supplied) {
    try { return supplied != null && MessageDigest.isEqual(HexFormat.of().parseHex(expected), HexFormat.of().parseHex(supplied)); }
    catch (IllegalArgumentException e) { return false; }
  }
  public static String query(Map<String, String> params) {
    return new TreeMap<>(params).entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .map(e -> encode(e.getKey()) + "=" + encode(e.getValue())).collect(Collectors.joining("&"));
  }
}
