import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class CustomerDashboard {

    private static final String ALL = "All Categories";

    // Optional online image URL for the shopping banner
    private static final String HERO_IMAGE_URL = "https://media.istockphoto.com/id/2061836383/photo/items-purchased-through-online-shopping-are-delivered-3d-rendering.jpg?s=612x612&w=0&k=20&c=1utqkVmKKMtM6W-JqtHL6vfhna5ROaGG5qj_MxRZ5yk=";

    public static void show(Stage stage, User user) {

        if (!isCustomer(stage, user)) {
            return;
        }

        Map<String, Image> imageCache = new HashMap<>();

        List<Product> products = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        try {

            for (Product p : ProductService.getProducts()) {

                if (p.isActive()) {
                    products.add(p);
                }
            }

            categories.addAll(
                ProductService.getCategories().keySet()
            );

        } catch (Exception ex) {

            UIComponents.error("Unable to load products.");
            ex.printStackTrace();
        }

        // ================= HEADER =================

        TextField search = new TextField();

        search.setPromptText("Search for products...");

        search.getStyleClass().add("cd-search");

        search.setMaxWidth(Double.MAX_VALUE);

        Button cart = UIComponents.secondaryButton("Cart");

        updateCartCount(cart, user);

        VBox header = createHeader(
            stage, user, search, cart
        );

        // ================= FILTERS =================

        ComboBox<String> category = new ComboBox<>();

        category.getItems().add(ALL);
        category.getItems().addAll(categories);
        category.setValue(ALL);

        ComboBox<String> sort = new ComboBox<>();

        sort.getItems().addAll(
            "Default",
            "Price: Low to High",
            "Price: High to Low",
            "Name: A to Z"
        );

        sort.setValue("Default");

        Button refresh =
                UIComponents.secondaryButton("Refresh");

        refresh.setOnAction(e -> show(stage, user));

        Label resultCount = UIComponents.mutedText("");

        // ================= CATEGORY NAVIGATION =================

        HBox categoryNav = new HBox(12);

        categoryNav.setAlignment(Pos.CENTER_LEFT);

        categoryNav.getStyleClass().add("cd-category-nav");

        ToggleGroup navigationGroup = new ToggleGroup();

        List<String> navigationCategories = new ArrayList<>();

        navigationCategories.add(ALL);
        navigationCategories.addAll(categories);

        for (String name : navigationCategories) {

            ToggleButton button = new ToggleButton(name);

            button.setToggleGroup(navigationGroup);

            button.getStyleClass().add("cd-category-nav-btn");

            button.setSelected(ALL.equals(name));

            button.setOnAction(e -> {

                button.setSelected(true);

                category.setValue(name);
            });

            categoryNav.getChildren().add(button);
        }

        ScrollPane navigationScroll =
                new ScrollPane(categoryNav);

        navigationScroll.setFitToHeight(true);

        navigationScroll.setVbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER
        );

        navigationScroll.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        navigationScroll.getStyleClass()
                        .add("cd-navigation-scroll");

        header.getChildren().add(navigationScroll);

        // ================= PRODUCT GRID =================

        FlowPane productGrid = new FlowPane();

        productGrid.setHgap(18);
        productGrid.setVgap(18);
        productGrid.setAlignment(Pos.TOP_LEFT);

        Label allProductsTitle =
                UIComponents.heading("All Products");

        Region filterSpace = new Region();

        HBox.setHgrow(filterSpace, Priority.ALWAYS);

        HBox filters = new HBox(
            12, resultCount, filterSpace,
            category, sort, refresh
        );

        filters.setAlignment(Pos.CENTER_LEFT);

        VBox allProductsSection = new VBox(
            16,
            allProductsTitle,
            filters,
            productGrid
        );

        // ================= SHOPPING CONTENT =================

        VBox content = new VBox(28);

        content.setPadding(new Insets(24, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.getStyleClass().add("cd-scroll");

        // Scroll to the All Products section

        Runnable scrollToProducts = () -> {

            content.applyCss();
            content.layout();

            double available =
                content.getHeight()
                - scroll.getViewportBounds().getHeight();

            if (available > 0) {

                double position =
                    allProductsSection.getBoundsInParent()
                                      .getMinY();

                scroll.setVvalue(
                    Math.min(1, position / available)
                );
            }
        };

        // ================= HERO BANNER =================

        HBox banner = createBanner(() -> {

            search.clear();
            category.setValue(ALL);

            scrollToProducts.run();
        });

        // ================= SHOP BY CATEGORY =================

        Label categoryHeading =
                UIComponents.heading("Shop by Category");

        FlowPane categoryGrid = new FlowPane();

        categoryGrid.setHgap(16);
        categoryGrid.setVgap(16);

        for (String name : categories) {

            Button categoryCard = createCategoryCard(name);

            categoryCard.setOnAction(e -> {

                search.clear();
                category.setValue(name);

                scrollToProducts.run();
            });

            categoryGrid.getChildren().add(categoryCard);
        }

        VBox categorySection = new VBox(
            16, categoryHeading, categoryGrid
        );

        // ================= RECOMMENDATIONS =================

        Label recommendationTitle =
                UIComponents.heading("Recommended for You");

        Label recommendationSubtitle =
                UIComponents.mutedText(
                    "Discover products selected for you."
                );

        FlowPane recommendationGrid = new FlowPane();

        recommendationGrid.setHgap(18);
        recommendationGrid.setVgap(18);

        VBox recommendationSection = new VBox(
            10,
            recommendationTitle,
            recommendationSubtitle,
            recommendationGrid
        );

        try {

            List<Integer> recommendedIds =
                RecommendationService.getRecommendations(
                    user.getUserId()
                );

            Map<Integer, Product> productMap =
                    new HashMap<>();

            for (Product p : products) {

                productMap.put(p.getId(), p);
            }

            for (int id : recommendedIds) {

                Product p = productMap.get(id);

                if (p != null && p.getStock() > 0) {

                    recommendationGrid.getChildren().add(
                        createProductCard(
                            stage, user, p,
                            cart, imageCache, true
                        )
                    );
                }
            }

        } catch (Exception ex) {

            recommendationGrid.getChildren().add(
                UIComponents.mutedText(
                    "Recommendations are currently unavailable."
                )
            );

            ex.printStackTrace();
        }

        if (recommendationGrid.getChildren().isEmpty()) {

            recommendationSection.getChildren().add(
                UIComponents.emptyState(
                    "No recommendations yet",
                    "Explore our product collection below."
                )
            );
        }

        // ================= SEARCH AND SORTING =================

        Runnable displayProducts = () -> {

            List<Product> filtered = new ArrayList<>();

            String text = search.getText()
                                .trim()
                                .toLowerCase(Locale.ROOT);

            String selectedCategory = category.getValue();

            for (Product p : products) {

                boolean nameMatch =
                    p.getName()
                     .toLowerCase(Locale.ROOT)
                     .contains(text);

                boolean categoryTextMatch =
                    p.getCategory() != null
                    && p.getCategory()
                        .toLowerCase(Locale.ROOT)
                        .contains(text);

                boolean categoryMatch =
                    ALL.equals(selectedCategory)
                    || Objects.equals(
                        selectedCategory, p.getCategory()
                    );

                if ((nameMatch || categoryTextMatch)
                        && categoryMatch) {

                    filtered.add(p);
                }
            }

            switch (sort.getValue()) {

                case "Price: Low to High":

                    filtered.sort(
                        Comparator.comparing(
                            Product::getPrice
                        )
                    );

                    break;

                case "Price: High to Low":

                    filtered.sort(
                        Comparator.comparing(
                            Product::getPrice
                        ).reversed()
                    );

                    break;

                case "Name: A to Z":

                    filtered.sort(
                        Comparator.comparing(
                            Product::getName,
                            String.CASE_INSENSITIVE_ORDER
                        )
                    );

                    break;

                default:
                    break;
            }

            productGrid.getChildren().clear();

            for (Product p : filtered) {

                productGrid.getChildren().add(
                    createProductCard(
                        stage, user, p,
                        cart, imageCache, false
                    )
                );
            }

            resultCount.setText(
                filtered.size() + " products found"
            );

            if (filtered.isEmpty()) {

                productGrid.getChildren().add(
                    UIComponents.emptyState(
                        "No products found",
                        "Try another search or category."
                    )
                );
            }

            // Synchronize category navigation

            for (Toggle toggle :
                    navigationGroup.getToggles()) {

                ToggleButton button =
                        (ToggleButton) toggle;

                button.setSelected(
                    button.getText().equals(selectedCategory)
                );
            }
        };

        // ================= EVENT HANDLING =================

        search.textProperty().addListener(
            (obs, oldValue, newValue) ->
                displayProducts.run()
        );

        search.setOnAction(e -> scrollToProducts.run());

        category.setOnAction(e -> {

            displayProducts.run();
            scrollToProducts.run();
        });

        sort.setOnAction(e -> displayProducts.run());

        displayProducts.run();

        // ================= FOOTER =================

        Label footerText = new Label(
            "A2Z Commerce  |  Everything from A to Z"
        );

        footerText.getStyleClass().add("cd-footer-text");

        Label footerDescription = new Label(
            "Smart E-Commerce Order & Inventory Management System"
        );

        footerDescription.getStyleClass()
                         .add("cd-footer-description");

        VBox footer = new VBox(
            6, footerText, footerDescription
        );

        footer.setAlignment(Pos.CENTER);

        footer.getStyleClass().add("cd-footer");

        // ================= FINAL LAYOUT =================

        content.getChildren().addAll(
            banner,
            categorySection,
            recommendationSection,
            allProductsSection,
            footer
        );

        BorderPane root = new BorderPane();

        root.setTop(header);
        root.setCenter(scroll);

        root.getStyleClass().add("cd-root");

        showScene(stage, root, "A2Z - Customer Shopping");
    }

    // =====================================================
    // COMMON CUSTOMER HEADER
    // =====================================================

    public static VBox createHeader(
            Stage stage, User user,
            TextField search, Button cart) {

        HBox brand = UIComponents.brand(false);

        brand.setOnMouseClicked(e -> show(stage, user));

        brand.setStyle("-fx-cursor: hand;");

        Label customerName = new Label(user.getName());

        customerName.getStyleClass().add("cd-customer-name");

        Label role = UIComponents.mutedText("Customer");

        String initial = user.getName() == null
                || user.getName().isBlank()
                ? "C"
                : user.getName().substring(0, 1)
                      .toUpperCase(Locale.ROOT);

        Label avatar = new Label(initial);

        avatar.getStyleClass().add("cd-avatar");

        VBox profileText = new VBox(
            2, customerName, role
        );

        HBox profile = new HBox(
            8, avatar, profileText
        );

        profile.setAlignment(Pos.CENTER_LEFT);

        Button orders =
                UIComponents.secondaryButton("My Orders");

        Button logout =
                UIComponents.tertiaryButton("Logout");

        cart.setOnAction(e ->
            ShoppingCart.show(stage, user)
        );

        orders.setOnAction(e ->
            MyOrders.show(stage, user)
        );

        logout.setOnAction(e -> {

            if (UIComponents.confirm(
                    "Confirm Logout",
                    "Are you sure you want to logout?",
                    "Logout")) {

                LoginPage.show(stage);
            }
        });

        HBox mainHeader = new HBox(18);

        mainHeader.setAlignment(Pos.CENTER_LEFT);

        mainHeader.getStyleClass().add("cd-header-main");

        mainHeader.getChildren().add(brand);

        if (search != null) {

            HBox.setHgrow(search, Priority.ALWAYS);

            mainHeader.getChildren().add(search);

        } else {

            Region space = new Region();

            HBox.setHgrow(space, Priority.ALWAYS);

            mainHeader.getChildren().add(space);
        }

        mainHeader.getChildren().addAll(
            profile, cart, orders, logout
        );

        VBox header = new VBox(mainHeader);

        header.getStyleClass().add("cd-header");

        return header;
    }

    // =====================================================
    // HERO BANNER
    // =====================================================

    private static HBox createBanner(Runnable shopAction) {

        Label tag = new Label("WELCOME TO A2Z COMMERCE");

        tag.getStyleClass().add("cd-hero-tag");

        Label title = new Label(
            "Everything from A to Z."
        );

        title.getStyleClass().add("cd-hero-title");

        title.setWrapText(true);

        Label description = new Label(
            "Discover amazing products across your favourite "
            + "categories. Shop smarter with A2Z."
        );

        description.getStyleClass()
                   .add("cd-hero-description");

        description.setWrapText(true);

        description.setMaxWidth(540);

        Button shopNow =
                UIComponents.primaryButton("Shop Now");

        shopNow.getStyleClass().add("cd-hero-button");

        shopNow.setOnAction(e -> shopAction.run());

        VBox text = new VBox(
            14, tag, title, description, shopNow
        );

        text.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(text, Priority.ALWAYS);

        StackPane visual = new StackPane();

        visual.setPrefSize(300, 200);

        Label illustration = new Label("A2Z");

        illustration.getStyleClass()
                    .add("cd-hero-illustration");

        Label caption = new Label("SHOP SMARTER");

        caption.getStyleClass()
               .add("cd-hero-caption");

        VBox fallback = new VBox(
            8, illustration, caption
        );

        fallback.setAlignment(Pos.CENTER);

        visual.getChildren().add(fallback);

        if (!HERO_IMAGE_URL.isBlank()) {

            try {

                Image image = new Image(
                    HERO_IMAGE_URL, 320, 220,
                    true, true, true
                );

                ImageView view = new ImageView(image);

                view.setFitWidth(300);
                view.setFitHeight(200);
                view.setPreserveRatio(true);
                view.setVisible(false);

                visual.getChildren().add(view);

                Runnable update = () -> {

                    boolean loaded =
                        !image.isError()
                        && image.getProgress() >= 1;

                    view.setVisible(loaded);
                    fallback.setVisible(!loaded);
                };

                image.progressProperty().addListener(
                    (obs, oldValue, newValue) ->
                        update.run()
                );

                image.errorProperty().addListener(
                    (obs, oldValue, newValue) ->
                        update.run()
                );

                update.run();

            } catch (Exception ex) {

                fallback.setVisible(true);
            }
        }

        HBox banner = new HBox(24, text, visual);

        banner.setAlignment(Pos.CENTER_LEFT);

        banner.getStyleClass().add("cd-hero");

        return banner;
    }

    // =====================================================
    // CATEGORY CARD
    // =====================================================

    private static Button createCategoryCard(String name) {

        Label icon = new Label(categoryIcon(name));

        icon.getStyleClass().add("cd-category-icon");

        Label title = new Label(name);

        title.getStyleClass().add("cd-category-name");

        title.setWrapText(true);

        title.setAlignment(Pos.CENTER);

        VBox graphic = new VBox(12, icon, title);

        graphic.setAlignment(Pos.CENTER);

        Button card = new Button();

        card.setGraphic(graphic);

        card.setPrefSize(150, 120);

        card.getStyleClass().add("cd-category-card");

        return card;
    }

    private static String categoryIcon(String category) {

        String value = category.toLowerCase(Locale.ROOT);

        if (value.contains("electronic")) {
            return "💻";
        }

        if (value.contains("fashion")) {
            return "👕";
        }

        if (value.contains("home")) {
            return "🏠";
        }

        if (value.contains("book")) {
            return "📚";
        }

        if (value.contains("beauty")) {
            return "🧴";
        }

        if (value.contains("sport")) {
            return "⚽";
        }

        if (value.contains("accessor")) {
            return "🎒";
        }

        return "📦";
    }

    // =====================================================
    // PRODUCT CARD
    // =====================================================

    private static VBox createProductCard(
            Stage stage, User user,
            Product product, Button cart,
            Map<String, Image> imageCache,
            boolean recommended) {

        StackPane imageBox = createImageBox(
            product.getImagePath(),
            220, 170, imageCache
        );

        if (recommended) {

            Label badge = new Label("Recommended");

            badge.getStyleClass().add("cd-recommended-badge");

            StackPane.setAlignment(badge, Pos.TOP_LEFT);

            StackPane.setMargin(badge, new Insets(8));

            imageBox.getChildren().add(badge);
        }

        Label category = UIComponents.mutedText(
            product.getCategory() == null
            ? "Uncategorized"
            : product.getCategory()
        );

        Label name = new Label(product.getName());

        name.getStyleClass().add("cd-product-name");

        name.setWrapText(true);

        name.setMinHeight(44);
        name.setMaxHeight(44);

        name.setTooltip(new Tooltip(product.getName()));

        Label price = new Label(
            UIComponents.currency(product.getPrice())
        );

        price.getStyleClass().add("cd-product-price");

        Label status = createStockLabel(product);

        Button details =
                UIComponents.secondaryButton("View Details");

        Button addToCart =
                UIComponents.primaryButton("Add to Cart");

        details.setMaxWidth(Double.MAX_VALUE);

        addToCart.setMaxWidth(Double.MAX_VALUE);

        details.setOnAction(e ->

            ProductDetails.show(
                stage, user, product.getId()
            )
        );

        imageBox.setOnMouseClicked(e ->

            ProductDetails.show(
                stage, user, product.getId()
            )
        );

        imageBox.setStyle("-fx-cursor: hand;");

        if (product.getStock() <= 0) {

            addToCart.setDisable(true);

            addToCart.setText("Unavailable");
        }

        addToCart.setOnAction(e ->
            addProductToCart(user, product, cart)
        );

        VBox card = new VBox(
            10,
            imageBox,
            category,
            name,
            price,
            status,
            details,
            addToCart
        );

        card.getStyleClass().add("cd-product-card");

        card.setPrefWidth(250);
        card.setMinWidth(250);
        card.setMaxWidth(250);

        return card;
    }

    // =====================================================
    // STOCK STATUS
    // =====================================================

    public static Label createStockLabel(Product product) {

        Label status = new Label();

        if (!product.isActive()) {

            status.setText("Unavailable");

            status.getStyleClass().add("badge-neutral");

        } else if (product.getStock() <= 0) {

            status.setText("Out of Stock");

            status.getStyleClass().add("badge-error");

        } else if (product.getStock()
                <= product.getMinStock()) {

            status.setText(
                "Only " + product.getStock() + " left"
            );

            status.getStyleClass().add("badge-warning");

        } else {

            status.setText("In Stock");

            status.getStyleClass().add("badge-success");
        }

        return status;
    }

    // =====================================================
    // ADD PRODUCT TO CART
    // =====================================================

    public static void addProductToCart(
            User user, Product product, Button cart) {

        try {

            CartService.addToCart(
                user.getUserId(),
                product.getId()
            );

        } catch (IllegalArgumentException ex) {

            UIComponents.warning(ex.getMessage());

            return;

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to add product to cart."
            );

            ex.printStackTrace();

            return;
        }

        updateCartCount(cart, user);

        UIComponents.success(
            product.getName()
            + " added to your cart successfully!"
        );
    }

    // =====================================================
    // UPDATE CART COUNT
    // =====================================================

    public static void updateCartCount(
            Button cart, User user) {

        try {

            int count = CartService.getCartCount(
                user.getUserId()
            );

            cart.setText("Cart (" + count + ")");

        } catch (Exception ex) {

            cart.setText("Cart");

            ex.printStackTrace();
        }
    }

    // =====================================================
    // IMAGE DISPLAY
    // =====================================================

    public static StackPane createImageBox(
            String path,
            double width,
            double height,
            Map<String, Image> imageCache) {

        StackPane box = new StackPane();

        box.setPrefSize(width, height);
        box.setMinSize(width, height);
        box.setMaxSize(width, height);

        box.getStyleClass().add("cd-image-box");

        Label placeholder = new Label("Loading Image...");

        placeholder.getStyleClass()
                   .add("cd-image-placeholder");

        box.getChildren().add(placeholder);

        Image image = loadImage(
            path, width, height, imageCache
        );

        if (image == null || image.isError()) {

            placeholder.setText("Image Not Available");

            return box;
        }

        ImageView view = new ImageView(image);

        view.setFitWidth(width - 12);
        view.setFitHeight(height - 12);

        view.setPreserveRatio(true);
        view.setSmooth(true);
        view.setVisible(false);

        box.getChildren().add(view);

        Runnable update = () -> {

            if (image.isError()) {

                view.setVisible(false);

                placeholder.setText("Image Not Available");

                placeholder.setVisible(true);

            } else if (image.getProgress() >= 1.0) {

                view.setVisible(true);

                placeholder.setVisible(false);

            } else {

                view.setVisible(false);

                placeholder.setText("Loading Image...");

                placeholder.setVisible(true);
            }
        };

        image.progressProperty().addListener(
            (obs, oldValue, newValue) -> update.run()
        );

        image.errorProperty().addListener(
            (obs, oldValue, newValue) -> update.run()
        );

        update.run();

        return box;
    }

    // =====================================================
    // LOAD ONLINE OR EXISTING LOCAL IMAGES
    // =====================================================

    private static Image loadImage(
            String path,
            double width,
            double height,
            Map<String, Image> imageCache) {

        if (path == null || path.isBlank()) {
            return null;
        }

        path = path.trim();

        String key = path + "|" + width + "|" + height;

        if (imageCache.containsKey(key)) {

            return imageCache.get(key);
        }

        try {

            Image image = null;

            String lower = path.toLowerCase(Locale.ROOT);

            if (lower.startsWith("https://")
                    || lower.startsWith("http://")) {

                image = new Image(
                    path,
                    width * 2,
                    height * 2,
                    true,
                    true,
                    true
                );

            } else {

                File file = new File(path);

                if (file.isFile()) {

                    image = new Image(
                        file.toURI().toString()
                    );

                } else {

                    String resourcePath =
                        path.startsWith("/")
                        ? path
                        : "/" + path;

                    try (InputStream input =
                        CustomerDashboard.class
                            .getResourceAsStream(
                                resourcePath
                            )) {

                        if (input != null) {

                            image = new Image(input);
                        }
                    }
                }
            }

            if (image != null && !image.isError()) {

                imageCache.put(key, image);

                return image;
            }

        } catch (Exception ex) {

            System.out.println(
                "Unable to load product image."
            );
        }

        return null;
    }

    // =====================================================
    // CUSTOMER ACCESS VALIDATION
    // =====================================================

    public static boolean isCustomer(
            Stage stage, User user) {

        if (user == null
                || !"CUSTOMER".equals(user.getRole())) {

            LoginPage.show(stage);

            return false;
        }

        return true;
    }

    // =====================================================
    // COMMON CUSTOMER SCENE
    // =====================================================

    public static void showScene(
            Stage stage, BorderPane root, String title) {

        Scene scene = new Scene(root, 1440, 900);

        UIComponents.applyStyles(scene);

        stage.setTitle(title);

        stage.setScene(scene);

        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        stage.setMaximized(true);

        stage.show();
    }
}