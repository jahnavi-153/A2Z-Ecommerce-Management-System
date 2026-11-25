
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
import javafx.util.StringConverter;

import java.util.Locale;
import java.util.function.Function;
import java.sql.SQLIntegrityConstraintViolationException;

public class SupplierManagement {

    // ==========================================
    // SUPPLIER MANAGEMENT PAGE
    // ==========================================

    public static void show(Stage stage, User user) {

        if (!isAdmin(stage, user)) {
            return;
        }

        VBox content = new VBox(20);

        content.setPadding(new Insets(24));

        // PAGE HEADER

        VBox heading = new VBox(6,

            UIComponents.heading(
                "Supplier Management"),

            UIComponents.mutedText(
                "Manage suppliers and product sourcing relationships.")
        );

        Button add = UIComponents.primaryButton(
            "+ Add Supplier");

        Button refresh = UIComponents.secondaryButton(
            "↻ Refresh");

        add.setOnAction(e ->
            openForm(stage, user, null));

        refresh.setOnAction(e ->
            show(stage, user));

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
            12, heading, spacer, refresh, add);

        header.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(header);

        try {

            ObservableList<Supplier> suppliers =
                SupplierService.getSuppliers();

            // STATISTICS

            content.getChildren().add(
                createStatistics(suppliers.size()));

            // SEARCH BAR

            TextField search = new TextField();

            search.setPromptText(
                "Search supplier name, company, email or phone...");

            search.setPrefWidth(430);

            Button clear =
                UIComponents.secondaryButton(
                    "Clear Search");

            HBox filters = new HBox(
                12, search, clear);

            filters.setAlignment(Pos.CENTER_LEFT);

            // SUPPLIER TABLE

            TableView<Supplier> table =
                createTable(stage, user);

            FilteredList<Supplier> filtered =
                new FilteredList<>(
                    suppliers, s -> true);

            SortedList<Supplier> sorted =
                new SortedList<>(filtered);

            sorted.comparatorProperty().bind(
                table.comparatorProperty());

            table.setItems(sorted);

            Label resultCount =
                UIComponents.mutedText(
                    "Showing " + suppliers.size()
                    + " suppliers");

            search.textProperty().addListener(
                (obs, oldValue, newValue) -> {

                    String keyword = newValue.trim()
                        .toLowerCase(Locale.ROOT);

                    filtered.setPredicate(s ->

                        contains(s.getName(), keyword)

                        || contains(s.getCompany(), keyword)

                        || contains(s.getEmail(), keyword)

                        || contains(s.getPhone(), keyword)

                        || contains(s.getAddress(), keyword)

                        || String.valueOf(s.getId())
                                 .contains(keyword)
                    );

                    resultCount.setText(

                        "Showing " + filtered.size()

                        + " of " + suppliers.size()

                        + " suppliers"
                    );
                });

            clear.setOnAction(e -> search.clear());

            // SUPPLIER DIRECTORY CARD

            VBox tableCard = UIComponents.card(16);

            tableCard.getStyleClass().add(
                "sm-table-card");

            tableCard.getChildren().addAll(

                UIComponents.cardHeading(
                    "Supplier Directory"),

                UIComponents.mutedText(
                    "View and manage registered suppliers."),

                filters,

                table,

                resultCount
            );

            content.getChildren().add(tableCard);

        } catch (Exception ex) {

            Label error = new Label(
                "Unable to load suppliers. Please refresh.");

            error.getStyleClass().add("sm-error");

            content.getChildren().add(error);

            ex.printStackTrace();
        }

