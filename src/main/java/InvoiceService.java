import java.sql.*;
import java.math.BigDecimal;

public class InvoiceService {

    public static String generateInvoice(
            int orderId, int userId)
            throws SQLException {

        StringBuilder invoice = new StringBuilder();

        String orderSql =
            "SELECT o.*, u.name, u.email "
            + "FROM orders o "
            + "JOIN users u "
            + "ON o.user_id = u.user_id "
            + "WHERE o.order_id = ? "
            + "AND o.user_id = ?";

        String itemSql =
            "SELECT p.product_name, oi.quantity, oi.price "
            + "FROM order_items oi "
            + "JOIN products p "
            + "ON oi.product_id = p.product_id "
            + "WHERE oi.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(orderSql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                        "Order not found.");
                }

                invoice.append(
                    "========================================\n");

                invoice.append(
                    "                  A2Z\n");

                invoice.append(
                    "         Everything from A to Z\n");

                invoice.append(
                    "========================================\n\n");

                invoice.append("INVOICE\n\n");

                invoice.append("Invoice Number: INV-")
                       .append(String.format("%06d", orderId))
                       .append("\n");

                invoice.append("Order ID: ")
                       .append(orderId)
                       .append("\n");

                invoice.append("Order Date: ")
                       .append(rs.getTimestamp("order_date"))
                       .append("\n");

                invoice.append("Order Status: ")
                       .append(rs.getString("status"))
                       .append("\n\n");

                invoice.append("CUSTOMER DETAILS\n");

                invoice.append("----------------------------------------\n");

                invoice.append("Name: ")
                       .append(rs.getString("name"))
                       .append("\n");

                invoice.append("Email: ")
                       .append(rs.getString("email"))
                       .append("\n");

                invoice.append("Delivery Address: ")
                       .append(rs.getString("delivery_address"))
                       .append("\n\n");

                invoice.append("PURCHASED PRODUCTS\n");

                invoice.append("----------------------------------------\n");

                try (PreparedStatement itemPs =
                         con.prepareStatement(itemSql)) {

                    itemPs.setInt(1, orderId);

                    try (ResultSet items =
                             itemPs.executeQuery()) {

                        int number = 1;

                        while (items.next()) {

                            int quantity =
                                items.getInt("quantity");

                            BigDecimal price =
                                items.getBigDecimal("price");

                            BigDecimal subtotal =
                                price.multiply(
                                    BigDecimal.valueOf(quantity));

                            invoice.append(number++)
                                   .append(". ")
                                   .append(items.getString(
                                       "product_name"))
                                   .append("\n");

                            invoice.append("   Quantity: ")
                                   .append(quantity)
                                   .append("\n");

                            invoice.append("   Unit Price: ₹")
                                   .append(price)
                                   .append("\n");

                            invoice.append("   Subtotal: ₹")
                                   .append(subtotal)
                                   .append("\n\n");
                        }
                    }
                }

                invoice.append("----------------------------------------\n");

                invoice.append("GRAND TOTAL: ₹")
                       .append(rs.getBigDecimal("total_amount"))
                       .append("\n");

                invoice.append("----------------------------------------\n\n");

                invoice.append("PAYMENT DETAILS\n\n");

                invoice.append("Payment Method: ")
                       .append(rs.getString("payment_method"))
                       .append("\n");

                invoice.append("Payment Status: ")
                       .append(rs.getString("payment_status"))
                       .append("\n\n");

                invoice.append("========================================\n");

                invoice.append(
                    "    Thank you for shopping with A2Z!\n");

                invoice.append(
                    "       Computer-generated invoice\n");

                invoice.append(
                    "     Academic demonstration only\n");

                invoice.append(
                    "========================================\n");
            }
        }

        return invoice.toString();
    }
}