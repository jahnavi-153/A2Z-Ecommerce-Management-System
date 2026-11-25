import java.sql.*;

public class AuthService {

    public static boolean register(
            String name, String email,
            String phone, String password)
            throws SQLException {

        String sql = "INSERT INTO users "
                + "(name, email, phone, password_hash, role) "
                + "VALUES (?, ?, ?, ?, 'CUSTOMER')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4,
                    PasswordUtil.hashPassword(password));

            return ps.executeUpdate() > 0;
        }
    }

    public static User login(String email, String password)
            throws SQLException {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    String hash = rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(password, hash)) {

                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                        );
                    }
                }
            }
        }

        return null;
    }
}