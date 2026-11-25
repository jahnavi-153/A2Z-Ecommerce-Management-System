import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

public class ProductDetails {

    public static void show(
            Stage stage, User user, int productId) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        // Load the latest product information

        Product selected = null;

        try {

            for (Product p : ProductService.getProducts()) {

                if (p.getId() == productId) {

                    selected = p;

                    break;
                }
            }

        } catch (Exception ex) {

            UIComponents.error(
                "Unable to load product details."
            );

            ex.printStackTrace();

            return;
        }

        if (selected == null || !selected.isActive()) {

            UIComponents.warning(
                "This product is no longer available."
            );

            CustomerDashboard.show(stage, user);

            return;
        }

        final Product product = selected;

        Map<String, Image> imageCache = new HashMap<>();

        // ================= HEADER =================

        Button cart =
                UIComponents.secondaryButton("Cart");

        CustomerDashboard.updateCartCount(cart, user);

        VBox header = CustomerDashboard.createHeader(
            stage, user, null, cart
        );

        // ================= BACK BUTTON =================

        Button back = UIComponents.tertiaryButton(
            "← Back to Shopping"
        );

        back.setOnAction(e ->
            CustomerDashboard.show(stage, user)
        );

        Label breadcrumb = UIComponents.mutedText(
            "Home / "
            + (product.getCategory() == null
                ? "Products"
                : product.getCategory())
            + " / " + product.getName()
        );

        // ================= PRODUCT IMAGE =================

        StackPane imageBox =
            CustomerDashboard.createImageBox(
                product.getImagePath(),
                460,
                400,
                imageCache
            );

        Label imageCaption = UIComponents.mutedText(
            "Product Image"
        );

        VBox imageSection = new VBox(
            12, imageBox, imageCaption
        );

        imageSection.setAlignment(Pos.CENTER);

        imageSection.getStyleClass()
                    .add("pd-image-section");

        // ================= PRODUCT INFORMATION =================

        Label category = new Label(
            product.getCategory() == null
            ? "Uncategorized"
            : product.getCategory()
        );

        category.getStyleClass().add("pd-category");

        Label name = new Label(product.getName());

        name.getStyleClass().add("pd-product-name");

        name.setWrapText(true);

        Label productCode = UIComponents.mutedText(
            "Product ID: #" + product.getId()
        );

        Label price = new Label(
            UIComponents.currency(product.getPrice())
        );

        price.getStyleClass().add("pd-price");

        Label status =
            CustomerDashboard.createStockLabel(product);

        Label availableStock = UIComponents.mutedText(
            product.getStock() + " units available"
        );

        HBox stockRow = new HBox(
            12, status, availableStock
        );

        stockRow.setAlignment(Pos.CENTER_LEFT);

        // ================= DESCRIPTION =================

        Label descriptionTitle =
            UIComponents.cardHeading("Product Description");

        String descriptionText =
            product.getDescription() == null
            || product.getDescription().isBlank()

            ? "No description available."

            : product.getDescription();

        Label description = new Label(descriptionText);

        description.setWrapText(true);

        description.getStyleClass().add("pd-description");

        // ================= SHOPPING ACTIONS =================

        Button addToCart =
                UIComponents.primaryButton("Add to Cart");

        addToCart.setMaxWidth(Double.MAX_VALUE);

        addToCart.getStyleClass().add("pd-action-button");

        if (product.getStock() <= 0) {

            addToCart.setDisable(true);

            addToCart.setText("Out of Stock");
        }

        addToCart.setOnAction(e ->

            CustomerDashboard.addProductToCart(
                user, product, cart
            )
        );

        Button viewCart =
                UIComponents.secondaryButton("View Cart");

        viewCart.setMaxWidth(Double.MAX_VALUE);

        viewCart.getStyleClass().add("pd-action-button");

        viewCart.setOnAction(e ->
            ShoppingCart.show(stage, user)
        );

        Button continueShopping =
            UIComponents.tertiaryButton(
                "Continue Shopping"
            );

        continueShopping.setOnAction(e ->
            CustomerDashboard.show(stage, user)
        );

        VBox actions = new VBox(
            12,
            addToCart,
            viewCart,
            continueShopping
        );

        // ================= PRODUCT INFORMATION PANEL =================

        VBox information = new VBox(
            16,
            category,
            name,
            productCode,
            price,
            stockRow,
            new Separator(),
            descriptionTitle,
            description,
            new Separator(),
            actions
        );

        information.setAlignment(Pos.TOP_LEFT);

        information.setMaxWidth(Double.MAX_VALUE);

        information.getStyleClass()
                   .add("pd-information");

        // ================= PRODUCT CARD =================

        GridPane productCard = new GridPane();

        productCard.setHgap(35);

        productCard.setVgap(20);

        productCard.getStyleClass().add("pd-card");

        ColumnConstraints left =
                new ColumnConstraints();

        left.setPercentWidth(48);

        left.setHgrow(Priority.ALWAYS);

        ColumnConstraints right =
                new ColumnConstraints();

        right.setPercentWidth(52);

        right.setHgrow(Priority.ALWAYS);

        productCard.getColumnConstraints().addAll(
            left, right
        );

        productCard.add(imageSection, 0, 0);

        productCard.add(information, 1, 0);

        // ================= ADDITIONAL INFORMATION =================

        VBox feature1 = createFeature(
            "Product Availability",
            "Stock is verified before adding products to cart."
        );

        VBox feature2 = createFeature(
            "Order Tracking",
            "Track your order through the My Orders section."
        );

        VBox feature3 = createFeature(
            "Smart Shopping",
            "Discover more products through personalized recommendations."
        );

        HBox features = new HBox(
            16, feature1, feature2, feature3
        );

        // ================= FINAL LAYOUT =================

        VBox content = new VBox(
            20,
            back,
            breadcrumb,
            productCard,
            features
        );

        content.setPadding(
            new Insets(24, 32, 32, 32)
        );

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
            stage, root, "A2Z - Product Details"
        );
    }

    private static VBox createFeature(
            String title, String description) {

        Label heading = UIComponents.cardHeading(title);

        Label text = UIComponents.mutedText(description);

        text.setWrapText(true);

        VBox card = new VBox(
            8, heading, text
        );

        card.getStyleClass().add("pd-feature-card");

        card.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }
}