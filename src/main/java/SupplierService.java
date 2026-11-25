import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SupplierService {

    public static ObservableList<Supplier> getSuppliers()
            throws SQLException {

        ObservableList<Supplier> suppliers =
                FXCollections.observableArrayList();

        String sql = "SELECT * FROM suppliers "
                   + "ORDER BY supplier_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                suppliers.add(new Supplier(
                    rs.getInt("supplier_id"),
                    rs.getString("supplier_name"),
                    rs.getString("company_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                ));
            }
        }

        return suppliers;
    }

    public static void addSupplier(
            String name, String company,
            String email, String phone,
            String address) throws SQLException {

        String sql = "INSERT INTO suppliers "
                   + "(supplier_name, company_name, "
                   + "email, phone, address) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, company);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, address);

            ps.executeUpdate();
        }
    }

    public static void updateSupplier(
            int id, String name, String company,
            String email, String phone,
            String address) throws SQLException {

        String sql = "UPDATE suppliers SET "
                   + "supplier_name = ?, company_name = ?, "
                   + "email = ?, phone = ?, address = ? "
                   + "WHERE supplier_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, company);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, address);
            ps.setInt(6, id);

            ps.executeUpdate();
        }
    }

    public static void assignProduct(
            int supplierId, int productId)
            throws SQLException {

        String sql = "INSERT INTO product_suppliers "
                   + "(supplier_id, product_id) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, supplierId);
            ps.setInt(2, productId);

            ps.executeUpdate();
        }
    }

    public static ObservableList<String> getSupplierProducts(
            int supplierId) throws SQLException {

        ObservableList<String> products =
                FXCollections.observableArrayList();

        String sql = "SELECT p.product_name, p.stock "
                   + "FROM products p "
                   + "JOIN product_suppliers ps "
                   + "ON p.product_id = ps.product_id "
                   + "WHERE ps.supplier_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    products.add(
                        rs.getString("product_name")
                        + " | Current Stock: "
                        + rs.getInt("stock")
                    );
                }
            }
        }

        return products;
    }

    public static int assignedProductCount()
            throws SQLException {

        return DashboardService.getCount(
            "SELECT COUNT(DISTINCT product_id) "
            + "FROM product_suppliers"
        );
    }
}