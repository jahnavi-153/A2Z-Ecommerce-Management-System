import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class UIComponents {

    // Apply common stylesheet

    public static void applyStyles(Scene scene) {

        String css = UIComponents.class
                .getResource("/styles/a2z-javafx.css")
                .toExternalForm();

        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }


    // Primary button

    public static Button primaryButton(String text) {

        Button button = new Button(text);
        button.getStyleClass().add("btn-primary");

        return button;
    }


    // Secondary button

    public static Button secondaryButton(String text) {

        Button button = new Button(text);
        button.getStyleClass().add("btn-secondary");

        return button;
    }


    // Tertiary button

    public static Button tertiaryButton(String text) {

        Button button = new Button(text);
        button.getStyleClass().add("btn-tertiary");

        return button;
    }


    // Destructive button

    public static Button destructiveButton(String text) {

        Button button = new Button(text);
        button.getStyleClass().add("btn-destructive");

        return button;
    }


    // Main page heading

    public static Label heading(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("headline-lg");

        return label;
    }


    // Section heading

    public static Label sectionHeading(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("headline-md");

        return label;
    }


    // Card heading

    public static Label cardHeading(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("title-lg");

        return label;
    }


    // Secondary text

    public static Label mutedText(String text) {

        Label label = new Label(text);
        label.getStyleClass().add("text-muted");
        label.setWrapText(true);

        return label;
    }


    // Common white card

    public static VBox card(double spacing) {

        VBox card = new VBox(spacing);
        card.getStyleClass().add("card");

        return card;
    }


    // Statistics card

    public static VBox statCard(
            String title,
            String value,
            String description,
            String color) {

        Label titleLabel = mutedText(title);

        Label valueLabel = new Label(value);

        valueLabel.setStyle(
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + color + ";"
        );

        Label descriptionLabel = mutedText(description);

        VBox card = new VBox(
                8,
                titleLabel,
                valueLabel,
                descriptionLabel
        );

        card.getStyleClass().add("kpi-card");

        card.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(card, Priority.ALWAYS);

        return card;
    }


    // A2Z brand logo

    public static HBox brand(boolean darkBackground) {

        Label logo = new Label("A2Z");

        logo.setStyle(
                "-fx-background-color: #0D5C4B;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;"
        );

        logo.setAlignment(Pos.CENTER);
        logo.setMinSize(44, 44);
        logo.setPrefSize(44, 44);

        Label name = new Label("A2Z Commerce");

        name.setStyle(
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " +
                (darkBackground ? "#FFFFFF" : "#191C1B") +
                ";"
        );

        Label tagline = new Label("Everything from A to Z");

        tagline.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: " +
                (darkBackground ? "#C3C7C5" : "#5C5F5D") +
                ";"
        );

        VBox brandText = new VBox(2, name, tagline);

        HBox brand = new HBox(10, logo, brandText);
        brand.setAlignment(Pos.CENTER_LEFT);

        return brand;
    }


    // Order status badge

    public static Label orderBadge(String status) {

        String value = status == null
                ? ""
                : status.trim().toUpperCase(Locale.ROOT);

        Label badge = new Label();

        switch (value) {

            case "PLACED":
                badge.setText("Placed");
                badge.getStyleClass().add("badge-neutral");
                break;

            case "CONFIRMED":
                badge.setText("Confirmed");
                badge.getStyleClass().add("badge-warning");
                break;

            case "SHIPPED":
                badge.setText("Shipped");
                badge.getStyleClass().add("badge-primary");
                break;

            case "DELIVERED":
                badge.setText("Delivered");
                badge.getStyleClass().add("badge-success");
                break;

            case "CANCELLED":
                badge.setText("Cancelled");
                badge.getStyleClass().add("badge-error");
                break;

            default:
                badge.setText("Unknown");
                badge.getStyleClass().add("badge-neutral");
        }

        return badge;
    }


    // Product stock badge

    public static Label stockBadge(
            int stock,
            int minStock,
            boolean active) {

        Label badge = new Label();

        if (!active) {

            badge.setText("Inactive");
            badge.getStyleClass().add("badge-neutral");

        } else if (stock <= 0) {

            badge.setText("Out of Stock");
            badge.getStyleClass().add("badge-error");

        } else if (stock <= minStock) {

            badge.setText("Low Stock");
            badge.getStyleClass().add("badge-warning");

        } else {

            badge.setText("In Stock");
            badge.getStyleClass().add("badge-success");
        }

        return badge;
    }


    // Product image using online URL

    public static StackPane productImage(
            String imageUrl,
            double width,
            double height) {

        StackPane container = new StackPane();

        container.setPrefSize(width, height);
        container.setMinSize(width, height);
        container.setMaxSize(width, height);

        container.setStyle(
                "-fx-background-color: #EFF1EE;" +
                "-fx-background-radius: 10px;"
        );

        Label placeholder = new Label("Image Not Available");

        placeholder.getStyleClass().add("text-muted");
        placeholder.setWrapText(true);
        placeholder.setMaxWidth(Math.max(0, width - 8));
        placeholder.setAlignment(Pos.CENTER);

        container.getChildren().add(placeholder);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return container;
        }

        String url = imageUrl.trim();

        if (!url.toLowerCase(Locale.ROOT).startsWith("https://")
                && !url.toLowerCase(Locale.ROOT).startsWith("http://")) {

            return container;
        }

        try {

            placeholder.setText("Loading image...");

            Image image = new Image(
                    url,
                    width,
                    height,
                    true,
                    true,
                    true
            );

            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(width - 8);
            imageView.setFitHeight(height - 8);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            imageView.setVisible(false);

            container.getChildren().add(imageView);

            Runnable updateImage = () -> {

                if (image.isError()) {

                    imageView.setVisible(false);
                    placeholder.setVisible(true);
                    placeholder.setText("Image Not Available");

                } else if (image.getProgress() >= 1.0) {

                    placeholder.setVisible(false);
                    imageView.setVisible(true);
                }
            };

            image.progressProperty().addListener(
                    (obs, oldValue, newValue) ->
                            updateImage.run()
            );

            image.errorProperty().addListener(
                    (obs, oldValue, newValue) ->
                            updateImage.run()
            );

            updateImage.run();

        } catch (Exception e) {

            placeholder.setText("Image Not Available");
        }

        return container;
    }


    // Empty application state

    public static VBox emptyState(
            String title, String description) {

        Label icon = new Label("◇");

        icon.getStyleClass().add("state-empty-icon");

        Label heading = cardHeading(title);

        heading.setWrapText(true);
        heading.setAlignment(Pos.CENTER);
        heading.setTextAlignment(
            javafx.scene.text.TextAlignment.CENTER);

        Label message = mutedText(description);

        message.setAlignment(Pos.CENTER);
        message.setTextAlignment(
            javafx.scene.text.TextAlignment.CENTER);

        message.setMaxWidth(420);

        VBox box = new VBox(
            15, icon, heading, message);

        box.setAlignment(Pos.CENTER);

        box.setMaxWidth(Double.MAX_VALUE);

        box.getStyleClass().add("state-empty-card");

        return box;
    }


    // Indian currency formatting

    public static String currency(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        NumberFormat format =
                NumberFormat.getCurrencyInstance(
                        new Locale("en", "IN")
                );

        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        return format.format(amount);
    }


    // Success message

    public static void success(String message) {

        showAlert(
            Alert.AlertType.INFORMATION,
            "Success",
            message
        );
    }


    // Error message

    public static void error(String message) {

        showAlert(
            Alert.AlertType.ERROR,
            "Something Went Wrong",
            message
        );
    }


    // Warning message

    public static void warning(String message) {

        showAlert(
            Alert.AlertType.WARNING,
            "Attention Required",
            message
        );
    }

    // Common alert

    private static void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle("A2Z Commerce");

        alert.setHeaderText(null);

        alert.setGraphic(null);

        String symbol;
        String style;

        if (type == Alert.AlertType.ERROR) {

            symbol = "×";
            style = "state-icon-error";

        } else if (type == Alert.AlertType.WARNING) {

            symbol = "!";
            style = "state-icon-warning";

        } else {

            symbol = "✓";
            style = "state-icon-success";
        }

        VBox content = createDialogContent(
            symbol, style, title, message);

        alert.getDialogPane().setContent(content);

        alert.getButtonTypes().setAll(ButtonType.OK);

        styleDialog(alert);

        Button okay = (Button)
            alert.getDialogPane()
                .lookupButton(ButtonType.OK);

        okay.setText(
            type == Alert.AlertType.INFORMATION
                ? "Done" : "OK");

        okay.getStyleClass().add("btn-primary");

        alert.showAndWait();
    }


    // Confirmation dialog

    public static boolean confirm(
            String title,
            String message,
            String actionText) {

        ButtonType action = new ButtonType(
            actionText,
            ButtonBar.ButtonData.OK_DONE
        );

        ButtonType cancel = new ButtonType(
            "Go Back",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION);

        alert.setTitle("A2Z Commerce");

        alert.setHeaderText(null);

        alert.setGraphic(null);

        String operation =
            actionText.toLowerCase(Locale.ROOT);

        boolean destructive =
            operation.contains("delete")
            || operation.contains("remove")
            || operation.contains("deactivate")
            || operation.contains("cancel")
            || operation.contains("clear");

        VBox content = createDialogContent(

            destructive ? "!" : "?",

            destructive
                ? "state-icon-error"
                : "state-icon-warning",

            title,

            message
        );

        alert.getDialogPane().setContent(content);

        alert.getButtonTypes().setAll(cancel, action);

        styleDialog(alert);

        Button actionButton = (Button)
            alert.getDialogPane().lookupButton(action);

        Button cancelButton = (Button)
            alert.getDialogPane().lookupButton(cancel);

        actionButton.getStyleClass().add(

            destructive
                ? "btn-destructive"
                : "btn-primary"
        );

        cancelButton.getStyleClass().add(
            "btn-secondary");

        // Prevent accidental confirmation using Enter

        actionButton.setDefaultButton(false);

        cancelButton.setDefaultButton(true);

        cancelButton.setCancelButton(true);

        alert.setOnShown(e ->
            cancelButton.requestFocus());

        return alert.showAndWait()
                    .orElse(cancel) == action;
    }


    // Apply common styling to dialogs

    private static void styleDialog(Alert alert) {

        DialogPane pane = alert.getDialogPane();

        java.net.URL css = UIComponents.class
            .getResource("/styles/a2z-javafx.css");

        if (css != null) {

            pane.getStylesheets().add(
                css.toExternalForm());
        }

        pane.getStyleClass().addAll(
            "a2z-dialog",
            "state-dialog"
        );

        pane.setPrefWidth(460);

        pane.setMinHeight(Region.USE_PREF_SIZE);

        alert.setResizable(false);

        // Attach dialog to the currently active window

        for (javafx.stage.Window window :
                javafx.stage.Window.getWindows()) {

            if (window.isShowing()
                    && window.isFocused()) {

                alert.initOwner(window);

                break;
            }
        }
    }

    public static VBox loadingState(String message) {

        ProgressIndicator progress =
            new ProgressIndicator();

        progress.setPrefSize(45, 45);

        progress.setMaxSize(45, 45);

        progress.getStyleClass().add(
            "state-loading-spinner");

        Label heading = cardHeading("Loading");

        Label description = mutedText(message);

        VBox box = new VBox(
            15, progress, heading, description);

        box.setAlignment(Pos.CENTER);

        box.setMaxWidth(Double.MAX_VALUE);

        box.getStyleClass().add("state-empty-card");

        return box;
    }
    
    private static VBox createDialogContent(
            String symbol,
            String iconStyle,
            String title,
            String message) {

        Label icon = new Label(symbol);

        icon.getStyleClass().addAll(
            "state-dialog-icon", iconStyle);

        Label heading = new Label(title);

        heading.getStyleClass().add(
            "state-dialog-title");

        heading.setWrapText(true);

        Label description = new Label(message);

        description.setWrapText(true);

        description.setMaxWidth(400);

        description.getStyleClass().add(
            "state-dialog-description");

        VBox content = new VBox(
            16, icon, heading, description);

        content.setAlignment(Pos.TOP_LEFT);

        content.getStyleClass().add(
            "state-dialog-content");

        return content;
    }

}