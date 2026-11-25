
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Locale;

public class LoginPage {

    // Replace this with your preferred direct illustration URL.
    private static final String LOGIN_IMAGE_URL = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQp8fE9k3iN2D5wgpEJvrC0gxaTe-kRRhCFNQMFV3F7OQ&s=10";

    public static void show(Stage stage) {

        // ==========================================
        // LEFT BRANDING PANEL
        // ==========================================

        HBox brand = UIComponents.brand(true);

        StackPane illustration = createIllustration();

        Label marketingHeading = new Label(
                "Your Smart Shopping Destination"
        );

        marketingHeading.getStyleClass()
                .add("login-marketing-heading");

        marketingHeading.setWrapText(true);

        Label marketingDescription = new Label(
                "Unified desktop platform connecting seamless "
                + "customer shopping with real-time order lifecycle "
                + "and warehouse inventory control."
        );

        marketingDescription.getStyleClass()
                .add("login-marketing-description");

        marketingDescription.setWrapText(true);

        VBox shoppingFeature = featureCard(
                "🛍",
                "Easy Shopping",
                "Browse curated categories with live stock status."
        );

        VBox recommendationFeature = featureCard(
                "✦",
                "Smart Picks",
                "Personalized items based on order history."
        );

        VBox trackingFeature = featureCard(
                "▣",
                "Order Tracking",
                "Step-by-step fulfillment from placement to delivery."
        );

        HBox features = new HBox(
                12,
                shoppingFeature,
                recommendationFeature,
                trackingFeature
        );

        HBox.setHgrow(shoppingFeature, Priority.ALWAYS);
        HBox.setHgrow(recommendationFeature, Priority.ALWAYS);
        HBox.setHgrow(trackingFeature, Priority.ALWAYS);

        VBox marketingContent = new VBox(
                20,
                illustration,
                marketingHeading,
                marketingDescription,
                features
        );

        Region topSpace = new Region();
        Region bottomSpace = new Region();

        VBox.setVgrow(topSpace, Priority.ALWAYS);
        VBox.setVgrow(bottomSpace, Priority.ALWAYS);

        Label footerText = new Label(
                "A2Z Desktop Platform · JavaFX & MySQL"
        );

        footerText.getStyleClass().add("login-footer-text");

        Label version = new Label("v1.0");

        version.getStyleClass().add("login-footer-text");

        Region footerSpace = new Region();

        HBox.setHgrow(footerSpace, Priority.ALWAYS);

        HBox footer = new HBox(
                10, footerText, footerSpace, version
        );

        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("login-footer");

        VBox leftPanel = new VBox(
                20,
                brand,
                topSpace,
                marketingContent,
                bottomSpace,
                footer
        );

        leftPanel.getStyleClass().add("login-left-panel");
        leftPanel.setPadding(new Insets(40));
        leftPanel.setMinWidth(420);
        leftPanel.setMaxWidth(Double.MAX_VALUE);


        // ==========================================
        // RIGHT LOGIN PANEL
        // ==========================================

        Label heading = new Label("Welcome Back");

        heading.getStyleClass().add("login-heading");

        Label subtitle = new Label(
                "Sign in to your A2Z account"
        );

        subtitle.getStyleClass().add("login-subtitle");

        VBox formHeading = new VBox(
                5, heading, subtitle
        );


        // ==========================================
        // ACCOUNT ROLE SELECTION
        // ==========================================

        Label roleLabel = fieldLabel("Select Account Role");

        ToggleGroup roleGroup = new ToggleGroup();

        ToggleButton adminRole = roleCard(
                "♜",
                "Admin",
                "Manage your store"
        );

        adminRole.setUserData("ADMIN");
        adminRole.setToggleGroup(roleGroup);

        ToggleButton customerRole = roleCard(
                "🛍",
                "Customer",
                "Explore and shop"
        );

        customerRole.setUserData("CUSTOMER");
        customerRole.setToggleGroup(roleGroup);

        customerRole.setSelected(true);

        // Always keep one role selected.

        roleGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> {

                    if (newToggle == null
                            && oldToggle != null) {

                        oldToggle.setSelected(true);
                    }
                }
        );

        HBox roleOptions = new HBox(
                12, adminRole, customerRole
        );

        HBox.setHgrow(adminRole, Priority.ALWAYS);
        HBox.setHgrow(customerRole, Priority.ALWAYS);

        VBox roleSection = new VBox(
                8, roleLabel, roleOptions
        );


        // ==========================================
        // EMAIL FIELD
        // ==========================================

        Label emailLabel = fieldLabel("Email Address");

        TextField email = new TextField();

        email.setPromptText("Enter your email address");
        email.getStyleClass().add("login-input");

        Label emailIcon = new Label("✉");

        emailIcon.getStyleClass().add("login-field-icon");

        HBox emailBox = new HBox(
                10, emailIcon, email
        );

        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.getStyleClass().add("login-input-wrapper");

        HBox.setHgrow(email, Priority.ALWAYS);

