import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class HashAdminPassword {
  public static void main(String[] args) {
    byte[] bytes = null;
    try {
      String input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII)).readLine();
      if (input == null) throw new IllegalArgumentException("Password input is required.");
      bytes = Base64.getDecoder().decode(input);
      String password = new String(bytes, StandardCharsets.UTF_8);
      if (password.length() < 8 || bytes.length > 72)
        throw new IllegalArgumentException("Password must have at least 8 characters and at most 72 UTF-8 bytes.");
      var encoder = new BCryptPasswordEncoder(12);
      String hash = encoder.encode(password);
      if (!encoder.matches(password, hash)) throw new IllegalStateException("BCrypt verification failed.");
      System.out.println(hash);
    } catch (Exception e) {
      System.err.println("Cannot hash password. Use 8 or more characters and at most 72 UTF-8 bytes.");
      System.exit(1);
    } finally {
      if (bytes != null) Arrays.fill(bytes, (byte) 0);
    }
  }
}
