import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    public static List<Integer> getRecommendations(int userId)
            throws SQLException {

        List<Integer> recommendations = new ArrayList<>();

        String sql =
            "SELECT p.product_id, "
            + "CASE WHEN p.category_id IN ( "
            + "SELECT p2.category_id "
            + "FROM orders o2 "
            + "JOIN order_items oi2 "
            + "ON o2.order_id = oi2.order_id "
            + "JOIN products p2 "
            + "ON oi2.product_id = p2.product_id "
            + "WHERE o2.user_id = ? "
            + "AND o2.status = 'DELIVERED' "
            + ") THEN 1 ELSE 0 END AS preferred, "

            + "COALESCE(SUM(CASE "
            + "WHEN o.status = 'DELIVERED' "
            + "THEN oi.quantity ELSE 0 END), 0) AS popularity "

            + "FROM products p "

            + "LEFT JOIN order_items oi "
            + "ON p.product_id = oi.product_id "

            + "LEFT JOIN orders o "
            + "ON oi.order_id = o.order_id "

            + "WHERE p.active = 1 "
            + "AND p.stock > 0 "

            + "AND NOT EXISTS ( "
            + "SELECT 1 FROM orders o3 "
            + "JOIN order_items oi3 "
            + "ON o3.order_id = oi3.order_id "
            + "WHERE o3.user_id = ? "
            + "AND o3.status = 'DELIVERED' "
            + "AND oi3.product_id = p.product_id "
            + ") "

            + "GROUP BY p.product_id, p.category_id "

            + "ORDER BY preferred DESC, "
            + "popularity DESC, p.product_id ASC "

            + "LIMIT 4";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    recommendations.add(
                        rs.getInt("product_id")
                    );
                }
            }
        }

        return recommendations;
    }
}