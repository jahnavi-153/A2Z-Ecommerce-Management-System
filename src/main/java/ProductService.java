import java.sql.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductService {

    public static ObservableList<Product> getProducts()
            throws SQLException {

        ObservableList<Product> products =
                FXCollections.observableArrayList();

        String sql = "SELECT p.*, c.category_name "
                   + "FROM products p "
                   + "LEFT JOIN categories c "
                   + "ON p.category_id = c.category_id "
                   + "ORDER BY p.product_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                products.add(new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBigDecimal("price"),
                    rs.getInt("stock"),
                    rs.getInt("min_stock"),
                    rs.getString("image_path"),
                    rs.getBoolean("active")
                ));
            }
        }

        return products;
    }

    public static Map<String, Integer> getCategories()
            throws SQLException {

        Map<String, Integer> categories =
                new LinkedHashMap<>();

        String sql = "SELECT * FROM categories "
                   + "ORDER BY category_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                categories.put(
                    rs.getString("category_name"),
                    rs.getInt("category_id")
                );
            }
        }

        return categories;
    }

    public static void addProduct(
            String name, int categoryId,
            String description, BigDecimal price,
            int stock, int minStock, String imagePath)
            throws SQLException {

        String sql = "INSERT INTO products "
                   + "(product_name, category_id, description, "
                   + "price, stock, min_stock, image_path) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {

                int productId;

                try (PreparedStatement ps =
                     con.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setInt(2, categoryId);
                    ps.setString(3, description);
                    ps.setBigDecimal(4, price);
                    ps.setInt(5, stock);
                    ps.setInt(6, minStock);
                    ps.setString(7, imagePath);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Product ID not generated");
                        }

                        productId = rs.getInt(1);
                    }
                }

                if (stock > 0) {

                    String log = "INSERT INTO "
                        + "inventory_transactions "
                        + "(product_id, transaction_type, "
                        + "quantity, reason) "
                        + "VALUES (?, 'IN', ?, 'Initial stock')";

                    try (PreparedStatement ps =
                         con.prepareStatement(log)) {

                        ps.setInt(1, productId);
                        ps.setInt(2, stock);
                        ps.executeUpdate();
                    }
                }

                con.commit();

            } catch (SQLException e) {

                con.rollback();
                throw e;
            }
        }
    }

    public static void updateProduct(
            int id, String name, int categoryId,
            String description, BigDecimal price,
            int minStock, String imagePath)
            throws SQLException {

        String sql = "UPDATE products SET "
                   + "product_name = ?, category_id = ?, "
                   + "description = ?, price = ?, "
                   + "min_stock = ?, image_path = ? "
                   + "WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.setString(3, description);
            ps.setBigDecimal(4, price);
            ps.setInt(5, minStock);
            ps.setString(6, imagePath);
            ps.setInt(7, id);

            ps.executeUpdate();
        }
    }

    public static void setActive(int id, boolean active)
            throws SQLException {

        String sql = "UPDATE products SET active = ? "
                   + "WHERE product_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

public static Map<Integer, String> getProductSupplierNames()
        throws SQLException {

    Map<Integer, String> suppliers = new LinkedHashMap<>();

    String sql =
        "SELECT ps.product_id, "
        + "GROUP_CONCAT(DISTINCT s.company_name "
        + "ORDER BY s.company_name SEPARATOR ', ') AS names "
        + "FROM product_suppliers ps "
        + "JOIN suppliers s "
        + "ON ps.supplier_id = s.supplier_id "
        + "GROUP BY ps.product_id";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

            suppliers.put(
                rs.getInt("product_id"),
                rs.getString("names")
            );
        }
    }

    return suppliers;
}

public static java.util.List<Integer> getProductSupplierIds(
        int productId) throws SQLException {

    java.util.List<Integer> ids =
            new java.util.ArrayList<>();

    String sql =
        "SELECT supplier_id FROM product_suppliers "
        + "WHERE product_id = ?";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, productId);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getInt("supplier_id"));
            }
        }
    }

    return ids;
}

public static void saveProduct(
        Product existing,
        String name,
        int categoryId,
        String description,
        BigDecimal price,
        int stock,
        int minStock,
        String imagePath,
        boolean active,
        java.util.List<Integer> supplierIds)
        throws SQLException {

    try (Connection con = DBConnection.getConnection()) {

        con.setAutoCommit(false);

        try {

            int productId;

            if (existing == null) {

                String sql =
                    "INSERT INTO products "
                    + "(product_name, category_id, description, "
                    + "price, stock, min_stock, image_path, active) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                     con.prepareStatement(
                         sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, name);
                    ps.setInt(2, categoryId);
                    ps.setString(3, description);
                    ps.setBigDecimal(4, price);
                    ps.setInt(5, stock);
                    ps.setInt(6, minStock);
                    ps.setString(7, imagePath);
                    ps.setBoolean(8, active);

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (!rs.next()) {

                            throw new SQLException(
                                "Product ID not generated");
                        }

                        productId = rs.getInt(1);
                    }
                }

                if (stock > 0) {

                    String log =
                        "INSERT INTO inventory_transactions "
                        + "(product_id, transaction_type, "
                        + "quantity, reason) "
                        + "VALUES (?, 'IN', ?, 'Initial stock')";

                    try (PreparedStatement ps =
                         con.prepareStatement(log)) {

                        ps.setInt(1, productId);
                        ps.setInt(2, stock);
                        ps.executeUpdate();
                    }
                }

            } else {

                productId = existing.getId();

                String sql =
                    "UPDATE products SET "
                    + "product_name = ?, category_id = ?, "
                    + "description = ?, price = ?, "
                    + "min_stock = ?, image_path = ?, active = ? "
                    + "WHERE product_id = ?";

                try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

                    ps.setString(1, name);
                    ps.setInt(2, categoryId);
                    ps.setString(3, description);
                    ps.setBigDecimal(4, price);
                    ps.setInt(5, minStock);
                    ps.setString(6, imagePath);
                    ps.setBoolean(7, active);
                    ps.setInt(8, productId);

                    ps.executeUpdate();
                }
            }

            String delete =
                "DELETE FROM product_suppliers "
                + "WHERE product_id = ?";

            try (PreparedStatement ps =
                 con.prepareStatement(delete)) {

                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            String insert =
                "INSERT INTO product_suppliers "
                + "(supplier_id, product_id) VALUES (?, ?)";

            try (PreparedStatement ps =
                 con.prepareStatement(insert)) {

                for (Integer supplierId : supplierIds) {

                    ps.setInt(1, supplierId);
                    ps.setInt(2, productId);
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            con.commit();

        } catch (SQLException | RuntimeException ex) {

            con.rollback();
            throw ex;
        }
    }
}

}