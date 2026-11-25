import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartService {

    public static ObservableList<CartItem> getCart(int userId)
            throws SQLException {

        ObservableList<CartItem> items =
                FXCollections.observableArrayList();

        String sql = "SELECT c.product_id, p.product_name, "
                   + "p.price, c.quantity, p.stock, p.active "
                   + "FROM cart_items c JOIN products p "
                   + "ON c.product_id = p.product_id "
                   + "WHERE c.user_id = ? "
                   + "ORDER BY c.cart_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    items.add(new CartItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("stock"),
                        rs.getBoolean("active")
                    ));
                }
            }
        }

        return items;
    }

    public static void addToCart(int userId, int productId)
            throws SQLException {

        changeQuantity(userId, productId, 1);
    }

    public static void changeQuantity(
            int userId, int productId, int change)
            throws SQLException {

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int stock;
                boolean active;

                String productSql =
                    "SELECT stock, active FROM products "
                    + "WHERE product_id = ? FOR UPDATE";

                try (PreparedStatement ps =
                     con.prepareStatement(productSql)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {

                            throw new IllegalArgumentException(
                                "Product not found.");
                        }

                        stock = rs.getInt("stock");
                        active = rs.getBoolean("active");
                    }
                }

                int currentQuantity = 0;

                String cartSql =
                    "SELECT quantity FROM cart_items "
                    + "WHERE user_id = ? AND product_id = ? "
                    + "FOR UPDATE";

                try (PreparedStatement ps =
                     con.prepareStatement(cartSql)) {

                    ps.setInt(1, userId);
                    ps.setInt(2, productId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()) {

                            currentQuantity =
                                    rs.getInt("quantity");
                        }
                    }
                }

                long newQuantity =
                        (long) currentQuantity + change;

                if (newQuantity < 1) {

                    throw new IllegalArgumentException(
                        "Quantity must be at least 1.");
                }

                if (change > 0 && !active) {

                    throw new IllegalArgumentException(
                        "Product is unavailable.");
                }

                if (change > 0 && newQuantity > stock) {

                    throw new IllegalArgumentException(
                        "Only " + stock + " units available.");
                }

                String sql;

                if (currentQuantity == 0) {

                    sql = "INSERT INTO cart_items "
                        + "(quantity, user_id, product_id) "
                        + "VALUES (?, ?, ?)";

                } else {

                    sql = "UPDATE cart_items SET quantity = ? "
                        + "WHERE user_id = ? AND product_id = ?";
                }

                try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

                    ps.setInt(1, (int) newQuantity);
                    ps.setInt(2, userId);
                    ps.setInt(3, productId);

                    ps.executeUpdate();
                }

                con.commit();

            } catch (SQLException | RuntimeException ex) {

                con.rollback();
                throw ex;
            }
        }
    }

    public static void removeItem(
            int userId, int productId)
            throws SQLException {

        String sql = "DELETE FROM cart_items "
                   + "WHERE user_id = ? AND product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);

            ps.executeUpdate();
        }
    }

    public static void clearCart(int userId)
            throws SQLException {

        String sql =
                "DELETE FROM cart_items WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ps.executeUpdate();
        }
    }

    public static int getCartCount(int userId)
            throws SQLException {

        String sql = "SELECT COALESCE(SUM(quantity), 0) "
                   + "FROM cart_items WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }
}