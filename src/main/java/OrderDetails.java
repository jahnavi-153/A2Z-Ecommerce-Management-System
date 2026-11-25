
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

public class OrderDetails {

    public static void show(
                Stage stage, User user, int orderId) {

        show(stage, user, orderId,
                () -> OrderManagement.show(stage, user));
        }

        public static void show(
                Stage stage,
                User user,
                int orderId,
                Runnable onBack) {

        if (!OrderManagement.isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(20);

        content.setPadding(new Insets(24));

        Button back = UIComponents.tertiaryButton(
                "← Back");

        back.setOnAction(e ->
                onBack.run());

        content.getChildren().add(back);

        try {

            // LOAD LATEST ORDER INFORMATION

            Order order = OrderManagementService
                    .getOrders()
                    .stream()
                    .filter(o -> o.getOrderId() == orderId)
                    .findFirst()
                    .orElse(null);

            if (order == null) {

                UIComponents.error("Order not found.");

                onBack.run();

                return;
            }

            List<OrderItem> items =
                    OrderManagementService.getOrderItems(
                            orderId);

            // LOAD PRODUCT IMAGES

            Map<Integer, String> images =
                    new HashMap<>();

            try {

                for (Product product :
                        ProductService.getProducts()) {

                    images.put(
                        product.getId(),
                        product.getImagePath());
                }

            } catch (Exception ex) {

                // Order details remain available
                // even when product images cannot load.

                ex.printStackTrace();
            }

            // PAGE HEADER

            Label heading = UIComponents.heading(
                    "Order #" + order.getOrderId());

            Label status = OrderManagement.statusBadge(
                    order.getStatus());

            HBox title = new HBox(14, heading, status);

            title.setAlignment(Pos.CENTER_LEFT);

            Label date = UIComponents.mutedText(
                    "Placed on " + order.getOrderDate());

            VBox header = new VBox(6, title, date);

            Button refresh =
                    UIComponents.secondaryButton(
                            "↻ Refresh");

            refresh.setOnAction(e ->
                    show(stage, user, orderId, onBack));

            Region spacer = new Region();

            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox headerRow = new HBox(
                    15, header, spacer, refresh);

            headerRow.setAlignment(Pos.CENTER_LEFT);

            content.getChildren().add(headerRow);

            // ORDER TRACKING TIMELINE

            content.getChildren().add(
                    createTimeline(order));

            // PURCHASED PRODUCTS

            VBox products = createProductsCard(
                    order, items, images);

            // CUSTOMER INFORMATION

            VBox customer = UIComponents.card(14);

            customer.getStyleClass().add("od-card");

            customer.getChildren().addAll(

                UIComponents.cardHeading(
                        "Customer Information"),

                information(
                    "Customer Name",
                    order.getCustomerName()),

                information(
                    "Customer ID",
                    "#" + order.getUserId())
            );

            // DELIVERY INFORMATION

            VBox delivery = UIComponents.card(14);

            delivery.getStyleClass().add("od-card");

            delivery.getChildren().addAll(

                UIComponents.cardHeading(
                        "Delivery Address"),

                information(
                    "Shipping Address",
                    order.getDeliveryAddress())
            );

            // PAYMENT INFORMATION

            VBox payment = UIComponents.card(14);

            payment.getStyleClass().add("od-card");

            payment.getChildren().addAll(

                UIComponents.cardHeading(
                        "Payment Information"),

                information(
                    "Payment Method",
                    order.getPaymentMethod()),

                UIComponents.mutedText(
                    "Payment Status"),

                OrderManagement.paymentBadge(
                    order.getPaymentStatus()),

                new Separator(),

                information(
                    "Order Amount",
                    UIComponents.currency(
                        order.getTotalAmount()))
            );

            // ORDER ACTIONS

            VBox actions = createActions(
                    stage, user, order, onBack);

            // TWO COLUMN PAGE LAYOUT

            VBox left = new VBox(18, products);

            VBox right = new VBox(
                    18, customer, delivery,
                    payment, actions);

            left.setMinWidth(0);
            right.setMinWidth(0);

            GridPane layout = new GridPane();

            layout.setHgap(18);

            ColumnConstraints first =
                    new ColumnConstraints();

            first.setPercentWidth(64);
            first.setHgrow(Priority.ALWAYS);

            ColumnConstraints second =
                    new ColumnConstraints();

            second.setPercentWidth(36);
            second.setHgrow(Priority.ALWAYS);

            layout.getColumnConstraints().addAll(
                    first, second);

            layout.add(left, 0, 0);
            layout.add(right, 1, 0);

            content.getChildren().add(layout);

        } catch (Exception ex) {

            Label error = new Label(
                    "Unable to load order details.");

            error.getStyleClass().add("pm-error");

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        OrderManagement.showPage(
                stage,
                user,
                "Orders  ›  Order #" + orderId,
                content);
    }

    // ==========================================
    // ORDER TRACKING TIMELINE
    // ==========================================

    private static VBox createTimeline(Order order) {

        VBox card = UIComponents.card(18);

        card.getStyleClass().add("od-card");

        card.getChildren().add(
                UIComponents.cardHeading(
                        "Order Progress"));

        if ("CANCELLED".equals(order.getStatus())) {

            Label title = new Label("✕ Order Cancelled");

            title.getStyleClass().add(
                    "od-cancelled-title");

            Label description = new Label(
                    "This order has been cancelled. "
                    + "No further fulfilment actions are available.");

            description.setWrapText(true);

            VBox banner = new VBox(
                    8, title, description);

            banner.getStyleClass().add(
                    "od-cancelled-banner");

            card.getChildren().add(banner);

            return card;
        }

        String[] statuses = {

            "PLACED",
            "CONFIRMED",
            "SHIPPED",
            "DELIVERED"
        };

        String[] titles = {

            "Order Placed",
            "Confirmed",
            "Shipped",
            "Delivered"
        };

        String[] descriptions = {

            "Order received",
            "Order accepted",
            "Dispatched",
            "Reached customer"
        };

        int current = -1;

        for (int i = 0; i < statuses.length; i++) {

            if (statuses[i].equals(order.getStatus())) {

                current = i;
                break;
            }
        }

        HBox timeline = new HBox(8);

        timeline.setAlignment(Pos.CENTER);

        for (int i = 0; i < statuses.length; i++) {

            boolean completed = i <= current;

            Label circle = new Label(
                    completed
                    ? "✓"
                    : String.valueOf(i + 1));

            circle.getStyleClass().add(
                    completed
                    ? "od-step-complete"
                    : "od-step-pending");

            Label title = new Label(titles[i]);

            title.getStyleClass().add(
                    "od-step-title");

            title.setWrapText(true);

            title.setAlignment(Pos.CENTER);

            Label description =
                    UIComponents.mutedText(
                        descriptions[i]);

            description.setAlignment(Pos.CENTER);

            Label state = new Label(

                    i == current
                    ? "Current Status"

                    : i < current
                    ? "Completed"

                    : "Pending"
            );

            state.getStyleClass().add(

                    i == current
                    ? "od-current-state"

                    : "text-muted"
            );

            VBox step = new VBox(
                    8, circle, title,
                    description, state);

            step.setAlignment(Pos.TOP_CENTER);

            step.setMinWidth(0);

            step.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(step, Priority.ALWAYS);

            timeline.getChildren().add(step);

            if (i < statuses.length - 1) {

                Region line = new Region();

                line.getStyleClass().add(

                        i < current

                        ? "od-line-complete"

                        : "od-line-pending"
                );

                line.setPrefWidth(55);
                line.setMinWidth(15);

                timeline.getChildren().add(line);
            }
        }

        card.getChildren().add(timeline);

        return card;
    }

    // ==========================================
    // PURCHASED PRODUCTS CARD
    // ==========================================

    private static VBox createProductsCard(
            Order order,
            List<OrderItem> items,
            Map<Integer, String> images) {

        VBox card = UIComponents.card(16);

        card.getStyleClass().add("od-card");

        int totalQuantity = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        card.getChildren().addAll(

                UIComponents.cardHeading(
                        "Purchased Products"),

                UIComponents.mutedText(
                        items.size() + " product types · "
                        + totalQuantity + " total units")
        );

        GridPane table = new GridPane();

        table.setHgap(12);
        table.setVgap(12);

        double[] widths = {46, 14, 20, 20};

        for (double width : widths) {

            ColumnConstraints column =
                    new ColumnConstraints();

            column.setPercentWidth(width);

            column.setHgrow(Priority.ALWAYS);

            table.getColumnConstraints().add(column);
        }

        table.add(tableHeading("PRODUCT"), 0, 0);
        table.add(tableHeading("QTY"), 1, 0);
        table.add(tableHeading("PRICE"), 2, 0);
        table.add(tableHeading("SUBTOTAL"), 3, 0);

        int row = 1;

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : items) {

            StackPane image =
                    UIComponents.productImage(

                        images.get(item.getProductId()),

                        52, 52
                    );

            Label name = new Label(
                    item.getProductName());

            name.getStyleClass().add(
                    "od-product-name");

            name.setWrapText(true);
            name.setMinWidth(0);

            Label id = UIComponents.mutedText(
                    "Product #" + item.getProductId());

            VBox details = new VBox(5, name, id);

            details.setMinWidth(0);

            HBox.setHgrow(details, Priority.ALWAYS);

            HBox product = new HBox(
                    10, image, details);

            product.setAlignment(Pos.CENTER_LEFT);

            Label quantity = new Label(
                    String.valueOf(item.getQuantity()));

            Label price = new Label(
                    UIComponents.currency(
                        item.getPrice()));

            Label itemTotal = new Label(
                    UIComponents.currency(
                        item.getSubtotal()));

            itemTotal.getStyleClass().add(
                    "od-product-name");

            table.add(product, 0, row);
            table.add(quantity, 1, row);
            table.add(price, 2, row);
            table.add(itemTotal, 3, row);

            row++;

            Separator separator = new Separator();

            table.add(separator, 0, row, 4, 1);

            row++;

            subtotal = subtotal.add(
                    item.getSubtotal());
        }

        if (items.isEmpty()) {

            card.getChildren().add(
                    UIComponents.emptyState(
                        "No Order Items",
                        "No purchased products were found."));

        } else {

            card.getChildren().add(table);
        }

        // ORDER TOTALS

        VBox totals = new VBox(12);

        totals.getStyleClass().add("od-total-panel");

        totals.getChildren().add(

                totalRow(
                    "Items Subtotal",
                    UIComponents.currency(subtotal),
                    false)
        );

        BigDecimal difference =
                order.getTotalAmount().subtract(subtotal);

        if (difference.signum() != 0) {

            totals.getChildren().add(

                totalRow(
                    "Order Amount Difference",
                    UIComponents.currency(difference),
                    false)
            );
        }

        totals.getChildren().addAll(

                new Separator(),

                totalRow(
                    "Grand Total",
                    UIComponents.currency(
                        order.getTotalAmount()),
                    true)
        );

        card.getChildren().add(totals);

        return card;
    }

    // ==========================================
    // ORDER MANAGEMENT ACTIONS
    // ==========================================

    private static VBox createActions(
            Stage stage, User user, Order order, Runnable onBack) {

        VBox card = UIComponents.card(12);

        card.getStyleClass().add("od-card");

        card.getChildren().add(
                UIComponents.cardHeading(
                        "Order Actions"));

        String next =
                OrderManagement.nextStatus(
                        order.getStatus());

        if (next != null) {

            String title;

            switch (next) {

                case "CONFIRMED":
                    title = "Confirm Order";
                    break;

                case "SHIPPED":
                    title = "Mark as Shipped";
                    break;

                default:
                    title = "Mark as Delivered";
            }

            Button update =
                    UIComponents.primaryButton(title);

            update.setMaxWidth(Double.MAX_VALUE);

            update.setOnAction(e ->

                OrderManagement.performAction(

                    order,

                    next,

                    () -> show(
                        stage, user, order.getOrderId(), onBack)
                )
            );

            card.getChildren().add(update);
        }

        if (OrderManagement.canCancel(order)) {

            Button cancel =
                    UIComponents.destructiveButton(
                            "Cancel Order");

            cancel.setMaxWidth(Double.MAX_VALUE);

            cancel.setOnAction(e ->

                OrderManagement.performAction(

                    order,

                    "CANCELLED",

                    () -> show(
                        stage, user, order.getOrderId(), onBack)
                )
            );

            card.getChildren().addAll(

                    cancel,

                    UIComponents.mutedText(
                        "Cancelling this order restores "
                        + "the purchased quantities to inventory.")
            );
        }

        if ("DELIVERED".equals(order.getStatus())) {

            card.getChildren().add(
                    UIComponents.mutedText(
                        "This order has been successfully "
                        + "delivered. No further actions "
                        + "are required."));
        }

        if ("CANCELLED".equals(order.getStatus())) {

            card.getChildren().add(
                    UIComponents.mutedText(
                        "This order is cancelled. "
                        + "No further actions are available."));
        }

        Button back =
                UIComponents.secondaryButton(
                        "Back");

        back.setMaxWidth(Double.MAX_VALUE);

        back.setOnAction(e ->
                onBack.run());

        card.getChildren().add(back);

        return card;
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static VBox information(
            String title, String value) {

        Label label = UIComponents.mutedText(title);

        Label text = new Label(

                value == null || value.isBlank()

                ? "Not available"

                : value
        );

        text.setWrapText(true);

        text.getStyleClass().add(
                "od-information-value");

        return new VBox(5, label, text);
    }

    private static Label tableHeading(String text) {

        Label label = new Label(text);

        label.getStyleClass().add(
                "od-table-heading");

        return label;
    }

    private static HBox totalRow(
            String title,
            String amount,
            boolean grandTotal) {

        Label name = new Label(title);

        Label value = new Label(amount);

        if (grandTotal) {

            name.getStyleClass().add(
                    "od-grand-total-label");

            value.getStyleClass().add(
                    "od-grand-total-value");

        } else {

            name.getStyleClass().add("text-muted");

            value.getStyleClass().add(
                    "od-information-value");
        }

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(
                10, name, spacer, value);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }
}
