import java.sql.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;

public class ReportService {

    public static BigDecimal getTotalRevenue()
            throws SQLException {

        String sql =
            "SELECT COALESCE(SUM(total_amount), 0) "
            + "FROM orders WHERE status = 'DELIVERED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }

        return BigDecimal.ZERO;
    }

    public static int getTotalOrders()
            throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM orders");
    }

    public static int getTotalCustomers()
            throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM users "
            + "WHERE role = 'CUSTOMER'");
    }

    public static int getProductsSold()
            throws SQLException {

        return getCount(
            "SELECT COALESCE(SUM(oi.quantity), 0) "
            + "FROM order_items oi "
            + "JOIN orders o "
            + "ON oi.order_id = o.order_id "
            + "WHERE o.status = 'DELIVERED'");
    }

    private static int getCount(String sql)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    public static Map<String, Integer> getOrderStatus()
            throws SQLException {

        String sql =
            "SELECT status, COUNT(*) "
            + "FROM orders GROUP BY status";

        return getIntegerData(sql);
    }

    public static Map<String, Integer> getBestSellingProducts()
            throws SQLException {

        String sql =
            "SELECT p.product_name, SUM(oi.quantity) "
            + "FROM order_items oi "
            + "JOIN products p "
            + "ON oi.product_id = p.product_id "
            + "JOIN orders o "
            + "ON oi.order_id = o.order_id "
            + "WHERE o.status = 'DELIVERED' "
            + "GROUP BY p.product_id, p.product_name "
            + "ORDER BY SUM(oi.quantity) DESC "
            + "LIMIT 5";

        return getIntegerData(sql);
    }

    public static Map<String, Integer> getLowStockProducts()
            throws SQLException {

        String sql =
            "SELECT product_name, stock "
            + "FROM products "
            + "WHERE stock <= min_stock AND active = 1 "
            + "ORDER BY stock ASC";

        return getIntegerData(sql);
    }

    private static Map<String, Integer> getIntegerData(
            String sql) throws SQLException {

        Map<String, Integer> data = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                data.put(
                    rs.getString(1),
                    rs.getInt(2)
                );
            }
        }

        return data;
    }

    public static Map<String, BigDecimal> getDailySales()
            throws SQLException {

        Map<String, BigDecimal> sales =
                new LinkedHashMap<>();

        String sql =
            "SELECT DATE(order_date), SUM(total_amount) "
            + "FROM orders "
            + "WHERE status = 'DELIVERED' "
            + "GROUP BY DATE(order_date) "
            + "ORDER BY DATE(order_date)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                sales.put(
                    rs.getString(1),
                    rs.getBigDecimal(2)
                );
            }
        }

        return sales;
    }
}