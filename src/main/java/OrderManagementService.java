import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManagementService {

    public static List<Order> getOrders()
            throws SQLException {

        List<Order> orders = new ArrayList<>();

        String sql =
            "SELECT o.*, u.name AS customer_name "
            + "FROM orders o "
            + "JOIN users u ON o.user_id = u.user_id "
            + "ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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

        return orders;
    }

    public static List<OrderItem> getOrderItems(int orderId)
            throws SQLException {

        List<OrderItem> items = new ArrayList<>();

        String sql =
            "SELECT oi.*, p.product_name "
            + "FROM order_items oi "
            + "JOIN products p "
            + "ON oi.product_id = p.product_id "
            + "WHERE oi.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);

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

    public static void updateStatus(
            int orderId, String newStatus)
            throws SQLException {

        String oldStatus;

        switch (newStatus) {

            case "CONFIRMED":
                oldStatus = "PLACED";
                break;

            case "SHIPPED":
                oldStatus = "CONFIRMED";
                break;

            case "DELIVERED":
                oldStatus = "SHIPPED";
                break;

            default:
                throw new IllegalArgumentException(
                    "Invalid order status.");
        }

        String sql =
            "UPDATE orders SET status = ?, "
            + "payment_status = CASE "
            + "WHEN ? = 'DELIVERED' "
            + "AND payment_method = 'Cash on Delivery' "
            + "THEN 'PAID' ELSE payment_status END "
            + "WHERE order_id = ? AND status = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setInt(3, orderId);
            ps.setString(4, oldStatus);

            int rows = ps.executeUpdate();

            if (rows == 0) {

                throw new IllegalArgumentException(
                    "Invalid status transition. Refresh the orders.");
            }
        }
    }

    public static void cancelOrder(int orderId)
            throws SQLException {

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                String status;

                String checkSql =
                    "SELECT status FROM orders "
                    + "WHERE order_id = ? FOR UPDATE";

                try (PreparedStatement ps =
                     con.prepareStatement(checkSql)) {

                    ps.setInt(1, orderId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                "Order not found.");
                        }

                        status = rs.getString("status");
                    }
                }

                if (!"PLACED".equals(status)
                        && !"CONFIRMED".equals(status)) {

                    throw new IllegalArgumentException(
                        "This order cannot be cancelled.");
                }

                String itemSql =
                    "SELECT product_id, quantity "
                    + "FROM order_items WHERE order_id = ? "
                    + "ORDER BY product_id";

                String stockSql =
                    "UPDATE products "
                    + "SET stock = stock + ? "
                    + "WHERE product_id = ?";

                String inventorySql =
                    "INSERT INTO inventory_transactions "
                    + "(product_id, transaction_type, "
                    + "quantity, reason) "
                    + "VALUES (?, 'IN', ?, ?)";

                try (PreparedStatement itemPs =
                         con.prepareStatement(itemSql);

                     PreparedStatement stockPs =
                         con.prepareStatement(stockSql);

                     PreparedStatement inventoryPs =
                         con.prepareStatement(inventorySql)) {

                    itemPs.setInt(1, orderId);

                    try (ResultSet rs = itemPs.executeQuery()) {

                        while (rs.next()) {

                            int productId =
                                rs.getInt("product_id");

                            int quantity =
                                rs.getInt("quantity");

                            stockPs.setInt(1, quantity);
                            stockPs.setInt(2, productId);

                            if (stockPs.executeUpdate() != 1) {

                                throw new SQLException(
                                    "Unable to restore stock.");
                            }

                            inventoryPs.setInt(1, productId);
                            inventoryPs.setInt(2, quantity);

                            inventoryPs.setString(
                                3, "Cancelled Order #" + orderId);

                            inventoryPs.executeUpdate();
                        }
                    }
                }

                String cancelSql =
                    "UPDATE orders "
                    + "SET status = 'CANCELLED', "
                    + "payment_status = CASE "
                    + "WHEN payment_status = 'SIMULATED' "
                    + "THEN 'SIMULATED_REFUND' "
                    + "ELSE 'CANCELLED' END "
                    + "WHERE order_id = ?";

                try (PreparedStatement ps =
                     con.prepareStatement(cancelSql)) {

                    ps.setInt(1, orderId);

                    ps.executeUpdate();
                }

                con.commit();

            } catch (SQLException | RuntimeException ex) {

                con.rollback();

                throw ex;
            }
        }
    }
}