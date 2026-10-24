package com.badminton.common.support;

import jakarta.persistence.criteria.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

public final class Queries {
  private Queries() {}

  public static Path<?> path(Root<?> root, String key) {
    Path<?> p = root;
    for (String k : key.split("\\.")) p = p.get(k);
    return p;
  }

  public static <T> Specification<T> eq(String key, Object value) {
    return (r, q, b) -> value == null ? b.conjunction() : b.equal(path(r, key), value);
  }

  public static <T> Specification<T> all() {
    return (r, q, b) -> b.conjunction();
  }

  public static <T> Specification<T> contains(String key, String value) {
    return (r, q, b) ->
        value == null || value.isBlank()
            ? b.conjunction()
            : b.like(
                b.lower(path(r, key).as(String.class)),
                "%"
                    + value
                        .toLowerCase(Locale.ROOT)
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_")
                    + "%",
                '\\');
  }

  public static Pageable page(
      int page, int size, String sort, String defaultField, Set<String> fields) {
    Checks.bad(page >= 0 && size >= 1 && size <= 100, "page >= 0 và size trong 1–100");
    String[] parts = (sort == null ? defaultField + ",desc" : sort).split(",");
    Checks.bad(fields.contains(parts[0]), "Trường sắp xếp không hợp lệ");
    Checks.bad(
        parts.length <= 2
            && (parts.length == 1
                || parts[1].equalsIgnoreCase("asc")
                || parts[1].equalsIgnoreCase("desc")),
        "Chiều sắp xếp không hợp lệ");
    return PageRequest.of(
        page,
        size,
        Sort.by(
            parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC,
            parts[0]));
  }
}
