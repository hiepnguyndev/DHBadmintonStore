import java.nio.file.Path;
import org.flywaydb.core.Flyway;

class CatalogSchemaCheck {
  public static void main(String[] args) {
    String url = System.getenv("TEST_DB_URL");
    if (url == null || !url.matches("jdbc:mysql://(localhost|127\\.0\\.0\\.1):3307/[a-zA-Z0-9_]+_test(?:\\?.*)?"))
      throw new IllegalArgumentException("Only isolated port 3307 test databases are allowed");
    var flyway = Flyway.configure()
        .dataSource(url, System.getenv("TEST_DB_USERNAME"), System.getenv("TEST_DB_PASSWORD"))
        .locations("filesystem:" + Path.of("backend/src/main/resources/db/migration").toAbsolutePath())
        .load();
    flyway.migrate();
    flyway.validate();
  }
}
