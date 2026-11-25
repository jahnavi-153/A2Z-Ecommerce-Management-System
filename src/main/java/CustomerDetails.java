
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.math.BigDecimal;
import java.util.List;

public class CustomerDetails {

    public static void show(
            Stage stage, User user, int customerId) {

        show(stage, user, customerId, false);
    }

    public static void show(
            Stage stage,
            User user,
            int customerId,
            boolean focusHistory) {

        if (!OrderManagement.isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(20);

        content.setPadding(new Insets(24));

        // BACK BUTTON

        Button back = UIComponents.tertiaryButton(
            "← Back to Customers");

        back.setOnAction(e ->
            CustomerManagement.show(stage, user));

        content.getChildren().add(back);

        try {

            // LOAD LATEST CUSTOMER DATA

            Customer customer = CustomerService
                .getCustomers()
                .stream()
                .filter(c -> c.getUserId() == customerId)
                .findFirst()
                .orElse(null);

            if (customer == null) {

                UIComponents.error("Customer not found.");

                CustomerManagement.show(stage, user);

                return;
            }

            List<Order> orders =
                CustomerService.getCustomerOrders(
                    customerId);

            // PAGE HEADER

            VBox heading = new VBox(6,

                UIComponents.heading("Customer Details"),

                UIComponents.mutedText(
                    "View customer profile and purchase history.")
            );

            Button refresh =
                UIComponents.secondaryButton("↻ Refresh");

            refresh.setOnAction(e ->

                show(stage, user,
                    customerId, focusHistory));

            Region spacer = new Region();

            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox header = new HBox(
                15, heading, spacer, refresh);

            header.setAlignment(Pos.CENTER_LEFT);

            // CUSTOMER PROFILE

            VBox profile = createProfile(customer);

            // CUSTOMER STATISTICS

            HBox statistics =
                createStatistics(customer);

            // ORDER HISTORY

            VBox history =
                createOrderHistory(
                    stage, user, customer, orders);

            content.getChildren().addAll(

                header,

                profile,

                statistics,

                history
            );

        } catch (Exception ex) {

            Label error = new Label(
                "Unable to load customer details.");

            error.getStyleClass().add("pm-error");

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        CustomerManagement.showPage(

            stage,

            user,

            "Customers  ›  Customer #" + customerId,

            content,

            focusHistory ? 1.0 : 0.0
        );
    }

    // ==========================================
    // CUSTOMER PROFILE CARD
    // ==========================================

    private static VBox createProfile(Customer customer) {

        VBox card = UIComponents.card(20);

        card.getStyleClass().add("cd-profile-card");

        Label avatar = CustomerManagement.createAvatar(
            customer.getName(), true);

        Label name = UIComponents.heading(
            customer.getName());

        Label customerId = UIComponents.mutedText(
            "Customer #" + customer.getUserId());

        Label role = new Label("CUSTOMER");

        role.getStyleClass().add("badge-success");

        VBox identity = new VBox(
            6, name, customerId, role);

        HBox profileHeader = new HBox(
            18, avatar, identity);

        profileHeader.setAlignment(Pos.CENTER_LEFT);

        // CUSTOMER INFORMATION GRID

        GridPane information = new GridPane();

        information.setHgap(25);
        information.setVgap(22);

        for (int i = 0; i < 2; i++) {

            ColumnConstraints column =
                new ColumnConstraints();

            column.setPercentWidth(50);

            column.setHgrow(Priority.ALWAYS);

            information.getColumnConstraints()
                       .add(column);
        }

        information.add(

            informationField(
                "Full Name",
                customer.getName()),

            0, 0
        );

        information.add(

            informationField(
                "Email Address",
                customer.getEmail()),

            1, 0
        );

        information.add(

            informationField(
                "Phone Number",
                customer.getPhone()),

            0, 1
        );

        information.add(

            informationField(
                "Registered On",

                customer.getCreatedAt() == null

                    ? "Not available"

                    : customer.getCreatedAt()
                              .toLocalDateTime()
                              .toLocalDate()
                              .toString()),

            1, 1
        );

        card.getChildren().addAll(

            UIComponents.cardHeading(
                "Customer Profile"),

            profileHeader,

            new Separator(),

            information
        );

        return card;
    }

    // ==========================================
    // CUSTOMER PURCHASE STATISTICS
    // ==========================================

    private static HBox createStatistics(
            Customer customer) {

        VBox total = UIComponents.statCard(

            "Total Orders",

            String.valueOf(customer.getTotalOrders()),

            "All orders placed",

            "#0D5C4B"
        );

        VBox delivered = UIComponents.statCard(

            "Delivered Orders",

            String.valueOf(customer.getDeliveredOrders()),

            "Successfully completed orders",

            "#15803D"
        );

        VBox spent = UIComponents.statCard(

            "Total Spent",

            UIComponents.currency(
                customer.getTotalSpent()),

            "Total value of delivered orders",

            "#D95D24"
        );

        for (VBox card : new VBox[]{
                total, delivered, spent}) {

            card.setMinWidth(0);

            card.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(card, Priority.ALWAYS);
        }

        return new HBox(15, total, delivered, spent);
    }

    // ==========================================
    // CUSTOMER ORDER HISTORY
    // ==========================================

    private static VBox createOrderHistory(
            Stage stage,
            User user,
            Customer customer,
            List<Order> orders) {

        VBox card = UIComponents.card(16);

        card.getStyleClass().add("cd-history-card");

        Label heading = UIComponents.cardHeading(
            "Order History");

        Label description = UIComponents.mutedText(
            "View all orders placed by "
            + customer.getName() + ".");

        ComboBox<String> statusFilter =
            new ComboBox<>();

        statusFilter.getItems().addAll(

            "All Status",

            "PLACED",

            "CONFIRMED",

            "SHIPPED",

            "DELIVERED",

            "CANCELLED"
        );

        statusFilter.setValue("All Status");

        TextField search = new TextField();

        search.setPromptText("Search order ID...");

        search.setPrefWidth(230);

        Button clear =
            UIComponents.secondaryButton("Clear Filters");

        FlowPane filters = new FlowPane(
            12, 10, search, statusFilter, clear);

        filters.setAlignment(Pos.CENTER_LEFT);

        // ORDER HISTORY TABLE

        TableView<Order> table = new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(420);

        table.setFixedCellSize(65);

        table.setColumnResizePolicy(
            TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(

            UIComponents.emptyState(

                "No Orders Found",

                "No customer orders match the current filters.")
        );

        TableColumn<Order, Integer> id =
            CustomerManagement.column(
                "ORDER ID", Order::getOrderId, 95);

        TableColumn<Order, String> date =
            CustomerManagement.column(

                "ORDER DATE",

                o -> o.getOrderDate() == null

                    ? "Not available"

                    : o.getOrderDate().toString(),

                175
            );

        TableColumn<Order, BigDecimal> amount =
            CustomerManagement.column(

                "TOTAL AMOUNT",

                Order::getTotalAmount,

                140
            );

        amount.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    BigDecimal value, boolean empty) {

                super.updateItem(value, empty);

                setText(empty || value == null

                    ? null

                    : UIComponents.currency(value));
            }
        });

        TableColumn<Order, String> payment =
            CustomerManagement.column(

                "PAYMENT METHOD",

                Order::getPaymentMethod,

                165
            );

        TableColumn<Order, String> paymentStatus =
            CustomerManagement.column(

                "PAYMENT STATUS",

                Order::getPaymentStatus,

                155
            );

        paymentStatus.setCellFactory(
                col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                setGraphic(empty || value == null

                    ? null

                    : OrderManagement.paymentBadge(value));
            }
        });

        TableColumn<Order, String> status =
            CustomerManagement.column(

                "ORDER STATUS",

                Order::getStatus,

                135
            );

        status.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                setGraphic(empty || value == null

                    ? null

                    : OrderManagement.statusBadge(value));
            }
        });

        // VIEW ORDER DETAILS

        TableColumn<Order, Order> action =
            CustomerManagement.column(
                "ACTION", o -> o, 145);

        action.setSortable(false);

        action.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Order order, boolean empty) {

                super.updateItem(order, empty);

                setText(null);

                if (empty || order == null) {

                    setGraphic(null);
                    return;
                }

                Button view =
                    UIComponents.secondaryButton(
                        "View Order");

                view.getStyleClass().add("cm-row-button");

                view.setOnAction(e ->

                    OrderDetails.show(

                        stage,

                        user,

                        order.getOrderId(),

                        () -> CustomerDetails.show(

                            stage,

                            user,

                            customer.getUserId(),

                            true
                        )
                    )
                );

                setGraphic(view);
            }
        });

        table.getColumns().addAll(

            id, date, amount,

            payment, paymentStatus,

            status, action
        );

        // SEARCH AND FILTER FUNCTIONALITY

        FilteredList<Order> filtered =
            new FilteredList<>(

                FXCollections.observableArrayList(orders),

                o -> true
            );

        SortedList<Order> sorted =
            new SortedList<>(filtered);

        sorted.comparatorProperty().bind(
            table.comparatorProperty());

        table.setItems(sorted);

        Label resultCount = UIComponents.mutedText(
            "Showing " + orders.size() + " orders");

        Runnable applyFilters = () -> {

            String keyword = search.getText().trim();

            if (keyword.startsWith("#")) {

                keyword = keyword.substring(1);
            }

            final String query = keyword;

            filtered.setPredicate(order -> {

                boolean searchMatch =

                    String.valueOf(order.getOrderId())
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

                + " of " + orders.size()

                + " orders"
            );
        };

        search.textProperty().addListener(

            (obs, oldValue, newValue) ->

                applyFilters.run()
        );

        statusFilter.setOnAction(
            e -> applyFilters.run());

        clear.setOnAction(e -> {

            search.clear();

            statusFilter.setValue("All Status");

            applyFilters.run();
        });

        card.getChildren().addAll(

            heading,

            description,

            filters,

            table,

            resultCount
        );

        return card;
    }

    // ==========================================
    // INFORMATION FIELD
    // ==========================================

    private static VBox informationField(
            String title, String value) {

        Label label = UIComponents.mutedText(title);

        Label text = new Label(
            CustomerManagement.displayValue(value));

        text.getStyleClass().add(
            "cd-information-value");

        text.setWrapText(true);

        return new VBox(6, label, text);
    }
}
