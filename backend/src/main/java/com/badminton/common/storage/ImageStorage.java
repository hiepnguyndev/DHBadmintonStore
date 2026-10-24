package com.badminton.common.storage;

import static com.badminton.common.support.Checks.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageStorage {
  private final Path root;

  public ImageStorage(@Value("${app.storage.root:./var/uploads}") String root) {
    this.root = Path.of(root).toAbsolutePath().normalize();
  }

  public record Stored(String key, String mime) {}

  public Stored store(MultipartFile file, boolean privateFile) {
    bad(
        file != null && !file.isEmpty() && file.getSize() <= 5 * 1024 * 1024,
        "Ảnh phải từ 1 byte đến 5 MB");
    try {
      byte[] bytes = file.getBytes();
      String mime =
          bytes.length >= 8
                  && bytes[0] == (byte) 0x89
                  && bytes[1] == 0x50
                  && bytes[2] == 0x4e
                  && bytes[3] == 0x47
              ? "image/png"
              : bytes.length >= 3
                      && bytes[0] == (byte) 0xff
                      && bytes[1] == (byte) 0xd8
                      && bytes[2] == (byte) 0xff
                  ? "image/jpeg"
                  : null;
      bad(mime != null, "Chỉ nhận ảnh PNG hoặc JPEG hợp lệ");
      try (var input =
          javax.imageio.ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
        var readers = javax.imageio.ImageIO.getImageReaders(input);
        bad(readers.hasNext(), "Không đọc được ảnh");
        var reader = readers.next();
        try {
          reader.setInput(input);
          bad(
              (long) reader.getWidth(0) * reader.getHeight(0) <= 24000000,
              "Ảnh tối đa 24 megapixel");
          bad(reader.read(0) != null, "Ảnh không hợp lệ");
        } finally {
          reader.dispose();
        }
      }
      String key = UUID.randomUUID() + (mime.equals("image/png") ? ".png" : ".jpg");
      Path target = root.resolve(privateFile ? "private" : "public");
      Files.createDirectories(target);
      Files.write(target.resolve(key), bytes, StandardOpenOption.CREATE_NEW);
      if (org.springframework.transaction.support.TransactionSynchronizationManager
          .isSynchronizationActive())
        org.springframework.transaction.support.TransactionSynchronizationManager
            .registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                  public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED)
                      try {
                        Files.deleteIfExists(target.resolve(key));
                      } catch (IOException ex) {
                        org.slf4j.LoggerFactory.getLogger(ImageStorage.class)
                            .warn("Could not remove rolled-back upload {}", key);
                      }
                  }
                });
      return new Stored(key, mime);
    } catch (IOException e) {
      throw new IllegalStateException("Không lưu được ảnh", e);
    }
  }

  public byte[] read(String key, boolean privateFile) {
    bad(key != null && key.matches("[a-f0-9-]{36}\\.(png|jpg)"), "Đường dẫn ảnh không hợp lệ");
    try {
      return Files.readAllBytes(root.resolve(privateFile ? "private" : "public").resolve(key));
    } catch (IOException ex) {
      throw new com.badminton.exception.BusinessException(
          org.springframework.http.HttpStatus.NOT_FOUND, "IMAGE_NOT_FOUND", "Không tìm thấy ảnh");
    }
  }

  public String privatePreview(String key, String mime) {
    return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(read(key, true));
  }
}
