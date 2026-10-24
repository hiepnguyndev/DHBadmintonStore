package com.badminton.common.storage;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class MediaController {
  private final ImageStorage storage;

  public MediaController(ImageStorage storage) {
    this.storage = storage;
  }

  @GetMapping("/media/{key}")
  public ResponseEntity<byte[]> image(@PathVariable String key) {
    byte[] data = storage.read(key, false);
    return ResponseEntity.ok()
        .header("X-Content-Type-Options", "nosniff")
        .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
        .contentType(key.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
        .body(data);
  }
}
