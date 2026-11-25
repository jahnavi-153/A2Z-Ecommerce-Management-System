import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.sql.SQLIntegrityConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class RegisterPage {

    // Optional shopping illustration URL
    private static final String REGISTER_IMAGE_URL = "https://media.istockphoto.com/id/2237056850/photo/distributor-online-shopping-management-frontend-and-backend-developer-customer-relationship.jpg?s=612x612&w=0&k=20&c=JXtYU-K3DeuEpZO_iSfYW-vHeVFMZ9yWXHCiPUm4CJY=";

    public static void show(Stage stage) {

        // ================= LEFT BRANDING PANEL =================

        HBox brand = UIComponents.brand(true);

        Region topSpace = new Region();

        VBox.setVgrow(topSpace, Priority.ALWAYS);

        StackPane illustration;

        if (REGISTER_IMAGE_URL.isBlank()) {

            Label logo = new Label("A2Z");

            logo.getStyleClass().add(
                "reg-illustration-logo");

            illustration = new StackPane(logo);

        } else {

            illustration = UIComponents.productImage(
                REGISTER_IMAGE_URL, 320, 220);
        }

        illustration.getStyleClass().add(
            "reg-illustration");

        Label leftTitle = new Label(
            "Shop Smarter with A2Z.");

        leftTitle.getStyleClass().add(
            "reg-brand-heading");

        leftTitle.setWrapText(true);

        Label leftDescription = new Label(
            "Create your account and discover "
            + "a better shopping experience.");

        leftDescription.getStyleClass().add(
            "reg-brand-description");

        leftDescription.setWrapText(true);

        Label noticeTitle = new Label(
            "Customer Registration");

        noticeTitle.getStyleClass().add(
            "reg-notice-title");

        Label noticeText = new Label(
            "Public registration is available "
            + "for customer accounts only.");

        noticeText.getStyleClass().add(
            "reg-brand-description");

        noticeText.setWrapText(true);

        VBox notice = new VBox(
            8, noticeTitle, noticeText);

        notice.getStyleClass().add(
            "reg-notice");

        Region bottomSpace = new Region();

        VBox.setVgrow(
            bottomSpace, Priority.ALWAYS);

        Label footer = new Label(
            "A2Z Desktop Platform\n"
            + "JavaFX & MySQL · Version 1.0");

        footer.getStyleClass().add(
            "reg-brand-footer");

        VBox left = new VBox(

            24,

            brand,

            topSpace,

            illustration,

            leftTitle,

            leftDescription,

            notice,

            bottomSpace,

            footer
        );

        left.setAlignment(Pos.CENTER_LEFT);

        left.getStyleClass().add(
            "reg-brand-panel");

        // ================= REGISTRATION HEADER =================

        Hyperlink back = new Hyperlink(
            "← Back to Login");

        back.getStyleClass().add(
            "reg-link");

        back.setOnAction(e ->
            LoginPage.show(stage));

        Label heading = UIComponents.heading(
            "Create Your Account");

        Label subtitle = UIComponents.mutedText(
            "Join A2Z and start shopping today.");

        // ================= INPUT FIELDS =================

        TextField name = new TextField();

        name.setPromptText(
            "Enter your full name");

        TextField email = new TextField();

        email.setPromptText(
            "you@example.com");

        TextField phone = new TextField();

        phone.setPromptText(
            "10-digit phone number");

        phone.setTextFormatter(

            new TextFormatter<String>(change -> {

                if (change.getControlNewText()
                          .matches("[0-9]{0,10}")) {

                    return change;
                }

                return null;
            })
        );

        PasswordField password =
            new PasswordField();

        password.setPromptText(
            "Enter password");

        PasswordField confirmPassword =
            new PasswordField();

        confirmPassword.setPromptText(
            "Confirm password");

        // ================= FORM LAYOUT =================

        VBox nameField = field(
            "Full Name", name);

        GridPane contactFields = twoColumns(

            field("Email Address", email),

            field("Phone Number", phone)
        );

        GridPane passwordFields = twoColumns(

            field(
                "Password",
                passwordInput(password)),

            field(
                "Confirm Password",
                passwordInput(confirmPassword))
        );

        // ================= PASSWORD GUIDANCE =================

        Label passwordHint = new Label(
            "Use at least 8 characters. "
            + "A mixture of letters, numbers "
            + "and symbols is recommended.");

        passwordHint.setWrapText(true);

        passwordHint.getStyleClass().add(
            "reg-password-hint");

        // ================= ERROR MESSAGE =================

        Label message = new Label();

        message.setWrapText(true);

        message.getStyleClass().add(
            "reg-error");

        message.setVisible(false);

        message.managedProperty().bind(
            message.visibleProperty());

        // ================= REGISTER BUTTON =================

        Button register =
            UIComponents.primaryButton(
                "Create Account");

        register.setMaxWidth(
            Double.MAX_VALUE);

        register.setPrefHeight(46);

        register.setDefaultButton(true);

        register.setOnAction(e -> {

            message.setVisible(false);

            String fullName =
                name.getText().trim();

            String emailAddress =
                email.getText().trim()
                     .toLowerCase(Locale.ROOT);

            String phoneNumber =
                phone.getText().trim();

            String pass = password.getText();

            String confirm =
                confirmPassword.getText();

            // Required field validation

            if (fullName.isBlank()
                    || emailAddress.isBlank()
                    || phoneNumber.isBlank()
                    || pass.isBlank()
                    || confirm.isBlank()) {

                showError(
                    message,
                    "Please complete all required fields.");

                return;
            }

            // Email validation

            if (!emailAddress.matches(
                    "[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {

                showError(
                    message,
                    "Enter a valid email address.");

                return;
            }

            // Phone number validation

            if (!phoneNumber.matches("[0-9]{10}")) {

                showError(
                    message,
                    "Phone number must contain 10 digits.");

                return;
            }

            // Password length validation

            if (pass.length() < 8) {

                showError(
                    message,
                    "Password must contain at least "
                    + "8 characters.");

                return;
            }

            // BCrypt password byte limit

            if (pass.getBytes(
                    StandardCharsets.UTF_8).length > 72) {

                showError(
                    message,
                    "Password exceeds the allowed length.");

                return;
            }

            // Confirm password validation

            if (!pass.equals(confirm)) {

                showError(
                    message,
                    "Passwords do not match.");

                return;
            }

            register.setDisable(true);

            try {

                boolean created =
                    AuthService.register(

                        fullName,

                        emailAddress,

                        phoneNumber,

                        pass
                    );

                if (created) {

                    UIComponents.success(
                        "Account created successfully! "
                        + "You can now sign in.");

                    LoginPage.show(stage);

                } else {

                    showError(
                        message,
                        "Unable to create account.");
                }

            } catch (
                SQLIntegrityConstraintViolationException ex) {

                showError(
                    message,
                    "Account already exists or "
                    + "the entered details are invalid.");

            } catch (Exception ex) {

                showError(
                    message,
                    "Registration failed. "
                    + "Please try again.");

                ex.printStackTrace();

            } finally {

                register.setDisable(false);
            }
        });

        // ================= LOGIN LINK =================

        Label existing = new Label(
            "Already have an account?");

        existing.getStyleClass().add(
            "text-muted");

        Hyperlink signIn = new Hyperlink(
            "Sign In");

        signIn.getStyleClass().add(
            "reg-link");

        signIn.setOnAction(e ->
            LoginPage.show(stage));

        HBox loginLink = new HBox(
            5, existing, signIn);

        loginLink.setAlignment(Pos.CENTER);

        // ================= REGISTRATION CARD =================

        VBox card = new VBox(

            18,

            back,

            heading,

            subtitle,

            nameField,

            contactFields,

            passwordFields,

            passwordHint,

            message,

            register,

            new Separator(),

            loginLink
        );

        card.getStyleClass().add(
            "reg-card");

        card.setMaxWidth(620);

        card.setPrefWidth(580);

        card.setMaxHeight(
            Region.USE_PREF_SIZE);

        StackPane rightContent =
            new StackPane(card);

        rightContent.setPadding(
            new Insets(30));

        rightContent.setAlignment(Pos.CENTER);

        ScrollPane right =
            new ScrollPane(rightContent);

        right.setFitToWidth(true);

        right.setFitToHeight(true);

        right.setHbarPolicy(
            ScrollPane.ScrollBarPolicy.NEVER);

        right.getStyleClass().add(
            "reg-right-panel");

        // ================= MAIN WINDOW =================

        GridPane root = new GridPane();

        ColumnConstraints leftColumn =
            new ColumnConstraints();

        leftColumn.setPercentWidth(42);

        ColumnConstraints rightColumn =
            new ColumnConstraints();

        rightColumn.setPercentWidth(58);

        root.getColumnConstraints().addAll(
            leftColumn, rightColumn);

        RowConstraints row =
            new RowConstraints();

        row.setVgrow(Priority.ALWAYS);

        row.setFillHeight(true);

        root.getRowConstraints().add(row);

        root.add(left, 0, 0);

        root.add(right, 1, 0);

        root.getStyleClass().add(
            "reg-root");

        Scene scene = new Scene(
            root, 1280, 800);

        // Apply our new A2Z stylesheet

        UIComponents.applyStyles(scene);

        stage.setTitle(
            "A2Z - Create Account");

        stage.setMinWidth(1000);

        stage.setMinHeight(700);

        stage.setScene(scene);

        stage.setMaximized(true);

        stage.show();
    }

    // =====================================================
    // FORM FIELD COMPONENT
    // =====================================================

    private static VBox field(
            String title, Node input) {

        Label label = new Label(title);

        label.getStyleClass().add(
            "reg-field-label");

        if (input instanceof Region) {

            ((Region) input).setMaxWidth(
                Double.MAX_VALUE);
        }

        return new VBox(8, label, input);
    }

    // =====================================================
    // TWO COLUMN FORM LAYOUT
    // =====================================================

    private static GridPane twoColumns(
            Node first, Node second) {

        GridPane grid = new GridPane();

        grid.setHgap(16);

        ColumnConstraints c1 =
            new ColumnConstraints();

        c1.setPercentWidth(50);

        ColumnConstraints c2 =
            new ColumnConstraints();

        c2.setPercentWidth(50);

        grid.getColumnConstraints().addAll(
            c1, c2);

        grid.add(first, 0, 0);

        grid.add(second, 1, 0);

        return grid;
    }

    // =====================================================
    // SHOW / HIDE PASSWORD
    // =====================================================

    private static HBox passwordInput(
            PasswordField password) {

        TextField visiblePassword =
            new TextField();

        visiblePassword.setPromptText(
            password.getPromptText());

        visiblePassword.textProperty()
            .bindBidirectional(
                password.textProperty());

        visiblePassword.setVisible(false);

        visiblePassword.managedProperty()
            .bind(
                visiblePassword.visibleProperty());

        password.managedProperty().bind(
            password.visibleProperty());

        StackPane fields = new StackPane(
            password, visiblePassword);

        HBox.setHgrow(
            fields, Priority.ALWAYS);

        fields.setMinWidth(0);

        Button toggle = new Button("Show");

        toggle.getStyleClass().add(
            "reg-password-toggle");

        toggle.setOnAction(e -> {

            boolean showing =
                visiblePassword.isVisible();

            visiblePassword.setVisible(
                !showing);

            password.setVisible(showing);

            toggle.setText(
                showing ? "Show" : "Hide");
        });

        HBox wrapper = new HBox(
            6, fields, toggle);

        wrapper.setAlignment(Pos.CENTER_LEFT);

        return wrapper;
    }

    // =====================================================
    // DISPLAY VALIDATION ERROR
    // =====================================================

    private static void showError(
            Label message, String text) {

        message.setText(text);

        message.setVisible(true);
    }
}