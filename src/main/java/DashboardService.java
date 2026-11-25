import java.sql.*;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DashboardService {

    public static int getCount(String sql)
            throws SQLException {

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    public static int totalProducts() throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM products WHERE active = TRUE"
        );
    }

    public static int totalCustomers() throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM users WHERE role = 'CUSTOMER'"
        );
    }

    public static int totalOrders() throws SQLException {

        return getCount("SELECT COUNT(*) FROM orders");
    }

    public static BigDecimal totalRevenue()
            throws SQLException {

        return ReportService.getTotalRevenue();
    }

    public static int availableProducts() throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM products "
            + "WHERE active = TRUE AND stock > min_stock"
        );
    }

    public static int lowStockProducts() throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM products "
            + "WHERE active = TRUE "
            + "AND stock > 0 AND stock <= min_stock"
        );
    }

    public static int outOfStockProducts() throws SQLException {

        return getCount(
            "SELECT COUNT(*) FROM products "
            + "WHERE active = TRUE AND stock = 0"
        );
    }

    public static ObservableList<String> inventoryAlerts()
            throws SQLException {

        ObservableList<String> alerts =
                FXCollections.observableArrayList();

        String sql = "SELECT product_name, stock FROM products "
                   + "WHERE active = TRUE AND stock <= min_stock "
                   + "ORDER BY stock ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String name = rs.getString("product_name");
                int stock = rs.getInt("stock");

                if (stock == 0) {

                    alerts.add(name + " - Out of stock");

                } else {

                    alerts.add(name + " - Only "
                               + stock + " units remaining");
                }
            }
        }

        return alerts;
    }
    public static int pendingOrders() throws SQLException {

    return getCount(
        "SELECT COUNT(*) FROM orders "
        + "WHERE status IN "
        + "('PLACED', 'CONFIRMED', 'SHIPPED')"
    );
}

public static int deliveredOrders() throws SQLException {

    return getCount(
        "SELECT COUNT(*) FROM orders "
        + "WHERE status = 'DELIVERED'"
    );
}

public static int cancelledOrders() throws SQLException {

    return getCount(
        "SELECT COUNT(*) FROM orders "
        + "WHERE status = 'CANCELLED'"
    );
}

public static int totalCategories() throws SQLException {

    return getCount(
        "SELECT COUNT(DISTINCT category_id) "
        + "FROM products WHERE active = TRUE"
    );
}

public static java.util.List<String[]> recentOrders()
        throws SQLException {

    java.util.List<String[]> orders =
            new java.util.ArrayList<>();

    String sql =
        "SELECT o.order_id, u.name, o.order_date, "
        + "o.total_amount, o.payment_method, o.status "
        + "FROM orders o "
        + "JOIN users u ON o.user_id = u.user_id "
        + "ORDER BY o.order_date DESC, o.order_id DESC "
        + "LIMIT 5";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            orders.add(new String[] {
                rs.getString("order_id"),
                rs.getString("name"),
                rs.getDate("order_date").toString(),
                rs.getBigDecimal("total_amount").toPlainString(),
                rs.getString("payment_method"),
                rs.getString("status")
            });
        }
    }

    return orders;
}

public static java.util.List<String[]> stockAlertDetails()
        throws SQLException {

    java.util.List<String[]> alerts =
            new java.util.ArrayList<>();

    String sql =
        "SELECT product_name, stock, min_stock, image_path "
        + "FROM products "
        + "WHERE active = TRUE AND stock <= min_stock "
        + "ORDER BY stock ASC LIMIT 5";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            alerts.add(new String[] {
                rs.getString("product_name"),
                rs.getString("stock"),
                rs.getString("min_stock"),
                rs.getString("image_path")
            });
        }
    }

    return alerts;
}

public static java.util.List<String[]> bestSellingDetails()
        throws SQLException {

    java.util.List<String[]> products =
            new java.util.ArrayList<>();

    String sql =
        "SELECT p.product_name, p.price, p.image_path, "
        + "SUM(oi.quantity) AS units_sold "
        + "FROM order_items oi "
        + "JOIN products p "
        + "ON oi.product_id = p.product_id "
        + "JOIN orders o "
        + "ON oi.order_id = o.order_id "
        + "WHERE o.status = 'DELIVERED' "
        + "GROUP BY p.product_id, p.product_name, "
        + "p.price, p.image_path "
        + "ORDER BY units_sold DESC LIMIT 3";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            products.add(new String[] {
                rs.getString("product_name"),
                rs.getBigDecimal("price").toPlainString(),
                rs.getString("image_path"),
                rs.getString("units_sold")
            });
        }
    }

    return products;
}

public static java.util.Map<String, BigDecimal> lastSevenDaysSales() throws SQLException {

        java.util.Map<String, BigDecimal> sales =
                new java.util.LinkedHashMap<>();

        java.time.LocalDate today =
                java.time.LocalDate.now();

        for (int i = 6; i >= 0; i--) {

            sales.put(
                today.minusDays(i).toString(),
                BigDecimal.ZERO
            );
        }

        String sql =
            "SELECT DATE(order_date) AS sales_date, "
            + "SUM(total_amount) AS revenue "
            + "FROM orders "
            + "WHERE status = 'DELIVERED' "
            + "AND DATE(order_date) BETWEEN ? AND ? "
            + "GROUP BY DATE(order_date) "
            + "ORDER BY DATE(order_date)";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(today.minusDays(6)));
            ps.setDate(2, Date.valueOf(today));

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    sales.put(
                        rs.getString("sales_date"),
                        rs.getBigDecimal("revenue")
                    );
                }
            }
    }

    return sales;
}

}