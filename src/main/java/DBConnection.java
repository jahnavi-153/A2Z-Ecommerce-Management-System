import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection()
            throws SQLException {

        String url = "jdbc:mysql://localhost:3306/a2z_db";
        String user = "root";
        String password = System.getenv("A2Z_DB_PASSWORD");

        return DriverManager.getConnection(url, user, password);
    }
}