
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class OrderManagement {

    public static void show(Stage stage, User user) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(20);
        content.setPadding(new Insets(24));

        VBox heading = new VBox(5,

                UIComponents.heading("Order Management"),

                UIComponents.mutedText(
                    "Manage customer orders and track their fulfilment.")
        );

        Button refresh =
                UIComponents.secondaryButton("↻ Refresh");

        refresh.setOnAction(e -> show(stage, user));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                15, heading, spacer, refresh);

        header.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(header);

        try {

            ObservableList<Order> orders =
                    FXCollections.observableArrayList(
                        OrderManagementService.getOrders());

            // ORDER STATISTICS

            GridPane statistics = new GridPane();

            statistics.setHgap(14);
            statistics.setVgap(14);

            for (int i = 0; i < 3; i++) {

                ColumnConstraints column =
                        new ColumnConstraints();

                column.setPercentWidth(100.0 / 3);
                column.setHgrow(Priority.ALWAYS);

                statistics.getColumnConstraints()
                          .add(column);
            }

            statistics.add(
                stat("Total Orders",
                     orders.size(),
                     "All customer orders",
                     "#0D5C4B"), 0, 0);

            statistics.add(
                stat("Placed",
                     count(orders, "PLACED"),
                     "Awaiting confirmation",
                     "#B45309"), 1, 0);

            statistics.add(
                stat("Confirmed",
                     count(orders, "CONFIRMED"),
                     "Ready for shipment",
                     "#0D5C4B"), 2, 0);

            statistics.add(
                stat("Shipped",
                     count(orders, "SHIPPED"),
                     "On the way to customers",
                     "#2563EB"), 0, 1);

            statistics.add(
                stat("Delivered",
                     count(orders, "DELIVERED"),
                     "Successfully completed",
                     "#15803D"), 1, 1);

            statistics.add(
                stat("Cancelled",
                     count(orders, "CANCELLED"),
                     "Cancelled customer orders",
                     "#BA1A1A"), 2, 1);

            content.getChildren().add(statistics);

            // SEARCH AND STATUS FILTER

            TextField search = new TextField();

            search.setPromptText(
                    "Search order ID or customer name...");

            search.setPrefWidth(320);

            ComboBox<String> statusFilter =
                    new ComboBox<>();

            statusFilter.getItems().addAll(
                    "All Status",
                    "PLACED",
                    "CONFIRMED",
                    "SHIPPED",
                    "DELIVERED",
                    "CANCELLED");

            statusFilter.setValue("All Status");

            Button clear =
                    UIComponents.secondaryButton(
                            "Clear Filters");

            FlowPane filters = new FlowPane(
                    12, 10,
                    search, statusFilter, clear);

            filters.setAlignment(Pos.CENTER_LEFT);

            // ORDER TABLE

            TableView<Order> table =
                    createOrderTable(stage, user);

            FilteredList<Order> filtered =
                    new FilteredList<>(orders, o -> true);

            SortedList<Order> sorted =
                    new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                    table.comparatorProperty());

            table.setItems(sorted);

            Label resultCount =
                    UIComponents.mutedText(
                        "Showing " + orders.size()
                        + " orders");

            Runnable applyFilters = () -> {

                String keyword = search.getText()
                        .trim()
                        .toLowerCase(Locale.ROOT);

                if (keyword.startsWith("#")) {

                    keyword = keyword.substring(1);
                }

                final String query = keyword;

                filtered.setPredicate(order -> {

                    boolean searchMatch =

                        String.valueOf(order.getOrderId())
                              .contains(query)

                        || order.getCustomerName()
                                .toLowerCase(Locale.ROOT)
                                .contains(query);

                    boolean statusMatch =

                        "All Status".equals(
                            statusFilter.getValue())

                        || order.getStatus().equals(
                            statusFilter.getValue());

                    return searchMatch && statusMatch;
                });

                resultCount.setText(
                    "Showing " + filtered.size()
                    + " of " + orders.size() + " orders");
            };

            search.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        applyFilters.run());

            statusFilter.setOnAction(
                e -> applyFilters.run());

            clear.setOnAction(e -> {

                search.clear();

                statusFilter.setValue("All Status");

                applyFilters.run();
            });

            VBox tableCard = UIComponents.card(16);

            tableCard.getStyleClass().add(
                    "om-table-card");

            tableCard.getChildren().addAll(

                    UIComponents.cardHeading(
                            "Customer Orders"),

                    filters,

                    table,

                    resultCount
            );

            content.getChildren().add(tableCard);

        } catch (Exception ex) {

            Label error = new Label(
                    "Unable to load orders. Please refresh.");

            error.getStyleClass().add("pm-error");

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        showPage(stage, user, "Orders", content);
    }

    // ==========================================
    // ORDER TABLE
    // ==========================================

    private static TableView<Order> createOrderTable(
            Stage stage, User user) {

        TableView<Order> table = new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(540);
        table.setFixedCellSize(65);

        table.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
                UIComponents.emptyState(
                    "No Orders Found",
                    "No customer orders match your filters."));

        TableColumn<Order, String> id =
                column("ORDER ID",
                       o -> "#" + o.getOrderId(), 95);

        TableColumn<Order, String> customer =
                column("CUSTOMER",
                       Order::getCustomerName, 160);

        TableColumn<Order, String> date =
                column("ORDER DATE",
                       o -> o.getOrderDate().toString(),
                       175);

        TableColumn<Order, java.math.BigDecimal> amount =
                column("TOTAL AMOUNT",
                       Order::getTotalAmount, 125);

        amount.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    java.math.BigDecimal value,
                    boolean empty) {

                super.updateItem(value, empty);

                setText(empty || value == null
                        ? null
                        : UIComponents.currency(value));
            }
        });

        TableColumn<Order, String> payment =
                column("PAYMENT METHOD",
                       Order::getPaymentMethod, 155);

        TableColumn<Order, String> paymentStatus =
                column("PAYMENT STATUS",
                       Order::getPaymentStatus, 155);

        paymentStatus.setCellFactory(
                col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                setGraphic(empty || value == null
                        ? null
                        : paymentBadge(value));
            }
        });

        TableColumn<Order, String> status =
                column("ORDER STATUS",
                       Order::getStatus, 130);

        status.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                setGraphic(empty || value == null
                        ? null
                        : statusBadge(value));
            }
        });

        // INDIVIDUAL ORDER ACTIONS

        TableColumn<Order, Order> actions =
                column("ACTIONS", o -> o, 265);

        actions.setSortable(false);

        actions.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Order order, boolean empty) {

                super.updateItem(order, empty);

                setText(null);

                if (empty || order == null) {

                    setGraphic(null);
                    return;
                }

                Button view = UIComponents.secondaryButton(
                        "View");

                view.getStyleClass().add("om-row-button");

                view.setOnAction(e ->
                    OrderDetails.show(
                        stage, user, order.getOrderId()));

                HBox buttons = new HBox(6, view);

                buttons.setAlignment(Pos.CENTER_LEFT);

                String next = nextStatus(order.getStatus());

                if (next != null) {

                    Button update =
                        UIComponents.primaryButton(
                            actionName(next));

                    update.getStyleClass().add(
                            "om-row-button");

                    update.setOnAction(e ->
                        performAction(
                            order,
                            next,
                            () -> show(stage, user)));

                    buttons.getChildren().add(update);
                }

                if (canCancel(order)) {

                    Button cancel =
                        UIComponents.destructiveButton(
                            "Cancel");

                    cancel.getStyleClass().add(
                            "om-row-button");

                    cancel.setOnAction(e ->
                        performAction(
                            order,
                            "CANCELLED",
                            () -> show(stage, user)));

                    buttons.getChildren().add(cancel);
                }

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(
                id, customer, date, amount,
                payment, paymentStatus,
                status, actions);

        return table;
    }

    // ==========================================
    // ORDER ACTIONS
    // ==========================================

    public static void performAction(
            Order order,
            String newStatus,
            Runnable refresh) {

        boolean cancellation =
                "CANCELLED".equals(newStatus);

        String title = cancellation

                ? "Cancel Order"

                : "Update Order Status";

        String message;

        if (cancellation) {

            message =
                "Cancel Order #" + order.getOrderId()
                + "?\n\nPurchased quantities will be "
                + "restored to inventory.";

            if ("SIMULATED".equals(
                    order.getPaymentStatus())) {

                message +=
                    "\nThe simulated payment will be "
                    + "marked as refunded.";
            }

        } else {

            message =
                "Change Order #" + order.getOrderId()
                + " from " + order.getStatus()
                + " to " + newStatus + "?";
        }

        boolean approved = UIComponents.confirm(
                title,
                message,
                cancellation
                    ? "Cancel Order"
                    : actionName(newStatus));

        if (!approved) {
            return;
        }

        try {

            if (cancellation) {

                OrderManagementService.cancelOrder(
                        order.getOrderId());

                UIComponents.success(
                    "Order cancelled successfully. "
                    + "Stock restored to inventory.");

            } else {

                OrderManagementService.updateStatus(
                        order.getOrderId(), newStatus);

                UIComponents.success(
                    "Order status updated successfully.");
            }

        } catch (IllegalArgumentException ex) {

            UIComponents.error(ex.getMessage());

        } catch (Exception ex) {

            UIComponents.error(
                    "Unable to update the order.");

            ex.printStackTrace();
        }

        refresh.run();
    }

    public static String nextStatus(String current) {

        switch (current) {

            case "PLACED":
                return "CONFIRMED";

            case "CONFIRMED":
                return "SHIPPED";

            case "SHIPPED":
                return "DELIVERED";

            default:
                return null;
        }
    }

    public static String actionName(String status) {

        switch (status) {

            case "CONFIRMED":
                return "Confirm";

            case "SHIPPED":
                return "Ship";

            case "DELIVERED":
                return "Deliver";

            default:
                return "Update";
        }
    }

    public static boolean canCancel(Order order) {

        return "PLACED".equals(order.getStatus())
                || "CONFIRMED".equals(order.getStatus());
    }

    // ==========================================
    // STATUS BADGES
    // ==========================================

    public static Label statusBadge(String status) {

        Label badge = new Label(status);

        switch (status) {

            case "PLACED":

                badge.getStyleClass().add(
                        "badge-warning");
                break;

            case "CONFIRMED":

                badge.getStyleClass().add(
                        "om-badge-confirmed");
                break;

            case "SHIPPED":

                badge.getStyleClass().add(
                        "om-badge-shipped");
                break;

            case "DELIVERED":

                badge.getStyleClass().add(
                        "badge-success");
                break;

            case "CANCELLED":

                badge.getStyleClass().add(
                        "badge-error");
                break;

            default:

                badge.getStyleClass().add(
                        "badge-neutral");
        }

        return badge;
    }

    public static Label paymentBadge(String status) {

        Label badge = new Label(
                status == null
                ? "Unknown"
                : status.replace("_", " "));

        if ("PAID".equals(status)) {

            badge.getStyleClass().add("badge-success");

        } else if ("SIMULATED".equals(status)) {

            badge.getStyleClass().add(
                    "om-badge-confirmed");

        } else if ("SIMULATED_REFUND".equals(status)) {

            badge.getStyleClass().add(
                    "badge-neutral");

        } else if ("CANCELLED".equals(status)
                || "FAILED".equals(status)) {

            badge.getStyleClass().add("badge-error");

        } else {

            badge.getStyleClass().add("badge-warning");
        }

        return badge;
    }

    // ==========================================
    // REUSABLE ADMIN LAYOUT
    // ==========================================

    public static void showPage(
            Stage stage,
            User user,
            String breadcrumb,
            VBox content) {

        BorderPane root = new BorderPane();

        root.getStyleClass().add(
                "admin-dashboard-root");

        VBox sidebar =
                AdminDashboard.createSidebar(stage, user);

        for (Node node : sidebar.getChildren()) {

            if (node instanceof Button) {

                Button button = (Button) node;

                button.getStyleClass().remove("active");

                if (button.getText().trim()
                        .endsWith("Orders")) {

                    button.getStyleClass().add("active");
                }
            }
        }

        root.setLeft(sidebar);

        Label path = UIComponents.mutedText(
                "Admin Workspace  ›  " + breadcrumb);

        Label admin = new Label(user.getName());

        admin.getStyleClass().add("admin-top-name");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(
                12, path, spacer, admin);

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add("admin-topbar");

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox workspace = new VBox(topBar, scroll);

        root.setCenter(workspace);

        Scene scene = new Scene(root, 1440, 900);

        UIComponents.applyStyles(scene);

        stage.setTitle(
                "A2Z Commerce - " + breadcrumb);

        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static long count(
            List<Order> orders, String status) {

        return orders.stream()
                .filter(o -> status.equals(o.getStatus()))
                .count();
    }

    private static VBox stat(
            String title,
            long value,
            String description,
            String color) {

        VBox card = UIComponents.statCard(
                title,
                String.valueOf(value),
                description,
                color);

        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }

    private static <S, T> TableColumn<S, T> column(
            String title,
            Function<S, T> getter,
            double width) {

        TableColumn<S, T> column =
                new TableColumn<>(title);

        column.setPrefWidth(width);

        column.setCellValueFactory(data ->
            new SimpleObjectProperty<>(
                getter.apply(data.getValue())));

        return column;
    }

    public static boolean isAdmin(
            Stage stage, User user) {

        if (user == null
                || !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);
            return false;
        }

        return true;
    }
}
