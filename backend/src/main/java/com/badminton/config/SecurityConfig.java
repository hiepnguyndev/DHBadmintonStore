package com.badminton.config;

import com.badminton.security.*;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http, JwtAuthenticationFilter jwt, SecurityErrorWriter errors) throws Exception {
    return http.csrf(c -> c.disable())
        .cors(c -> {})
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(c -> c.disable())
        .httpBasic(c -> c.disable())
        .logout(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/ai/chat")
                    .permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/payments/methods", "/api/payments/callbacks/vnpay").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/payments/callbacks/momo", "/api/payments/callbacks/zalopay", "/api/payments/callbacks/sepay").permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/products",
                        "/api/posts",
                        "/api/posts/*",
                        "/api/products/*",
                        "/api/products/*/variants",
                        "/api/products/*/reviews",
                        "/api/categories",
                        "/api/brands",
                        "/api/marketplace/listings",
                        "/api/marketplace/listings/*",
                        "/api/sellers/*",
                        "/media/**")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/inspector/**")
                    .hasAnyRole("INSPECTOR", "ADMIN")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) ->
                            errors.write(res, 401, "AUTH_REQUIRED", "Vui lòng đăng nhập"))
                    .accessDeniedHandler(
                        (req, res, ex) ->
                            errors.write(res, 403, "ACCESS_DENIED", "Không có quyền truy cập")))
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  FilterRegistrationBean<JwtAuthenticationFilter> preventDoubleRegistration(
      JwtAuthenticationFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins}") String origins) {
    var config = new CorsConfiguration();
    var allowed =
        Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    if (allowed.isEmpty() || allowed.stream().anyMatch(s -> s.contains("*")))
      throw new IllegalArgumentException("Explicit CORS origins required");
    config.setAllowedOrigins(allowed);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setAllowCredentials(false);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
