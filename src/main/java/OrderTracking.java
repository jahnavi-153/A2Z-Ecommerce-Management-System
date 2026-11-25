import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class OrderTracking {

    public static void show(

            Stage stage,
            User user,
            int orderId) {

        showPage(stage, user, orderId, true);
    }

    public static void showDetails(

            Stage stage,
            User user,
            int orderId) {

        showPage(stage, user, orderId, false);
    }

    private static void showPage(

            Stage stage,
            User user,
            int orderId,
            boolean trackingMode) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        try {

            // Verify that the order belongs to this customer

            Order order = CustomerOrderService.getOrder(

                user.getUserId(), orderId);

            if (order == null) {

                UIComponents.error(
                    "Order not found or access denied.");

                MyOrders.show(stage, user);

                return;
            }

            List<OrderItem> items =
                CustomerOrderService.getOrderItems(

                    user.getUserId(), orderId);

            Map<Integer, String> images =
                MyOrders.loadImages();

            // ================= NAVIGATION =================

            Button back = UIComponents.secondaryButton(
                "← Back to My Orders");

            back.setOnAction(e ->
                MyOrders.show(stage, user));

            Button refresh = UIComponents.secondaryButton(
                "Refresh Status");

            refresh.setOnAction(e ->

                showPage(
                    stage, user, orderId, trackingMode)
            );

            Button invoice = UIComponents.secondaryButton(
                "View Invoice");

            invoice.setOnAction(e ->

                InvoicePage.show(stage, user, orderId)
            );

            Region space = new Region();

            HBox.setHgrow(space, Priority.ALWAYS);

            HBox navigation = new HBox(

                12, back, space, refresh, invoice
            );

            navigation.setAlignment(Pos.CENTER_LEFT);

            // ================= ORDER HEADER =================

            VBox identity = new VBox(

                7,

                UIComponents.heading(
                    "Order #" + orderId),

                UIComponents.mutedText(

                    "Placed on "
                    + MyOrders.formatDate(order))
            );

            Region headerSpace = new Region();

            HBox.setHgrow(headerSpace, Priority.ALWAYS);

            HBox header = new HBox(

                15,

                identity,

                headerSpace,

                UIComponents.orderBadge(
                    order.getStatus())
            );

            header.setAlignment(Pos.CENTER_LEFT);

            // ================= TRACKING PANEL =================

            VBox tracking = UIComponents.card(22);

            tracking.getChildren().addAll(

                UIComponents.cardHeading(
                    "Order Progress"),

                MyOrders.createTracking(
                    order.getStatus()),

                createStatusMessage(
                    order.getStatus())
            );

            // ================= PURCHASED PRODUCTS =================

            VBox products = UIComponents.card(18);

            products.getChildren().addAll(

                UIComponents.cardHeading(
                    "Purchased Products"),

                new Separator()
            );

            for (OrderItem item : items) {

                products.getChildren().add(

                    MyOrders.productRow(item, images)
                );
            }

            if (items.isEmpty()) {

                products.getChildren().add(

                    UIComponents.mutedText(
                        "No order items found.")
                );
            }

            Label amount = new Label(

                UIComponents.currency(
                    order.getTotalAmount())
            );

            amount.getStyleClass().add(
                "ot-grand-total");

            VBox totalPanel = new VBox(

                8,

                UIComponents.mutedText("Grand Total"),

                amount
            );

            totalPanel.getStyleClass().add(
                "ot-total-panel");

            products.getChildren().addAll(

                new Separator(),

                totalPanel
            );

            // ================= DELIVERY INFORMATION =================

            Label address = new Label(
                order.getDeliveryAddress());

            address.setWrapText(true);

            address.getStyleClass().add(
                "ot-address");

            VBox delivery = UIComponents.card(15);

            delivery.getChildren().addAll(

                UIComponents.cardHeading(
                    "Delivery Information"),

                new Separator(),

                UIComponents.mutedText("Customer"),

                new Label(order.getCustomerName()),

                UIComponents.mutedText(
                    "Delivery Address"),

                address
            );

            // ================= PAYMENT INFORMATION =================

            VBox payment = UIComponents.card(15);

            payment.getChildren().addAll(

                UIComponents.cardHeading(
                    "Payment Information"),

                new Separator(),

                CheckoutPage.summaryRow(

                    "Payment Method",

                    order.getPaymentMethod()),

                UIComponents.mutedText(
                    "Payment Status"),

                OrderManagement.paymentBadge(
                    order.getPaymentStatus()),

                CheckoutPage.summaryRow(

                    "Order Amount",

                    UIComponents.currency(
                        order.getTotalAmount()))
            );

            // ================= PAGE LAYOUT =================

            VBox left = new VBox(20, products);

            VBox right = new VBox(
                20, delivery, payment);

            GridPane layout =
                CheckoutPage.twoColumns(left, right);

            layout.getColumnConstraints()
                  .get(0).setPercentWidth(62);

            layout.getColumnConstraints()
                  .get(1).setPercentWidth(38);

            GridPane.setValignment(

                right, javafx.geometry.VPos.TOP);

            GridPane.setValignment(

                left, javafx.geometry.VPos.TOP);

            VBox content = new VBox(

                22,

                UIComponents.mutedText(

                    "Home / My Orders / "
                    + (trackingMode
                        ? "Order Tracking"
                        : "Order Details")),

                navigation,

                UIComponents.heading(

                    trackingMode
                        ? "Track Your Order"
                        : "Order Details"),

                header,

                tracking,

                layout
            );

            CheckoutPage.showPage(

                stage,

                user,

                content,

                trackingMode
                    ? "A2Z - Order Tracking"
                    : "A2Z - Order Details"
            );

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to retrieve order information.");

            ex.printStackTrace();
        }
    }

    // =====================================================
    // STATUS MESSAGE
    // =====================================================

    private static VBox createStatusMessage(
            String status) {

        String title;

        String description;

        switch (status) {

            case "PLACED":

                title = "Your order has been placed";

                description =
                    "Your order has been received "
                    + "and is awaiting confirmation.";

                break;

            case "CONFIRMED":

                title = "Your order is confirmed";

                description =
                    "Your order has been confirmed "
                    + "and is being prepared for shipment.";

                break;

            case "SHIPPED":

                title = "Your order has been shipped";

                description =
                    "Your order has been marked as "
                    + "shipped by the administrator.";

                break;

            case "DELIVERED":

                title = "Your order has been delivered";

                description =
                    "Your order has been marked "
                    + "as successfully delivered.";

                break;

            case "CANCELLED":

                title = "Your order was cancelled";

                description =
                    "This order is no longer being processed.";

                break;

            default:

                title = "Order status unavailable";

                description =
                    "Please refresh to check the current status.";
        }

        Label heading = new Label(title);

        heading.getStyleClass().add(
            "ot-status-title");

        Label message = new Label(description);

        message.setWrapText(true);

        VBox box = new VBox(8, heading, message);

        box.getStyleClass().add(

            "CANCELLED".equals(status)

                ? "ot-status-cancelled"

                : "ot-status-message"
        );

        return box;
    }
}