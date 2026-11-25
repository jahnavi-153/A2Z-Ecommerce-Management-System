import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class PaymentPage {

    public static void show(
            Stage stage,
            User user,
            CheckoutPage.CheckoutData data) {

        if (!CustomerDashboard.isCustomer(stage, user)) {
            return;
        }

        if (data == null
                || data.address == null
                || data.items == null
                || data.items.isEmpty()) {

            CheckoutPage.show(stage, user);
            return;
        }

        // ================= SIMULATION NOTICE =================

        Label noticeTitle = new Label(
            "Academic Payment Simulation");

        noticeTitle.getStyleClass().add(
            "pay-notice-title");

        Label noticeText = new Label(
            "This application does not process real "
            + "payments. Do not enter actual banking "
            + "or payment credentials.");

        noticeText.setWrapText(true);

        VBox notice = new VBox(
            7, noticeTitle, noticeText);

        notice.getStyleClass().add("pay-notice");

        // ================= PAYMENT METHOD SELECTION =================

        ToggleGroup group = new ToggleGroup();

        ToggleButton upi = methodButton(
            "UPI", "UPI (Simulation)", group);

        ToggleButton card = methodButton(
            "Card", "Card (Simulation)", group);

        ToggleButton cod = methodButton(
            "Cash on Delivery",
            "Cash on Delivery", group);

        HBox methods = new HBox(
            10, upi, card, cod);

        for (Toggle toggle : group.getToggles()) {

            if (data.paymentMethod.equals(
                    toggle.getUserData())) {

                group.selectToggle(toggle);
            }
        }

        if (group.getSelectedToggle() == null) {
            cod.setSelected(true);
        }

        // ================= PAYMENT DETAILS =================

        VBox methodContent = new VBox(16);

        Label error = new Label();

        error.getStyleClass().add("co-error");
        error.setWrapText(true);

        Button place = UIComponents.primaryButton(
            "Confirm Order");

        place.setMaxWidth(Double.MAX_VALUE);

        Runnable updatePayment = () -> {

            Toggle selected = group.getSelectedToggle();

            if (selected == null) {
                return;
            }

            data.paymentMethod =
                (String) selected.getUserData();

            methodContent.getChildren().clear();

            error.setText("");

            if ("UPI (Simulation)".equals(
                    data.paymentMethod)) {

                Label icon = new Label("UPI");

                icon.getStyleClass().add(
                    "pay-method-icon");

                Label title = UIComponents.cardHeading(
                    "UPI Payment Simulation");

                Label description =
                    UIComponents.mutedText(
                        "Simulate a successful UPI "
                        + "payment for your order.");

                Label amount = new Label(
                    UIComponents.currency(data.total));

                amount.getStyleClass().add(
                    "pay-amount");

                Label demo = new Label(
                    "DEMO UPI TRANSACTION");

                demo.getStyleClass().add(
                    "pay-demo-box");

                methodContent.getChildren().addAll(
                    icon,
                    title,
                    description,
                    demo,
                    amount
                );

                place.setText(
                    "Simulate UPI Payment & Place Order");

            } else if ("Card (Simulation)".equals(
                    data.paymentMethod)) {

                Label title = UIComponents.cardHeading(
                    "Card Payment Simulation");

                Label cardNumber = new Label(
                    "••••  ••••  ••••  DEMO");

                cardNumber.getStyleClass().add(
                    "pay-card-number");

                Label cardHolder = new Label(
                    "DEMONSTRATION CARD");

                cardHolder.getStyleClass().add(
                    "pay-card-caption");

                VBox demoCard = new VBox(

                    22,

                    new Label("A2Z DEMO"),

                    cardNumber,

                    cardHolder
                );

                demoCard.getStyleClass().add(
                    "pay-demo-card");

                methodContent.getChildren().addAll(

                    title,

                    demoCard,

                    UIComponents.mutedText(
                        "A demonstration card is used "
                        + "automatically. No real card "
                        + "information is required.")
                );

                place.setText(
                    "Simulate Card Payment & Place Order");

            } else {

                Label icon = new Label("COD");

                icon.getStyleClass().add(
                    "pay-method-icon");

                Label title = UIComponents.cardHeading(
                    "Cash on Delivery");

                Label description =
                    UIComponents.mutedText(
                        "Your order will be placed with "
                        + "payment pending until delivery.");

                methodContent.getChildren().addAll(
                    icon,
                    title,
                    description
                );

                place.setText(
                    "Confirm Cash on Delivery Order");
            }
        };

        group.selectedToggleProperty().addListener(
            (obs, oldValue, newValue) -> {

                if (newValue == null) {

                    group.selectToggle(oldValue);

                } else {

                    updatePayment.run();
                }
            }
        );

        updatePayment.run();

        // ================= PLACE ORDER =================

        place.setOnAction(e -> {

            error.setText("");

            boolean confirmed = UIComponents.confirm(

                "Confirm Order",

                "Total: "
                + UIComponents.currency(data.total)

                + "\nPayment: "
                + data.paymentMethod

                + "\n\nDelivery Address:\n"
                + data.address,

                "Place Order"
            );

            if (!confirmed) {
                return;
            }

            // Prevent repeated submission

            place.setDisable(true);

            methods.setDisable(true);

            int orderId;

            try {

                // Existing transactional backend

                orderId = OrderService.placeOrder(

                    user.getUserId(),

                    data.address,

                    data.paymentMethod,

                    data.total
                );

            } catch (IllegalArgumentException ex) {

                error.setText(ex.getMessage());

                place.setDisable(false);
                methods.setDisable(false);

                return;

            } catch (Exception ex) {

                error.setText(
                    "Unable to confirm order placement. "
                    + "Check My Orders before trying again.");

                methods.setDisable(false);

                ex.printStackTrace();

                return;
            }

            // OrderService has successfully committed

            OrderConfirmation.show(
                stage, user, orderId, data);
        });

        // ================= PAYMENT PANEL =================

        VBox paymentPanel = UIComponents.card(22);

        paymentPanel.getChildren().addAll(

            UIComponents.cardHeading("Payment Method"),

            methods,

            new Separator(),

            methodContent,

            error
        );

        // ================= DELIVERY ADDRESS PREVIEW =================

        Label address = new Label(data.address);

        address.setWrapText(true);

        Button edit = UIComponents.tertiaryButton(
            "Edit Delivery Details");

        edit.setOnAction(e ->
            CheckoutPage.show(stage, user, data));

        VBox deliveryPanel = UIComponents.card(14);

        deliveryPanel.getChildren().addAll(

            UIComponents.cardHeading("Delivering To"),

            new Label(user.getName()),

            address,

            edit
        );

        Button back = UIComponents.secondaryButton(
            "← Back to Delivery Details");

        back.setOnAction(e ->
            CheckoutPage.show(stage, user, data));

        VBox left = new VBox(
            20, notice, paymentPanel, deliveryPanel, back);

        VBox summary = CheckoutPage.createSummary(data);

        summary.getChildren().add(place);

        GridPane layout =
            CheckoutPage.twoColumns(left, summary);

        layout.getColumnConstraints()
              .get(0).setPercentWidth(65);

        layout.getColumnConstraints()
              .get(1).setPercentWidth(35);

        GridPane.setValignment(
            summary, javafx.geometry.VPos.TOP);

        VBox content = new VBox(

            22,

            UIComponents.mutedText(
                "Home / Cart / Checkout / Payment"),

            CheckoutPage.progress(3),

            UIComponents.heading("Payment"),

            UIComponents.mutedText(
                "Review your payment method "
                + "and confirm your order."),

            layout
        );

        CheckoutPage.showPage(
            stage, user, content, "A2Z - Payment");
    }

    // ================= PAYMENT BUTTON HELPER =================

    private static ToggleButton methodButton(
            String text,
            String value,
            ToggleGroup group) {

        ToggleButton button = new ToggleButton(text);

        button.setUserData(value);

        button.setToggleGroup(group);

        button.setMaxWidth(Double.MAX_VALUE);

        button.getStyleClass().add("pay-method-button");

        HBox.setHgrow(button, Priority.ALWAYS);

        return button;
    }
}