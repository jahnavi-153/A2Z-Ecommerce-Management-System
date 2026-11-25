import java.sql.*;
import java.util.Scanner;

public class CreateAdmin {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter admin name: ");
        String name = sc.nextLine();

        System.out.print("Enter admin email: ");
        String email = sc.nextLine().trim().toLowerCase();

        System.out.print("Enter admin password: ");
        String password = sc.nextLine();

        if (password.length() < 8
                || password.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8).length > 72) {

            System.out.println("Invalid password length.");
            return;
        }

        String sql = "INSERT INTO users "
                + "(name, email, password_hash, role) "
                + "VALUES (?, ?, ?, 'ADMIN')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3,
                    PasswordUtil.hashPassword(password));

            ps.executeUpdate();

            System.out.println("Admin created successfully!");

        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }
}