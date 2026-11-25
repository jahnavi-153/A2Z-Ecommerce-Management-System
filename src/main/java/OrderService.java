import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public static int placeOrder(
            int userId,
            String address,
            String paymentMethod,
            BigDecimal expectedTotal)
            throws SQLException {

        if (address == null || address.isBlank()
                || address.length() > 500) {

            throw new IllegalArgumentException(
                "Enter a valid delivery address.");
        }

        if (!"Cash on Delivery".equals(paymentMethod)
                && !"UPI (Simulation)".equals(paymentMethod)
                && !"Card (Simulation)".equals(paymentMethod)) {

            throw new IllegalArgumentException(
                "Select a valid payment method.");
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                List<CartItem> items = new ArrayList<>();

                BigDecimal total = BigDecimal.ZERO;

                String cartSql =
                    "SELECT c.product_id, p.product_name, "
                    + "p.price, c.quantity, p.stock, p.active "
                    + "FROM cart_items c "
                    + "JOIN products p "
                    + "ON c.product_id = p.product_id "
                    + "WHERE c.user_id = ? "
                    + "ORDER BY c.product_id FOR UPDATE";

                try (PreparedStatement ps =
                     con.prepareStatement(cartSql)) {

                    ps.setInt(1, userId);

                    try (ResultSet rs = ps.executeQuery()) {

                        while (rs.next()) {

                            CartItem item = new CartItem(
                                rs.getInt("product_id"),
                                rs.getString("product_name"),
                                rs.getBigDecimal("price"),
                                rs.getInt("quantity"),
                                rs.getInt("stock"),
                                rs.getBoolean("active")
                            );

                            if (!item.isActive()) {

                                throw new IllegalArgumentException(
                                    item.getProductName()
                                    + " is unavailable.");
                            }

                            if (item.getQuantity() <= 0
                                || item.getQuantity()
                                   > item.getStock()) {

                                throw new IllegalArgumentException(
                                    "Invalid quantity or insufficient stock for "
                                    + item.getProductName());
                            }

                            items.add(item);

                            total = total.add(
                                    item.getSubtotal());
                        }
                    }
                }

                if (items.isEmpty()) {

                    throw new IllegalArgumentException(
                        "Your shopping cart is empty.");
                }

                if (total.compareTo(expectedTotal) != 0) {

                    throw new IllegalArgumentException(
                        "Cart total has changed. "
                        + "Please return to your cart.");
                }

                String paymentStatus =
                    paymentMethod.equals("Cash on Delivery")
                    ? "PENDING" : "SIMULATED";

                int orderId;

                String orderSql =
                    "INSERT INTO orders "
                    + "(user_id, total_amount, "
                    + "delivery_address, status, "
                    + "payment_method, payment_status) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                     con.prepareStatement(
                         orderSql,
                         Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, userId);
                    ps.setBigDecimal(2, total);
                    ps.setString(3, address);
                    ps.setString(4, "PLACED");
                    ps.setString(5, paymentMethod);
                    ps.setString(6, paymentStatus);

                    ps.executeUpdate();

                    try (ResultSet rs =
                         ps.getGeneratedKeys()) {

                        if (!rs.next()) {

                            throw new SQLException(
                                "Unable to generate order ID.");
                        }

                        orderId = rs.getInt(1);
                    }
                }

                String itemSql =
                    "INSERT INTO order_items "
                    + "(order_id, product_id, quantity, price) "
                    + "VALUES (?, ?, ?, ?)";

                String stockSql =
                    "UPDATE products "
                    + "SET stock = stock - ? "
                    + "WHERE product_id = ? "
                    + "AND stock >= ? AND active = 1";

                String inventorySql =
                    "INSERT INTO inventory_transactions "
                    + "(product_id, transaction_type, "
                    + "quantity, reason) "
                    + "VALUES (?, ?, ?, ?)";

                try (PreparedStatement itemPs =
                         con.prepareStatement(itemSql);

                     PreparedStatement stockPs =
                         con.prepareStatement(stockSql);

                     PreparedStatement inventoryPs =
                         con.prepareStatement(inventorySql)) {

                    for (CartItem item : items) {

                        itemPs.setInt(1, orderId);
                        itemPs.setInt(2, item.getProductId());
                        itemPs.setInt(3, item.getQuantity());
                        itemPs.setBigDecimal(4, item.getPrice());

                        itemPs.executeUpdate();

                        stockPs.setInt(1, item.getQuantity());
                        stockPs.setInt(2, item.getProductId());
                        stockPs.setInt(3, item.getQuantity());

                        int updated = stockPs.executeUpdate();

                        if (updated != 1) {

                            throw new SQLException(
                                "Unable to deduct stock for "
                                + item.getProductName());
                        }

                        inventoryPs.setInt(
                            1, item.getProductId());

                        inventoryPs.setString(2, "OUT");

                        inventoryPs.setInt(
                            3, item.getQuantity());

                        inventoryPs.setString(
                            4, "Customer Order #" + orderId);

                        inventoryPs.executeUpdate();
                    }
                }

                String clearSql =
                    "DELETE FROM cart_items WHERE user_id = ?";

                try (PreparedStatement ps =
                     con.prepareStatement(clearSql)) {

                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }

                con.commit();

                return orderId;

            } catch (SQLException | RuntimeException ex) {

                con.rollback();

                throw ex;
            }
        }
    }
}