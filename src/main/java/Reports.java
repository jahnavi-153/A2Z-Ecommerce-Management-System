
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Locale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reports {

    private static final String[] STATUSES = {

        "PLACED",
        "CONFIRMED",
        "SHIPPED",
        "DELIVERED",
        "CANCELLED"
    };

    private static final String[] STATUS_COLORS = {

        "#D95D24",
        "#2563EB",
        "#7C3AED",
        "#0D5C4B",
        "#BA1A1A"
    };

    // ==========================================
    // MAIN REPORTS PAGE
    // ==========================================

    public static void show(Stage stage, User user) {

        if (user == null ||
                !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);
            return;
        }

        VBox content = new VBox(22);

        content.setPadding(new Insets(24));

        // PAGE HEADER

        VBox heading = new VBox(6,

            UIComponents.heading(
                "Sales Analytics & Reports"),

            UIComponents.mutedText(
                "Monitor sales performance, order trends "
                + "and inventory health.")
        );

        Button refresh =
            UIComponents.primaryButton("↻ Refresh Reports");

        refresh.setOnAction(e ->
            show(stage, user));

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
            15, heading, spacer, refresh);

        header.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(header);

        try {

            // LOAD ACTUAL DATABASE VALUES

            BigDecimal revenue =
                ReportService.getTotalRevenue();

            int totalOrders =
                ReportService.getTotalOrders();

            int totalCustomers =
                ReportService.getTotalCustomers();

            int productsSold =
                ReportService.getProductsSold();

            Map<String, BigDecimal> dailySales =
                ReportService.getDailySales();

            Map<String, Integer> orderStatuses =
                ReportService.getOrderStatus();

            Map<String, Integer> bestProducts =
                ReportService.getBestSellingProducts();

            // LAST REFRESH INFORMATION

            Label updated = UIComponents.mutedText(

                "All-time report  •  Last updated: "

                + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                        "dd MMM yyyy, hh:mm a"))
            );

            // SUMMARY STATISTICS

            HBox statistics = createStatistics(

                revenue,

                totalOrders,

                totalCustomers,

                productsSold
            );

            // SALES AND ORDER CHARTS

            VBox salesPanel =
                createSalesPanel(dailySales);

            VBox orderPanel =
                createOrderPanel(orderStatuses);

            GridPane firstRow = createGrid(62, 38);

            firstRow.add(salesPanel, 0, 0);

            firstRow.add(orderPanel, 1, 0);

            // BEST-SELLING PRODUCTS

            VBox productPanel =
                createBestSellingPanel(bestProducts);

            VBox rankingPanel =
                createRankingPanel(bestProducts);

            GridPane secondRow = createGrid(62, 38);

            secondRow.add(productPanel, 0, 0);

            secondRow.add(rankingPanel, 1, 0);

            // INVENTORY ALERTS

            VBox inventoryPanel =
                createInventoryPanel(stage, user);

            content.getChildren().addAll(

                updated,

                statistics,

                firstRow,

                secondRow,

                inventoryPanel
            );

        } catch (Exception ex) {

            VBox error = UIComponents.card(12);

            Label message = new Label(
                "Unable to load analytics.");

            message.getStyleClass().add(
                "rp-error-title");

            Button retry =
                UIComponents.secondaryButton("Try Again");

            retry.setOnAction(e ->
                show(stage, user));

            error.getChildren().addAll(

                message,

                UIComponents.mutedText(
                    "Check your database connection "
                    + "and refresh the reports."),

                retry
            );

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        showPage(stage, user, content);
    }

    // ==========================================
    // KPI SUMMARY CARDS
    // ==========================================

    private static HBox createStatistics(

            BigDecimal revenue,

            int orders,

            int customers,

            int productsSold) {

        VBox revenueCard = UIComponents.statCard(

            "Total Revenue",

            UIComponents.currency(revenue),

            "Value of delivered orders",

            "#0D5C4B"
        );

        VBox ordersCard = UIComponents.statCard(

            "Total Orders",

            String.valueOf(orders),

            "Orders across all statuses",

            "#2563EB"
        );

        VBox customersCard = UIComponents.statCard(

            "Total Customers",

            String.valueOf(customers),

            "Registered customer accounts",

            "#7C3AED"
        );

        VBox soldCard = UIComponents.statCard(

            "Products Sold",

            String.valueOf(productsSold),

            "Total delivered product units",

            "#D95D24"
        );

        for (VBox card : new VBox[]{

                revenueCard,

                ordersCard,

                customersCard,

                soldCard}) {

            card.setMinWidth(0);

            card.setMaxWidth(Double.MAX_VALUE);

            card.getStyleClass().add("rp-kpi-card");

            HBox.setHgrow(card, Priority.ALWAYS);
        }

        return new HBox(

            16,

            revenueCard,

            ordersCard,

            customersCard,

            soldCard
        );
    }

    // ==========================================
    // DAILY SALES LINE CHART
    // ==========================================

    private static VBox createSalesPanel(
            Map<String, BigDecimal> sales) {

        VBox panel = createPanel(

            "Daily Sales Revenue",

            "Delivered order value grouped by order date."
        );

        if (sales.isEmpty()) {

            panel.getChildren().add(

                createEmptyState(

                    "No Sales Data Yet",

                    "Sales analytics will appear after "
                    + "orders are delivered."
                )
            );

            return panel;
        }

        CategoryAxis xAxis = new CategoryAxis();

        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Order Date");

        xAxis.setTickLabelRotation(-30);

        yAxis.setLabel("Revenue (₹)");

        yAxis.setForceZeroInRange(true);

        LineChart<String, Number> chart =
            new LineChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);

        chart.setAnimated(false);

        chart.setCreateSymbols(true);

        chart.setPrefHeight(320);

        chart.setMinWidth(0);

        chart.setMaxWidth(Double.MAX_VALUE);

        chart.getStyleClass().add("rp-sales-chart");

        XYChart.Series<String, Number> series =
            new XYChart.Series<>();

        series.setName("Sales Revenue");

        for (Map.Entry<String, BigDecimal> entry
                : sales.entrySet()) {

            XYChart.Data<String, Number> point =
                new XYChart.Data<>(

                    entry.getKey(),

                    entry.getValue()
                );

            String tooltipText =

                entry.getKey()

                + "\nRevenue: "

                + UIComponents.currency(entry.getValue());

            point.nodeProperty().addListener(

                (obs, oldNode, newNode) -> {

                    if (newNode != null) {

                        Tooltip.install(

                            newNode,

                            new Tooltip(tooltipText)
                        );
                    }
                }
            );

            series.getData().add(point);
        }

        chart.getData().add(series);

        panel.getChildren().add(chart);

        return panel;
    }

    // ==========================================
    // ORDER STATUS DONUT CHART
    // ==========================================

    private static VBox createOrderPanel(
            Map<String, Integer> statuses) {

        VBox panel = createPanel(

            "Order Status Distribution",

            "Overview of the complete order lifecycle."
        );

        int total = statuses.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum();

        if (total == 0) {

            panel.getChildren().add(

                createEmptyState(

                    "No Orders Yet",

                    "Order distribution will appear "
                    + "when customers place orders."
                )
            );

            return panel;
        }

        PieChart chart = new PieChart();

        chart.setAnimated(false);

        chart.setLegendVisible(false);

        chart.setLabelsVisible(false);

        chart.setStartAngle(90);

        chart.setClockwise(true);

        chart.setPrefSize(280, 250);

        chart.setMaxSize(280, 250);

        chart.getStyleClass().add("rp-donut-chart");

        // ADD ORDER STATUS DATA

        for (int i = 0; i < STATUSES.length; i++) {

            String status = STATUSES[i];

            int count = statuses.getOrDefault(status, 0);

            if (count == 0) {
                continue;
            }

            addPieSlice(

                chart,

                status,

                count,

                STATUS_COLORS[i],

                total
            );
        }

        // HANDLE ANY ADDITIONAL DATABASE STATUS

        for (Map.Entry<String, Integer> entry
                : statuses.entrySet()) {

            boolean known = false;

            for (String status : STATUSES) {

                if (status.equals(entry.getKey())) {

                    known = true;
                    break;
                }
            }

            if (!known && entry.getValue() > 0) {

                addPieSlice(

                    chart,

                    entry.getKey(),

                    entry.getValue(),

                    "#929895",

                    total
                );
            }
        }

        // DONUT CENTER

        Circle hole = new Circle(57);

        hole.setFill(Color.WHITE);

        hole.setMouseTransparent(true);

        Label number = new Label(
            String.valueOf(total));

        number.getStyleClass().add(
            "rp-donut-total");

        Label caption =
            UIComponents.mutedText("Total Orders");

        VBox center = new VBox(
            2, number, caption);

        center.setAlignment(Pos.CENTER);

        center.setMouseTransparent(true);

        StackPane donut = new StackPane(

            chart,

            hole,

            center
        );

        donut.setAlignment(Pos.CENTER);

        // CUSTOM STATUS LEGEND

        VBox legend = new VBox(10);

        for (int i = 0; i < STATUSES.length; i++) {

            int count =
                statuses.getOrDefault(STATUSES[i], 0);

            legend.getChildren().add(

                createLegendRow(

                    STATUSES[i],

                    count,

                    total,

                    STATUS_COLORS[i]
                )
            );
        }

        for (Map.Entry<String, Integer> entry
                : statuses.entrySet()) {

            boolean known = false;

            for (String status : STATUSES) {

                if (status.equals(entry.getKey())) {

                    known = true;
                    break;
                }
            }

            if (!known) {

                legend.getChildren().add(

                    createLegendRow(

                        entry.getKey(),

                        entry.getValue(),

                        total,

                        "#929895"
                    )
                );
            }
        }

        panel.getChildren().addAll(donut, legend);

        return panel;
    }

    private static void addPieSlice(

            PieChart chart,

            String status,

            int count,

            String color,

            int total) {

        PieChart.Data slice =
            new PieChart.Data(status, count);

        String description =

            status + ": " + count

            + " orders ("

            + String.format(

                Locale.ROOT,

                "%.1f%%",

                count * 100.0 / total

            ) + ")";

        slice.nodeProperty().addListener(

            (obs, oldNode, newNode) -> {

                if (newNode != null) {

                    newNode.setStyle(

                        "-fx-pie-color: " + color + ";"
                    );

                    Tooltip.install(

                        newNode,

                        new Tooltip(description)
                    );
                }
            }
        );

        chart.getData().add(slice);

        if (slice.getNode() != null) {

            slice.getNode().setStyle(

                "-fx-pie-color: " + color + ";"
            );

            Tooltip.install(

                slice.getNode(),

                new Tooltip(description)
            );
        }
    }

    private static HBox createLegendRow(

            String status,

            int count,

            int total,

            String color) {

        Circle dot = new Circle(5);

        dot.setFill(Color.web(color));

        Label name = new Label(status);

        name.getStyleClass().add(
            "rp-legend-label");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(

            count + "  •  "

            + String.format(

                Locale.ROOT,

                "%.1f%%",

                total == 0
                    ? 0.0
                    : count * 100.0 / total
            )
        );

        value.getStyleClass().add(
            "rp-legend-value");

        HBox row = new HBox(

            9, dot, name, spacer, value);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // ==========================================
    // BEST-SELLING PRODUCTS BAR CHART
    // ==========================================

    private static VBox createBestSellingPanel(
            Map<String, Integer> products) {

        VBox panel = createPanel(

            "Top 5 Best-Selling Products",

            "Ranked by quantity sold in delivered orders."
        );

        if (products.isEmpty()) {

            panel.getChildren().add(

                createEmptyState(

                    "No Product Sales Yet",

                    "Best-selling products will appear "
                    + "after orders are delivered."
                )
            );

            return panel;
        }

        NumberAxis xAxis = new NumberAxis();

        CategoryAxis yAxis = new CategoryAxis();

        xAxis.setLabel("Units Sold");

        xAxis.setForceZeroInRange(true);

        xAxis.setMinorTickVisible(false);

        BarChart<Number, String> chart =
            new BarChart<>(xAxis, yAxis);

        chart.setAnimated(false);

        chart.setLegendVisible(false);

        chart.setCategoryGap(18);

        chart.setBarGap(4);

        chart.setPrefHeight(330);

        chart.setMinWidth(0);

        chart.setMaxWidth(Double.MAX_VALUE);

        chart.getStyleClass().add(
            "rp-product-chart");

        XYChart.Series<Number, String> series =
            new XYChart.Series<>();

        for (Map.Entry<String, Integer> entry
                : products.entrySet()) {

            XYChart.Data<Number, String> item =
                new XYChart.Data<>(

                    entry.getValue(),

                    entry.getKey()
                );

            String description =

                entry.getKey()

                + "\nUnits Sold: "

                + entry.getValue();

            item.nodeProperty().addListener(

                (obs, oldNode, newNode) -> {

                    if (newNode != null) {

                        Tooltip.install(

                            newNode,

                            new Tooltip(description)
                        );
                    }
                }
            );

            series.getData().add(item);
        }

        chart.getData().add(series);

        panel.getChildren().add(chart);

        return panel;
    }

    // ==========================================
    // TOP PRODUCT RANKING
    // ==========================================

    private static VBox createRankingPanel(
            Map<String, Integer> products) {

        VBox panel = createPanel(

            "Product Performance",

            "Your highest-selling products."
        );

        if (products.isEmpty()) {

            panel.getChildren().add(

                createEmptyState(

                    "No Rankings Available",

                    "Complete customer orders "
                    + "to generate rankings."
                )
            );

            return panel;
        }

        int maxSold = products.values()

            .stream()

            .mapToInt(Integer::intValue)

            .max()

            .orElse(1);

        int rank = 1;

        for (Map.Entry<String, Integer> entry
                : products.entrySet()) {

            Label position = new Label(
                String.valueOf(rank));

            position.getStyleClass().add(
                "rp-rank-number");

            Label name = new Label(entry.getKey());

            name.setWrapText(true);

            name.getStyleClass().add(
                "rp-product-name");

            Label quantity =
                UIComponents.mutedText(

                    entry.getValue() + " units sold"
                );

            ProgressBar progress = new ProgressBar(

                maxSold == 0

                    ? 0

                    : entry.getValue() * 1.0 / maxSold
            );

            progress.setMaxWidth(Double.MAX_VALUE);

            progress.getStyleClass().add(
                "rp-ranking-progress");

            VBox information = new VBox(

                6,

                name,

                quantity,

                progress
            );

            information.setMinWidth(0);

            HBox.setHgrow(

                information,

                Priority.ALWAYS
            );

            HBox row = new HBox(

                12,

                position,

                information
            );

            row.setAlignment(Pos.CENTER_LEFT);

            row.getStyleClass().add(
                "rp-ranking-row");

            panel.getChildren().add(row);

            rank++;
        }

        return panel;
    }

    // ==========================================
    // INVENTORY ALERTS
    // ==========================================

    private static VBox createInventoryPanel(

            Stage stage,

            User user) throws Exception {

        VBox panel = UIComponents.card(16);

        panel.getStyleClass().add("rp-panel");

        VBox heading = new VBox(5,

            UIComponents.cardHeading(
                "Inventory Alerts"),

            UIComponents.mutedText(
                "Products requiring stock replenishment.")
        );

        Button manage =
            UIComponents.secondaryButton(
                "Manage Inventory →");

        manage.setOnAction(e ->

            InventoryManagement.show(stage, user));

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(

            12, heading, spacer, manage);

        header.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().add(header);

        // LOAD PRODUCT DETAILS

        java.util.List<Product> alerts =
            new java.util.ArrayList<>();

        for (Product product :
                ProductService.getProducts()) {

            if (product.isActive()

                    && product.getStock()
                        <= product.getMinStock()) {

                alerts.add(product);
            }
        }

        alerts.sort(

            java.util.Comparator.comparingInt(
                Product::getStock)
        );

        long outOfStock = alerts.stream()

            .filter(p -> p.getStock() == 0)

            .count();

        long lowStock = alerts.stream()

            .filter(p -> p.getStock() > 0)

            .count();

        VBox lowCard = UIComponents.statCard(

            "Low Stock",

            String.valueOf(lowStock),

            "Products at or below minimum stock",

            "#B45309"
        );

        VBox outCard = UIComponents.statCard(

            "Out of Stock",

            String.valueOf(outOfStock),

            "Products with zero available stock",

            "#BA1A1A"
        );

        lowCard.setMaxWidth(Double.MAX_VALUE);

        outCard.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(lowCard, Priority.ALWAYS);

        HBox.setHgrow(outCard, Priority.ALWAYS);

        HBox stockCards = new HBox(

            14, lowCard, outCard);

        panel.getChildren().add(stockCards);

        // EMPTY STATE

        if (alerts.isEmpty()) {

            Label safe = new Label(

                "All active products have sufficient stock.");

            safe.getStyleClass().add(
                "rp-stock-safe");

            safe.setMaxWidth(Double.MAX_VALUE);

            panel.getChildren().add(safe);

            return panel;
        }

        // STOCK TABLE

        TableView<Product> table =
            new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setFixedCellSize(65);

        table.setPrefHeight(

            Math.min(400, 45 + alerts.size() * 65)
        );

        table.setColumnResizePolicy(

            TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Product, Product> name =
            new TableColumn<>("PRODUCT");

        name.setCellValueFactory(data ->

            new javafx.beans.property
                .SimpleObjectProperty<>(
                    data.getValue())
        );

        name.setSortable(false);

        name.setPrefWidth(280);

        name.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(

                    Product product,

                    boolean empty) {

                super.updateItem(product, empty);

                setText(null);

                if (empty || product == null) {

                    setGraphic(null);

                    return;
                }

                StackPane image =
                    UIComponents.productImage(

                        product.getImagePath(),

                        42,

                        42
                    );

                Label title = new Label(
                    product.getName());

                title.getStyleClass().add(
                    "rp-product-name");

                VBox details = new VBox(

                    4,

                    title,

                    UIComponents.mutedText(
                        product.getCategory())
                );

                HBox row = new HBox(

                    10,

                    image,

                    details
                );

                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
            }
        });

        TableColumn<Product, Integer> stock =
            new TableColumn<>("CURRENT STOCK");

        stock.setCellValueFactory(data ->

            new javafx.beans.property
                .SimpleObjectProperty<>(
                    data.getValue().getStock())
        );

        stock.setPrefWidth(130);

        TableColumn<Product, Integer> minimum =
            new TableColumn<>("MINIMUM STOCK");

        minimum.setCellValueFactory(data ->

            new javafx.beans.property
                .SimpleObjectProperty<>(
                    data.getValue().getMinStock())
        );

        minimum.setPrefWidth(130);

        TableColumn<Product, Product> status =
            new TableColumn<>("STATUS");

        status.setCellValueFactory(data ->

            new javafx.beans.property
                .SimpleObjectProperty<>(
                    data.getValue())
        );

        status.setSortable(false);

        status.setPrefWidth(150);

        status.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(

                    Product product,

                    boolean empty) {

                super.updateItem(product, empty);

                setText(null);

                if (empty || product == null) {

                    setGraphic(null);

                    return;
                }

                setGraphic(

                    UIComponents.stockBadge(

                        product.getStock(),

                        product.getMinStock(),

                        product.isActive()
                    )
                );
            }
        });

        table.getColumns().addAll(

            name,

            stock,

            minimum,

            status
        );

        table.getItems().addAll(alerts);

        panel.getChildren().add(table);

        return panel;
    }

    // ==========================================
    // COMMON PANEL HELPERS
    // ==========================================

    private static VBox createPanel(

            String title,

            String description) {

        VBox panel = UIComponents.card(14);

        panel.getStyleClass().add("rp-panel");

        panel.setMinWidth(0);

        panel.setMaxWidth(Double.MAX_VALUE);

        panel.getChildren().addAll(

            UIComponents.cardHeading(title),

            UIComponents.mutedText(description)
        );

        return panel;
    }

    private static VBox createEmptyState(

            String title,

            String description) {

        VBox wrapper = new VBox(

            UIComponents.emptyState(

                title,

                description
            )
        );

        wrapper.setAlignment(Pos.CENTER);

        wrapper.setMinHeight(250);

        return wrapper;
    }

    private static GridPane createGrid(

            double leftWidth,

            double rightWidth) {

        GridPane grid = new GridPane();

        grid.setHgap(18);

        grid.setVgap(18);

        ColumnConstraints left =
            new ColumnConstraints();

        left.setPercentWidth(leftWidth);

        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right =
            new ColumnConstraints();

        right.setPercentWidth(rightWidth);

        right.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(

            left,

            right
        );

        return grid;
    }

    // ==========================================
    // SHARED ADMIN LAYOUT
    // ==========================================

    private static void showPage(

            Stage stage,

            User user,

            VBox content) {

        BorderPane root = new BorderPane();

        root.getStyleClass().add(
            "admin-dashboard-root");

        // EXISTING ADMIN SIDEBAR

        VBox sidebar =
            AdminDashboard.createSidebar(stage, user);

        for (Node node : sidebar.getChildren()) {

            if (node instanceof Button) {

                Button button = (Button) node;

                button.getStyleClass().remove("active");

                if (button.getText()

                          .trim()

                          .endsWith("Reports")) {

                    button.getStyleClass().add("active");
                }
            }
        }

        root.setLeft(sidebar);

        // TOP NAVIGATION

        Label breadcrumb = UIComponents.mutedText(

            "Admin Workspace  ›  Reports"
        );

        Label admin = new Label(user.getName());

        admin.getStyleClass().add(
            "admin-top-name");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(

            12,

            breadcrumb,

            spacer,

            admin
        );

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add(
            "admin-topbar");

        // SCROLLABLE REPORT CONTENT

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(

            ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox workspace = new VBox(

            topBar,

            scroll
        );

        root.setCenter(workspace);

        Scene scene = new Scene(root, 1440, 900);

        UIComponents.applyStyles(scene);

        stage.setTitle(

            "A2Z Commerce - Sales Analytics & Reports"
        );

        stage.setMinWidth(1100);

        stage.setMinHeight(700);

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.show();
    }
}
