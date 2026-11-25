
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
import javafx.beans.property.SimpleStringProperty;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class ProductManagement {

    // ==========================================
    // PRODUCT MANAGEMENT PAGE
    // ==========================================

    public static void show(Stage stage, User user) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(18);

        content.setPadding(new Insets(24));

        Label heading =
                UIComponents.heading("Product Management");

        Label subtitle = UIComponents.mutedText(
                "Manage your product catalog, pricing, "
                + "suppliers and availability.");

        VBox headingBox = new VBox(5, heading, subtitle);

        Button add = UIComponents.primaryButton(
                "+ Add Product");

        add.setOnAction(e ->
                showAddProduct(stage, user));

        Region headingSpace = new Region();

        HBox.setHgrow(headingSpace, Priority.ALWAYS);

        HBox pageHeader = new HBox(
                12, headingBox, headingSpace, add);

        pageHeader.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(pageHeader);

        try {

            ObservableList<Product> products =
                    ProductService.getProducts();

            Map<String, Integer> categories =
                    ProductService.getCategories();

            Map<Integer, String> suppliers =
                    ProductService.getProductSupplierNames();

            long activeCount = products.stream()
                    .filter(Product::isActive)
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

            HBox stats = new HBox(
                    14,

                    statCard(
                        "Total Products",
                        products.size(),
                        "▣", "#0D5C4B", "#E9F3EE"),

                    statCard(
                        "Active Products",
                        activeCount,
                        "✓", "#15803D", "#DCFCE7"),

                    statCard(
                        "Low Stock",
                        lowStock,
                        "!", "#B45309", "#FEF3C7"),

                    statCard(
                        "Out of Stock",
                        outOfStock,
                        "×", "#BA1A1A", "#FEE2E2")
            );

            content.getChildren().add(stats);

            // SEARCH AND FILTERS

            TextField search = new TextField();

            search.setPromptText(
                    "Search by product name or ID...");

            search.setPrefWidth(260);

            ComboBox<String> categoryFilter =
                    new ComboBox<>();

            categoryFilter.getItems().add(
                    "All Categories");

            categoryFilter.getItems().addAll(
                    categories.keySet());

            categoryFilter.setValue("All Categories");

            ComboBox<String> stockFilter =
                    new ComboBox<>();

            stockFilter.getItems().addAll(
                    "All Stock Status",
                    "Available",
                    "Low Stock",
                    "Out of Stock"
            );

            stockFilter.setValue("All Stock Status");

            ComboBox<String> activeFilter =
                    new ComboBox<>();

            activeFilter.getItems().addAll(
                    "All Products",
                    "Active",
                    "Inactive"
            );

            activeFilter.setValue("All Products");

            Button refresh =
                    UIComponents.secondaryButton("↻ Refresh");

            refresh.setOnAction(e -> show(stage, user));

            FlowPane filters = new FlowPane(10, 10);

            filters.setAlignment(Pos.CENTER_LEFT);

            filters.getChildren().addAll(
                    search,
                    categoryFilter,
                    stockFilter,
                    activeFilter,
                    refresh
            );

            // PRODUCT TABLE

            TableView<Product> table =
                    createTable(stage, user, suppliers);

            FilteredList<Product> filtered =
                    new FilteredList<>(products, p -> true);

            SortedList<Product> sorted =
                    new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                    table.comparatorProperty());

            table.setItems(sorted);

            Label resultCount = UIComponents.mutedText(
                    "Showing " + products.size()
                    + " products");

            Runnable applyFilter = () -> {

                String query = search.getText()
                        .trim().toLowerCase(Locale.ROOT);

                String category =
                        categoryFilter.getValue();

                String stockStatus =
                        stockFilter.getValue();

                String activeStatus =
                        activeFilter.getValue();

                filtered.setPredicate(p -> {

                    boolean searchMatch =
                        p.getName().toLowerCase(Locale.ROOT)
                            .contains(query)
                        || String.valueOf(p.getId())
                            .contains(query);

                    boolean categoryMatch =
                        "All Categories".equals(category)
                        || Objects.equals(
                                category, p.getCategory());

                    boolean stockMatch =
                        "All Stock Status".equals(stockStatus)
                        || stockStatus.equals(stockStatus(p));

                    boolean activeMatch =
                        "All Products".equals(activeStatus)
                        || ("Active".equals(activeStatus)
                                && p.isActive())
                        || ("Inactive".equals(activeStatus)
                                && !p.isActive());

                    return searchMatch
                            && categoryMatch
                            && stockMatch
                            && activeMatch;
                });

                resultCount.setText(
                        "Showing " + filtered.size()
                        + " of " + products.size()
                        + " products");
            };

            search.textProperty().addListener(
                    (obs, oldValue, newValue) ->
                            applyFilter.run());

            categoryFilter.setOnAction(
                    e -> applyFilter.run());

            stockFilter.setOnAction(
                    e -> applyFilter.run());

            activeFilter.setOnAction(
                    e -> applyFilter.run());

            VBox tablePanel = UIComponents.card(14);

            tablePanel.getStyleClass().add(
                    "pm-table-panel");

            tablePanel.getChildren().addAll(
                    filters, table, resultCount);

            content.getChildren().add(tablePanel);

        } catch (Exception ex) {

            Label error = new Label(
                    "Unable to load products. "
                    + "Please check your database connection.");

            error.getStyleClass().add("pm-error");
            error.setWrapText(true);

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        showPage(stage, user,
                "Products", content);
    }

    // ==========================================
    // PRODUCT TABLE
    // ==========================================

    private static TableView<Product> createTable(
            Stage stage,
            User user,
            Map<Integer, String> suppliers) {

        TableView<Product> table = new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setColumnResizePolicy(
                TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPrefHeight(520);
        table.setFixedCellSize(68);

        table.setPlaceholder(
                UIComponents.emptyState(
                    "No Products Found",
                    "Try changing your search or filters."));

        TableColumn<Product, String> id =
                textColumn("ID",
                    p -> "#" + p.getId(), 60);

        TableColumn<Product, String> image =
                new TableColumn<>("IMAGE");

        image.setPrefWidth(80);
        image.setSortable(false);

        image.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getImagePath()));

        image.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String path, boolean empty) {

                super.updateItem(path, empty);

                setText(null);

                if (empty) {

                    setGraphic(null);

                } else {

                    setGraphic(
                        UIComponents.productImage(
                                path, 48, 48));
                }
            }
        });

        TableColumn<Product, String> name =
                textColumn("PRODUCT NAME",
                    Product::getName, 175);

        TableColumn<Product, String> category =
                textColumn("CATEGORY",
                    p -> p.getCategory() == null
                        ? "Uncategorized"
                        : p.getCategory(), 125);

        TableColumn<Product, String> supplier =
                textColumn("SUPPLIER",
                    p -> suppliers.getOrDefault(
                        p.getId(), "Not assigned"), 140);

        TableColumn<Product, String> price =
                textColumn("PRICE",
                    p -> UIComponents.currency(
                        p.getPrice()), 105);

        TableColumn<Product, String> stock =
                textColumn("STOCK",
                    p -> String.valueOf(
                        p.getStock()), 65);

        TableColumn<Product, String> minimum =
                textColumn("MIN STOCK",
                    p -> String.valueOf(
                        p.getMinStock()), 85);

        TableColumn<Product, String> status =
                new TableColumn<>("STATUS");

        status.setPrefWidth(115);

        status.setCellValueFactory(data ->
                new SimpleStringProperty(
                    data.getValue().getStatus()));

        status.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);

                } else {

                    setGraphic(statusBadge(value));
                }
            }
        });

        TableColumn<Product, Void> actions =
                new TableColumn<>("ACTIONS");

        actions.setPrefWidth(175);
        actions.setSortable(false);

        actions.setCellFactory(col -> new TableCell<>() {

            private final Button edit =
                    new Button("Edit");

            private final Button toggle =
                    new Button();

            private final HBox buttons =
                    new HBox(6, edit, toggle);

            {
                edit.getStyleClass().add("pm-edit-button");

                buttons.setAlignment(Pos.CENTER_LEFT);

                edit.setOnAction(e -> {

                    Product product =
                            getTableRow().getItem();

                    if (product != null) {

                        openForm(stage, user, product);
                    }
                });

                toggle.setOnAction(e -> {

                    Product product =
                            getTableRow().getItem();

                    if (product != null) {

                        changeStatus(
                                stage, user, product);
                    }
                });
            }

            @Override
            protected void updateItem(
                    Void value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                Product product =
                        getTableRow().getItem();

                if (empty || product == null) {

                    setGraphic(null);
                    return;
                }

                toggle.setText(
                        product.isActive()
                        ? "Deactivate"
                        : "Activate");

                toggle.getStyleClass().removeAll(
                        "pm-deactivate-button",
                        "pm-activate-button");

                toggle.getStyleClass().add(
                        product.isActive()
                        ? "pm-deactivate-button"
                        : "pm-activate-button");

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(
                id, image, name, category,
                supplier, price, stock,
                minimum, status, actions);

        return table;
    }

    private static TableColumn<Product, String> textColumn(
            String title,
            Function<Product, String> getter,
            double width) {

        TableColumn<Product, String> column =
                new TableColumn<>(title);

        column.setPrefWidth(width);

        column.setCellValueFactory(data ->
                new SimpleStringProperty(
                    getter.apply(data.getValue())));

        column.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                setText(null);

                if (empty || value == null) {

                    setGraphic(null);

                } else {

                    Label label = new Label(value);

                    label.setWrapText(true);

                    label.maxWidthProperty().bind(
                            widthProperty().subtract(14));

                    setGraphic(label);
                }
            }
        });

        return column;
    }

    // ==========================================
    // ADD PRODUCT PAGE
    // ==========================================

    public static void showAddProduct(
            Stage stage, User user) {

        openForm(stage, user, null);
    }

    // ==========================================
    // ADD AND EDIT PRODUCT FORM
    // ==========================================

    private static void openForm(
            Stage stage,
            User user,
            Product product) {

        if (!isAdmin(stage, user)) {
            return;
        }

        boolean editing = product != null;

        Map<String, Integer> categories;

        ObservableList<Supplier> suppliers;

        List<Integer> selectedSuppliers;

        try {

            categories = ProductService.getCategories();

            suppliers = SupplierService.getSuppliers();

            selectedSuppliers = editing
                ? ProductService.getProductSupplierIds(
                    product.getId())
                : new ArrayList<>();

        } catch (Exception ex) {

            showMessage(
                    Alert.AlertType.ERROR,
                    "Unable to load form data.");

            ex.printStackTrace();
            return;
        }

        VBox content = new VBox(18);

        content.setPadding(new Insets(24));

        Button back = UIComponents.tertiaryButton(
                "← Back to Products");

        back.setOnAction(e -> show(stage, user));

        Label heading = UIComponents.heading(
                editing ? "Edit Product" : "Add Product");

        Label subtitle = UIComponents.mutedText(
                editing
                ? "Update product information and availability."
                : "Create a new product in your A2Z catalog.");

        content.getChildren().addAll(
                back, heading, subtitle);

        // PRODUCT INFORMATION

        TextField name = new TextField();

        name.setPromptText("Enter product name");

        ComboBox<String> category = new ComboBox<>();

        category.getItems().addAll(categories.keySet());

        category.setPromptText("Select category");

        category.setMaxWidth(Double.MAX_VALUE);

        TextArea description = new TextArea();

        description.setPromptText(
                "Enter product description...");

        description.setWrapText(true);
        description.setPrefRowCount(4);

        // SUPPLIER SELECTION

        VBox supplierOptions = new VBox(8);

        Map<CheckBox, Integer> supplierChecks =
                new LinkedHashMap<>();

        for (Supplier supplier : suppliers) {

            CheckBox option = new CheckBox(
                    supplier.toString());

            option.setWrapText(true);

            option.setSelected(
                    selectedSuppliers.contains(
                            supplier.getId()));

            supplierChecks.put(
                    option, supplier.getId());

            supplierOptions.getChildren().add(option);
        }

        if (suppliers.isEmpty()) {

            supplierOptions.getChildren().add(
                    UIComponents.mutedText(
                        "No suppliers available."));
        }

        ScrollPane supplierScroll =
                new ScrollPane(supplierOptions);

        supplierScroll.setFitToWidth(true);
        supplierScroll.setPrefViewportHeight(100);

        supplierScroll.getStyleClass().add(
                "pm-supplier-list");

        VBox information = UIComponents.card(16);

        information.getStyleClass().add("pm-form-card");

        information.getChildren().addAll(

                UIComponents.cardHeading(
                        "Product Information"),

                field("Product Name *", name),

                field("Category *", category),

                field("Suppliers", supplierScroll),

                UIComponents.mutedText(
                    "Select one or more suppliers."),

                field("Description", description)
        );

        // PRICING AND INVENTORY

        TextField price = new TextField();

        price.setPromptText("0.00");

        TextField stock = new TextField("0");

        TextField minStock = new TextField("5");

        CheckBox active = new CheckBox(
                "Product is active");

        active.setSelected(true);

        HBox stockRow = new HBox(
                14,
                field(editing
                        ? "Current Stock"
                        : "Initial Stock *", stock),
                field("Minimum Stock *", minStock));

        VBox pricing = UIComponents.card(16);

        pricing.getStyleClass().add("pm-form-card");

        pricing.getChildren().addAll(

                UIComponents.cardHeading(
                        "Pricing & Inventory"),

                field("Selling Price (₹) *", price),

                stockRow,

                UIComponents.mutedText(
                        "Minimum stock determines when "
                        + "a low-stock alert is displayed.")
        );

        if (editing) {

            stock.setEditable(false);

            stock.getStyleClass().add(
                    "pm-readonly-input");

            pricing.getChildren().add(
                    UIComponents.mutedText(
                        "Use Inventory Management to "
                        + "adjust the current stock."));
        }

        // PRODUCT IMAGE

        TextField imageUrl = new TextField();

        imageUrl.setPromptText(
                "https://example.com/product.jpg");

        StackPane preview = new StackPane();

        preview.getStyleClass().add(
                "pm-image-preview");

        preview.setPrefHeight(250);

        preview.getChildren().add(
                UIComponents.productImage(
                        "", 250, 220));

        Runnable updatePreview = () -> {

            preview.getChildren().setAll(
                    UIComponents.productImage(
                        imageUrl.getText().trim(),
                        250, 220));
        };

        Button previewButton =
                UIComponents.secondaryButton(
                        "Preview Image");

        previewButton.setMaxWidth(Double.MAX_VALUE);

        previewButton.setOnAction(
                e -> updatePreview.run());

        PauseTransition delay =
                new PauseTransition(
                        Duration.millis(700));

        delay.setOnFinished(
                e -> updatePreview.run());

        imageUrl.textProperty().addListener(
                (obs, oldValue, newValue) -> {

                    delay.playFromStart();
                });

        VBox imageCard = UIComponents.card(14);

        imageCard.getStyleClass().add("pm-form-card");

        imageCard.getChildren().addAll(

                UIComponents.cardHeading(
                        "Product Image"),

                preview,

                field("Image URL", imageUrl),

                previewButton,

                UIComponents.mutedText(
                        "Use a direct HTTP or HTTPS "
                        + "image URL. A placeholder is "
                        + "shown when the image is unavailable.")
        );

        // PRODUCT AVAILABILITY

        VBox availability = UIComponents.card(12);

        availability.getStyleClass().add("pm-form-card");

        availability.getChildren().addAll(

                UIComponents.cardHeading(
                        "Product Availability"),

                active,

                UIComponents.mutedText(
                        "Active products are visible "
                        + "in the customer catalog.")
        );

        // LOAD EXISTING PRODUCT INFORMATION

        if (editing) {

            name.setText(product.getName());

            category.setValue(product.getCategory());

            description.setText(
                    product.getDescription());

            price.setText(
                    product.getPrice().toPlainString());

            stock.setText(
                    String.valueOf(product.getStock()));

            minStock.setText(
                    String.valueOf(product.getMinStock()));

            imageUrl.setText(
                    product.getImagePath() == null
                    ? ""
                    : product.getImagePath());

            active.setSelected(product.isActive());

            updatePreview.run();
        }

        // FORM LAYOUT

        VBox leftColumn = new VBox(
                16, information, pricing);

        VBox rightColumn = new VBox(
                16, imageCard, availability);

        leftColumn.setMinWidth(0);
        rightColumn.setMinWidth(0);

        GridPane form = new GridPane();

        form.setHgap(18);

        ColumnConstraints left =
                new ColumnConstraints();

        left.setPercentWidth(62);
        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right =
                new ColumnConstraints();

        right.setPercentWidth(38);
        right.setHgrow(Priority.ALWAYS);

        form.getColumnConstraints().addAll(
                left, right);

        form.add(leftColumn, 0, 0);
        form.add(rightColumn, 1, 0);

        // FORM VALIDATION MESSAGE

        Label error = new Label();

        error.getStyleClass().add("pm-error");

        error.setWrapText(true);

        error.setMaxWidth(Double.MAX_VALUE);

        error.setVisible(false);
        error.setManaged(false);

        // SAVE AND CANCEL

        Button cancel =
                UIComponents.secondaryButton("Cancel");

        cancel.setOnAction(e -> show(stage, user));

        Button save =
                UIComponents.primaryButton(
                        editing
                        ? "Save Changes"
                        : "Add Product");

        save.setDefaultButton(true);

        save.setOnAction(e -> {

            error.setVisible(false);
            error.setManaged(false);

            try {

                if (name.getText().isBlank()) {

                    throw new IllegalArgumentException(
                            "Enter the product name.");
                }

                if (category.getValue() == null
                        || !categories.containsKey(
                            category.getValue())) {

                    throw new IllegalArgumentException(
                            "Select a valid category.");
                }

                BigDecimal productPrice =
                        new BigDecimal(
                                price.getText().trim());

                int productStock =
                        Integer.parseInt(
                                stock.getText().trim());

                int minimum =
                        Integer.parseInt(
                                minStock.getText().trim());

                if (productPrice.signum() < 0
                        || productStock < 0
                        || minimum < 0) {

                    throw new IllegalArgumentException(
                            "Values cannot be negative.");
                }

                if (productPrice.compareTo(
                        new BigDecimal("99999999.99")) > 0
                        || productPrice.scale() > 2) {

                    throw new IllegalArgumentException(
                        "Enter a valid price "
                        + "with up to 2 decimal places.");
                }

                String url = imageUrl.getText().trim();

                boolean unchangedImage =
                        editing && Objects.equals(
                            url, product.getImagePath());

                if (!url.isEmpty()
                        && !unchangedImage
                        && !validImageUrl(url)) {

                    throw new IllegalArgumentException(
                        "Enter a valid HTTP or HTTPS image URL.");
                }

                List<Integer> supplierIds =
                        new ArrayList<>();

                for (Map.Entry<CheckBox, Integer> entry
                        : supplierChecks.entrySet()) {

                    if (entry.getKey().isSelected()) {

                        supplierIds.add(entry.getValue());
                    }
                }

                if (editing
                        && product.isActive()
                        && !active.isSelected()) {

                    boolean confirmed =
                            UIComponents.confirm(
                                "Deactivate Product",
                                "This product will no longer "
                                + "be available to customers. "
                                + "Continue?",
                                "Deactivate");

                    if (!confirmed) {
                        return;
                    }
                }

                ProductService.saveProduct(
                        product,
                        name.getText().trim(),
                        categories.get(category.getValue()),
                        description.getText().trim(),
                        productPrice,
                        productStock,
                        minimum,
                        url,
                        active.isSelected(),
                        supplierIds
                );

                showMessage(
                        Alert.AlertType.INFORMATION,
                        editing
                        ? "Product updated successfully!"
                        : "Product added successfully!");

                show(stage, user);

            } catch (NumberFormatException ex) {

                showError(error,
                        "Enter valid price and stock values.");

            } catch (IllegalArgumentException ex) {

                showError(error, ex.getMessage());

            } catch (Exception ex) {

                showError(error,
                        "Unable to save product. "
                        + "Please try again.");

                ex.printStackTrace();
            }
        });

        HBox actions = new HBox(12, cancel, save);

        actions.setAlignment(Pos.CENTER_RIGHT);

        actions.getStyleClass().add("pm-form-actions");

        content.getChildren().addAll(
                form, error, actions);

        showPage(
                stage,
                user,
                editing
                ? "Products  ›  Edit Product"
                : "Products  ›  Add Product",
                content
        );
    }

    // ==========================================
    // ACTIVATE / DEACTIVATE PRODUCT
    // ==========================================

    private static void changeStatus(
            Stage stage,
            User user,
            Product product) {

        boolean active = !product.isActive();

        String action =
                active ? "Activate" : "Deactivate";

        boolean confirmed = UIComponents.confirm(
                action + " Product",
                "Are you sure you want to "
                + action.toLowerCase(Locale.ROOT)
                + " " + product.getName() + "?",
                action
        );

        if (!confirmed) {
            return;
        }

        try {

            ProductService.setActive(
                    product.getId(), active);

            showMessage(
                    Alert.AlertType.INFORMATION,
                    "Product status updated successfully!");

            show(stage, user);

        } catch (Exception ex) {

            showMessage(
                    Alert.AlertType.ERROR,
                    "Unable to update product status.");

            ex.printStackTrace();
        }
    }

    // ==========================================
    // COMMON ADMIN PAGE LAYOUT
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

            if (node instanceof Button button) {

                button.getStyleClass().remove("active");

                if (button.getText().trim()
                        .endsWith("Products")) {

                    button.getStyleClass().add("active");
                }
            }
        }

        root.setLeft(sidebar);

        Label path = UIComponents.mutedText(
                "Admin Workspace  ›  " + breadcrumb);

        Label adminName = new Label(user.getName());

        adminName.getStyleClass().add("admin-top-name");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(
                12, path, spacer, adminName);

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add("admin-topbar");

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox workspace = new VBox(topBar, scroll);

        root.setCenter(workspace);

        Scene scene = new Scene(root, 1440, 900);

        UIComponents.applyStyles(scene);

        stage.setTitle("A2Z Commerce - " + breadcrumb);

        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    // ==========================================
    // STATISTICS CARD
    // ==========================================

    private static VBox statCard(
            String title,
            long value,
            String icon,
            String color,
            String background) {

        Label titleLabel =
                UIComponents.mutedText(title);

        Label iconLabel = new Label(icon);

        iconLabel.setAlignment(Pos.CENTER);

        iconLabel.setMinSize(36, 36);

        iconLabel.setStyle(
                "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + color + ";"
                + "-fx-background-radius: 8px;"
                + "-fx-font-size: 18px;"
                + "-fx-font-weight: bold;");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(
                5, titleLabel, spacer, iconLabel);

        top.setAlignment(Pos.CENTER_LEFT);

        Label number =
                new Label(String.valueOf(value));

        number.getStyleClass().add("pm-stat-value");

        number.setStyle(
                "-fx-text-fill: " + color + ";");

        VBox card = new VBox(12, top, number);

        card.getStyleClass().add("pm-stat-card");

        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }

    // ==========================================
    // FORM FIELD
    // ==========================================

    private static VBox field(
            String title, Node input) {

        Label label = new Label(title);

        label.getStyleClass().add("pm-field-label");

        if (input instanceof Region region) {

            region.setMaxWidth(Double.MAX_VALUE);
        }

        VBox field = new VBox(7, label, input);

        field.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(field, Priority.ALWAYS);

        return field;
    }

    // ==========================================
    // PRODUCT STATUS
    // ==========================================

    private static String stockStatus(Product product) {

        if (product.getStock() == 0) {

            return "Out of Stock";
        }

        if (product.getStock()
                <= product.getMinStock()) {

            return "Low Stock";
        }

        return "Available";
    }

    private static Label statusBadge(String status) {

        Label badge = new Label(status);

        switch (status) {

            case "Available":
                badge.getStyleClass().add("badge-success");
                break;

            case "Low Stock":
                badge.getStyleClass().add("badge-warning");
                break;

            case "Out of Stock":
                badge.getStyleClass().add("badge-error");
                break;

            default:
                badge.getStyleClass().add("badge-neutral");
        }

        return badge;
    }

    // ==========================================
    // VALIDATE IMAGE URL
    // ==========================================

    private static boolean validImageUrl(String value) {

        try {

            java.net.URI uri =
                    java.net.URI.create(value);

            return uri.getHost() != null
                    && ("https".equalsIgnoreCase(
                            uri.getScheme())
                    || "http".equalsIgnoreCase(
                            uri.getScheme()));

        } catch (Exception ex) {

            return false;
        }
    }

    // ==========================================
    // ADMIN ACCESS VALIDATION
    // ==========================================

    private static boolean isAdmin(
            Stage stage, User user) {

        if (user == null
                || !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);

            return false;
        }

        return true;
    }

    private static void showError(
            Label error, String message) {

        error.setText(message);
        error.setVisible(true);
        error.setManaged(true);
    }

    private static void showMessage(
            Alert.AlertType type, String message) {

        Alert alert = new Alert(type);

        alert.setTitle("A2Z Commerce");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
