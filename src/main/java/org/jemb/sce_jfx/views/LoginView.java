package org.jemb.sce_jfx.views;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.services.AuthService;

public class LoginView extends VBox {
    private TextField emailField;
    private PasswordField passwordField;
    private TextField passwordVisibleField;
    private Button togglePasswordBtn;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private Label alertLabel;
    private Label emailErrorLabel;
    private Label passwordErrorLabel;
    private ProgressIndicator progressIndicator;

    private final AuthService authService;
    private Runnable onLoginSuccess;

    public LoginView() {
        this.authService = new AuthService();
        initializeUI();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    private void initializeUI() {
        // Cargar CSS (colocar login.css en resources/org/jemb/sce_jfx/styles/)
        getStylesheets().add(getClass().getResource("/org/jemb/sce_jfx/styles/login.css").toExternalForm());
        getStyleClass().add("login-root");

        setAlignment(Pos.CENTER);
        setPadding(new Insets(24));
        setSpacing(12);

        VBox card = createLoginCard();
        HBox wrapper = new HBox(card);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setMaxWidth(440);
        getChildren().add(wrapper);
    }

    private VBox createLoginCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("login-card");
        card.setPadding(new Insets(32));
        card.setAlignment(Pos.TOP_CENTER);

        // Logo
        StackPane logo = new StackPane();
        logo.getStyleClass().add("logo");
        //Label iconLabel = new Label("\uD83C\uDF93");
        Label iconLabel = new Label("SCE");
        iconLabel.setFont(Font.font("System", 26));
        iconLabel.setTextFill(Color.WHITE);
        iconLabel.getStyleClass().add("login-icon");
        logo.getChildren().add(iconLabel);

        Label title = new Label("Bienvenido");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Sistema de Control de Estudiantes");
        subtitle.getStyleClass().add("subtitle");

        VBox header = new VBox(6, logo, title, subtitle);
        header.setAlignment(Pos.CENTER);

        // Alert (oculto por defecto)
        alertLabel = new Label();
        alertLabel.getStyleClass().addAll("alert");
        alertLabel.setWrapText(true);
        alertLabel.setVisible(false);
        alertLabel.setManaged(false);

        // Form
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);

        // Email group
        VBox emailGroup = new VBox(6);
        Label emailLabel = new Label("Correo electrónico");
        emailLabel.getStyleClass().add("label");
        emailField = new TextField();
        emailField.setPromptText("tu@email.com");
        emailField.getStyleClass().add("input");
        emailField.setPrefHeight(44);

        emailErrorLabel = new Label("Por favor ingresa un correo válido");
        emailErrorLabel.getStyleClass().add("error-message");
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);

        emailField.focusedProperty().addListener((obs, ov, nv) -> {
            if (!nv) validateEmail();
        });
        emailField.textProperty().addListener((o, oldV, newV) -> {
            hideAlert();
            clearEmailError();
        });

        emailGroup.getChildren().addAll(emailLabel, emailField, emailErrorLabel);

        // Password group
        VBox passwordGroup = new VBox(6);
        Label passwordLabel = new Label("Contraseña");
        passwordLabel.getStyleClass().add("label");

        StackPane passwordContainer = new StackPane();
        passwordContainer.setPrefHeight(44);

        passwordField = new PasswordField();
        passwordField.getStyleClass().addAll("input", "password-input");
        passwordField.setPrefHeight(44);

        passwordVisibleField = new TextField();
        passwordVisibleField.getStyleClass().addAll("input", "password-input");
        passwordVisibleField.setPrefHeight(44);
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);

        // Toggle password button (simple)
        togglePasswordBtn = new Button("👁");
        togglePasswordBtn.getStyleClass().add("toggle-btn");
        togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());

        HBox toggleBox = new HBox(togglePasswordBtn);
        toggleBox.setAlignment(Pos.CENTER_RIGHT);
        toggleBox.setPadding(new Insets(0, 6, 0, 0));
        toggleBox.setPickOnBounds(false);

        BorderPane passwordPane = new BorderPane();
        passwordPane.setCenter(passwordField);
        passwordPane.setRight(toggleBox);

        // When visible, swap center node
        passwordVisibleField.visibleProperty().addListener((obs, oldV, newV) -> {
            passwordVisibleField.setManaged(newV);
        });

        passwordGroup.getChildren().addAll(passwordLabel, new StackPane(passwordField, passwordVisibleField, toggleBox));

        passwordErrorLabel = new Label("La contraseña debe tener al menos 6 caracteres");
        passwordErrorLabel.getStyleClass().add("error-message");
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);

        passwordField.focusedProperty().addListener((obs, ov, nv) -> {
            if (!nv) validatePassword();
        });
        passwordVisibleField.focusedProperty().addListener((obs, ov, nv) -> {
            if (!nv) validatePassword();
        });

        passwordField.textProperty().addListener((o, oldV, newV) -> {
            if (passwordVisibleField.isVisible()) passwordVisibleField.setText(newV);
            clearPasswordError();
            hideAlert();
        });
        passwordVisibleField.textProperty().addListener((o, oldV, newV) -> {
            if (passwordField.isVisible()) passwordField.setText(newV);
            clearPasswordError();
            hideAlert();
        });

        // Options row
        HBox options = new HBox(8);
        options.setAlignment(Pos.CENTER_LEFT);
        rememberMeCheckBox = new CheckBox("Recordarme");
