
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AdminDashboard {

    public static void show(Stage stage, User user) {

        if (user == null ||
                !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);
            return;
        }

        BorderPane root = new BorderPane();
        root.getStyleClass().add("admin-dashboard-root");

        root.setLeft(createSidebar(stage, user));

        VBox workspace = new VBox();

        workspace.getChildren().add(
                createTopBar(stage, user));

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));

        Label heading =
                UIComponents.heading("Dashboard");

        Label welcome = UIComponents.mutedText(
                "Welcome back, " + user.getName()
                + ". Here is the live overview of A2Z "
                + "store orders, revenue, and stock health."
        );

        VBox pageHeading = new VBox(5, heading, welcome);

        Button addProduct =
                UIComponents.primaryButton("+ Add Product");

        Button inventory =
                UIComponents.secondaryButton("Manage Inventory");

        Button orders =
                UIComponents.secondaryButton("Manage Orders");

        Button reports =
                UIComponents.secondaryButton("View Reports");

        addProduct.setOnAction(e ->
                ProductManagement.showAddProduct(stage, user));

        inventory.setOnAction(e ->
                InventoryManagement.show(stage, user));

        orders.setOnAction(e ->
                OrderManagement.show(stage, user));

        reports.setOnAction(e ->
                Reports.show(stage, user));

        FlowPane actions = new FlowPane(10, 10);

        actions.getChildren().addAll(
                addProduct, inventory, orders, reports);

        content.getChildren().addAll(pageHeading, actions);

        try {

            HBox mainStats = new HBox(14);

            mainStats.getChildren().addAll(

                statCard(
                    "Total Products",
                    String.valueOf(
                        DashboardService.totalProducts()),
                    "Across "
                    + DashboardService.totalCategories()
                    + " active categories",
                    "▣", "#0D5C4B", "#E9F3EE",
                    "#191C1B"
                ),

                statCard(
                    "Total Orders",
                    String.valueOf(
                        DashboardService.totalOrders()),
                    "All recorded customer orders",
                    "▤", "#0D5C4B", "#E9F3EE",
                    "#191C1B"
                ),

                statCard(
                    "Total Customers",
                    String.valueOf(
                        DashboardService.totalCustomers()),
                    "Registered customer accounts",
                    "♙", "#0D5C4B", "#E9F3EE",
                    "#191C1B"
                ),

                statCard(
                    "Total Revenue",
                    UIComponents.currency(
                        DashboardService.totalRevenue()),
                    "Delivered order value",
                    "₹", "#15803D", "#DCFCE7",
                    "#191C1B"
                )
            );

            HBox smallStats = new HBox(14);

            smallStats.getChildren().addAll(

                smallCard(
                    "Low Stock Products",
                    DashboardService.lowStockProducts(),
                    "!", "#B45309", "#FEF3C7"
                ),

                smallCard(
                    "Out of Stock Products",
                    DashboardService.outOfStockProducts(),
                    "×", "#BA1A1A", "#FEE2E2"
                ),

                smallCard(
                    "Pending Orders",
                    DashboardService.pendingOrders(),
                    "▤", "#0D5C4B", "#E9F3EE"
                ),

                smallCard(
                    "Delivered Orders",
                    DashboardService.deliveredOrders(),
                    "✓", "#15803D", "#DCFCE7"
                )
            );

            VBox salesPanel = createSalesPanel();

            VBox statusPanel = createStatusPanel();

            HBox chartRow = new HBox(
                    16, salesPanel, statusPanel);

            HBox.setHgrow(salesPanel, Priority.ALWAYS);
            HBox.setHgrow(statusPanel, Priority.ALWAYS);

            salesPanel.prefWidthProperty().bind(
                    chartRow.widthProperty()
                            .subtract(16).multiply(0.58));

            statusPanel.prefWidthProperty().bind(
                    chartRow.widthProperty()
                            .subtract(16).multiply(0.42));

            VBox recentOrders =
                    createRecentOrders(stage, user);

            VBox stockAlerts =
                    createStockAlerts(stage, user);

            VBox bestSelling =
                    createBestSelling();

            VBox rightColumn = new VBox(
                    16, stockAlerts, bestSelling);

            HBox bottomRow = new HBox(
                    16, recentOrders, rightColumn);

            HBox.setHgrow(recentOrders, Priority.ALWAYS);
            HBox.setHgrow(rightColumn, Priority.ALWAYS);

            recentOrders.prefWidthProperty().bind(
                    bottomRow.widthProperty()
                            .subtract(16).multiply(0.58));

            rightColumn.prefWidthProperty().bind(
                    bottomRow.widthProperty()
                            .subtract(16).multiply(0.42));

            content.getChildren().addAll(
                    mainStats,
                    smallStats,
                    chartRow,
                    bottomRow
            );

        } catch (Exception ex) {

            Label error = new Label(
                    "Unable to load dashboard data. "
                    + "Please check your database connection.");

            error.getStyleClass().add("admin-error");
            error.setWrapText(true);

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        workspace.getChildren().add(scroll);

        root.setCenter(workspace);

        Scene scene = new Scene(root, 1440, 900);

        UIComponents.applyStyles(scene);

        stage.setTitle("A2Z Commerce - Admin Dashboard");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    // ADMIN SIDEBAR

    public static VBox createSidebar(
            Stage stage, User user) {

        Label logo = new Label("A2Z");

        logo.getStyleClass().add("admin-logo");

        Label brandName = new Label("A2Z Commerce");

        brandName.getStyleClass().add("admin-brand-name");

        Label caption = new Label("Admin Workspace");

        caption.getStyleClass().add("admin-brand-caption");

        VBox brandText = new VBox(3, brandName, caption);

        HBox brand = new HBox(9, logo, brandText);

        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(5, 2, 18, 2));

        Button dashboard =
                menuButton("▦", "Dashboard");

        dashboard.getStyleClass().add("active");

        Button products =
                menuButton("▣", "Products");

        Button inventory =
                menuButton("▤", "Inventory");

        Button orders =
                menuButton("▧", "Orders");

        Button customers =
                menuButton("♙", "Customers");

        Button suppliers =
                menuButton("▱", "Suppliers");

        Button reports =
                menuButton("▥", "Reports");

        Button logout =
                menuButton("↪", "Logout");

        dashboard.setOnAction(e ->
                show(stage, user));

        products.setOnAction(e ->
                ProductManagement.show(stage, user));

        inventory.setOnAction(e ->
                InventoryManagement.show(stage, user));

        orders.setOnAction(e ->
                OrderManagement.show(stage, user));

        customers.setOnAction(e ->
                CustomerManagement.show(stage, user));

        suppliers.setOnAction(e ->
                SupplierManagement.show(stage, user));

        reports.setOnAction(e ->
                Reports.show(stage, user));

        logout.setOnAction(e -> {

            boolean confirmed = UIComponents.confirm(
                    "Confirm Logout",
                    "Are you sure you want to sign out "
                    + "of your A2Z session?",
                    "Logout"
            );

            if (confirmed) {
                LoginPage.show(stage);
            }
        });

        Region spacer = new Region();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label avatar = new Label(
                initials(user.getName()));

        avatar.getStyleClass().add("admin-avatar-dark");

        Label name = new Label(user.getName());

        name.getStyleClass().add("admin-profile-name");
        name.setWrapText(true);

        Label role = new Label("Administrator");

        role.getStyleClass().add("admin-brand-caption");

        VBox profileText = new VBox(3, name, role);

        HBox profile = new HBox(9, avatar, profileText);

        profile.setAlignment(Pos.CENTER_LEFT);

        profile.getStyleClass().add("admin-profile");

        VBox sidebar = new VBox(
                7,
                brand,
                dashboard,
                products,
                inventory,
                orders,
                customers,
                suppliers,
                reports,
                spacer,
                profile,
                logout
        );

        sidebar.getStyleClass().addAll(
                "sidebar", "admin-sidebar");

        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);

        return sidebar;
    }

    private static Button menuButton(
            String icon, String name) {

        Button button = new Button(icon + "    " + name);

        button.getStyleClass().add("sidebar-nav-btn");

        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        return button;
    }

    // TOP NAVIGATION

    private static HBox createTopBar(
            Stage stage, User user) {

        Label breadcrumb = UIComponents.mutedText(
                "Admin Workspace   ›   Dashboard");

        Button refresh =
                UIComponents.secondaryButton("↻ Refresh");

        refresh.getStyleClass().add("admin-compact-button");

        refresh.setOnAction(e -> show(stage, user));

        Label avatar = new Label(
                initials(user.getName()));

        avatar.getStyleClass().add("admin-avatar-light");

        Label name = new Label(user.getName());

        name.getStyleClass().add("admin-top-name");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(
                12, breadcrumb, spacer,
                refresh, avatar, name);

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add("admin-topbar");

        return topBar;
    }

    // MAIN STATISTICS CARDS

    private static VBox statCard(
            String title,
            String value,
            String description,
            String icon,
            String iconColor,
            String iconBackground,
            String valueColor) {

        Label titleLabel =
                UIComponents.mutedText(title);

        Label iconLabel = statIcon(
                icon, iconColor, iconBackground);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(
                5, titleLabel, spacer, iconLabel);

        top.setAlignment(Pos.CENTER_LEFT);

        Label number = new Label(value);

        number.getStyleClass().add("admin-stat-value");

        number.setStyle("-fx-text-fill: "
                + valueColor + ";");

        number.setWrapText(true);

        Label details =
                UIComponents.mutedText(description);

        details.getStyleClass().add(
                "admin-stat-description");

        VBox card = new VBox(
                10, top, number, details);

        card.getStyleClass().addAll(
                "kpi-card", "admin-main-stat");

        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(0);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // SMALL STATISTICS CARDS

    private static VBox smallCard(
            String title,
            int value,
            String icon,
            String color,
            String background) {

        Label titleLabel =
                UIComponents.mutedText(title);

        Label number = new Label(String.valueOf(value));

        number.getStyleClass().add("admin-small-value");

        number.setStyle("-fx-text-fill: " + color + ";");

        VBox text = new VBox(4, titleLabel, number);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(
                5, text, spacer,
                statIcon(icon, color, background));

        row.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(row);

        card.getStyleClass().addAll(
                "kpi-card", "admin-small-stat");

        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(0);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    private static Label statIcon(
            String icon,
            String color,
            String background) {

        Label label = new Label(icon);

        label.setAlignment(Pos.CENTER);
        label.setMinSize(36, 36);
        label.setPrefSize(36, 36);

        label.setStyle(
                "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + color + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-font-size: 18px;"
                + "-fx-font-weight: bold;"
        );

        return label;
    }

    // DAILY SALES TREND

    private static VBox createSalesPanel()
            throws Exception {

        Label heading =
                UIComponents.cardHeading("Daily Sales Trend");

        Label subtitle = UIComponents.mutedText(
                "Delivered order revenue over the last 7 days");

        Label period = new Label("Last 7 Days");

        period.getStyleClass().add("badge-primary");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                5, heading, spacer, period);

        header.setAlignment(Pos.CENTER_LEFT);

        CategoryAxis xAxis = new CategoryAxis();

        NumberAxis yAxis = new NumberAxis();

        yAxis.setForceZeroInRange(true);

        yAxis.setTickLabelFormatter(
                new javafx.util.StringConverter<Number>() {

            @Override
            public String toString(Number value) {

                if (value.doubleValue() >= 1000) {

                    return "₹"
                            + String.format(
                                "%.1f",
                                value.doubleValue() / 1000)
                            + "k";
                }

                return "₹" + value.intValue();
            }

            @Override
            public Number fromString(String value) {
                return 0;
            }
        });

        AreaChart<String, Number> chart =
                new AreaChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(245);
        chart.setMinHeight(245);

        chart.getStyleClass().add("admin-sales-chart");

        XYChart.Series<String, Number> series =
                new XYChart.Series<>();

        Map<String, BigDecimal> sales =
                DashboardService.lastSevenDaysSales();

        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("EEE");

        for (Map.Entry<String, BigDecimal> entry
                : sales.entrySet()) {

            String day = LocalDate.parse(
                    entry.getKey()).format(format);

            series.getData().add(
                    new XYChart.Data<>(
                        day, entry.getValue()));
        }

        chart.getData().add(series);

        VBox panel = UIComponents.card(8);

        panel.getStyleClass().add("admin-panel");

        panel.getChildren().addAll(
                header, subtitle, chart);

        panel.setMinWidth(0);

        return panel;
    }

    // ORDER STATUS DONUT CHART

    private static VBox createStatusPanel()
            throws Exception {

        int delivered =
                DashboardService.deliveredOrders();

        int pending =
                DashboardService.pendingOrders();

        int cancelled =
                DashboardService.cancelledOrders();

        int total =
                DashboardService.totalOrders();

        Label heading = UIComponents.cardHeading(
                "Order Status Breakdown");

        Label subtitle = UIComponents.mutedText(
                "Distribution across "
                + total + " total orders");

        VBox panel = UIComponents.card(10);

        panel.getStyleClass().add("admin-panel");

        panel.setMinWidth(0);

        panel.getChildren().addAll(heading, subtitle);

        if (total == 0) {

            VBox empty = UIComponents.emptyState(
                    "No Orders Available",
                    "Order statistics will appear here.");

            empty.setPrefHeight(220);

            panel.getChildren().add(empty);

            return panel;
        }

        PieChart chart = new PieChart();

        chart.setLegendVisible(false);
        chart.setLabelsVisible(false);
        chart.setAnimated(false);
        chart.setStartAngle(90);

        chart.setPrefSize(210, 210);
        chart.setMinSize(170, 170);

        chart.getStyleClass().add("admin-donut-chart");

        addPieData(chart, "Delivered",
                delivered, "#0D5C4B");

        addPieData(chart, "In Progress",
                pending, "#D95D24");

        addPieData(chart, "Cancelled",
                cancelled, "#BA1A1A");

        Circle hole = new Circle(52);

        hole.setFill(javafx.scene.paint.Color.WHITE);

        Label totalLabel =
                new Label(String.valueOf(total));

        totalLabel.getStyleClass().add("admin-donut-total");

        Label caption =
                UIComponents.mutedText("Total Orders");

        VBox center = new VBox(3, totalLabel, caption);

        center.setAlignment(Pos.CENTER);
        center.setMouseTransparent(true);

        StackPane donut = new StackPane(
                chart, hole, center);

        donut.setPrefSize(210, 230);

        VBox legend = new VBox(
                15,
                legendItem("Delivered",
                        delivered, "#0D5C4B"),

                legendItem("In Progress",
                        pending, "#D95D24"),

                legendItem("Cancelled",
                        cancelled, "#BA1A1A")
        );

        legend.setAlignment(Pos.CENTER_LEFT);

        HBox chartContent = new HBox(
                12, donut, legend);

        chartContent.setAlignment(Pos.CENTER);

        HBox.setHgrow(donut, Priority.ALWAYS);

        panel.getChildren().add(chartContent);

        return panel;
    }

    private static void addPieData(
            PieChart chart,
            String name,
            int value,
            String color) {

        if (value <= 0) {
            return;
        }

        PieChart.Data data =
                new PieChart.Data(name, value);

        chart.getData().add(data);

        data.nodeProperty().addListener(
                (obs, oldNode, newNode) -> {

                    if (newNode != null) {

                        newNode.setStyle(
                                "-fx-pie-color: "
                                + color + ";");
                    }
                });

        if (data.getNode() != null) {

            data.getNode().setStyle(
                    "-fx-pie-color: " + color + ";");
        }
    }

    private static HBox legendItem(
            String title, int count, String color) {

        Circle dot = new Circle(5);

        dot.setFill(
                javafx.scene.paint.Color.web(color));

        Label label = new Label(title);

        label.getStyleClass().add("admin-legend-label");

        Label value = new Label(
                String.valueOf(count));

        value.getStyleClass().add("admin-legend-value");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(
                7, dot, label, spacer, value);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // RECENT ORDERS

    private static VBox createRecentOrders(
            Stage stage, User user) throws Exception {

        Label heading =
                UIComponents.cardHeading("Recent Orders");

        Label subtitle = UIComponents.mutedText(
                "Latest customer transactions");

        Button viewAll =
                UIComponents.tertiaryButton("View All Orders");

        viewAll.getStyleClass().add("admin-compact-button");

        viewAll.setOnAction(e ->
                OrderManagement.show(stage, user));

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                5, heading, spacer, viewAll);

        header.setAlignment(Pos.CENTER_LEFT);

        TableView<String[]> table = new TableView<>();

        table.getStyleClass().add("admin-recent-table");

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
                new Label("No orders available"));

        table.getColumns().addAll(

                textColumn("ORDER ID", 0),
                textColumn("CUSTOMER", 1),
                textColumn("DATE", 2),
                textColumn("AMOUNT", 3),
                textColumn("PAYMENT", 4),
                statusColumn()
        );

        table.setItems(
                FXCollections.observableArrayList(
                    DashboardService.recentOrders()));

        table.setPrefHeight(300);

        VBox panel = UIComponents.card(10);

        panel.getStyleClass().add("admin-panel");

        panel.getChildren().addAll(
                header, subtitle, table);

        panel.setMinWidth(0);

        return panel;
    }

    private static TableColumn<String[], String> textColumn(
            String title, int index) {

        TableColumn<String[], String> column =
                new TableColumn<>(title);

        column.setCellValueFactory(data -> {

            String value = data.getValue()[index];

            if (index == 0) {

                value = "#ORD-" + value;

            } else if (index == 3) {

                value = UIComponents.currency(
                        new BigDecimal(value));

            } else if (index == 4 && value != null) {

                if (value.toUpperCase().contains("UPI")) {
                    value = "UPI";
                } else if (value.toUpperCase()
                        .contains("CARD")) {
                    value = "Card";
                } else if (value.toUpperCase()
                        .contains("CASH")) {
                    value = "COD";
                }
            }

            return new SimpleStringProperty(value);
        });

        column.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String item, boolean empty) {

                super.updateItem(item, empty);

                setText(null);

                if (empty || item == null) {

                    setGraphic(null);

                } else {

                    Label label = new Label(item);

                    label.setWrapText(true);
                    label.maxWidthProperty().bind(
                            widthProperty().subtract(12));

                    setGraphic(label);
                }
            }
        });

        return column;
    }

    private static TableColumn<String[], String>
            statusColumn() {

        TableColumn<String[], String> column =
                new TableColumn<>("STATUS");

        column.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue()[5]));

        column.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String status, boolean empty) {

                super.updateItem(status, empty);

                setText(null);

                if (empty || status == null) {

                    setGraphic(null);

                } else {

                    setGraphic(
                        UIComponents.orderBadge(status));
                }
            }
        });

        return column;
    }

    // LOW AND OUT OF STOCK ALERTS

    private static VBox createStockAlerts(
            Stage stage, User user) throws Exception {

        Label heading = UIComponents.cardHeading(
                "Low & Out of Stock Alerts");

        Button restock =
                UIComponents.tertiaryButton("Restock");

        restock.getStyleClass().add("admin-compact-button");

        restock.setOnAction(e ->
                InventoryManagement.show(stage, user));

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                5, heading, spacer, restock);

        header.setAlignment(Pos.CENTER_LEFT);

        VBox panel = UIComponents.card(12);

        panel.getStyleClass().add("admin-panel");

        panel.getChildren().add(header);

        java.util.List<String[]> alerts =
                DashboardService.stockAlertDetails();

        if (alerts.isEmpty()) {

            Label empty = new Label(
                    "✓ All products have sufficient stock.");

            empty.getStyleClass().add("admin-success-text");

            empty.setWrapText(true);

            panel.getChildren().add(empty);
        }

        for (String[] item : alerts) {

            int stock = Integer.parseInt(item[1]);

            StackPane image =
                    UIComponents.productImage(
                            item[3], 45, 45);

            Label name = new Label(item[0]);

            name.getStyleClass().add("admin-product-name");

            name.setWrapText(true);

            Label minimum = UIComponents.mutedText(
                    "Min threshold: " + item[2] + " units");

            VBox details = new VBox(3, name, minimum);

            HBox.setHgrow(details, Priority.ALWAYS);

            Label badge = new Label(stock + " left");

            badge.getStyleClass().add(
                    stock == 0
                    ? "badge-error"
                    : "badge-warning");

            HBox row = new HBox(
                    10, image, details, badge);

            row.setAlignment(Pos.CENTER_LEFT);

            row.getStyleClass().add("admin-product-row");

            panel.getChildren().add(row);
        }

        return panel;
    }

    // BEST SELLING PRODUCTS

    private static VBox createBestSelling()
            throws Exception {

        VBox panel = UIComponents.card(12);

        panel.getStyleClass().add("admin-panel");

        panel.getChildren().add(
                UIComponents.cardHeading(
                        "Best-Selling Products"));

        java.util.List<String[]> products =
                DashboardService.bestSellingDetails();

        if (products.isEmpty()) {

            panel.getChildren().add(
                    UIComponents.mutedText(
                        "Best-selling products will appear "
                        + "after orders are delivered."));
        }

        for (String[] item : products) {

            StackPane image =
                    UIComponents.productImage(
                            item[2], 45, 45);

            Label name = new Label(item[0]);

            name.getStyleClass().add("admin-product-name");

            name.setWrapText(true);

            Label sold = UIComponents.mutedText(
                    item[3] + " units sold");

            VBox details = new VBox(3, name, sold);

            HBox.setHgrow(details, Priority.ALWAYS);

            Label price = new Label(
                    UIComponents.currency(
                            new BigDecimal(item[1])));

            price.getStyleClass().add("admin-product-price");

            HBox row = new HBox(
                    10, image, details, price);

            row.setAlignment(Pos.CENTER_LEFT);

            row.getStyleClass().add("admin-product-row");

            panel.getChildren().add(row);
        }

        return panel;
    }

    // USER INITIALS

    private static String initials(String name) {

        if (name == null || name.isBlank()) {
            return "AD";
        }

        String[] words = name.trim().split("\\s+");

        String result =
                words[0].substring(0, 1).toUpperCase();

        if (words.length > 1) {

            result += words[words.length - 1]
                    .substring(0, 1).toUpperCase();
        }

        return result;
    }
}
