
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class InventoryManagement {

    // ==========================================
    // INVENTORY MANAGEMENT PAGE
    // ==========================================

    public static void show(Stage stage, User user) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));

        Button history = UIComponents.secondaryButton(
                "Transaction History");

        history.setOnAction(e ->
                showHistory(stage, user, null));

        Button refresh =
                UIComponents.secondaryButton("↻ Refresh");

        refresh.setOnAction(e -> show(stage, user));

        HBox header = pageHeader(
                "Inventory Management",
                "Monitor stock levels and manage inventory adjustments.",
                history, refresh);

        content.getChildren().add(header);

        try {

            ObservableList<Product> products =
                    ProductService.getProducts();

            long totalUnits = products.stream()
                    .mapToLong(Product::getStock)
                    .sum();

            long available = products.stream()
                    .filter(p -> p.isActive()
                            && p.getStock() > p.getMinStock())
                    .count();

            long lowStock = products.stream()
                    .filter(p -> p.isActive()
                            && p.getStock() > 0
                            && p.getStock() <= p.getMinStock())
                    .count();

            long outOfStock = products.stream()
                    .filter(p -> p.isActive()
                            && p.getStock() == 0)
                    .count();

            HBox stats = new HBox(14,

                stat("Total Stock Units",
                     totalUnits,
                     "Units across all products",
                     "#0D5C4B"),

                stat("In Stock",
                     available,
                     "Products with sufficient stock",
                     "#15803D"),

                stat("Low Stock",
                     lowStock,
                     "Products requiring restocking",
                     "#B45309"),

                stat("Out of Stock",
                     outOfStock,
                     "Products currently unavailable",
                     "#BA1A1A")
            );

            content.getChildren().add(stats);

            // RESTOCK WARNING

            if (lowStock + outOfStock > 0) {

                Label warning = new Label(
                        "⚠ Restock Required");

                warning.getStyleClass().add(
                        "inv-warning-title");

                Label message = new Label(
                        lowStock + " products have low stock and "
                        + outOfStock + " products are out of stock. "
                        + "Review their inventory levels.");

                message.setWrapText(true);

                VBox banner = new VBox(6, warning, message);

                banner.getStyleClass().add(
                        "inv-warning-banner");

                content.getChildren().add(banner);
            }

            // SEARCH AND FILTERS

            TextField search = new TextField();

            search.setPromptText(
                    "Search product name or ID...");

            search.setPrefWidth(280);

            ComboBox<String> category = new ComboBox<>();

            category.getItems().add("All Categories");

            category.getItems().addAll(
                    ProductService.getCategories().keySet());

            category.setValue("All Categories");

            ComboBox<String> status = new ComboBox<>();

            status.getItems().addAll(
                    "All Stock Status",
                    "Available",
                    "Low Stock",
                    "Out of Stock",
                    "Inactive");

            status.setValue("All Stock Status");

            FlowPane filters = new FlowPane(
                    12, 10, search, category, status);

            filters.setAlignment(Pos.CENTER_LEFT);

            // INVENTORY TABLE

            TableView<Product> table =
                    createInventoryTable(stage, user);

            FilteredList<Product> filtered =
                    new FilteredList<>(products, p -> true);

            SortedList<Product> sorted =
                    new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                    table.comparatorProperty());

            table.setItems(sorted);

            Label count = UIComponents.mutedText(
                    "Showing " + products.size() + " products");

            Runnable applyFilters = () -> {

                String query = search.getText()
                        .trim().toLowerCase(Locale.ROOT);

                filtered.setPredicate(p -> {

                    boolean searchMatch =
                            p.getName()
                             .toLowerCase(Locale.ROOT)
                             .contains(query)

                            || String.valueOf(p.getId())
                                     .contains(query);

                    boolean categoryMatch =
                            "All Categories".equals(
                                    category.getValue())

                            || Objects.equals(
                                    category.getValue(),
                                    p.getCategory());

                    boolean statusMatch =
                            "All Stock Status".equals(
                                    status.getValue())

                            || Objects.equals(
                                    status.getValue(),
                                    p.getStatus());

                    return searchMatch
                            && categoryMatch
                            && statusMatch;
                });

                count.setText(
                        "Showing " + filtered.size()
                        + " of " + products.size()
                        + " products");
            };

            search.textProperty().addListener(
                    (obs, oldValue, newValue) ->
                            applyFilters.run());

            category.setOnAction(
                    e -> applyFilters.run());

            status.setOnAction(
                    e -> applyFilters.run());

            VBox tableCard = UIComponents.card(14);

            tableCard.getStyleClass().add(
                    "inv-table-panel");

            tableCard.getChildren().addAll(

                    UIComponents.cardHeading(
                            "Product Inventory"),

                    filters, table, count
            );

            content.getChildren().add(tableCard);

        } catch (Exception ex) {

            content.getChildren().add(
                    errorLabel(
                        "Unable to load inventory data."));

            ex.printStackTrace();
        }

        showPage(stage, user, "Inventory", content);
    }

    // ==========================================
    // INVENTORY TABLE
    // ==========================================

    private static TableView<Product> createInventoryTable(
            Stage stage, User user) {

        TableView<Product> table = new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(540);
        table.setFixedCellSize(72);

        table.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
                UIComponents.emptyState(
                    "No Products Found",
                    "Try changing your search or filters."));

        TableColumn<Product, Integer> id =
                column("ID", Product::getId, 65);

        // PRODUCT IMAGE AND NAME

        TableColumn<Product, Product> product =
                column("PRODUCT", p -> p, 240);

        product.setSortable(false);

        product.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Product p, boolean empty) {

                super.updateItem(p, empty);

                setText(null);

                if (empty || p == null) {

                    setGraphic(null);
                    return;
                }

                StackPane image =
                        UIComponents.productImage(
                            p.getImagePath(), 48, 48);

                Label name = new Label(p.getName());

                name.getStyleClass().add(
                        "inv-product-name");

                name.setWrapText(true);
                name.setMaxWidth(165);

                Label category =
                        UIComponents.mutedText(
                            p.getCategory() == null
                            ? "Uncategorized"
                            : p.getCategory());

                VBox details = new VBox(
                        4, name, category);

                HBox row = new HBox(
                        10, image, details);

                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
            }
        });

        TableColumn<Product, Integer> stock =
                column("CURRENT STOCK",
                        Product::getStock, 120);

        TableColumn<Product, Integer> minimum =
                column("MIN STOCK",
                        Product::getMinStock, 100);

        // STOCK LEVEL INDICATOR

        TableColumn<Product, Product> level =
                column("STOCK LEVEL", p -> p, 150);

        level.setSortable(false);

        level.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Product p, boolean empty) {

                super.updateItem(p, empty);

                setText(null);

                if (empty || p == null) {

                    setGraphic(null);
                    return;
                }

                double reference = Math.max(
                        1.0, p.getMinStock() * 2.0);

                double progress = Math.min(
                        1.0, p.getStock() / reference);

                ProgressBar bar =
                        new ProgressBar(progress);

                bar.setPrefWidth(120);

                String color;

                if (!p.isActive()) {

                    color = "#929895";

                } else if (p.getStock() == 0) {

                    color = "#BA1A1A";

                } else if (p.getStock()
                           <= p.getMinStock()) {

                    color = "#D97706";

                } else {

                    color = "#0D5C4B";
                }

                bar.setStyle("-fx-accent: " + color + ";");

                Label units = UIComponents.mutedText(
                        p.getStock() + " units");

                VBox box = new VBox(5, bar, units);

                setGraphic(box);
            }
        });

        // STATUS BADGE

        TableColumn<Product, String> status =
                column("STATUS",
                        Product::getStatus, 125);

        status.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);

                } else {

                    setGraphic(stockBadge(value));
                }
            }
        });

        // ROW ACTIONS

        TableColumn<Product, Product> actions =
                column("ACTIONS", p -> p, 220);

        actions.setSortable(false);

        actions.setCellFactory(col -> new TableCell<>() {

            private final Button adjust =
                    new Button("Adjust Stock");

            private final Button history =
                    new Button("View History");

            private final HBox buttons =
                    new HBox(7, adjust, history);

            {
                adjust.getStyleClass().add(
                        "inv-adjust-button");

                history.getStyleClass().add(
                        "inv-history-button");

                buttons.setAlignment(Pos.CENTER_LEFT);

                adjust.setOnAction(e -> {

                    Product p = getItem();

                    if (p != null) {

                        showStockForm(stage, user, p);
                    }
                });

                history.setOnAction(e -> {

                    Product p = getItem();

                    if (p != null) {

                        showHistory(stage, user, p);
                    }
                });
            }

            @Override
            protected void updateItem(
                    Product p, boolean empty) {

                super.updateItem(p, empty);

                setText(null);

                setGraphic(
                    empty || p == null
                    ? null
                    : buttons);
            }
        });

        table.getColumns().addAll(
                id, product, stock, minimum,
                level, status, actions);

        return table;
    }

    // ==========================================
    // STOCK UPDATE PAGE
    // ==========================================

    private static void showStockForm(
            Stage stage, User user, Product selected) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(18);

        content.setPadding(new Insets(24));

        Button back = UIComponents.tertiaryButton(
                "← Back to Inventory");

        back.setOnAction(e -> show(stage, user));

        content.getChildren().addAll(

                back,

                UIComponents.heading("Stock Adjustment"),

                UIComponents.mutedText(
                    "Update product stock and record "
                    + "the inventory transaction.")
        );

        // SELECTED PRODUCT SUMMARY

        StackPane image =
                UIComponents.productImage(
                    selected.getImagePath(), 120, 120);

        Label name = UIComponents.cardHeading(
                selected.getName());

        Label productId = UIComponents.mutedText(
                "Product ID: #" + selected.getId());

        Label category = UIComponents.mutedText(
                selected.getCategory() == null
                ? "Uncategorized"
                : selected.getCategory());

        VBox details = new VBox(
                8, name, productId, category,
                stockBadge(selected.getStatus()));

        HBox productInfo = new HBox(
                18, image, details);

        productInfo.setAlignment(Pos.CENTER_LEFT);

        HBox stockStats = new HBox(14,

                stat("Current Stock",
                     selected.getStock(),
                     "Available units",
                     "#0D5C4B"),

                stat("Minimum Stock",
                     selected.getMinStock(),
                     "Low-stock threshold",
                     "#B45309")
        );

        VBox productCard = UIComponents.card(16);

        productCard.getStyleClass().add("pm-form-card");

        productCard.getChildren().addAll(

                UIComponents.cardHeading(
                        "Selected Product"),

                productInfo,

                new Separator(),

                stockStats
        );

        // TRANSACTION TYPE

        ToggleGroup typeGroup = new ToggleGroup();

        ToggleButton stockIn =
                new ToggleButton("+ Stock In");

        ToggleButton stockOut =
                new ToggleButton("− Stock Out");

        stockIn.setToggleGroup(typeGroup);
        stockOut.setToggleGroup(typeGroup);

        stockIn.setUserData("IN");
        stockOut.setUserData("OUT");

        stockIn.getStyleClass().addAll(
                "inv-type-button", "inv-type-in");

        stockOut.getStyleClass().addAll(
                "inv-type-button", "inv-type-out");

        stockIn.setMaxWidth(Double.MAX_VALUE);
        stockOut.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(stockIn, Priority.ALWAYS);
        HBox.setHgrow(stockOut, Priority.ALWAYS);

        stockIn.setSelected(true);

        HBox typeOptions = new HBox(
                12, stockIn, stockOut);

        // QUANTITY AND REASON

        TextField quantity = new TextField();

        quantity.setPromptText("Enter quantity");

        ComboBox<String> reason = new ComboBox<>();

        reason.setEditable(true);
        reason.setPromptText("Select or enter reason");
        reason.setMaxWidth(Double.MAX_VALUE);

        // RESULTING STOCK PREVIEW

        Label current = new Label(
                String.valueOf(selected.getStock()));

        current.getStyleClass().add("inv-preview-value");

        Label adjustment = new Label("+ 0");

        adjustment.getStyleClass().add(
                "inv-preview-value");

        Label resulting = new Label(
                String.valueOf(selected.getStock()));

        resulting.getStyleClass().add(
                "inv-preview-value");

        Label resultStatus = new Label();

        Label previewNote = UIComponents.mutedText(
                "Preview based on the currently displayed stock.");

        VBox preview = new VBox(14,

                UIComponents.cardHeading(
                        "Resulting Stock Preview"),

                previewRow("Current Stock", current),

                previewRow("Adjustment", adjustment),

                new Separator(),

                previewRow("New Stock", resulting),

                resultStatus,

                previewNote
        );

        preview.getStyleClass().add(
                "inv-preview-card");

        Label error = errorLabel("");

        error.setVisible(false);
        error.setManaged(false);

        Button confirm =
                UIComponents.primaryButton(
                        "Confirm Adjustment");

        confirm.setDisable(true);

        // REASON OPTIONS

        Runnable updateReasons = () -> {

            String type = selectedType(typeGroup);

            reason.getItems().clear();
            reason.setValue(null);
            reason.getEditor().clear();

            if ("IN".equals(type)) {

                reason.getItems().addAll(
                        "Supplier restocking",
                        "Returned product",
                        "Stock correction",
                        "Other");

            } else {

                reason.getItems().addAll(
                        "Damaged products",
                        "Expired products",
                        "Lost products",
                        "Stock correction",
                        "Other");
            }
        };

        // LIVE STOCK CALCULATION

        Runnable updatePreview = () -> {

            String type = selectedType(typeGroup);

            String sign = "IN".equals(type)
                    ? "+ " : "− ";

            adjustment.setText(sign + "0");

            resulting.setText(
                    String.valueOf(selected.getStock()));

            resultStatus.setText("");

            confirm.setDisable(true);

            try {

                int qty = Integer.parseInt(
                        quantity.getText().trim());

                long updated = "IN".equals(type)

                        ? (long) selected.getStock() + qty

                        : (long) selected.getStock() - qty;

                if (qty <= 0) {

                    throw new IllegalArgumentException(
                            "Quantity must be greater than zero.");
                }

                adjustment.setText(sign + qty);

                adjustment.setStyle(
                        "IN".equals(type)
                        ? "-fx-text-fill: #15803D;"
                        : "-fx-text-fill: #D95D24;");

                if (updated < 0) {

                    throw new IllegalArgumentException(
                            "Insufficient stock.");
                }

                if (updated > Integer.MAX_VALUE) {

                    throw new IllegalArgumentException(
                            "Stock exceeds the allowed limit.");
                }

                resulting.setText(
                        String.valueOf(updated));

                String status;

                if (!selected.isActive()) {

                    status = "Inactive";

                } else if (updated == 0) {

                    status = "Out of Stock";

                } else if (updated
                           <= selected.getMinStock()) {

                    status = "Low Stock";

                } else {

                    status = "Available";
                }

                Label badge = stockBadge(status);

                resultStatus.setText(
                        "New Status: " + status);

                resultStatus.getStyleClass().setAll(
                        badge.getStyleClass());

                String reasonText =
                        reason.getEditor().getText().trim();

                confirm.setDisable(
                        reasonText.isEmpty()
                        || reasonText.length() > 255);

            } catch (NumberFormatException ex) {

                resulting.setText("—");

                resultStatus.setText(
                        quantity.getText().isBlank()
                        ? "Enter quantity"
                        : "Enter a valid whole number");

                resultStatus.getStyleClass().setAll(
                        "label", "text-muted");

            } catch (IllegalArgumentException ex) {

                resulting.setText("—");

                resultStatus.setText(ex.getMessage());

                resultStatus.getStyleClass().setAll(
                        "label", "badge-error");
            }
        };

        typeGroup.selectedToggleProperty().addListener(
                (obs, oldValue, newValue) -> {

                    if (newValue == null) {

                        if (oldValue != null) {

                            oldValue.setSelected(true);
                        }

                        return;
                    }

                    updateReasons.run();
                    updatePreview.run();
                });

        quantity.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        updatePreview.run());

        reason.getEditor().textProperty().addListener(
                (obs, oldValue, newValue) ->
                        updatePreview.run());

        updateReasons.run();
        updatePreview.run();

        // CONFIRM STOCK ADJUSTMENT

        confirm.setOnAction(e -> {

            error.setVisible(false);
            error.setManaged(false);

            try {

                int qty = Integer.parseInt(
                        quantity.getText().trim());

                String type = selectedType(typeGroup);

                String reasonText =
                        reason.getEditor().getText().trim();

                boolean approved = UIComponents.confirm(

                        "Confirm Stock Adjustment",

                        ("IN".equals(type)
                            ? "Add " : "Remove ")
                        + qty + " units "
                        + ("IN".equals(type)
                            ? "to " : "from ")
                        + selected.getName() + "?",

                        "Confirm Adjustment"
                );

                if (!approved) {
                    return;
                }

                InventoryService.updateStock(
                        selected.getId(),
                        type,
                        qty,
                        reasonText);

                UIComponents.success(
                        "Stock updated successfully!");

                show(stage, user);

            } catch (NumberFormatException ex) {

                showError(error,
                        "Enter a valid quantity.");

            } catch (IllegalArgumentException ex) {

                showError(error, ex.getMessage());

            } catch (Exception ex) {

                showError(error,
                        "Unable to update stock. "
                        + "Please try again.");

                ex.printStackTrace();
            }
        });

        // ADJUSTMENT FORM

        VBox formCard = UIComponents.card(18);

        formCard.getStyleClass().add("pm-form-card");

        formCard.getChildren().addAll(

                UIComponents.cardHeading(
                        "Adjustment Details"),

                field("Transaction Type", typeOptions),

                UIComponents.mutedText(
                    "Stock In increases inventory. "
                    + "Stock Out decreases inventory."),

                field("Quantity *", quantity),

                field("Reason *", reason),

                UIComponents.mutedText(
                        "Maximum 255 characters."),

                error
        );

        // TWO-COLUMN LAYOUT

        VBox left = new VBox(
                16, productCard, formCard);

        VBox right = new VBox(16, preview);

        left.setMinWidth(0);
        right.setMinWidth(0);

        GridPane layout = new GridPane();

        layout.setHgap(18);

        ColumnConstraints first =
                new ColumnConstraints();

        first.setPercentWidth(62);
        first.setHgrow(Priority.ALWAYS);

        ColumnConstraints second =
                new ColumnConstraints();

        second.setPercentWidth(38);
        second.setHgrow(Priority.ALWAYS);

        layout.getColumnConstraints().addAll(
                first, second);

        layout.add(left, 0, 0);
        layout.add(right, 1, 0);

        // FOOTER ACTIONS

        Button cancel =
                UIComponents.secondaryButton("Cancel");

        cancel.setOnAction(e -> show(stage, user));

        HBox actions = new HBox(
                12, cancel, confirm);

        actions.setAlignment(Pos.CENTER_RIGHT);

        actions.getStyleClass().add(
                "pm-form-actions");

        content.getChildren().addAll(
                layout, actions);

        showPage(stage, user,
                "Inventory  ›  Stock Adjustment",
                content);
    }

    // ==========================================
    // TRANSACTION HISTORY PAGE
    // ==========================================

    private static void showHistory(
            Stage stage, User user, Product product) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(18);

        content.setPadding(new Insets(24));

        Button back = UIComponents.tertiaryButton(
                "← Back to Inventory");

        back.setOnAction(e -> show(stage, user));

        Button refresh =
                UIComponents.secondaryButton("↻ Refresh");

        refresh.setOnAction(e ->
                showHistory(stage, user, product));

        Button all =
                UIComponents.secondaryButton(
                        "All Transactions");

        all.setOnAction(e ->
                showHistory(stage, user, null));

        HBox header = pageHeader(

                "Inventory Transaction History",

                product == null
                ? "Review all stock movements."
                : "Stock movements for " + product.getName(),

                all, refresh
        );

        content.getChildren().addAll(back, header);

        try {

            ObservableList<InventoryTransaction> transactions =

                    product == null

                    ? InventoryService.getTransactions()

                    : InventoryService.getTransactions(
                            product.getId());

            long stockIn = transactions.stream()

                    .filter(t -> "IN".equals(t.getType()))

                    .mapToLong(
                            InventoryTransaction::getQuantity)

                    .sum();

            long stockOut = transactions.stream()

                    .filter(t -> "OUT".equals(t.getType()))

                    .mapToLong(
                            InventoryTransaction::getQuantity)

                    .sum();

            HBox stats = new HBox(14,

                    stat("Total Transactions",
                         transactions.size(),
                         "Recorded stock movements",
                         "#0D5C4B"),

                    stat("Total Stock In",
                         stockIn,
                         "Units added",
                         "#15803D"),

                    stat("Total Stock Out",
                         stockOut,
                         "Units removed",
                         "#D95D24"),

                    stat("Net Stock Movement",
                         stockIn - stockOut,
                         "Stock in minus stock out",
                         "#191C1B")
            );

            content.getChildren().add(stats);

            // HISTORY FILTERS

            TextField search = new TextField();

            search.setPromptText(
                    "Search product, ID or reason...");

            search.setPrefWidth(260);

            ComboBox<String> type = new ComboBox<>();

            type.getItems().addAll(
                    "All Types",
                    "Stock In",
                    "Stock Out");

            type.setValue("All Types");

            DatePicker from = new DatePicker();

            from.setPromptText("From date");
            from.setEditable(false);

            DatePicker to = new DatePicker();

            to.setPromptText("To date");
            to.setEditable(false);

            Button clear =
                    UIComponents.secondaryButton(
                            "Clear Filters");

            FlowPane filters = new FlowPane(
                    10, 10,
                    search, type, from, to, clear);

            filters.setAlignment(Pos.CENTER_LEFT);

            // TRANSACTION TABLE

            TableView<InventoryTransaction> table =
                    createHistoryTable();

            FilteredList<InventoryTransaction> filtered =
                    new FilteredList<>(
                            transactions, t -> true);

            SortedList<InventoryTransaction> sorted =
                    new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                    table.comparatorProperty());

            table.setItems(sorted);

            Label count = UIComponents.mutedText(
                    "Showing " + transactions.size()
                    + " transactions");

            Label dateError = errorLabel("");

            dateError.setVisible(false);
            dateError.setManaged(false);

            Runnable applyFilters = () -> {

                LocalDate start = from.getValue();

                LocalDate end = to.getValue();

                if (start != null && end != null
                        && start.isAfter(end)) {

                    showError(dateError,
                            "From date cannot be after To date.");

                    return;
                }

                dateError.setVisible(false);
                dateError.setManaged(false);

                String query = search.getText()
                        .trim().toLowerCase(Locale.ROOT);

                filtered.setPredicate(t -> {

                    boolean searchMatch =

                            t.getProductName()
                             .toLowerCase(Locale.ROOT)
                             .contains(query)

                            || String.valueOf(t.getId())
                                     .contains(query)

                            || (t.getReason() != null
                                && t.getReason()
                                    .toLowerCase(Locale.ROOT)
                                    .contains(query));

                    boolean typeMatch =

                            "All Types".equals(
                                    type.getValue())

                            || ("Stock In".equals(
                                    type.getValue())
                                && "IN".equals(t.getType()))

                            || ("Stock Out".equals(
                                    type.getValue())
                                && "OUT".equals(t.getType()));

                    LocalDate date =
                            transactionDate(t.getDate());

                    boolean dateMatch =

                            (start == null && end == null)

                            || (date != null

                                && (start == null
                                    || !date.isBefore(start))

                                && (end == null
                                    || !date.isAfter(end)));

                    return searchMatch
                            && typeMatch
                            && dateMatch;
                });

                count.setText(
                        "Showing " + filtered.size()
                        + " of " + transactions.size()
                        + " transactions");
            };

            search.textProperty().addListener(
                    (obs, oldValue, newValue) ->
                            applyFilters.run());

            type.setOnAction(
                    e -> applyFilters.run());

            from.setOnAction(
                    e -> applyFilters.run());

            to.setOnAction(
                    e -> applyFilters.run());

            clear.setOnAction(e -> {

                search.clear();

                type.setValue("All Types");

                from.setValue(null);
                to.setValue(null);

                applyFilters.run();
            });

            VBox tableCard = UIComponents.card(14);

            tableCard.getStyleClass().add(
                    "inv-table-panel");

            tableCard.getChildren().addAll(

                    UIComponents.cardHeading(
                            "Transaction Records"),

                    filters, dateError, table, count
            );

            content.getChildren().add(tableCard);

        } catch (Exception ex) {

            content.getChildren().add(
                    errorLabel(
                        "Unable to load transaction history."));

            ex.printStackTrace();
        }

        showPage(stage, user,
                "Inventory  ›  Transaction History",
                content);
    }

    // ==========================================
    // TRANSACTION HISTORY TABLE
    // ==========================================

    private static TableView<InventoryTransaction>
            createHistoryTable() {

        TableView<InventoryTransaction> table =
                new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(540);
        table.setFixedCellSize(60);

        table.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(
                UIComponents.emptyState(
                    "No Transactions Found",
                    "No inventory transactions match your filters."));

        TableColumn<InventoryTransaction, Integer> id =
                column("TRANSACTION ID",
                        InventoryTransaction::getId, 125);

        TableColumn<InventoryTransaction, String> name =
                column("PRODUCT",
                        InventoryTransaction::getProductName,
                        210);

        TableColumn<InventoryTransaction, String> type =
                column("TYPE",
                        InventoryTransaction::getType, 115);

        type.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);
                    return;
                }

                Label badge = new Label(
                        "IN".equals(value)
                        ? "↑ Stock In"
                        : "↓ Stock Out");

                badge.getStyleClass().add(
                        "IN".equals(value)
                        ? "inv-badge-in"
                        : "inv-badge-out");

                setGraphic(badge);
            }
        });

        TableColumn<InventoryTransaction, Integer> quantity =
                column("QUANTITY",
                        InventoryTransaction::getQuantity,
                        100);

        TableColumn<InventoryTransaction, String> reason =
                column("REASON",
                        InventoryTransaction::getReason,
                        260);

        reason.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);
                    setTooltip(null);

                } else {

                    Label text = new Label(value);

                    text.setWrapText(true);

                    text.maxWidthProperty().bind(
                            widthProperty().subtract(16));

                    setGraphic(text);

                    setTooltip(new Tooltip(value));
                }
            }
        });

        TableColumn<InventoryTransaction, String> date =
                column("DATE & TIME",
                        InventoryTransaction::getDate,
                        190);

        table.getColumns().addAll(
                id, name, type, quantity, reason, date);

        return table;
    }

    // ==========================================
    // COMMON ADMIN PAGE
    // ==========================================

    private static void showPage(
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
                        .endsWith("Inventory")) {

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
    // COMMON PAGE HEADER
    // ==========================================

    private static HBox pageHeader(
            String title,
            String description,
            Button... buttons) {

        VBox heading = new VBox(

                5,

                UIComponents.heading(title),

                UIComponents.mutedText(description)
        );

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                12, heading, spacer);

        header.getChildren().addAll(buttons);

        header.setAlignment(Pos.CENTER_LEFT);

        return header;
    }

    // ==========================================
    // GENERIC TABLE COLUMN
    // ==========================================

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

    // ==========================================
    // STATISTICS CARD
    // ==========================================

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

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // ==========================================
    // STOCK STATUS BADGE
    // ==========================================

    private static Label stockBadge(String status) {

        Label badge = new Label(status);

        switch (status) {

            case "Available":

                badge.getStyleClass().add(
                        "badge-success");
                break;

            case "Low Stock":

                badge.getStyleClass().add(
                        "badge-warning");
                break;

            case "Out of Stock":

                badge.getStyleClass().add(
                        "badge-error");
                break;

            default:

                badge.getStyleClass().add(
                        "badge-neutral");
        }

        return badge;
    }

    // ==========================================
    // FORM FIELD
    // ==========================================

    private static VBox field(
            String title, Node input) {

        Label label = new Label(title);

        label.getStyleClass().add("pm-field-label");

        if (input instanceof Region) {

            ((Region) input).setMaxWidth(
                    Double.MAX_VALUE);
        }

        return new VBox(8, label, input);
    }

    private static HBox previewRow(
            String title, Label value) {

        Label label = UIComponents.mutedText(title);

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(
                10, label, spacer, value);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    private static String selectedType(
            ToggleGroup group) {

        Toggle selected = group.getSelectedToggle();

        return selected == null
                ? "IN"
                : selected.getUserData().toString();
    }

    private static LocalDate transactionDate(
            String value) {

        try {

            return LocalDate.parse(
                    value.substring(0, 10));

        } catch (Exception ex) {

            return null;
        }
    }

    private static Label errorLabel(String message) {

        Label label = new Label(message);

        label.getStyleClass().add("pm-error");

        label.setWrapText(true);

        label.setMaxWidth(Double.MAX_VALUE);

        return label;
    }

    private static void showError(
            Label label, String message) {

        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private static boolean isAdmin(
            Stage stage, User user) {

        if (user == null
                || !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);
            return false;
        }

        return true;
    }
}
