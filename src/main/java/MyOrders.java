import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.*;
import java.time.format.DateTimeFormatter;

public class MyOrders {

    public static void show(Stage stage, User user) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        Button shopping = UIComponents.secondaryButton(
            "Continue Shopping");

        shopping.setOnAction(e ->
            CustomerDashboard.show(stage, user));

        Button refresh = UIComponents.secondaryButton(
            "Refresh");

        refresh.setOnAction(e -> show(stage, user));

        HBox actions = new HBox(12, shopping, refresh);

        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(

            22,

            UIComponents.mutedText("Home / My Orders"),

            UIComponents.heading("My Orders"),

            UIComponents.mutedText(
                "View your purchases, track orders "
                + "and access invoices."),

            actions
        );

        try {

            List<Order> orders =
                CustomerOrderService.getOrders(
                    user.getUserId());

            Map<Integer, String> images = loadImages();

            Map<Integer, List<OrderItem>> orderItems =
                new HashMap<>();

            for (Order order : orders) {

                orderItems.put(

                    order.getOrderId(),

                    CustomerOrderService.getOrderItems(
                        user.getUserId(),
                        order.getOrderId())
                );
            }

            // ================= STATISTICS =================

            long active = orders.stream()
                .filter(o ->
                    "PLACED".equals(o.getStatus())
                    || "CONFIRMED".equals(o.getStatus())
                    || "SHIPPED".equals(o.getStatus()))
                .count();

            long delivered = orders.stream()
                .filter(o ->
                    "DELIVERED".equals(o.getStatus()))
                .count();

            long cancelled = orders.stream()
                .filter(o ->
                    "CANCELLED".equals(o.getStatus()))
                .count();

            HBox statistics = new HBox(

                16,

                UIComponents.statCard(
                    "Total Orders",
                    String.valueOf(orders.size()),
                    "All purchases",
                    "#191C1B"),

                UIComponents.statCard(
                    "Active Orders",
                    String.valueOf(active),
                    "Currently in progress",
                    "#B45309"),

                UIComponents.statCard(
                    "Delivered",
                    String.valueOf(delivered),
                    "Completed orders",
                    "#0D5C4B"),

                UIComponents.statCard(
                    "Cancelled",
                    String.valueOf(cancelled),
                    "Cancelled orders",
                    "#BA1A1A")
            );

            // ================= SEARCH AND FILTER =================

            TextField search = new TextField();

            search.setPromptText("Search by order ID");

            HBox.setHgrow(search, Priority.ALWAYS);

            ComboBox<String> filter = new ComboBox<>();

            filter.getItems().addAll(

                "All Orders",
                "PLACED",
                "CONFIRMED",
                "SHIPPED",
                "DELIVERED",
                "CANCELLED"
            );

            filter.setValue("All Orders");

            Button clear = UIComponents.secondaryButton(
                "Clear Filters");

            HBox filters = new HBox(
                12, search, filter, clear);

            filters.setAlignment(Pos.CENTER_LEFT);

            Label resultCount = UIComponents.mutedText("");

            VBox orderList = new VBox(20);

            Runnable applyFilters = () -> {

                orderList.getChildren().clear();

                String keyword = search.getText()
                    .trim().replace("#", "");

                String selected = filter.getValue();

                int count = 0;

                for (Order order : orders) {

                    boolean matchesSearch =
                        String.valueOf(order.getOrderId())
                              .contains(keyword);

                    boolean matchesStatus =
                        "All Orders".equals(selected)
                        || selected.equals(order.getStatus());

                    if (matchesSearch && matchesStatus) {

                        orderList.getChildren().add(

                            createOrderCard(

                                stage,
                                user,
                                order,

                                orderItems.get(
                                    order.getOrderId()),

                                images
                            )
                        );

                        count++;
                    }
                }

                resultCount.setText(
                    "Showing " + count + " of "
                    + orders.size() + " orders");

                if (count == 0) {

                    VBox empty = UIComponents.emptyState(

                        orders.isEmpty()
                            ? "No orders yet"
                            : "No matching orders",

                        orders.isEmpty()
                            ? "Your purchases will appear here."
                            : "Try changing your search or filter."
                    );

                    if (orders.isEmpty()) {

                        Button browse =
                            UIComponents.primaryButton(
                                "Start Shopping");

                        browse.setOnAction(e ->
                            CustomerDashboard.show(
                                stage, user));

                        empty.getChildren().add(browse);
                    }

                    orderList.getChildren().add(empty);
                }
            };

            search.textProperty().addListener(
                (obs, oldValue, newValue) ->
                    applyFilters.run());

            filter.valueProperty().addListener(
                (obs, oldValue, newValue) ->
                    applyFilters.run());

            clear.setOnAction(e -> {

                search.clear();

                filter.setValue("All Orders");
            });

            applyFilters.run();

            content.getChildren().addAll(

                statistics,

                filters,

                resultCount,

                orderList
            );

        } catch (Exception ex) {

            content.getChildren().add(

                UIComponents.emptyState(

                    "Unable to load orders",

                    "Please refresh and try again."
                )
            );

            ex.printStackTrace();
        }

