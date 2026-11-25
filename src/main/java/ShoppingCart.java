import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {

    public static void show(Stage stage, User user) {

        // Customer access validation

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        // ================= HEADER =================

        Button cartButton =
                UIComponents.secondaryButton("Cart");

        CustomerDashboard.updateCartCount(
            cartButton, user
        );

        VBox header = CustomerDashboard.createHeader(
            stage, user, null, cartButton
        );

        // ================= PAGE HEADING =================

        Label breadcrumb = UIComponents.mutedText(
            "Home / Shopping Cart"
        );

        Label heading =
                UIComponents.heading("Shopping Cart");

        Label description = UIComponents.mutedText(
            "Review your products before checkout."
        );

        VBox headingBox = new VBox(
            6, breadcrumb, heading, description
        );

        Button refresh =
                UIComponents.secondaryButton("Refresh");

        refresh.setOnAction(e -> show(stage, user));

        Region headingSpace = new Region();

        HBox.setHgrow(
            headingSpace, Priority.ALWAYS
        );

        HBox pageHeading = new HBox(
            15, headingBox, headingSpace, refresh
        );

        pageHeading.setAlignment(Pos.CENTER_LEFT);

        // ================= CONTINUE SHOPPING =================

        Button back = UIComponents.tertiaryButton(
            "← Continue Shopping"
        );

        back.setOnAction(e ->
            CustomerDashboard.show(stage, user)
        );

        // ================= MAIN CONTENT =================

        VBox content = new VBox(24);

        content.setPadding(
            new Insets(24, 32, 32, 32)
        );

        content.getChildren().add(pageHeading);

        try {

            // Load cart items from MySQL

            ObservableList<CartItem> items =
                CartService.getCart(user.getUserId());

            if (items.isEmpty()) {

                content.getChildren().add(
                    createEmptyCart(stage, user)
                );

            } else {

                // Load product information for images
                // and categories

                Map<Integer, Product> productMap =
                        new HashMap<>();

                try {

                    for (Product product :
                            ProductService.getProducts()) {

                        productMap.put(
                            product.getId(), product
                        );
                    }

                } catch (Exception ex) {

                    // Cart remains usable even if
                    // product images cannot be loaded

                    ex.printStackTrace();
                }

                Map<String, Image> imageCache =
                        new HashMap<>();

                // Calculate total and validate stock

                BigDecimal total = BigDecimal.ZERO;

                long totalQuantity = 0;

                boolean valid = true;

                for (CartItem item : items) {

                    total = total.add(
                        item.getSubtotal()
                    );

                    totalQuantity += item.getQuantity();

                    if (!item.isActive()
                            || item.getQuantity() < 1
                            || item.getQuantity()
                               > item.getStock()) {

                        valid = false;
                    }
                }

                // ================= CART ITEMS PANEL =================

                Label itemsHeading =
                    UIComponents.cardHeading(
                        "Cart Items (" + items.size() + ")"
                    );

                Button clear =
                    UIComponents.destructiveButton(
                        "Clear Cart"
                    );

                clear.getStyleClass().add(
                    "sc-clear-button"
                );

                clear.setOnAction(e ->
                    clearCart(stage, user)
                );

                Region itemSpace = new Region();

                HBox.setHgrow(
                    itemSpace, Priority.ALWAYS
                );

                HBox itemsHeader = new HBox(
                    12, itemsHeading, itemSpace, clear
                );

                itemsHeader.setAlignment(
                    Pos.CENTER_LEFT
                );

                VBox itemsPanel = new VBox(18);

                itemsPanel.getStyleClass().add(
                    "sc-items-panel"
                );

                itemsPanel.getChildren().addAll(
                    itemsHeader,
                    new Separator()
                );

                for (int i = 0; i < items.size(); i++) {

                    CartItem item = items.get(i);

                    Product product = productMap.get(
                        item.getProductId()
                    );

                    itemsPanel.getChildren().add(
                        createItemCard(
                            stage,
                            user,
                            item,
                            product,
                            imageCache
                        )
                    );

                    if (i < items.size() - 1) {

                        itemsPanel.getChildren().add(
                            new Separator()
                        );
                    }
                }

                // ================= ORDER SUMMARY =================

                VBox summary = createOrderSummary(
                    stage,
                    user,
                    items.size(),
                    totalQuantity,
                    total,
                    valid
                );

                // ================= STOCK WARNING =================

                if (!valid) {

                    Label warningTitle = new Label(
                        "Some products require attention"
                    );

                    warningTitle.getStyleClass().add(
                        "sc-warning-title"
                    );

                    Label warningText = new Label(
                        "Remove unavailable products or "
                        + "reduce quantities to the "
                        + "available stock before checkout."
                    );

                    warningText.setWrapText(true);

                    warningText.getStyleClass().add(
                        "sc-warning-text"
                    );

                    VBox warning = new VBox(
                        6, warningTitle, warningText
                    );

                    warning.getStyleClass().add(
                        "sc-warning-panel"
                    );

                    content.getChildren().add(warning);
                }

                // ================= TWO COLUMN LAYOUT =================

                VBox leftSection = new VBox(
                    18, itemsPanel, back
                );

                GridPane layout = new GridPane();

                layout.setHgap(24);
                layout.setVgap(20);

                ColumnConstraints left =
                        new ColumnConstraints();

                left.setPercentWidth(65);

                left.setHgrow(Priority.ALWAYS);

                ColumnConstraints right =
                        new ColumnConstraints();

                right.setPercentWidth(35);

                right.setHgrow(Priority.ALWAYS);

                layout.getColumnConstraints().addAll(
                    left, right
                );

                layout.add(leftSection, 0, 0);
                layout.add(summary, 1, 0);

                GridPane.setValignment(
                    summary,
                    javafx.geometry.VPos.TOP
                );

                content.getChildren().add(layout);
            }

        } catch (Exception ex) {

            content.getChildren().addAll(
                UIComponents.emptyState(
                    "Unable to load shopping cart",
                    "Please refresh and try again."
                ),
                back
            );

            ex.printStackTrace();
        }

        // ================= FINAL SCENE =================

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER
        );

        scroll.getStyleClass().add("cd-scroll");

        BorderPane root = new BorderPane();

        root.setTop(header);
        root.setCenter(scroll);

        root.getStyleClass().add("cd-root");

        CustomerDashboard.showScene(
            stage, root, "A2Z - Shopping Cart"
        );
    }

    // =====================================================
    // INDIVIDUAL CART ITEM
    // =====================================================

    private static HBox createItemCard(
            Stage stage,
            User user,
            CartItem item,
            Product product,
            Map<String, Image> imageCache) {

        // ================= PRODUCT IMAGE =================

        String imagePath = product == null
                ? null
                : product.getImagePath();

        StackPane image =
            CustomerDashboard.createImageBox(
                imagePath, 125, 125, imageCache
            );

        if (item.isActive()) {

            image.setStyle("-fx-cursor: hand;");

            image.setOnMouseClicked(e ->
                ProductDetails.show(
                    stage,
                    user,
                    item.getProductId()
                )
            );
        }

        // ================= PRODUCT INFORMATION =================

        Label category = UIComponents.mutedText(
            product == null
            || product.getCategory() == null
                ? "Product"
                : product.getCategory()
        );

        Label name = new Label(
            item.getProductName()
        );

        name.getStyleClass().add("sc-product-name");

        name.setWrapText(true);

        Label price = new Label(
            UIComponents.currency(item.getPrice())
            + " / unit"
        );

        price.getStyleClass().add("sc-unit-price");

        Label stock = createStockLabel(item);

        // ================= QUANTITY CONTROLS =================

        Button decrease =
                UIComponents.secondaryButton("−");

        Button increase =
                UIComponents.secondaryButton("+");

        decrease.getStyleClass().add(
            "sc-quantity-button"
        );

        increase.getStyleClass().add(
            "sc-quantity-button"
        );

        Label quantity = new Label(
            String.valueOf(item.getQuantity())
        );

        quantity.getStyleClass().add(
            "sc-quantity-value"
        );

        // Minimum quantity is 1

        decrease.setDisable(
            item.getQuantity() <= 1
        );

        // Cannot increase unavailable products
        // or exceed available stock

        increase.setDisable(
            !item.isActive()
            || item.getQuantity() >= item.getStock()
        );

        decrease.setOnAction(e ->
            updateQuantity(stage, user, item, -1)
        );

        increase.setOnAction(e ->
            updateQuantity(stage, user, item, 1)
        );

        HBox quantityControls = new HBox(
            8, decrease, quantity, increase
        );

        quantityControls.setAlignment(
            Pos.CENTER_LEFT
        );

        Label quantityLabel =
                UIComponents.mutedText("Quantity");

        HBox quantityRow = new HBox(
            12, quantityLabel, quantityControls
        );

        quantityRow.setAlignment(Pos.CENTER_LEFT);

        VBox information = new VBox(
            8,
            category,
            name,
            price,
            stock,
            quantityRow
        );

        information.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(
            information, Priority.ALWAYS
        );

        // ================= SUBTOTAL AND REMOVE =================

        Label subtotalTitle =
                UIComponents.mutedText("Subtotal");

        Label subtotal = new Label(
            UIComponents.currency(item.getSubtotal())
        );

        subtotal.getStyleClass().add(
            "sc-item-subtotal"
        );

        Button remove =
                UIComponents.tertiaryButton("Remove");

        remove.getStyleClass().add(
            "sc-remove-button"
        );

        remove.setOnAction(e ->
            removeItem(stage, user, item)
        );

        VBox amountBox = new VBox(
            12, subtotalTitle, subtotal, remove
        );

        amountBox.setAlignment(Pos.CENTER_RIGHT);

        // ================= FINAL ITEM CARD =================

        HBox card = new HBox(
            18, image, information, amountBox
        );

        card.setAlignment(Pos.CENTER_LEFT);

        card.getStyleClass().add("sc-item-card");

        return card;
    }

    // =====================================================
    // PRODUCT STOCK STATUS
    // =====================================================

    private static Label createStockLabel(
            CartItem item) {

        Label label = new Label();

        if (!item.isActive()) {

            label.setText("Unavailable");

            label.getStyleClass().add("badge-error");

        } else if (item.getStock() <= 0) {

            label.setText("Out of Stock");

            label.getStyleClass().add("badge-error");

        } else if (item.getQuantity()
                > item.getStock()) {

            label.setText(
                "Only " + item.getStock()
                + " units available"
            );

            label.getStyleClass().add("badge-warning");

        } else {

            label.setText(
                "In Stock (" + item.getStock()
                + " available)"
            );

            label.getStyleClass().add("badge-success");
        }

        label.setWrapText(true);

        return label;
    }

    // =====================================================
    // ORDER SUMMARY
    // =====================================================

    private static VBox createOrderSummary(
            Stage stage,
            User user,
            int productCount,
            long totalQuantity,
            BigDecimal total,
            boolean valid) {

        Label title =
                UIComponents.cardHeading(
                    "Order Summary"
                );

        HBox productsRow = summaryRow(
            "Different Products",
            String.valueOf(productCount)
        );

        HBox quantityRow = summaryRow(
            "Total Items",
            String.valueOf(totalQuantity)
        );

        HBox subtotalRow = summaryRow(
            "Subtotal",
            UIComponents.currency(total)
        );

        HBox deliveryRow = summaryRow(
            "Delivery",
            "Free"
        );

        Label totalTitle = new Label("Grand Total");

        totalTitle.getStyleClass().add(
            "sc-total-title"
        );

        Label totalValue = new Label(
            UIComponents.currency(total)
        );

        totalValue.getStyleClass().add(
            "sc-grand-total"
        );

        VBox totalBox = new VBox(
            8, totalTitle, totalValue
        );

        totalBox.getStyleClass().add(
            "sc-total-panel"
        );

        // ================= CHECKOUT BUTTON =================

        Button checkout =
            UIComponents.primaryButton(
                "Proceed to Checkout →"
            );

        checkout.setMaxWidth(Double.MAX_VALUE);

        checkout.getStyleClass().add(
            "sc-checkout-button"
        );

        checkout.setDisable(!valid);

        checkout.setOnAction(e ->
            proceedToCheckout(stage, user)
        );

        Button continueShopping =
            UIComponents.secondaryButton(
                "Continue Shopping"
            );

        continueShopping.setMaxWidth(
            Double.MAX_VALUE
        );

        continueShopping.setOnAction(e ->
            CustomerDashboard.show(stage, user)
        );

        Label note = UIComponents.mutedText(
            "Product availability and stock will "
            + "be verified before order placement."
        );

        note.setWrapText(true);

        VBox summary = new VBox(
            18,
            title,
            new Separator(),
            productsRow,
            quantityRow,
            subtotalRow,
            deliveryRow,
            new Separator(),
            totalBox,
            checkout,
            continueShopping,
            note
        );

        summary.getStyleClass().add(
            "sc-summary-panel"
        );

        summary.setMaxWidth(Double.MAX_VALUE);

        return summary;
    }

    // =====================================================
    // SUMMARY ROW
    // =====================================================

    private static HBox summaryRow(
            String title, String value) {

        Label name = UIComponents.mutedText(title);

        Label amount = new Label(value);

        amount.getStyleClass().add(
            "sc-summary-value"
        );

        Region space = new Region();

        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(
            12, name, space, amount
        );

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // =====================================================
    // UPDATE QUANTITY
    // =====================================================

    private static void updateQuantity(
            Stage stage,
            User user,
            CartItem item,
            int change) {

        try {

            CartService.changeQuantity(
                user.getUserId(),
                item.getProductId(),
                change
            );

            // Reload latest quantity and total

            show(stage, user);

        } catch (IllegalArgumentException ex) {

            UIComponents.warning(ex.getMessage());

            show(stage, user);

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to update quantity."
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // REMOVE INDIVIDUAL PRODUCT
    // =====================================================

    private static void removeItem(
            Stage stage,
            User user,
            CartItem item) {

        boolean confirmed = UIComponents.confirm(
            "Remove Product",
            "Remove " + item.getProductName()
            + " from your shopping cart?",
            "Remove"
        );

        if (!confirmed) {
            return;
        }

        try {

            CartService.removeItem(
                user.getUserId(),
                item.getProductId()
            );

            show(stage, user);

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to remove product."
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // CLEAR COMPLETE CART
    // =====================================================

    private static void clearCart(
            Stage stage, User user) {

        boolean confirmed = UIComponents.confirm(
            "Clear Shopping Cart",
            "Are you sure you want to remove "
            + "all products from your cart?",
            "Clear Cart"
        );

        if (!confirmed) {
            return;
        }

        try {

            CartService.clearCart(
                user.getUserId()
            );

            show(stage, user);

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to clear shopping cart."
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // CHECKOUT VALIDATION
    // =====================================================

    private static void proceedToCheckout(
            Stage stage, User user) {

        try {

            // Reload cart to check latest stock

            ObservableList<CartItem> items =
                CartService.getCart(user.getUserId());

            if (items.isEmpty()) {

                UIComponents.warning(
                    "Your shopping cart is empty."
                );

                show(stage, user);

                return;
            }

            for (CartItem item : items) {

                if (!item.isActive()) {

                    UIComponents.warning(
                        item.getProductName()
                        + " is no longer available."
                    );

                    show(stage, user);

                    return;
                }

                if (item.getQuantity() < 1
                        || item.getQuantity()
                           > item.getStock()) {

                    UIComponents.warning(
                        "Insufficient stock for "
                        + item.getProductName()
                        + ". Available stock: "
                        + item.getStock()
                    );

                    show(stage, user);

                    return;
                }
            }

            // Open existing checkout page

            CheckoutPage.show(stage, user);

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to proceed to checkout."
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // EMPTY CART STATE
    // =====================================================

    private static VBox createEmptyCart(
            Stage stage, User user) {

        Label icon = new Label("🛒");

        icon.getStyleClass().add(
            "sc-empty-icon"
        );

        Label title = UIComponents.heading(
            "Your cart is empty"
        );

        Label description = UIComponents.mutedText(
            "Explore our collection and add your "
            + "favourite products to the cart."
        );

        description.setWrapText(true);

        description.setTextAlignment(
            javafx.scene.text.TextAlignment.CENTER
        );

        Button shop =
                UIComponents.primaryButton(
                    "Start Shopping"
                );

        shop.setOnAction(e ->
            CustomerDashboard.show(stage, user)
        );

        VBox empty = new VBox(
            18,
            icon,
            title,
            description,
            shop
        );

        empty.setAlignment(Pos.CENTER);

        empty.getStyleClass().add(
            "sc-empty-panel"
        );

        return empty;
    }
}