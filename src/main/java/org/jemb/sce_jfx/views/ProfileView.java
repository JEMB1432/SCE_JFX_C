package org.jemb.sce_jfx.views;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.jemb.sce_jfx.controllers.UserController;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.services.AuthService;
import org.jemb.sce_jfx.utils.UserSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProfileView extends VBox {

    private final User currentUser;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private ImageView userAvatar;
    private Button editButton;
    private Button saveButton;
    private Button cancelButton;
    private boolean editMode = false;

    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField visiblePasswordField;
    private TextField visibleConfirmPasswordField;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    private final UserController userController;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,49}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^.{6,}$");

    private final Map<TextField, PauseTransition> validationPauses = new HashMap<>();

    public ProfileView() {
        currentUser = UserSession.getInstance().getCurrentUser();
        userController = new UserController();

        try {
            getStylesheets().addAll(
                    getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                    getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                    getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                    getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm(),
                    getClass().getResource("/org/jemb/sce_jfx/styles/profile.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Error cargando estilos: " + e.getMessage());
        }

        setPadding(new Insets(30));
        setSpacing(20);
        getStyleClass().add("profile-view");

        // Estructura principal
        getChildren().addAll(
                createHeader(),
                createContent());
    }

    private VBox createHeader() {
        Label title = new Label("Mi Perfil");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona tu información personal y credenciales");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createContent() {
        HBox content = new HBox(40);
        content.setAlignment(Pos.TOP_LEFT);

        // Sección Izquierda: Avatar
        VBox avatarSection = createAvatarSection();

        // Sección Derecha: Formulario
        VBox formSection = createFormSection();
        HBox.setHgrow(formSection, Priority.ALWAYS);

        content.getChildren().addAll(avatarSection, formSection);
        return content;
    }

    private VBox createAvatarSection() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_CENTER);
        container.getStyleClass().add("avatar-container");
        container.setPrefWidth(250);

        // Usar placeholder image
        String imageUrl = "/org/jemb/sce_jfx/images/profilePlaceholder.png";
        Image image;
        try {
            image = new Image(getClass().getResourceAsStream(imageUrl));
        } catch (Exception e) {
            // Crear una imagen placeholder en código si falla
            image = createPlaceholderImage();
        }

        userAvatar = new ImageView(image);
        userAvatar.setFitWidth(150);
        userAvatar.setFitHeight(150);
        userAvatar.setPreserveRatio(true);

        Circle clip = new Circle(75, 75, 75);
        userAvatar.setClip(clip);
        userAvatar.getStyleClass().add("user-avatar");

        Label nameLabel = new Label(currentUser.getFullName());
        nameLabel.getStyleClass().add("profile-name");

        Label roleLabel = new Label(getRoleDisplayName(currentUser.getRole()));
        roleLabel.getStyleClass().addAll("badge", "role-" + currentUser.getRole());

        container.getChildren().addAll(userAvatar, nameLabel, roleLabel);
        return container;
    }

    private Image createPlaceholderImage() {
        // Retornar una imagen simple si no se encuentra el recurso
        try {
            return new Image("https://via.placeholder.com/150");
        } catch (Exception e) {
            return null;
        }
    }

    private String getRoleDisplayName(String role) {
        switch (role) {
            case "admin":
                return "Administrador";
            case "teacher":
                return "Profesor";
            case "assistant":
                return "Asistente";
            default:
                return role;
        }
    }

    private VBox createFormSection() {
        VBox container = new VBox(20);
        container.getStyleClass().add("form-container");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Fila 1: Nombre y Apellido
        firstNameField = createTextField(currentUser.getFirstName());
        lastNameField = createTextField(currentUser.getLastName());

        addFormField(grid, "Nombre", firstNameField, 0, 0);
        addFormField(grid, "Apellido", lastNameField, 1, 0);

        // Fila 2: Email
        emailField = createTextField(currentUser.getEmail());
        VBox emailBox = new VBox(5);
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("form-label");
        emailBox.getChildren().addAll(emailLabel, emailField);
        grid.add(emailBox, 0, 1, 2, 1); // Columna 0, fila 1, span 2 columnas

        // Separador
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Fila 3: Contraseña
        Label passwordHeader = new Label("Cambiar Contraseña");
        passwordHeader.getStyleClass().add("section-header");

        StackPane passwordContainer = createPasswordFieldWithOverlayIcon(true);
        StackPane confirmPasswordContainer = createPasswordFieldWithOverlayIcon(false);

        GridPane passwordGrid = createPasswordGrid(passwordContainer, confirmPasswordContainer);

        // Botones
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        editButton = new Button("Editar Perfil");
        editButton.getStyleClass().add("primary-button");

        saveButton = new Button("Guardar Cambios");
        saveButton.getStyleClass().add("primary-button");
        saveButton.setVisible(false);
        saveButton.setManaged(false);

        cancelButton = new Button("Cancelar");
        cancelButton.getStyleClass().add("secondary-button");
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);

        buttonBox.getChildren().addAll(cancelButton, saveButton, editButton);

        // Ensamblar todo
        container.getChildren().addAll(
                grid,
                separator,
                passwordHeader,
                passwordGrid,
                buttonBox);

        setupButtonActions();
        setupValidationPauses();

        return container;
    }

    private void addFormField(GridPane grid, String labelText, Control field, int col, int row) {
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        VBox box = new VBox(5, label, field);
        grid.add(box, col, row);
    }

    private GridPane createPasswordGrid(StackPane pass, StackPane confirm) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Crear contenedores para los campos de contraseña
        Label passwordLabel = new Label("Nueva Contraseña");
        passwordLabel.getStyleClass().add("form-label");
        VBox passwordBox = new VBox(5, passwordLabel, pass);
        grid.add(passwordBox, 0, 0);

        Label confirmLabel = new Label("Confirmar Contraseña");
        confirmLabel.getStyleClass().add("form-label");
        VBox confirmBox = new VBox(5, confirmLabel, confirm);
        grid.add(confirmBox, 1, 0);

        return grid;
    }

    private TextField createTextField(String value) {
        TextField field = new TextField(value);
        field.setDisable(true);
        field.getStyleClass().add("form-field");
        return field;
    }

    private StackPane createPasswordFieldWithOverlayIcon(boolean isPasswordField) {
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER_RIGHT);

        if (isPasswordField) {
            passwordField = new PasswordField();
            passwordField.setDisable(true);
            passwordField.getStyleClass().add("form-field");

            visiblePasswordField = new TextField();
            visiblePasswordField.setDisable(true);
            visiblePasswordField.getStyleClass().add("form-field");
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);

            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (passwordVisible && !visiblePasswordField.getText().equals(newVal)) {
                    visiblePasswordField.setText(newVal);
                }
            });

            visiblePasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (passwordVisible && !passwordField.getText().equals(newVal)) {
                    passwordField.setText(newVal);
                }
            });

            container.getChildren().addAll(passwordField, visiblePasswordField);
        } else {
            confirmPasswordField = new PasswordField();
            confirmPasswordField.setDisable(true);
            confirmPasswordField.getStyleClass().add("form-field");

            visibleConfirmPasswordField = new TextField();
            visibleConfirmPasswordField.setDisable(true);
            visibleConfirmPasswordField.getStyleClass().add("form-field");
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);

            confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (confirmPasswordVisible && !visibleConfirmPasswordField.getText().equals(newVal)) {
                    visibleConfirmPasswordField.setText(newVal);
                }
            });

            visibleConfirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (confirmPasswordVisible && !confirmPasswordField.getText().equals(newVal)) {
                    confirmPasswordField.setText(newVal);
                }
            });

            container.getChildren().addAll(confirmPasswordField, visibleConfirmPasswordField);
        }

        Button toggleButton = new Button("👁");
        toggleButton.getStyleClass().add("icon-button");
        toggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
        toggleButton.setFocusTraversable(false);
        toggleButton.setTooltip(new Tooltip("Mostrar contraseña"));

        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggleButton, new Insets(0, 10, 0, 0));

        toggleButton.setOnAction(e -> {
            boolean isVisible;
            if (isPasswordField) {
                togglePasswordVisibility();
                isVisible = passwordVisible;
            } else {
                toggleConfirmPasswordVisibility();
                isVisible = confirmPasswordVisible;
            }
            toggleButton.setText(isVisible ? "🙈" : "👁");
            toggleButton.setTooltip(new Tooltip(isVisible ? "Ocultar contraseña" : "Mostrar contraseña"));
        });

        container.getChildren().add(toggleButton);
        return container;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        if (confirmPasswordVisible) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.setVisible(true);
            visibleConfirmPasswordField.setManaged(true);
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(false);
        }
    }

    private void setupValidationPauses() {
        initValidationPause(firstNameField, NAME_PATTERN, 250);
        initValidationPause(lastNameField, NAME_PATTERN, 250);
        initValidationPause(emailField, EMAIL_PATTERN, 250);
        initValidationPause(passwordField, PASSWORD_PATTERN, 250);
        initValidationPause(visiblePasswordField, PASSWORD_PATTERN, 250);
        initPasswordConfirmationPause(confirmPasswordField, 250);
        initPasswordConfirmationPause(visibleConfirmPasswordField, 250);
    }

    private void initPasswordConfirmationPause(TextField field, int delayMs) {
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> validatePasswordConfirmation(field));
        validationPauses.put(field, pause);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (field.isDisabled())
                return;
            field.getStyleClass().removeAll("error", "success");
            PauseTransition fieldPause = validationPauses.get(field);
            if (fieldPause != null) {
                fieldPause.stop();
                fieldPause.playFromStart();
            }
        });
    }

    private void validatePasswordConfirmation(TextField field) {
        if (field.isDisabled())
            return;
        String text = field.getText().trim();
        String password = getCurrentPassword();
        field.getStyleClass().removeAll("error", "success");

        if (text.isEmpty())
            return;

        if (!text.equals(password)) {
            field.getStyleClass().add("error");
        }
    }

    private void initValidationPause(TextField field, Pattern pattern, int delayMs) {
        PauseTransition pause = new PauseTransition(Duration.millis(delayMs));
        pause.setOnFinished(e -> validateField(field, pattern));
        validationPauses.put(field, pause);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (field.isDisabled())
                return;
            field.getStyleClass().removeAll("error", "success");
            PauseTransition fieldPause = validationPauses.get(field);
            if (fieldPause != null) {
                fieldPause.stop();
                fieldPause.playFromStart();
            }
        });
    }

    private void validateField(TextField field, Pattern pattern) {
        if (field.isDisabled())
            return;
        String text = field.getText().trim();
        boolean isValid = pattern.matcher(text).matches();
        field.getStyleClass().removeAll("error", "success");

        if (text.isEmpty()) {
            return;
        }

        if (!isValid) {
            field.getStyleClass().add("error");
        }
    }

    private void setupButtonActions() {
        editButton.setOnAction(e -> enableEditMode());
        saveButton.setOnAction(e -> saveChanges());
        cancelButton.setOnAction(e -> cancelChanges());
    }

    private void enableEditMode() {
        editMode = true;
        firstNameField.setDisable(false);
        lastNameField.setDisable(false);
        emailField.setDisable(false);
        passwordField.setDisable(false);
        visiblePasswordField.setDisable(false);
        confirmPasswordField.setDisable(false);
        visibleConfirmPasswordField.setDisable(false);

        saveButton.setVisible(true);
        saveButton.setManaged(true);
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);
        editButton.setVisible(false);
        editButton.setManaged(false);
    }

    private void disableEditMode() {
        editMode = false;
        firstNameField.setDisable(true);
        lastNameField.setDisable(true);
        emailField.setDisable(true);
        passwordField.setDisable(true);
        visiblePasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        visibleConfirmPasswordField.setDisable(true);

        clearFieldStyles();

        saveButton.setVisible(false);
        saveButton.setManaged(false);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
        editButton.setVisible(true);
        editButton.setManaged(true);
    }

    private void clearFieldStyles() {
        firstNameField.getStyleClass().removeAll("error", "success");
        lastNameField.getStyleClass().removeAll("error", "success");
        emailField.getStyleClass().removeAll("error", "success");
        passwordField.getStyleClass().removeAll("error", "success");
        visiblePasswordField.getStyleClass().removeAll("error", "success");
        confirmPasswordField.getStyleClass().removeAll("error", "success");
        visibleConfirmPasswordField.getStyleClass().removeAll("error", "success");
    }

    private String getCurrentPassword() {
        return passwordVisible ? visiblePasswordField.getText().trim() : passwordField.getText().trim();
    }

    private String getCurrentConfirmPassword() {
        return confirmPasswordVisible ? visibleConfirmPasswordField.getText().trim()
                : confirmPasswordField.getText().trim();
    }

    private void saveChanges() {
        List<String> errores = new ArrayList<>();

        if (!validateFieldOnSave(firstNameField, NAME_PATTERN))
            errores.add("Nombre inválido");
        if (!validateFieldOnSave(lastNameField, NAME_PATTERN))
            errores.add("Apellido inválido");
        if (!validateFieldOnSave(emailField, EMAIL_PATTERN))
            errores.add("Email inválido");

        String password = getCurrentPassword();
        String confirmPassword = getCurrentConfirmPassword();

        if (!password.isEmpty() || !confirmPassword.isEmpty()) {
            TextField activePasswordField = passwordVisible ? visiblePasswordField : passwordField;
            if (!validateFieldOnSave(activePasswordField, PASSWORD_PATTERN)) {
                errores.add("Contraseña inválida: Mínimo 6 caracteres");
            }
            if (!password.equals(confirmPassword)) {
                errores.add("Las contraseñas no coinciden");
                TextField activeConfirmField = confirmPasswordVisible ? visibleConfirmPasswordField
                        : confirmPasswordField;
                activeConfirmField.getStyleClass().add("error");
            }
        }

        if (!errores.isEmpty()) {
            showAlert("Error", String.join("\n", errores), Alert.AlertType.ERROR);
            return;
        }

        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());

        try {
            userController.updateUser(currentUser);
            if (!password.isEmpty()) {
                userController.changePassword(currentUser.getId(), password);
                passwordField.clear();
                visiblePasswordField.clear();
                confirmPasswordField.clear();
                visibleConfirmPasswordField.clear();
            }
            showAlert("Éxito", "Perfil actualizado correctamente.", Alert.AlertType.INFORMATION);
            disableEditMode();
        } catch (Exception e) {
            showAlert("Error", "Error al actualizar el perfil: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFieldOnSave(TextField field, Pattern pattern) {
        String text = field.getText().trim();
        boolean isValid = pattern.matcher(text).matches();

        if (text.isEmpty()) {
            field.getStyleClass().add("error");
            return false;
        }

        if (!isValid) {
            if (!field.getStyleClass().contains("error")) {
                field.getStyleClass().add("error");
            }
            return false;
        }
        return true;
    }

    private void cancelChanges() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());

        passwordField.clear();
        visiblePasswordField.clear();
        confirmPasswordField.clear();
        visibleConfirmPasswordField.clear();

        passwordVisible = false;
        confirmPasswordVisible = false;
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        confirmPasswordField.setVisible(true);
        confirmPasswordField.setManaged(true);
        visibleConfirmPasswordField.setVisible(false);
        visibleConfirmPasswordField.setManaged(false);

        clearFieldStyles();
        disableEditMode();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}