        VBox emailSection = new VBox(
                7, emailLabel, emailBox
        );


        // ==========================================
        // PASSWORD FIELD
        // ==========================================

        Label passwordLabel = fieldLabel("Password");

        PasswordField password = new PasswordField();

        password.setPromptText("Enter your password");
        password.getStyleClass().add("login-input");

        TextField visiblePassword = new TextField();

        visiblePassword.setPromptText("Enter your password");
        visiblePassword.getStyleClass().add("login-input");

        visiblePassword.textProperty()
                .bindBidirectional(password.textProperty());

        visiblePassword.setVisible(false);
        visiblePassword.setManaged(false);

        StackPane passwordFields = new StackPane(
                password, visiblePassword
        );

        HBox.setHgrow(passwordFields, Priority.ALWAYS);

        Label lockIcon = new Label("🔒");

        lockIcon.getStyleClass().add("login-field-icon");

        ToggleButton showPassword = new ToggleButton("Show");

        showPassword.getStyleClass()
                .add("login-password-toggle");

        showPassword.setAccessibleText(
                "Show or hide password"
        );

        showPassword.setOnAction(e -> {

            boolean show = showPassword.isSelected();

            password.setVisible(!show);
            password.setManaged(!show);

            visiblePassword.setVisible(show);
            visiblePassword.setManaged(show);

            showPassword.setText(show ? "Hide" : "Show");
        });

        HBox passwordBox = new HBox(
                10,
                lockIcon,
                passwordFields,
                showPassword
        );

        passwordBox.setAlignment(Pos.CENTER_LEFT);

        passwordBox.getStyleClass()
                .add("login-input-wrapper");

        VBox passwordSection = new VBox(
                7, passwordLabel, passwordBox
        );


        // ==========================================
        // VALIDATION MESSAGE
        // ==========================================

        Label message = new Label();