        CheckoutPage.showPage(
            stage, user, content, "A2Z - My Orders");
    }

    // =====================================================
    // CREATE ORDER CARD
    // =====================================================

    private static VBox createOrderCard(

            Stage stage,
            User user,
            Order order,
            List<OrderItem> items,
            Map<Integer, String> images) {

        Label id = UIComponents.cardHeading(
            "Order #" + order.getOrderId());

        Label date = UIComponents.mutedText(
            "Ordered on " + formatDate(order));

        VBox identity = new VBox(5, id, date);

        Region space = new Region();

        HBox.setHgrow(space, Priority.ALWAYS);

        Label amount = new Label(
            UIComponents.currency(order.getTotalAmount()));

        amount.getStyleClass().add("mo-order-amount");

        VBox total = new VBox(

            5,

            UIComponents.mutedText("Order Total"),

            amount
        );

        HBox header = new HBox(

            20,

            identity,

            space,

            total,

            UIComponents.orderBadge(order.getStatus())
        );

        header.setAlignment(Pos.CENTER_LEFT);

        // ================= PURCHASED PRODUCTS =================

        VBox products = new VBox(12);

        int visibleItems = Math.min(items.size(), 3);

        for (int i = 0; i < visibleItems; i++) {

            products.getChildren().add(

                productRow(items.get(i), images)
            );
        }

        if (items.size() > visibleItems) {

            products.getChildren().add(

                UIComponents.mutedText(

                    "+" + (items.size() - visibleItems)
                    + " more products"
                )
            );
        }

        if (items.isEmpty()) {

            products.getChildren().add(

                UIComponents.mutedText(
                    "No purchased products found.")
            );
        }

        // ================= PAYMENT =================

        HBox payment = new HBox(

            15,

            UIComponents.mutedText(
                "Payment: " + order.getPaymentMethod()),

            OrderManagement.paymentBadge(
                order.getPaymentStatus())
        );

        payment.setAlignment(Pos.CENTER_LEFT);

        // ================= ACTION BUTTONS =================

        Button details = UIComponents.secondaryButton(
            "View Details");

        Button track = UIComponents.primaryButton(
            "Track Order");

        Button invoice = UIComponents.secondaryButton(
            "View Invoice");

        details.setOnAction(e ->

            OrderTracking.showDetails(
                stage, user, order.getOrderId())
        );

        track.setOnAction(e ->

            OrderTracking.show(
                stage, user, order.getOrderId())
        );

        invoice.setOnAction(e ->

            InvoicePage.show(
                stage, user, order.getOrderId())
        );

        track.setDisable(
            "CANCELLED".equals(order.getStatus()));

        FlowPane buttons = new FlowPane(

            12, 12,

            details, track, invoice
        );

        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox card = UIComponents.card(18);

        card.getChildren().addAll(

            header,

            new Separator(),

            products,

            new Separator(),

            createTracking(order.getStatus()),

            payment,

            buttons
        );

        card.getStyleClass().add("mo-order-card");

        return card;
    }

    // =====================================================
    // SHARED PRODUCT ROW
    // =====================================================

    public static HBox productRow(

            OrderItem item,
            Map<Integer, String> images) {

        StackPane image = UIComponents.productImage(

            images.get(item.getProductId()),

            76, 76
        );

        Label name = new Label(item.getProductName());

        name.getStyleClass().add("mo-product-name");

        name.setWrapText(true);

        VBox information = new VBox(

            6,

            name,

            UIComponents.mutedText(

                "Quantity: " + item.getQuantity()),

            UIComponents.mutedText(

                "Unit Price: "
                + UIComponents.currency(item.getPrice()))
        );

        HBox.setHgrow(information, Priority.ALWAYS);

        Label subtotal = new Label(

            UIComponents.currency(item.getSubtotal())
        );

        subtotal.getStyleClass().add("mo-product-total");

        HBox row = new HBox(

            16, image, information, subtotal
        );

        row.setAlignment(Pos.CENTER_LEFT);

        row.getStyleClass().add("mo-product-row");

        return row;
    }

    // =====================================================
    // SHARED ORDER TRACKING INDICATOR
    // =====================================================

    public static HBox createTracking(String status) {

        HBox tracking = new HBox(12);

        tracking.setAlignment(Pos.CENTER);

        if ("CANCELLED".equals(status)) {

            Label cancelled = new Label(
                "Order Cancelled");

            cancelled.getStyleClass().add(
                "mo-cancelled-banner");

            cancelled.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(cancelled, Priority.ALWAYS);

            tracking.getChildren().add(cancelled);

            return tracking;
        }

        String[] stages = {

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

        int current = -1;

        for (int i = 0; i < stages.length; i++) {

            if (stages[i].equals(status)) {
                current = i;
            }
        }

        for (int i = 0; i < stages.length; i++) {

            boolean completed =
                i < current
                || (i == current && current == 3);

            Label circle = new Label(

                completed ? "✓" : String.valueOf(i + 1)
            );

            circle.getStyleClass().add(
                "mo-step-circle");

            circle.getStyleClass().add(

                i <= current
                    ? "mo-step-complete"
                    : "mo-step-pending"
            );

            if (i == current) {

                circle.getStyleClass().add(
                    "mo-step-current");
            }

            Label title = new Label(titles[i]);

            title.getStyleClass().add("mo-step-title");

            if (i <= current) {

                title.getStyleClass().add(
                    "mo-step-title-active");
            }

            VBox step = new VBox(8, circle, title);

            step.setAlignment(Pos.CENTER);

            tracking.getChildren().add(step);

            if (i < stages.length - 1) {

                Region line = new Region();

                line.getStyleClass().add(

                    i < current
                        ? "mo-line-complete"
                        : "mo-line-pending"
                );

                HBox.setHgrow(line, Priority.ALWAYS);

                tracking.getChildren().add(line);
            }
        }

        return tracking;
    }

    // =====================================================
    // PRODUCT IMAGE MAPPING
    // =====================================================

    public static Map<Integer, String> loadImages() {

        Map<Integer, String> images = new HashMap<>();

        try {

            for (Product product :
                    ProductService.getProducts()) {

                images.put(

                    product.getId(),

                    product.getImagePath()
                );
            }

        } catch (Exception ex) {

            // Image loading failure should not prevent
            // customers from viewing their orders.

            ex.printStackTrace();
        }

        return images;
    }

    public static String formatDate(Order order) {

        if (order.getOrderDate() == null) {
            return "Not available";
        }

        return order.getOrderDate()

            .toLocalDateTime()

            .format(DateTimeFormatter.ofPattern(
                "dd MMM yyyy, hh:mm a"));
    }
}