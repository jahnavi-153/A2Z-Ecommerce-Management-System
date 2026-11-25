import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutPage {

    // Preserve delivery details when returning from payment

    public static class CheckoutData {

        String phone = "";
        String house = "";
        String street = "";
        String landmark = "";
        String city = "";
        String pin = "";
        String state;
        String address;

        String paymentMethod = "Cash on Delivery";

        BigDecimal total = BigDecimal.ZERO;

        List<CartItem> items;
    }

    public static void show(Stage stage, User user) {

        show(stage, user, new CheckoutData());
    }

    public static void show(
            Stage stage, User user, CheckoutData data) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        List<CartItem> items;

        try {

            items = CartService.getCart(user.getUserId());

        } catch (Exception ex) {

            UIComponents.error("Unable to load cart.");
            return;
        }

        if (items.isEmpty()) {

            UIComponents.warning("Your cart is empty.");

            ShoppingCart.show(stage, user);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;

        boolean validStock = true;

        for (CartItem item : items) {

            total = total.add(item.getSubtotal());

            if (!item.isActive()
                    || item.getQuantity() <= 0
                    || item.getQuantity() > item.getStock()) {

                validStock = false;
            }
        }

        data.items = items;
        data.total = total;

        // ================= DELIVERY FORM =================

        TextField name = field(
            "Customer Name", user.getName());

        name.setEditable(false);

        TextField email = field(
            "Email Address", user.getEmail());

        email.setEditable(false);

        TextField phone = field(
            "10-digit mobile number", data.phone);

        TextField house = field(
            "House / Flat number", data.house);

        TextField street = field(
            "Street / Area", data.street);

        TextField landmark = field(
            "Nearby landmark", data.landmark);

        TextField city = field(
            "City", data.city);

        TextField pin = field(
            "6-digit PIN code", data.pin);

        TextField country = field("Country", "India");

        country.setEditable(false);

        phone.setTextFormatter(
            new TextFormatter<String>(change ->
                change.getControlNewText()
                      .matches("[0-9]{0,10}")
                      ? change : null)
        );

        pin.setTextFormatter(
            new TextFormatter<String>(change ->
                change.getControlNewText()
                      .matches("[0-9]{0,6}")
                      ? change : null)
        );

        ComboBox<String> state = new ComboBox<>();

        state.getItems().addAll(
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chhattisgarh",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttar Pradesh",
            "Uttarakhand",
            "West Bengal",
            "Andaman and Nicobar Islands",
            "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi",
            "Jammu and Kashmir",
            "Ladakh",
            "Lakshadweep",
            "Puducherry"
        );

        state.setPromptText("Select State / UT");
        state.setValue(data.state);
        state.setMaxWidth(Double.MAX_VALUE);

        // ================= CONTACT INFORMATION =================

        VBox contact = UIComponents.card(18);

        contact.getChildren().addAll(

            UIComponents.cardHeading(
                "Contact Information"),

            twoColumns(
                formField("Full Name", name),
                formField("Email Address", email)
            ),

            formField("Mobile Number *", phone)
        );

        // ================= DELIVERY ADDRESS =================

        VBox delivery = UIComponents.card(18);

        delivery.getChildren().addAll(

            UIComponents.cardHeading(
                "Delivery Address"),

            formField("House / Flat Number *", house),

            formField("Street / Area *", street),

            formField("Landmark (Optional)", landmark),

            twoColumns(
                formField("City *", city),
                formField("PIN Code *", pin)
            ),

            twoColumns(
                formField("State / UT *", state),
                formField("Country", country)
            )
        );

        // ================= PAYMENT OPTIONS =================

        ToggleGroup paymentGroup = new ToggleGroup();

        RadioButton upi = paymentOption(
            "UPI (Simulation)", paymentGroup);

        RadioButton card = paymentOption(
            "Card (Simulation)", paymentGroup);

        RadioButton cod = paymentOption(
            "Cash on Delivery", paymentGroup);

        for (Toggle toggle : paymentGroup.getToggles()) {

            if (data.paymentMethod.equals(
                    toggle.getUserData())) {

                paymentGroup.selectToggle(toggle);
            }
        }

        if (paymentGroup.getSelectedToggle() == null) {
            cod.setSelected(true);
        }

        VBox payment = UIComponents.card(16);

        payment.getChildren().addAll(

            UIComponents.cardHeading(
                "Select Payment Method"),

            upi,
            card,
            cod,

            UIComponents.mutedText(
                "UPI and card payments are simulated. "
                + "No real money will be transferred.")
        );

        // ================= ERROR MESSAGE =================

        Label error = new Label();

        error.getStyleClass().add("co-error");
        error.setWrapText(true);

        if (!validStock) {

            error.setText(
                "Some products are unavailable. "
                + "Please update your cart.");
        }

        // ================= NAVIGATION =================

        Button back = UIComponents.secondaryButton(
            "← Back to Cart");

        back.setOnAction(e ->
            ShoppingCart.show(stage, user));

        Button next = UIComponents.primaryButton(
            "Continue to Payment →");

        next.setMaxWidth(Double.MAX_VALUE);
        next.setDisable(!validStock);

        next.setOnAction(e -> {

            error.setText("");

            data.phone = phone.getText().trim();
            data.house = house.getText().trim();
            data.street = street.getText().trim();
            data.landmark = landmark.getText().trim();
            data.city = city.getText().trim();
            data.pin = pin.getText().trim();
            data.state = state.getValue();

            data.paymentMethod = (String)
                paymentGroup.getSelectedToggle()
                            .getUserData();

            // Existing address validation

            String validation = AddressValidator.validate(
                data.phone,
                data.house,
                data.street,
                data.landmark,
                data.city,
                data.pin,
                data.state
            );

            if (validation != null) {

                error.setText(validation);
                return;
            }

            data.address = AddressValidator.buildAddress(
                data.phone,
                data.house,
                data.street,
                data.landmark,
                data.city,
                data.pin,
                data.state
            );

            if (data.address.length() > 500) {

                error.setText(
                    "Delivery address is too long.");

                return;
            }

            // Do not place the order yet

            PaymentPage.show(stage, user, data);
        });

        VBox left = new VBox(
            20, contact, delivery, payment, error, back
        );

        VBox summary = createSummary(data);

        summary.getChildren().add(next);

        GridPane layout = twoColumns(left, summary);

        layout.getColumnConstraints()
              .get(0).setPercentWidth(65);

        layout.getColumnConstraints()
              .get(1).setPercentWidth(35);

        GridPane.setValignment(
            summary, javafx.geometry.VPos.TOP);

        VBox content = new VBox(

            22,

            UIComponents.mutedText(
                "Home / Cart / Checkout"),

            progress(2),

            UIComponents.heading("Checkout"),

            UIComponents.mutedText(
                "Enter your delivery information "
                + "to complete your purchase."),

            layout
        );

        showPage(
            stage, user, content, "A2Z - Checkout");
    }

    // =====================================================
    // SHARED CHECKOUT PROGRESS INDICATOR
    // =====================================================

    public static HBox progress(int currentStep) {

        String[] steps = {
            "Cart",
            "Delivery Details",
            "Payment",
            "Confirmation"
        };

        HBox row = new HBox(12);

        row.setAlignment(Pos.CENTER);

        row.getStyleClass().add("co-progress");

        for (int i = 0; i < steps.length; i++) {

            int number = i + 1;

            Label circle = new Label(
                number < currentStep
                    ? "✓"
                    : String.valueOf(number)
            );

            circle.getStyleClass().add("co-step-circle");

            Label title = new Label(steps[i]);

            title.getStyleClass().add("co-step-label");

            if (number < currentStep) {

                circle.getStyleClass().add(
                    "co-step-complete");

            } else if (number == currentStep) {

                circle.getStyleClass().add(
                    "co-step-active");

                title.getStyleClass().add(
                    "co-step-label-active");

            } else {

                circle.getStyleClass().add(
                    "co-step-pending");
            }

            HBox step = new HBox(8, circle, title);

            step.setAlignment(Pos.CENTER);

            row.getChildren().add(step);

            if (i < steps.length - 1) {

                Region line = new Region();

                line.getStyleClass().add(
                    number < currentStep
                        ? "co-line-complete"
                        : "co-line-pending"
                );

                HBox.setHgrow(line, Priority.ALWAYS);

                row.getChildren().add(line);
            }
        }

        return row;
    }

    // =====================================================
    // SHARED ORDER SUMMARY
    // =====================================================

    public static VBox createSummary(CheckoutData data) {

        VBox summary = UIComponents.card(16);

        summary.getChildren().addAll(

            UIComponents.cardHeading("Order Summary"),

            new Separator()
        );

        long quantity = 0;

        for (CartItem item : data.items) {

            Label name = new Label(
                item.getProductName()
                + " × " + item.getQuantity());

            name.setWrapText(true);
            name.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(name, Priority.ALWAYS);

            Label amount = new Label(
                UIComponents.currency(item.getSubtotal()));

            amount.getStyleClass().add("co-summary-value");

            HBox product = new HBox(12, name, amount);

            product.setAlignment(Pos.CENTER_LEFT);

            summary.getChildren().add(product);

            quantity += item.getQuantity();
        }

        Label total = new Label(
            UIComponents.currency(data.total));

        total.getStyleClass().add("sc-grand-total");

        VBox totalPanel = new VBox(
            8, new Label("Grand Total"), total);

        totalPanel.getStyleClass().add("sc-total-panel");

        summary.getChildren().addAll(

            new Separator(),

            summaryRow(
                "Total Items", String.valueOf(quantity)),

            summaryRow(
                "Subtotal",
                UIComponents.currency(data.total)),

            summaryRow("Delivery Charges", "FREE"),

            new Separator(),

            totalPanel,

            UIComponents.mutedText(
                "Stock and price are verified "
                + "during order placement.")
        );

        return summary;
    }

    // =====================================================
    // COMMON SUMMARY ROW
    // =====================================================

    public static HBox summaryRow(
            String title, String value) {

        Label label = UIComponents.mutedText(title);

        Label amount = new Label(value);

        amount.setWrapText(true);

        amount.getStyleClass().add("co-summary-value");

        Region space = new Region();

        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(
            12, label, space, amount);

        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    // =====================================================
    // COMMON CUSTOMER PAGE LAYOUT
    // =====================================================

    public static void showPage(
            Stage stage,
            User user,
            VBox content,
            String title) {

        Button cart = UIComponents.secondaryButton("Cart");

        CustomerDashboard.updateCartCount(cart, user);

        VBox header = CustomerDashboard.createHeader(
            stage, user, null, cart);

        content.setPadding(
            new Insets(24, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER);

        scroll.getStyleClass().add("cd-scroll");

        BorderPane root = new BorderPane();

        root.setTop(header);
        root.setCenter(scroll);

        root.getStyleClass().add("cd-root");

        CustomerDashboard.showScene(
            stage, root, title);
    }

    // =====================================================
    // FORM HELPERS
    // =====================================================

    private static TextField field(
            String prompt, String value) {

        TextField field = new TextField(value);

        field.setPromptText(prompt);

        field.setMaxWidth(Double.MAX_VALUE);

        return field;
    }

    private static VBox formField(
            String title,
            javafx.scene.Node control) {

        Label label = new Label(title);

        label.getStyleClass().add("co-field-label");

        return new VBox(7, label, control);
    }

    public static GridPane twoColumns(
            javafx.scene.Node left,
            javafx.scene.Node right) {

        GridPane grid = new GridPane();

        grid.setHgap(22);
        grid.setVgap(16);

        ColumnConstraints first =
            new ColumnConstraints();

        first.setPercentWidth(50);
        first.setHgrow(Priority.ALWAYS);

        ColumnConstraints second =
            new ColumnConstraints();

        second.setPercentWidth(50);
        second.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(
            first, second);

        grid.add(left, 0, 0);
        grid.add(right, 1, 0);

        return grid;
    }

    private static RadioButton paymentOption(
            String text, ToggleGroup group) {

        RadioButton button = new RadioButton(text);

        button.setToggleGroup(group);
        button.setUserData(text);

        button.setMaxWidth(Double.MAX_VALUE);

        button.getStyleClass().add("co-payment-option");

        return button;
    }
}