import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrderService {

    // Retrieve all orders belonging to the customer

    public static List<Order> getOrders(int userId)
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

                    orders.add(mapOrder(rs));
                }
            }
        }

        return orders;
    }

    // Retrieve one specific order belonging to the customer

    public static Order getOrder(int userId, int orderId)
            throws SQLException {

        String sql =
            "SELECT o.*, u.name AS customer_name "
            + "FROM orders o "
            + "JOIN users u ON o.user_id = u.user_id "
            + "WHERE o.user_id = ? AND o.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return mapOrder(rs);
                }
            }
        }

        return null;
    }

    // Retrieve purchased products securely

    public static List<OrderItem> getOrderItems(
            int userId, int orderId)
            throws SQLException {

        List<OrderItem> items = new ArrayList<>();

        String sql =
            "SELECT oi.*, p.product_name "
            + "FROM order_items oi "
            + "JOIN products p "
            + "ON oi.product_id = p.product_id "
            + "JOIN orders o "
            + "ON oi.order_id = o.order_id "
            + "WHERE o.user_id = ? "
            + "AND oi.order_id = ? "
            + "ORDER BY oi.order_item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    items.add(new OrderItem(

                        rs.getInt("order_item_id"),

                        rs.getInt("order_id"),

                        rs.getInt("product_id"),

                        rs.getString("product_name"),

                        rs.getInt("quantity"),

                        rs.getBigDecimal("price")
                    ));
                }
            }
        }

        return items;
    }

    private static Order mapOrder(ResultSet rs)
            throws SQLException {

        return new Order(

            rs.getInt("order_id"),

            rs.getInt("user_id"),

            rs.getString("customer_name"),

            rs.getTimestamp("order_date"),

            rs.getBigDecimal("total_amount"),

            rs.getString("delivery_address"),

            rs.getString("status"),

            rs.getString("payment_method"),

            rs.getString("payment_status")
        );
    }
}