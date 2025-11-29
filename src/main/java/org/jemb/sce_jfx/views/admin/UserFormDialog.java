package org.jemb.sce_jfx.views.admin;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.UserController;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.utils.FormValidator;

/**
 * Diálogo para crear y editar usuarios del sistema
 */
public class UserFormDialog extends Dialog<User> {
    private final UserController controller;
    private final User existingUser;
    private final boolean isEditMode;

    private TextField emailField;
    private TextField firstNameField;
    private TextField lastNameField;
    private ComboBox<String> roleCombo;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private CheckBox isActiveCheckBox;

    private Label emailError;
    private Label firstNameError;
    private Label lastNameError;
    private Label passwordError;
    private Label confirmPasswordError;

    public UserFormDialog(User user) {
        this.controller = new UserController();
        this.existingUser = user;
        this.isEditMode = (user != null);

        initializeDialog();
        createForm();
        setupValidation();
        setupResult();

        if (isEditMode) {
            loadExistingData();
        }
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Usuario" : "Nuevo Usuario");
        setHeaderText(null);

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/dialogs.css").toExternalForm());

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButtonType, cancelButtonType);

        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("primary-button");

        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("cancel-button");
    }

    private void createForm() {
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(16));
        formContainer.setPrefWidth(500);

        Label formTitle = new Label(isEditMode ? "Modificar Usuario" : "Registrar Nuevo Usuario");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.getStyleClass().add("form-title");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // Email
        VBox emailSection = createLabeledField("Email *",
                emailField = createTextField("usuario@ejemplo.com"),
                emailError = createErrorLabel());
        formGrid.add(emailSection, 0, 0, 2, 1);

        // Nombre
        VBox firstNameSection = createLabeledField("Nombre *",
                firstNameField = createTextField("Ingrese el nombre"),
                firstNameError = createErrorLabel());
        formGrid.add(firstNameSection, 0, 1);

        // Apellido
        VBox lastNameSection = createLabeledField("Apellido *",
                lastNameField = createTextField("Ingrese el apellido"),
                lastNameError = createErrorLabel());
        formGrid.add(lastNameSection, 1, 1);

        // Rol
        VBox roleSection = createLabeledField("Rol *",
                createRoleCombo(),
                createErrorLabel());
        formGrid.add(roleSection, 0, 2);

        // Estado (solo en edición)
        if (isEditMode) {
            VBox statusSection = new VBox(6);
            Label statusLabel = new Label("Estado");
            statusLabel.getStyleClass().add("form-label");

            isActiveCheckBox = new CheckBox("Usuario activo");
            isActiveCheckBox.getStyleClass().add("form-field");

            statusSection.getChildren().addAll(statusLabel, isActiveCheckBox);
            formGrid.add(statusSection, 1, 2);
        }

        // Contraseña
        VBox passwordSection = createLabeledField(
                isEditMode ? "Nueva Contraseña (dejar en blanco para no cambiar)" : "Contraseña *",
                passwordField = createPasswordField("Mínimo 6 caracteres"),
                passwordError = createErrorLabel());
        formGrid.add(passwordSection, 0, 3);

        // Confirmar contraseña
        VBox confirmPasswordSection = createLabeledField(
                isEditMode ? "Confirmar Nueva Contraseña" : "Confirmar Contraseña *",
                confirmPasswordField = createPasswordField("Repita la contraseña"),
                confirmPasswordError = createErrorLabel());
        formGrid.add(confirmPasswordSection, 1, 3);

        if (isEditMode) {
            emailField.setDisable(true);
            emailField.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
        }

        formContainer.getChildren().addAll(
                formTitle,
                new Separator(),
                formGrid);

        Label noteLabel = new Label("* Campos obligatorios");
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        formContainer.getChildren().add(noteLabel);

        if (isEditMode) {
            Label passwordNote = new Label("Deja los campos de contraseña en blanco si no deseas cambiarla");
            passwordNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
            formContainer.getChildren().add(passwordNote);
        }

        getDialogPane().setContent(formContainer);
    }

    private VBox createLabeledField(String labelText, Control field, Label errorLabel) {
        VBox container = new VBox(4);

        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");

        VBox fieldContainer = new VBox(2);
        fieldContainer.getChildren().addAll(field, errorLabel);

        container.getChildren().addAll(label, fieldContainer);
        return container;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-field");
        field.setPrefHeight(35);
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-field");
        field.setPrefHeight(35);
        return field;
    }

    private ComboBox<String> createRoleCombo() {
        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "teacher", "assistant");
        roleCombo.setValue("teacher");
        roleCombo.getStyleClass().add("form-field");
        roleCombo.setPrefHeight(35);
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        // Mostrar roles en español
        roleCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(UserController.getRoleDisplayName(item));
                }
            }
        });

        roleCombo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(UserController.getRoleDisplayName(item));
                }
            }
        });

        return roleCombo;
    }

    private Label createErrorLabel() {
        Label label = new Label();
        label.getStyleClass().add("error-label");
        label.setVisible(false);
        label.setManaged(false);
        label.setStyle("-fx-font-size: 11px;");
        return label;
    }

    private void setupValidation() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail());
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateFirstName());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateLastName());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validatePassword());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateConfirmPassword());
    }

    private boolean validateEmail() {
        String email = emailField.getText().trim();

        if (FormValidator.isEmpty(email)) {
            showError(emailField, emailError, "El email es obligatorio");
            return false;
        }

        if (!FormValidator.isValidEmail(email)) {
            showError(emailField, emailError, "Formato de email inválido");
            return false;
        }

        // Validar email único (solo en creación o si cambió el email)
        if (!isEditMode) {
            if (controller.emailExists(email)) {
                showError(emailField, emailError, "Este email ya está registrado");
                return false;
            }
        }

        clearError(emailField, emailError);
        return true;
    }

    private boolean validateFirstName() {
        String name = firstNameField.getText().trim();

        if (FormValidator.isEmpty(name)) {
            showError(firstNameField, firstNameError, "El nombre es obligatorio");
            return false;
        }

        if (!FormValidator.hasMinLength(name, 2)) {
            showError(firstNameField, firstNameError, "El nombre debe tener al menos 2 caracteres");
            return false;
        }

        clearError(firstNameField, firstNameError);
        return true;
    }

    private boolean validateLastName() {
        String lastName = lastNameField.getText().trim();

        if (FormValidator.isEmpty(lastName)) {
            showError(lastNameField, lastNameError, "El apellido es obligatorio");
            return false;
        }

        if (!FormValidator.hasMinLength(lastName, 2)) {
            showError(lastNameField, lastNameError, "El apellido debe tener al menos 2 caracteres");
            return false;
        }

        clearError(lastNameField, lastNameError);
        return true;
    }

    private boolean validatePassword() {
        String password = passwordField.getText();

        // En modo edición, contraseña es opcional
        if (isEditMode && password.isEmpty()) {
            clearError(passwordField, passwordError);
            clearError(confirmPasswordField, confirmPasswordError);
            return true;
        }

        // En modo creación, contraseña es obligatoria
        if (!isEditMode && password.isEmpty()) {
            showError(passwordField, passwordError, "La contraseña es obligatoria");
            return false;
        }

        if (password.length() < 6) {
            showError(passwordField, passwordError, "La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        clearError(passwordField, passwordError);
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // En modo edición, si ambos están vacíos, está bien
        if (isEditMode && password.isEmpty() && confirmPassword.isEmpty()) {
            clearError(confirmPasswordField, confirmPasswordError);
            return true;
        }

        // Si hay contraseña, debe coincidir con la confirmación
        if (!password.equals(confirmPassword)) {
            showError(confirmPasswordField, confirmPasswordError, "Las contraseñas no coinciden");
            return false;
        }

        clearError(confirmPasswordField, confirmPasswordError);
        return true;
    }

    private boolean validateAll() {
        boolean valid = true;

        valid &= validateEmail();
        valid &= validateFirstName();
        valid &= validateLastName();
        valid &= validatePassword();
        valid &= validateConfirmPassword();

        return valid;
    }

    private void showError(Control field, Label errorLabel, String message) {
        FormValidator.addErrorStyle(field);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError(Control field, Label errorLabel) {
        FormValidator.removeErrorStyle(field);
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void setupResult() {
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().stream()
                        .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                        .findFirst()
                        .orElse(null));

        if (saveButton != null) {
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!validateAll()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Validación");
                    alert.setHeaderText("Errores en el formulario");
                    alert.setContentText("Por favor corrija los errores indicados antes de continuar.");
                    alert.showAndWait();
                    event.consume();
                }
            });
        }

        setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    String email = emailField.getText().trim();
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    String role = roleCombo.getValue();
                    String password = passwordField.getText();

                    User user;

                    if (isEditMode) {
                        // Modo edición
                        user = existingUser;
                        user.setFirstName(firstName);
                        user.setLastName(lastName);
                        user.setRole(role);
                        user.setActive(isActiveCheckBox.isSelected());

                        // Si se ingresó nueva contraseña, actualizarla
                        if (!password.isEmpty()) {
                            user = controller.updateUserWithPassword(user, password);
                        } else {
                            user = controller.updateUser(user);
                        }
                    } else {
                        // Modo creación
                        user = controller.createUser(email, password, role, firstName, lastName);
                    }

                    return user;
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo guardar el usuario");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error del sistema");
                    alert.setHeaderText("Error inesperado");
                    alert.setContentText("Ocurrió un error: " + e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }

    private void loadExistingData() {
        if (existingUser != null) {
            emailField.setText(existingUser.getEmail());
            firstNameField.setText(existingUser.getFirstName());
            lastNameField.setText(existingUser.getLastName());
            roleCombo.setValue(existingUser.getRole());

            if (isActiveCheckBox != null) {
                isActiveCheckBox.setSelected(existingUser.isActive());
            }

            // Dejar contraseñas vacías
            passwordField.setText("");
            confirmPasswordField.setText("");
        }
    }
}