        message.getStyleClass().add("login-error");
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);

        message.setVisible(false);
        message.setManaged(false);


        // ==========================================
        // SIGN IN BUTTON
        // ==========================================

        Button login = UIComponents.primaryButton(
                "Sign In to A2Z  →"
        );

        login.getStyleClass().add("login-submit");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setDefaultButton(true);

        login.setOnAction(e -> {

            message.setVisible(false);
            message.setManaged(false);

            String enteredEmail = email.getText()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            String enteredPassword = password.getText();

            // Empty field validation

            if (enteredEmail.isBlank()
                    || enteredPassword.isBlank()) {

                showError(
                        message,
                        "Please enter your email and password."
                );

                return;
            }

            // Email format validation

            if (!enteredEmail.matches(
                    "[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

                showError(
                        message,
                        "Please enter a valid email address."
                );

                return;
            }

            Toggle selectedToggle =
                    roleGroup.getSelectedToggle();

            if (selectedToggle == null) {

                showError(
                        message,
                        "Please select an account role."
                );

                return;
            }

            String selectedRole =
                    selectedToggle.getUserData().toString();

            try {

                User user = AuthService.login(
                        enteredEmail,
                        enteredPassword
                );

                // Invalid credentials

                if (user == null) {

                    showError(
                            message,
                            "Invalid email or password. "
                            + "Please try again."
                    );

                    return;
                }

                // Role mismatch validation

                if (!selectedRole.equals(user.getRole())) {

                    showError(
                            message,
                            "Account role mismatch. Please select "
                            + "the correct account role."
                    );

                    return;
                }

                // Navigate to the correct dashboard

                if ("ADMIN".equals(user.getRole())) {

                    AdminDashboard.show(stage, user);

                } else if ("CUSTOMER".equals(user.getRole())) {

                    CustomerDashboard.show(stage, user);

                } else {

                    showError(
                            message,
                            "Invalid user role."
                    );
                }

            } catch (Exception ex) {

                showError(
                        message,
                        "Unable to sign in. Please try again."
                );

                ex.printStackTrace();
            }
        });


        // ==========================================
        // REGISTRATION NAVIGATION
        // ==========================================

        Label newUser = new Label("New to A2Z?");

        newUser.getStyleClass().add("login-subtitle");

        Hyperlink register = new Hyperlink(
                "Create Account  →"
        );

        register.getStyleClass().add("login-link");

        register.setOnAction(
                e -> RegisterPage.show(stage)
        );

        Region registerSpace = new Region();

        HBox.setHgrow(registerSpace, Priority.ALWAYS);

        HBox registrationRow = new HBox(
                10, newUser, registerSpace, register
        );

        registrationRow.setAlignment(Pos.CENTER_LEFT);


        // ==========================================
        // WHITE LOGIN FORM CARD
        // ==========================================

        VBox loginCard = new VBox(
                22,
                formHeading,
                roleSection,
                emailSection,
                passwordSection,
                message,
                login,
                new Separator(),
                registrationRow
        );

        loginCard.getStyleClass().add("login-card");

        loginCard.setPadding(new Insets(32));
        loginCard.setMaxWidth(470);
        loginCard.setMinWidth(350);

        StackPane rightPanel = new StackPane(loginCard);

        rightPanel.setPadding(new Insets(40));

        rightPanel.getStyleClass()
                .add("login-right-panel");


        // ==========================================
        // MAIN APPLICATION LAYOUT
        // ==========================================

        GridPane root = new GridPane();

        root.getStyleClass().add("login-root");

        ColumnConstraints leftColumn =
                new ColumnConstraints();

        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);

        ColumnConstraints rightColumn =
                new ColumnConstraints();

        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);

        root.getColumnConstraints().addAll(
                leftColumn, rightColumn
        );

        RowConstraints row = new RowConstraints();

        row.setVgrow(Priority.ALWAYS);

        root.getRowConstraints().add(row);

        root.add(leftPanel, 0, 0);
        root.add(rightPanel, 1, 0);

        GridPane.setFillHeight(leftPanel, true);
        GridPane.setFillHeight(rightPanel, true);

        GridPane.setFillWidth(leftPanel, true);
        GridPane.setFillWidth(rightPanel, true);

        Scene scene = new Scene(root, 1280, 800);

        UIComponents.applyStyles(scene);

        stage.setTitle("A2Z Commerce - Login");
        stage.setMinWidth(1000);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }


    // ==========================================
    // FIELD LABEL
    // ==========================================

    private static Label fieldLabel(String text) {

        Label label = new Label(text);

        label.getStyleClass().add("login-field-label");

        return label;
    }


    // ==========================================
    // ROLE SELECTION CARD
    // ==========================================

    private static ToggleButton roleCard(
            String icon,
            String title,
            String description) {

        Label iconLabel = new Label(icon);

        iconLabel.getStyleClass().add("login-role-icon");

        Label titleLabel = new Label(title);

        titleLabel.getStyleClass().add("login-role-title");

        Label descriptionLabel = new Label(description);

        descriptionLabel.getStyleClass()
                .add("login-role-description");

        descriptionLabel.setWrapText(true);

        VBox text = new VBox(
                4, titleLabel, descriptionLabel
        );

        HBox content = new HBox(
                10, iconLabel, text
        );

        content.setAlignment(Pos.CENTER_LEFT);

        ToggleButton button = new ToggleButton();

        button.setGraphic(content);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        button.getStyleClass().add("login-role-card");

        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(78);

        button.setAccessibleText(title);

        return button;
    }


    // ==========================================
    // MARKETING FEATURE CARD
    // ==========================================

    private static VBox featureCard(
            String icon,
            String title,
            String description) {

        Label iconLabel = new Label(icon);

        iconLabel.getStyleClass().add("login-feature-icon");

        Label titleLabel = new Label(title);

        titleLabel.getStyleClass().add("login-feature-title");

        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(description);

        descriptionLabel.getStyleClass()
                .add("login-feature-description");

        descriptionLabel.setWrapText(true);

        VBox card = new VBox(
                8,
                iconLabel,
                titleLabel,
                descriptionLabel
        );

        card.getStyleClass().add("login-feature-card");

        card.setPrefWidth(150);
        card.setMinWidth(0);
        card.setMaxWidth(Double.MAX_VALUE);

        return card;
    }


    // ==========================================
    // LOGIN ILLUSTRATION
    // ==========================================

    private static StackPane createIllustration() {

        StackPane container = new StackPane();

        container.getStyleClass()
                .add("login-illustration");

        container.setPrefHeight(240);
        container.setMinHeight(180);

        Label icon = new Label("🛍");

        icon.getStyleClass()
                .add("login-illustration-icon");

        Label title = new Label("Everything from A to Z");

        title.getStyleClass()
                .add("login-illustration-title");

        Label subtitle = new Label(
                "Discover · Shop · Track"
        );

        subtitle.getStyleClass()
                .add("login-illustration-subtitle");

        VBox placeholder = new VBox(
                12, icon, title, subtitle
        );

        placeholder.setAlignment(Pos.CENTER);

        container.getChildren().add(placeholder);

        if (LOGIN_IMAGE_URL.isBlank()) {

            return container;
        }

        try {

            Image image = new Image(
                    LOGIN_IMAGE_URL, true
            );

            ImageView imageView = new ImageView(image);

            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            imageView.fitWidthProperty().bind(
                    container.widthProperty().subtract(20)
            );

            imageView.fitHeightProperty().bind(
                    container.heightProperty().subtract(20)
            );

            imageView.setVisible(false);

            container.getChildren().add(imageView);

            Runnable updateImage = () -> {

                if (image.isError()) {

                    placeholder.setVisible(true);
                    imageView.setVisible(false);

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

        } catch (Exception ex) {

            placeholder.setVisible(true);
        }

        return container;
    }


    // ==========================================
    // DISPLAY ERROR MESSAGE
    // ==========================================

    private static void showError(
            Label message,
            String text) {

        message.setText(text);
        message.setVisible(true);
        message.setManaged(true);
    }
}
