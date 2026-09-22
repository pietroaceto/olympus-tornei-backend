package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class V2__seed_admin_user extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String username = System.getenv().getOrDefault("ADMIN_USERNAME", "admin");
        String password = System.getenv("ADMIN_PASSWORD");

        if (password == null) {
            password = "ChangeMe123!";
            System.out.println("[V2__seed_admin_user] ADMIN_PASSWORD non impostata: uso la password di default "
                    + "'ChangeMe123!'. Cambiarla al primo accesso o prima del deploy in produzione.");
        }

        String passwordHash = new BCryptPasswordEncoder().encode(password);

        Connection connection = context.getConnection();
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password_hash, role, created_at) VALUES (?, ?, 'ADMIN', ?)")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            ps.executeUpdate();
        }
    }
}
