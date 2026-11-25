import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    public static List<Customer> getCustomers()
            throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String sql =
            "SELECT u.user_id, u.name, u.email, "
            + "u.phone, u.created_at, "
            + "COUNT(o.order_id) AS total_orders, "
            + "SUM(CASE WHEN o.status = 'DELIVERED' "
            + "THEN 1 ELSE 0 END) AS delivered_orders, "
            + "SUM(CASE WHEN o.status = 'DELIVERED' "
            + "THEN o.total_amount ELSE 0 END) AS total_spent "
            + "FROM users u "
            + "LEFT JOIN orders o "
            + "ON u.user_id = o.user_id "
            + "WHERE u.role = 'CUSTOMER' "
            + "GROUP BY u.user_id, u.name, u.email, "
            + "u.phone, u.created_at "
            + "ORDER BY total_spent DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                customers.add(new Customer(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("total_orders"),
                    rs.getInt("delivered_orders"),
                    rs.getBigDecimal("total_spent")
                ));
            }
        }

        return customers;
    }

    public static List<Order> getCustomerOrders(int userId)
            throws SQLException {

        List<Order> orders = new ArrayList<>();

        String sql =
            "SELECT o.*, u.name AS customer_name "
            + "FROM orders o "
            + "JOIN users u ON o.user_id = u.user_id "
            + "WHERE o.user_id = ? "
            + "ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    orders.add(new Order(
                        rs.getInt("order_id"),
                        rs.getInt("user_id"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_date"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("delivery_address"),
                        rs.getString("status"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status")
                    ));
                }
            }
        }

        return orders;
    }
}