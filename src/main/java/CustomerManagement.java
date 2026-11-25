
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class CustomerManagement {

    public static void show(Stage stage, User user) {

        if (!OrderManagement.isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(20);

        content.setPadding(new Insets(24));

        // PAGE HEADER

        VBox heading = new VBox(6,

            UIComponents.heading("Customer Management"),

            UIComponents.mutedText(
                "Manage customer information and view purchase history.")
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

            ObservableList<Customer> customers =
                FXCollections.observableArrayList(
                    CustomerService.getCustomers());

            // CUSTOMER STATISTICS

            content.getChildren().add(
                createStatistics(customers));

            // SEARCH AND FILTERS

            TextField search = new TextField();

            search.setPromptText(
                "Search by name, email, phone or ID...");

            search.setPrefWidth(380);

            ComboBox<String> filter = new ComboBox<>();

            filter.getItems().addAll(
                "All Customers",
                "Active Buyers",
                "No Orders"
            );

            filter.setValue("All Customers");

            Button clear =
                UIComponents.secondaryButton("Clear Filters");

            FlowPane filters = new FlowPane(
                12, 10, search, filter, clear);

            filters.setAlignment(Pos.CENTER_LEFT);

            // CUSTOMER TABLE

            TableView<Customer> table =
                createTable(stage, user);

            FilteredList<Customer> filtered =
                new FilteredList<>(customers, c -> true);

            SortedList<Customer> sorted =
                new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                table.comparatorProperty());

            table.setItems(sorted);

            Label resultCount =
                UIComponents.mutedText(
                    "Showing " + customers.size()
                    + " customers");

            Runnable applyFilters = () -> {

                String keyword = search.getText()
                    .trim()
                    .toLowerCase(Locale.ROOT);

                if (keyword.startsWith("#")) {
                    keyword = keyword.substring(1);
                }

                final String query = keyword;

                filtered.setPredicate(customer -> {

                    boolean matchesSearch =

                        contains(customer.getName(), query)

                        || contains(customer.getEmail(), query)

                        || contains(customer.getPhone(), query)

                        || String.valueOf(customer.getUserId())
                                 .contains(query);

                    boolean matchesFilter = true;

                    if ("Active Buyers".equals(
                            filter.getValue())) {

                        matchesFilter =
                            customer.getTotalOrders() > 0;

                    } else if ("No Orders".equals(
                            filter.getValue())) {

                        matchesFilter =
                            customer.getTotalOrders() == 0;
                    }

                    return matchesSearch && matchesFilter;
                });

                resultCount.setText(
                    "Showing " + filtered.size()
                    + " of " + customers.size()
                    + " customers");
            };

            search.textProperty().addListener(
                (obs, oldValue, newValue) ->
                    applyFilters.run());

            filter.setOnAction(
                e -> applyFilters.run());

            clear.setOnAction(e -> {

                search.clear();

                filter.setValue("All Customers");

                applyFilters.run();
            });

            // CUSTOMER TABLE CARD

            VBox tableCard = UIComponents.card(16);

            tableCard.getStyleClass().add(
                "cm-table-card");

            tableCard.getChildren().addAll(

                UIComponents.cardHeading(
                    "Customer Directory"),

                UIComponents.mutedText(
                    "View registered customers and their order activity."),

                filters,

                table,

                resultCount
            );

            content.getChildren().add(tableCard);

        } catch (Exception ex) {

            Label error = new Label(
                "Unable to load customers. Please refresh.");

            error.getStyleClass().add("pm-error");

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        showPage(stage, user, "Customers", content);
    }

    // ==========================================
    // CUSTOMER STATISTICS
    // ==========================================

    private static HBox createStatistics(
            List<Customer> customers) {

        long activeBuyers = customers.stream()

            .filter(c -> c.getTotalOrders() > 0)

            .count();

        Customer topCustomer = customers.stream()

            .filter(c -> c.getTotalSpent().signum() > 0)

            .max((a, b) ->
                a.getTotalSpent().compareTo(
                    b.getTotalSpent()))

            .orElse(null);

        VBox total = UIComponents.statCard(

            "Total Customers",

            String.valueOf(customers.size()),

            "Registered customer accounts",

            "#0D5C4B"
        );

        VBox active = UIComponents.statCard(

            "Active Buyers",

            String.valueOf(activeBuyers),

            "Customers with at least one order",

            "#15803D"
        );

        VBox top = UIComponents.statCard(

            "Top Customer",

            topCustomer == null
                ? "None"
                : topCustomer.getName(),

            topCustomer == null

                ? "No delivered purchases yet"

                : UIComponents.currency(
                    topCustomer.getTotalSpent())
                    + " in delivered orders",

            "#D95D24"
        );

        for (VBox card : new VBox[]{total, active, top}) {

            card.setMinWidth(0);

            card.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(card, Priority.ALWAYS);
        }

        return new HBox(15, total, active, top);
    }

    // ==========================================
    // CUSTOMER TABLE
    // ==========================================

    private static TableView<Customer> createTable(
            Stage stage, User user) {

        TableView<Customer> table = new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(540);

        table.setFixedCellSize(68);

        table.setColumnResizePolicy(
            TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
            UIComponents.emptyState(
                "No Customers Found",
                "No customers match your current filters."));

        TableColumn<Customer, Integer> id =
            column("ID", Customer::getUserId, 65);

        TableColumn<Customer, String> name =
            column("CUSTOMER", Customer::getName, 190);

        name.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);
                    return;
                }

                Label avatar = createAvatar(value, false);

                Label customerName = new Label(value);

                customerName.getStyleClass().add(
                    "cm-customer-name");

                HBox row = new HBox(
                    10, avatar, customerName);

                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
            }
        });

        TableColumn<Customer, String> email =
            column("EMAIL", Customer::getEmail, 210);

        TableColumn<Customer, String> phone =
            column("PHONE",
                c -> displayValue(c.getPhone()), 130);

        TableColumn<Customer, String> registered =
            column("REGISTERED",
                c -> c.getCreatedAt() == null

                    ? "Not available"

                    : c.getCreatedAt()
                       .toLocalDateTime()
                       .toLocalDate()
                       .toString(),

                120);

        TableColumn<Customer, Integer> orders =
            column("ORDERS",
                Customer::getTotalOrders, 85);

        TableColumn<Customer, Integer> delivered =
            column("DELIVERED",
                Customer::getDeliveredOrders, 100);

        TableColumn<Customer, BigDecimal> spent =
            column("TOTAL SPENT",
                Customer::getTotalSpent, 135);

        spent.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    BigDecimal value, boolean empty) {

                super.updateItem(value, empty);

                setText(empty || value == null

                    ? null

                    : UIComponents.currency(value));
            }
        });

        // CUSTOMER ACTIONS

        TableColumn<Customer, Customer> actions =
            column("ACTIONS", c -> c, 210);

        actions.setSortable(false);

        actions.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Customer customer, boolean empty) {

                super.updateItem(customer, empty);

                setText(null);

                if (empty || customer == null) {

                    setGraphic(null);
                    return;
                }

                Button details =
                    UIComponents.primaryButton("View Details");

                Button history =
                    UIComponents.secondaryButton("History");

                details.getStyleClass().add("cm-row-button");

                history.getStyleClass().add("cm-row-button");

                details.setOnAction(e ->

                    CustomerDetails.show(
                        stage, user, customer.getUserId()));

                history.setOnAction(e ->

                    CustomerDetails.show(
                        stage, user,
                        customer.getUserId(), true));

                HBox buttons = new HBox(
                    7, details, history);

                buttons.setAlignment(Pos.CENTER_LEFT);

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(

            id, name, email, phone,

            registered, orders, delivered,

            spent, actions
        );

        return table;
    }

    // ==========================================
    // CUSTOMER AVATAR
    // ==========================================

    public static Label createAvatar(
            String name, boolean large) {

        String initials = "?";

        if (name != null && !name.isBlank()) {

            String[] parts = name.trim().split("\\s+");

            initials = parts[0]
                .substring(0, 1)
                .toUpperCase(Locale.ROOT);

            if (parts.length > 1) {

                initials += parts[parts.length - 1]
                    .substring(0, 1)
                    .toUpperCase(Locale.ROOT);
            }
        }

        Label avatar = new Label(initials);

        avatar.getStyleClass().add(

            large

                ? "cm-avatar-large"

                : "cm-avatar"
        );

        return avatar;
    }

    // ==========================================
    // SHARED CUSTOMER PAGE LAYOUT
    // ==========================================

    public static void showPage(
            Stage stage,
            User user,
            String breadcrumb,
            VBox content) {

        showPage(stage, user, breadcrumb, content, 0);
    }

    public static void showPage(
            Stage stage,
            User user,
            String breadcrumb,
            VBox content,
            double scrollPosition) {

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
                          .endsWith("Customers")) {

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

        javafx.application.Platform.runLater(() -> {

            scroll.setVvalue(scrollPosition);
        });
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private static boolean contains(
            String value, String query) {

        return value != null

            && value.toLowerCase(Locale.ROOT)
                    .contains(query);
    }

    public static String displayValue(String value) {

        return value == null || value.isBlank()

            ? "Not provided"

            : value;
    }

    public static <S, T> TableColumn<S, T> column(
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
}