        showPage(stage, user, "Suppliers", content);
    }

    // ==========================================
    // SUPPLIER STATISTICS
    // ==========================================

    private static HBox createStatistics(
            int supplierCount) throws Exception {

        int linkedProducts =
            SupplierService.assignedProductCount();

        int totalProducts =
            ProductService.getProducts().size();

        double coverage = totalProducts == 0

            ? 0

            : (linkedProducts * 100.0)
                / totalProducts;

        VBox total = UIComponents.statCard(

            "Total Suppliers",

            String.valueOf(supplierCount),

            "Registered supplier accounts",

            "#0D5C4B"
        );

        VBox linked = UIComponents.statCard(

            "Linked Products",

            String.valueOf(linkedProducts),

            "Products with assigned suppliers",

            "#15803D"
        );

        VBox coverageCard = UIComponents.statCard(

            "Supplier Coverage",

            String.format(
                Locale.ROOT, "%.1f%%", coverage),

            linkedProducts + " of "
                + totalProducts + " products linked",

            "#D95D24"
        );

        ProgressBar progress = new ProgressBar(
            Math.min(1.0, coverage / 100.0));

        progress.setMaxWidth(Double.MAX_VALUE);

        progress.getStyleClass().add(
            "sm-coverage-progress");

        coverageCard.getChildren().add(progress);

        for (VBox card : new VBox[]{
                total, linked, coverageCard}) {

            card.setMinWidth(0);

            card.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(card, Priority.ALWAYS);
        }

        return new HBox(
            15, total, linked, coverageCard);
    }

    // ==========================================
    // SUPPLIER TABLE
    // ==========================================

    private static TableView<Supplier> createTable(
            Stage stage, User user) {

        TableView<Supplier> table =
            new TableView<>();

        table.getStyleClass().add("pm-table");

        table.setPrefHeight(540);

        table.setFixedCellSize(72);

        table.setColumnResizePolicy(
            TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(

            UIComponents.emptyState(

                "No Suppliers Found",

                "Add a supplier or change your search.")
        );

        TableColumn<Supplier, Integer> id =
            column("ID", Supplier::getId, 65);

        TableColumn<Supplier, String> name =
            column("SUPPLIER NAME",
                Supplier::getName, 190);

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

                Label avatar = createAvatar(value);

                Label supplierName =
                    new Label(value);

                supplierName.getStyleClass().add(
                    "sm-supplier-name");

                HBox row = new HBox(
                    10, avatar, supplierName);

                row.setAlignment(Pos.CENTER_LEFT);

                setGraphic(row);
            }
        });

        TableColumn<Supplier, String> company =
            column("COMPANY",
                Supplier::getCompany, 180);

        TableColumn<Supplier, String> email =
            column("EMAIL",
                s -> displayValue(s.getEmail()), 210);

        TableColumn<Supplier, String> phone =
            column("PHONE",
                s -> displayValue(s.getPhone()), 130);

        TableColumn<Supplier, String> address =
            column("ADDRESS",
                s -> displayValue(s.getAddress()), 200);

        address.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    String value, boolean empty) {

                super.updateItem(value, empty);

                if (empty || value == null) {

                    setText(null);
                    setTooltip(null);

                } else {

                    setText(value);
                    setTooltip(new Tooltip(value));
                }
            }
        });

        // SUPPLIER ACTIONS

        TableColumn<Supplier, Supplier> actions =
            column("ACTIONS", s -> s, 275);

        actions.setSortable(false);

        actions.setCellFactory(col -> new TableCell<>() {

            @Override
            protected void updateItem(
                    Supplier supplier, boolean empty) {

                super.updateItem(supplier, empty);

                setText(null);

                if (empty || supplier == null) {

                    setGraphic(null);
                    return;
                }

                Button edit =
                    UIComponents.secondaryButton("Edit");

                Button products =
                    UIComponents.secondaryButton(
                        "View Products");

                Button assign =
                    UIComponents.primaryButton("Assign");

                edit.getStyleClass().add(
                    "sm-row-button");

                products.getStyleClass().add(
                    "sm-row-button");

                assign.getStyleClass().add(
                    "sm-row-button");

                edit.setOnAction(e ->
                    openForm(stage, user, supplier));

                products.setOnAction(e ->
                    viewProducts(stage, user, supplier));

                assign.setOnAction(e ->
                    assignProduct(stage, user, supplier));

                HBox buttons = new HBox(
                    7, edit, products, assign);

                buttons.setAlignment(Pos.CENTER_LEFT);

                setGraphic(buttons);
            }
        });

        table.getColumns().addAll(

            id, name, company,

            email, phone, address,

            actions
        );

        return table;
    }

    // ==========================================
    // ADD / EDIT SUPPLIER PAGE
    // ==========================================

    private static void openForm(
            Stage stage,
            User user,
            Supplier supplier) {

        if (!isAdmin(stage, user)) {
            return;
        }

        boolean editing = supplier != null;

        VBox content = new VBox(20);

        content.setPadding(new Insets(24));

        Button back = UIComponents.tertiaryButton(
            "← Back to Suppliers");

        back.setOnAction(e ->
            show(stage, user));

        String title = editing

            ? "Edit Supplier"

            : "Add Supplier";

        VBox heading = new VBox(6,

            UIComponents.heading(title),

            UIComponents.mutedText(

                editing

                    ? "Update existing supplier information."

                    : "Register a new supplier for your store.")
        );

        // INPUT FIELDS

        TextField name = new TextField();

        name.setPromptText(
            "Enter supplier name");

        TextField company = new TextField();

        company.setPromptText(
            "Enter company name");

        TextField email = new TextField();

        email.setPromptText(
            "supplier@example.com");

        TextField phone = new TextField();

        phone.setPromptText(
            "Enter 10-digit phone number");

        TextArea address = new TextArea();

        address.setPromptText(
            "Enter complete supplier address");

        address.setPrefRowCount(4);

        address.setWrapText(true);

        // LOAD EXISTING DATA FOR EDITING

        if (editing) {

            name.setText(
                safeText(supplier.getName()));

            company.setText(
                safeText(supplier.getCompany()));

            email.setText(
                safeText(supplier.getEmail()));

            phone.setText(
                safeText(supplier.getPhone()));

            address.setText(
                safeText(supplier.getAddress()));
        }

        // BASIC INFORMATION

        VBox basicInformation =
            UIComponents.card(20);

        basicInformation.getStyleClass().add(
            "sm-form-card");

        GridPane basicGrid = createTwoColumnGrid();

        basicGrid.add(
            field("Supplier Name *", name), 0, 0);

        basicGrid.add(
            field("Company Name *", company), 1, 0);

        basicInformation.getChildren().addAll(

            UIComponents.cardHeading(
                "Basic Information"),

            UIComponents.mutedText(
                "Enter the supplier and company details."),

            basicGrid
        );

        // CONTACT INFORMATION

        VBox contactInformation =
            UIComponents.card(20);

        contactInformation.getStyleClass().add(
            "sm-form-card");

        GridPane contactGrid =
            createTwoColumnGrid();

        contactGrid.add(
            field("Email Address", email), 0, 0);

        contactGrid.add(
            field("Phone Number *", phone), 1, 0);

        contactInformation.getChildren().addAll(

            UIComponents.cardHeading(
                "Contact Information"),

            UIComponents.mutedText(
                "Provide supplier contact and address details."),

            contactGrid,

            field("Company Address", address)
        );

        // VALIDATION MESSAGE

        Label error = new Label();

        error.getStyleClass().add("sm-error");

        error.setWrapText(true);

        error.setVisible(false);

        error.managedProperty().bind(
            error.visibleProperty());

        // FORM ACTIONS

        Button cancel =
            UIComponents.secondaryButton("Cancel");

        Button save =
            UIComponents.primaryButton(

                editing

                    ? "Save Changes"

                    : "Add Supplier"
            );

        cancel.setOnAction(e ->
            show(stage, user));

        save.setOnAction(e -> {

            error.setVisible(false);

            try {

                String supplierName =
                    name.getText().trim();

                String companyName =
                    company.getText().trim();

                String emailAddress =
                    email.getText().trim();

                String phoneNumber =
                    phone.getText().trim();

                String supplierAddress =
                    address.getText().trim();

                // REQUIRED FIELD VALIDATION

                if (supplierName.isBlank()
                        || companyName.isBlank()
                        || phoneNumber.isBlank()) {

                    throw new IllegalArgumentException(

                        "Supplier name, company name "
                        + "and phone number are required.");
                }

                // PHONE VALIDATION

                if (!phoneNumber.matches("[0-9]{10}")) {

                    throw new IllegalArgumentException(
                        "Enter a valid 10-digit phone number.");
                }

                // EMAIL VALIDATION

                if (!emailAddress.isBlank()

                    && !emailAddress.matches(
                        "[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

                    throw new IllegalArgumentException(
                        "Enter a valid email address.");
                }

                // FIELD LENGTH VALIDATION

                if (supplierName.length() > 100

                        || companyName.length() > 100

                        || emailAddress.length() > 100

                        || supplierAddress.length() > 255) {

                    throw new IllegalArgumentException(
                        "One or more fields are too long.");
                }

                // SAVE TO DATABASE

                if (editing) {

                    SupplierService.updateSupplier(

                        supplier.getId(),

                        supplierName,

                        companyName,

                        emailAddress,

                        phoneNumber,

                        supplierAddress
                    );

                } else {

                    SupplierService.addSupplier(

                        supplierName,

                        companyName,

                        emailAddress,

                        phoneNumber,

                        supplierAddress
                    );
                }

                UIComponents.success(

                    editing

                        ? "Supplier updated successfully!"

                        : "Supplier added successfully!"
                );

                show(stage, user);

            } catch (IllegalArgumentException ex) {

                showError(error, ex.getMessage());

            } catch (Exception ex) {

                showError(
                    error, "Unable to save supplier.");

                ex.printStackTrace();
            }
        });

        HBox actions = new HBox(
            12, cancel, save);

        actions.setAlignment(Pos.CENTER_RIGHT);

        actions.getStyleClass().add(
            "sm-form-actions");

        VBox form = new VBox(

            18,

            basicInformation,

            contactInformation,

            error,

            actions
        );

        form.setMaxWidth(Double.MAX_VALUE);

        // RIGHT SIDE INFORMATION CARD

        VBox information = UIComponents.card(16);

        information.getStyleClass().add(
            "sm-information-card");

        Label icon = new Label("i");

        icon.getStyleClass().add(
            "sm-information-icon");

        Label informationTitle =
            UIComponents.cardHeading(
                "Supplier Information");

        Label description =
            UIComponents.mutedText(
                "Supplier records help manage product sourcing "
                + "and inventory relationships.");

        Label required =
            UIComponents.mutedText(
                "• Fields marked with * are required.");

        Label phoneHelp =
            UIComponents.mutedText(
                "• Enter a valid 10-digit phone number.");

        Label emailHelp =
            UIComponents.mutedText(
                "• Email and address are optional.");

        Label assignmentHelp =
            UIComponents.mutedText(
                "• Products can be assigned after "
                + "the supplier is saved.");

        information.getChildren().addAll(

            icon,

            informationTitle,

            description,

            new Separator(),

            required,

            phoneHelp,

            emailHelp,

            assignmentHelp
        );

        if (editing) {

            Label id = new Label(
                "Supplier ID: #" + supplier.getId());

            id.getStyleClass().add("sm-supplier-name");

            information.getChildren().addAll(
                new Separator(), id);
        }

        // FORM LAYOUT

        GridPane layout = new GridPane();

        layout.setHgap(20);

        ColumnConstraints left =
            new ColumnConstraints();

        left.setPercentWidth(70);

        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right =
            new ColumnConstraints();

        right.setPercentWidth(30);

        layout.getColumnConstraints().addAll(
            left, right);

        layout.add(form, 0, 0);

        layout.add(information, 1, 0);

        GridPane.setValignment(
            information, javafx.geometry.VPos.TOP);

        content.getChildren().addAll(

            back,

            heading,

            layout
        );

        showPage(

            stage,

            user,

            "Suppliers  ›  " + title,

            content
        );
    }

    // ==========================================
    // ASSIGN PRODUCT TO SUPPLIER
    // ==========================================

    private static void assignProduct(
            Stage stage,
            User user,
            Supplier supplier) {

        if (!isAdmin(stage, user)) {
            return;
        }

        try {

            Dialog<ButtonType> dialog =
                new Dialog<>();

            dialog.initOwner(stage);

            dialog.setTitle("Assign Product");

            dialog.setHeaderText(
                "Assign Product to Supplier");

            ButtonType assignType = new ButtonType(

                "Assign Product",

                ButtonBar.ButtonData.OK_DONE
            );

            dialog.getDialogPane()
                  .getButtonTypes()
                  .addAll(

                      assignType,

                      ButtonType.CANCEL
                  );

            // AVAILABLE PRODUCTS

            ComboBox<Product> products =
                new ComboBox<>();

            products.setMaxWidth(Double.MAX_VALUE);

            for (Product product :
                    ProductService.getProducts()) {

                if (product.isActive()) {

                    products.getItems().add(product);
                }
            }

            products.setPromptText(
                "Select a product");

            products.setConverter(
                new StringConverter<Product>() {

                @Override
                public String toString(Product p) {

                    return p == null

                        ? ""

                        : p.getName()
                            + " (ID: "
                            + p.getId() + ")";
                }

                @Override
                public Product fromString(
                        String text) {

                    return null;
                }
            });

            // SUPPLIER INFORMATION

            Label supplierName = new Label(
                supplier.getName());

            supplierName.getStyleClass().add(
                "sm-supplier-name");

            VBox identity = new VBox(

                4,

                supplierName,

                UIComponents.mutedText(
                    supplier.getCompany())
            );

            HBox supplierCard = new HBox(

                12,

                createAvatar(supplier.getName()),

                identity
            );

            supplierCard.setAlignment(
                Pos.CENTER_LEFT);

            supplierCard.getStyleClass().add(
                "sm-linked-supplier");

            Label error = new Label();

            error.getStyleClass().add("sm-error");

            error.setWrapText(true);

            error.setVisible(false);

            error.managedProperty().bind(
                error.visibleProperty());

            VBox form = new VBox(

                18,

                supplierCard,

                UIComponents.mutedText(
                    "Select an active product to link "
                    + "with this supplier."),

                field("Product *", products),

                error
            );

            form.setPadding(new Insets(15));

            form.setPrefWidth(460);

            dialog.getDialogPane().setContent(form);

            styleDialog(dialog);

            Button assignButton = (Button)
                dialog.getDialogPane()
                      .lookupButton(assignType);

            assignButton.getStyleClass().add(
                "btn-primary");

            assignButton.disableProperty().bind(

                products.valueProperty().isNull()
            );

            assignButton.addEventFilter(

                javafx.event.ActionEvent.ACTION,

                e -> {

                    Product selected =
                        products.getValue();

                    if (selected == null) {

                        showError(error,
                            "Please select a product.");

                        e.consume();

                        return;
                    }

                    try {

                        SupplierService.assignProduct(

                            supplier.getId(),

                            selected.getId()
                        );

                    } catch (
                        SQLIntegrityConstraintViolationException ex) {

                        showError(error,

                            "Product already assigned "
                            + "or invalid reference.");

                        e.consume();

                    } catch (Exception ex) {

                        showError(error,
                            "Unable to assign product.");

                        ex.printStackTrace();

                        e.consume();
                    }
                }
            );

            if (dialog.showAndWait()
                      .orElse(ButtonType.CANCEL)
                      == assignType) {

                UIComponents.success(
                    "Product assigned successfully!");

                show(stage, user);
            }

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to load products.");

            ex.printStackTrace();
        }
    }

    // ==========================================
    // VIEW LINKED PRODUCTS
    // ==========================================

    private static void viewProducts(
            Stage stage,
            User user,
            Supplier supplier) {

        if (!isAdmin(stage, user)) {
            return;
        }

        try {

            ObservableList<String> products =
                SupplierService.getSupplierProducts(
                    supplier.getId());

            Dialog<ButtonType> dialog =
                new Dialog<>();

            dialog.initOwner(stage);

            dialog.setTitle("Linked Products");

            dialog.setHeaderText(
                "Supplier Product Relationships");

            dialog.getDialogPane()
                  .getButtonTypes()
                  .add(ButtonType.CLOSE);

            Label supplierName = new Label(
                supplier.getName());

            supplierName.getStyleClass().add(
                "sm-supplier-name");

            VBox identity = new VBox(

                5,

                supplierName,

                UIComponents.mutedText(
                    supplier.getCompany()),

                UIComponents.mutedText(
                    "Supplier #" + supplier.getId())
            );

            HBox supplierCard = new HBox(

                12,

                createAvatar(supplier.getName()),

                identity
            );

            supplierCard.setAlignment(
                Pos.CENTER_LEFT);

            supplierCard.getStyleClass().add(
                "sm-linked-supplier");

            Label count = UIComponents.cardHeading(

                "Linked Products (" + products.size() + ")"
            );

            ListView<String> list =
                new ListView<>(products);

            list.setPrefHeight(300);

            list.getStyleClass().add(
                "sm-product-list");

            list.setPlaceholder(

                UIComponents.emptyState(

                    "No Linked Products",

                    "This supplier has no assigned products.")
            );

            VBox content = new VBox(

                18,

                supplierCard,

                count,

                list
            );

            content.setPadding(new Insets(15));

            content.setPrefWidth(560);

            dialog.getDialogPane().setContent(content);

            styleDialog(dialog);

            dialog.showAndWait();

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to retrieve supplier products.");

            ex.printStackTrace();
        }
    }

    // ==========================================
    // SHARED ADMIN PAGE LAYOUT
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

        // HIGHLIGHT SUPPLIERS

        for (Node node : sidebar.getChildren()) {

            if (node instanceof Button) {

                Button button = (Button) node;

                button.getStyleClass().remove("active");

                if (button.getText()
                          .trim()
                          .endsWith("Suppliers")) {

                    button.getStyleClass().add("active");
                }
            }
        }

        root.setLeft(sidebar);

        // TOP BAR

        Label path = UIComponents.mutedText(

            "Admin Workspace  ›  " + breadcrumb
        );

        Label admin = new Label(user.getName());

        admin.getStyleClass().add("admin-top-name");

        Region spacer = new Region();

        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(

            12, path, spacer, admin
        );

        topBar.setAlignment(Pos.CENTER_LEFT);

        topBar.getStyleClass().add("admin-topbar");

        // SCROLLABLE CONTENT

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox workspace = new VBox(
            topBar, scroll);

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

    private static boolean isAdmin(
            Stage stage, User user) {

        if (user == null

                || !"ADMIN".equals(user.getRole())) {

            LoginPage.show(stage);

            return false;
        }

        return true;
    }

    private static GridPane createTwoColumnGrid() {

        GridPane grid = new GridPane();

        grid.setHgap(20);

        grid.setVgap(18);

        for (int i = 0; i < 2; i++) {

            ColumnConstraints column =
                new ColumnConstraints();

            column.setPercentWidth(50);

            column.setHgrow(Priority.ALWAYS);

            grid.getColumnConstraints().add(column);
        }

        return grid;
    }

    private static VBox field(
            String title, Node input) {

        Label label = new Label(title);

        label.getStyleClass().add(
            "sm-field-label");

        if (input instanceof Region) {

            ((Region) input)
                .setMaxWidth(Double.MAX_VALUE);
        }

        return new VBox(8, label, input);
    }

    private static Label createAvatar(String name) {

        String initials = "?";

        if (name != null && !name.isBlank()) {

            String[] parts =
                name.trim().split("\\s+");

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

        avatar.getStyleClass().add("sm-avatar");

        return avatar;
    }

    private static boolean contains(
            String value, String keyword) {

        return value != null

            && value.toLowerCase(Locale.ROOT)
                    .contains(keyword);
    }

    private static String safeText(String value) {

        return value == null ? "" : value;
    }

    private static String displayValue(String value) {

        return value == null || value.isBlank()

            ? "Not provided"

            : value;
    }

    private static void showError(
            Label label, String message) {

        label.setText(message);

        label.setVisible(true);
    }

    private static void styleDialog(
            Dialog<?> dialog) {

        java.net.URL css =
            SupplierManagement.class.getResource(
                "/styles/a2z-javafx.css");

        if (css != null) {

            dialog.getDialogPane()
                  .getStylesheets()
                  .add(css.toExternalForm());
        }

        dialog.getDialogPane()
              .getStyleClass()
              .add("a2z-dialog");
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
                getter.apply(data.getValue()))
        );

        return column;
    }
}
