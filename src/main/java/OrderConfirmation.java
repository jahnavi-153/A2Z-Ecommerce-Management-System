import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class OrderConfirmation {

    public static void show(
            Stage stage,
            User user,
            int orderId,
            CheckoutPage.CheckoutData data) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        // ================= SUCCESS INDICATOR =================

        Label check = new Label("✓");

        check.getStyleClass().add(
            "oc-success-icon");

        Label heading = UIComponents.heading(
            "Order Placed Successfully!");

        Label description = UIComponents.mutedText(
            "Thank you for shopping with A2Z. "
            + "Your order has been received.");

        description.setWrapText(true);

        description.setTextAlignment(
            javafx.scene.text.TextAlignment.CENTER);

        Label orderNumber = new Label(
            "Order #" + orderId);

        orderNumber.getStyleClass().add(
            "oc-order-number");

        Label status = new Label("PLACED");

        status.getStyleClass().add("badge-success");

        // ================= ORDER INFORMATION =================

        String paymentStatus =
            "Cash on Delivery".equals(data.paymentMethod)
                ? "PENDING"
                : "SIMULATED";

        Label address = new Label(data.address);

        address.setWrapText(true);

        address.getStyleClass().add(
            "oc-address-value");

        VBox details = new VBox(

            16,

            UIComponents.cardHeading(
                "Order Information"),

            new Separator(),

            CheckoutPage.summaryRow(
                "Order ID", "#" + orderId),

            CheckoutPage.summaryRow(
                "Customer", user.getName()),

            CheckoutPage.summaryRow(
                "Order Status", "PLACED"),

            CheckoutPage.summaryRow(
                "Payment Method", data.paymentMethod),

            CheckoutPage.summaryRow(
                "Payment Status", paymentStatus),

            CheckoutPage.summaryRow(
                "Total Amount",
                UIComponents.currency(data.total)),

            new Separator(),

            UIComponents.cardHeading(
                "Delivery Address"),

            address
        );

        details.getStyleClass().add(
            "oc-details-panel");

        // ================= NAVIGATION BUTTONS =================

        Button track = UIComponents.primaryButton(
            "Track Order");

        Button orders = UIComponents.secondaryButton(
            "My Orders");

        Button shopping = UIComponents.secondaryButton(
            "Continue Shopping");

        // Open existing My Orders page.
        // Direct order tracking will be added in Stage 14.

        track.setOnAction(e ->
            OrderTracking.show(stage, user, orderId));

        orders.setOnAction(e ->
            MyOrders.show(stage, user));

        shopping.setOnAction(e ->
            CustomerDashboard.show(stage, user));

        FlowPane actions = new FlowPane(
            12, 12, track, orders, shopping);

        actions.setAlignment(Pos.CENTER);

        Label note = UIComponents.mutedText(
            "You can view your order status "
            + "from the My Orders section.");

        note.setWrapText(true);

        // ================= SUCCESS CARD =================

        VBox confirmation = new VBox(

            20,

            check,

            heading,

            description,

            orderNumber,

            status,

            details,

            actions,

            note
        );

        confirmation.setAlignment(Pos.CENTER);

        confirmation.setMaxWidth(760);

        confirmation.getStyleClass().add(
            "oc-confirmation-card");

        StackPane centered = new StackPane(
            confirmation);

        centered.setAlignment(Pos.TOP_CENTER);

        VBox content = new VBox(

            25,

            CheckoutPage.progress(4),

            centered
        );

        CheckoutPage.showPage(

            stage,

            user,

            content,

            "A2Z - Order Confirmation"
        );
    }
}