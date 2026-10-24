package com.badminton.payment;

import com.fasterxml.jackson.databind.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class GatewayHttpClient {
  private final ObjectMapper json;
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
      .followRedirects(HttpClient.Redirect.NEVER).build();
  public GatewayHttpClient(ObjectMapper json) { this.json = json; }
  public JsonNode post(String url, Map<String, Object> body, boolean form) {
    try {
      var values = new LinkedHashMap<String, String>();
      body.forEach((k, v) -> values.put(k, String.valueOf(v)));
      String payload = form ? PaymentCrypto.query(values) : json.writeValueAsString(body);
      var request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(35))
          .header("Content-Type", form ? "application/x-www-form-urlencoded" : "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw new IllegalStateException("Payment provider unavailable");
      return json.readTree(response.body());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); throw new IllegalStateException("Payment request interrupted");
    } catch (java.io.IOException e) { throw new IllegalStateException("Payment provider unavailable"); }
  }
}
