import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryService {

    public static void updateStock(
            int productId, String type,
            int quantity, String reason)
            throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "Quantity must be greater than zero.");
        }

        if (!"IN".equals(type) && !"OUT".equals(type)) {
            throw new IllegalArgumentException(
                "Invalid transaction type.");
        }

        if (reason == null || reason.isBlank()
                || reason.length() > 255) {
            throw new IllegalArgumentException(
                "Enter a valid reason (maximum 255 characters).");
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int currentStock;

                String select = "SELECT stock FROM products "
                              + "WHERE product_id = ? FOR UPDATE";

                try (PreparedStatement ps =
                     con.prepareStatement(select)) {

                    ps.setInt(1, productId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Product not found.");
                        }

                        currentStock = rs.getInt("stock");
                    }
                }

                long newStock;

                if (type.equals("IN")) {

                    newStock = (long) currentStock + quantity;

                } else {

                    newStock = (long) currentStock - quantity;
                }

                if (newStock < 0) {

                    throw new IllegalArgumentException(
                        "Insufficient stock. Available stock: "
                        + currentStock);
                }

                if (newStock > Integer.MAX_VALUE) {

                    throw new IllegalArgumentException(
                        "Stock exceeds the allowed limit.");
                }

                String update = "UPDATE products SET stock = ? "
                              + "WHERE product_id = ?";

                try (PreparedStatement ps =
                     con.prepareStatement(update)) {

                    ps.setInt(1, (int) newStock);
                    ps.setInt(2, productId);

                    ps.executeUpdate();
                }

                String insert = "INSERT INTO inventory_transactions "
                              + "(product_id, transaction_type, "
                              + "quantity, reason) VALUES (?, ?, ?, ?)";

                try (PreparedStatement ps =
                     con.prepareStatement(insert)) {

                    ps.setInt(1, productId);
                    ps.setString(2, type);
                    ps.setInt(3, quantity);
                    ps.setString(4, reason.trim());

                    ps.executeUpdate();
                }

                con.commit();

            } catch (SQLException | RuntimeException e) {

                con.rollback();
                throw e;
            }
        }
    }

    public static ObservableList<InventoryTransaction>
            getTransactions() throws SQLException {

        ObservableList<InventoryTransaction> transactions =
                FXCollections.observableArrayList();

        String sql = "SELECT t.*, p.product_name "
                   + "FROM inventory_transactions t "
                   + "JOIN products p "
                   + "ON t.product_id = p.product_id "
                   + "ORDER BY t.transaction_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                transactions.add(new InventoryTransaction(
                    rs.getInt("transaction_id"),
                    rs.getString("product_name"),
                    rs.getString("transaction_type"),
                    rs.getInt("quantity"),
                    rs.getString("reason"),
                    rs.getString("transaction_date")
                ));
            }
        }

        return transactions;
    }
    public static ObservableList<InventoryTransaction>
            getTransactions(int productId) throws SQLException {

        ObservableList<InventoryTransaction> transactions =
                FXCollections.observableArrayList();

        String sql = "SELECT t.*, p.product_name "
                + "FROM inventory_transactions t "
                + "JOIN products p "
                + "ON t.product_id = p.product_id "
                + "WHERE t.product_id = ? "
                + "ORDER BY t.transaction_id DESC";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    transactions.add(new InventoryTransaction(
                        rs.getInt("transaction_id"),
                        rs.getString("product_name"),
                        rs.getString("transaction_type"),
                        rs.getInt("quantity"),
                        rs.getString("reason"),
                        rs.getString("transaction_date")
                    ));
                }
            }
        }

        return transactions;
    }
}