//        Hyperlink forgot = new Hyperlink("¿Olvidaste tu contraseña?");
//        forgot.setOnAction(e -> showAlert("Funcionalidad de recuperación próximamente", "info"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        options.getChildren().addAll(rememberMeCheckBox, spacer);

        // Login button + progress
        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        progressIndicator.setPrefSize(20, 20);

        loginButton = new Button("Iniciar Sesión");
        loginButton.getStyleClass().add("btn-login");
        loginButton.setPrefHeight(44);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());

        HBox loginRow = new HBox(10, loginButton, progressIndicator);
        loginRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(loginButton, Priority.ALWAYS);

        // Divider + register
        HBox divider = new HBox();
        divider.getStyleClass().add("divider");
        Label or = new Label("o");
        or.getStyleClass().add("divider-text");

        HBox register = new HBox();
        register.setAlignment(Pos.CENTER);
        Label noAcc = new Label("¿No tienes una cuenta? ");
        Hyperlink regLink = new Hyperlink("Regístrate");
        regLink.setOnAction(e -> showAlert("Funcionalidad de registro próximamente", "info"));
        register.getChildren().addAll(noAcc, regLink);

        // Assemble form
        form.getChildren().addAll(emailGroup, passwordGroup, passwordErrorLabel, options, loginRow);

        card.getChildren().addAll(header, alertLabel, form, divider);
        return card;
    }

    private void togglePasswordVisibility() {
        boolean visible = passwordVisibleField.isVisible();
        if (visible) {
            // hide visible, show password field
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        } else {
            // show visible text
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
        }
    }

    private boolean validateEmail() {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            showEmailError();
            return false;
        }
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        if (!email.matches(emailRegex)) {
            showEmailError();
            return false;
        }
        clearEmailError();
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();
        if (password == null || password.length() < 6) {
            showPasswordError();
            return false;
        }
        clearPasswordError();
        return true;
    }

    private void showEmailError() {
        emailErrorLabel.setVisible(true);
        emailErrorLabel.setManaged(true);
        emailField.getStyleClass().removeAll("error");
        emailField.getStyleClass().add("error");
    }

    private void clearEmailError() {
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        emailField.getStyleClass().removeAll("error");
    }

    private void showPasswordError() {
        passwordErrorLabel.setVisible(true);
        passwordErrorLabel.setManaged(true);
        passwordField.getStyleClass().removeAll("error");
        passwordVisibleField.getStyleClass().removeAll("error");
        passwordField.getStyleClass().add("error");
        passwordVisibleField.getStyleClass().add("error");
    }

    private void clearPasswordError() {
        passwordErrorLabel.setVisible(false);
        passwordErrorLabel.setManaged(false);
        passwordField.getStyleClass().removeAll("error");
        passwordVisibleField.getStyleClass().removeAll("error");
    }

    private void showAlert(String message, String type) {
        alertLabel.setText(message);
        alertLabel.getStyleClass().removeAll("alert-success", "alert-error", "alert-info");
        alertLabel.getStyleClass().add("alert");
        switch (type) {
            case "success" -> alertLabel.getStyleClass().add("alert-success");
            case "error" -> alertLabel.getStyleClass().add("alert-error");
            default -> alertLabel.getStyleClass().add("alert-info");
        }
        alertLabel.setVisible(true);
        alertLabel.setManaged(true);
    }

    private void hideAlert() {
        alertLabel.setVisible(false);
        alertLabel.setManaged(false);
    }

    private void handleLogin() {
        hideAlert();

        boolean okEmail = validateEmail();
        boolean okPass = validatePassword();
        if (!okEmail || !okPass) return;

        final String email = emailField.getText().trim();
        final String password = passwordField.isVisible() ? passwordField.getText() : passwordVisibleField.getText();

        loginButton.setDisable(true);
        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // simula latencia; en producción llamar authService directamente
                Thread.sleep(300);
                return authService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(evt -> {
            User user = loginTask.getValue();
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);

            if (user != null) {
                showAlert("Inicio de sesión exitoso. Redirigiendo...", "success");

                // opcional: persistir "remember me"
                if (rememberMeCheckBox.isSelected()) {
                    // guardar preferencia (implementar)
                }

                if (onLoginSuccess != null) onLoginSuccess.run();
            } else {
                showAlert("Correo o contraseña incorrectos", "error");
                loginButton.setDisable(false);
            }
        });

        loginTask.setOnFailed(evt -> {
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
            Throwable ex = loginTask.getException();
            showAlert("Error: " + (ex != null ? ex.getMessage() : "intenta nuevamente"), "error");
            loginButton.setDisable(false);
        });

        new Thread(loginTask, "login-task-thread").start();
    }

    // Exponer servicio (para tests)
    public AuthService getAuthService() {
        return authService;
    }
}
