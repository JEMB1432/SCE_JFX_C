package org.jemb.sce_jfx.views.admin;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.StudentController;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.utils.FormValidator;

import java.time.LocalDate;

/**
 * Diálogo para crear y editar estudiantes con diseño compacto en dos columnas
 */
public class StudentFormDialog extends Dialog<Student> {
    private final StudentController studentController;
    private final Student existingStudent;
    private final boolean isEditMode;

    private TextField studentCodeField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private DatePicker dateOfBirthPicker;
    private ComboBox<Integer> semesterCombo;
    private TextArea addressArea;
    private ComboBox<String> statusCombo;

    private Label studentCodeError;
    private Label firstNameError;
    private Label lastNameError;
    private Label emailError;
    private Label phoneError;
    private Label dateOfBirthError;
    private Label semesterError;

    public StudentFormDialog(Student student) {
        this.studentController = new StudentController();
        this.existingStudent = student;
        this.isEditMode = (student != null);

        initializeDialog();
        createCompactForm();
        setupValidation();
        setupResult();

        if (isEditMode) {
            loadExistingData();
        }
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Estudiante" : "Nuevo Estudiante");
        setHeaderText(null);

        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/dialogs.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/students.css").toExternalForm()
        );

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButtonType, cancelButtonType);

        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("primary-button");

        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("cancel-button");
    }

    private void createCompactForm() {
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(16));
        formContainer.setPrefWidth(550);

        Label formTitle = new Label(isEditMode ? "Modificar Información de Estudiante" : "Registrar Nuevo Estudiante");
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

        VBox codeSection = createLabeledField("Código de Estudiante *",
                studentCodeField = createTextField("Ej: EST001"),
                studentCodeError = createErrorLabel());
        formGrid.add(codeSection, 0, 0);

        VBox dobSection = createLabeledField("Fecha de Nacimiento",
                dateOfBirthPicker = createDatePicker(),
                dateOfBirthError = createErrorLabel());
        formGrid.add(dobSection, 1, 0);

        VBox firstNameSection = createLabeledField("Nombre *",
                firstNameField = createTextField("Ingrese el nombre"),
                firstNameError = createErrorLabel());
        formGrid.add(firstNameSection, 0, 1);

        VBox lastNameSection = createLabeledField("Apellido *",
                lastNameField = createTextField("Ingrese el apellido"),
                lastNameError = createErrorLabel());
        formGrid.add(lastNameSection, 1, 1);

        VBox emailSection = createLabeledField("Correo Electrónico *",
                emailField = createTextField("ejemplo@correo.com"),
                emailError = createErrorLabel());
        formGrid.add(emailSection, 0, 2);

        VBox phoneSection = createLabeledField("Teléfono",
                phoneField = createTextField("10 dígitos"),
                phoneError = createErrorLabel());
        formGrid.add(phoneSection, 1, 2);

        VBox semesterSection = createLabeledField("Semestre *",
                createSemesterCombo(),
                semesterError = createErrorLabel());
        formGrid.add(semesterSection, 0, 3);

        if (isEditMode) {
            VBox statusSection = createLabeledField("Estado",
                    createStatusCombo(),
                    createErrorLabel());
            formGrid.add(statusSection, 1, 3);
        }

        VBox addressSection = new VBox(6);
        Label addressLabel = new Label("Dirección");
        addressLabel.getStyleClass().add("form-label");

        addressArea = new TextArea();
        addressArea.setPromptText("Ingrese la dirección completa (opcional)");
        addressArea.setPrefRowCount(2); // Menos filas para compactar
        addressArea.setPrefHeight(60);
        addressArea.getStyleClass().add("form-field");

        addressSection.getChildren().addAll(addressLabel, addressArea);

        int addressRow = isEditMode ? 4 : 4;
        formGrid.add(addressSection, 0, addressRow, 2, 1); // Ocupa 2 columnas

        if (isEditMode) {
            studentCodeField.setDisable(true);
            studentCodeField.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
        }

        formContainer.getChildren().addAll(
                formTitle,
                new Separator(),
                formGrid
        );

        Label noteLabel = new Label("* Campos obligatorios");
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        formContainer.getChildren().add(noteLabel);

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

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPromptText("Seleccione la fecha");
        picker.getStyleClass().add("form-field");
        picker.setPrefHeight(35);
        return picker;
    }

    private ComboBox<Integer> createSemesterCombo() {
        semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        semesterCombo.setValue(1);
        semesterCombo.setPromptText("Seleccione semestre");
        semesterCombo.getStyleClass().add("form-field");
        semesterCombo.setPrefHeight(35);
        semesterCombo.setMaxWidth(Double.MAX_VALUE);
        return semesterCombo;
    }

    private ComboBox<String> createStatusCombo() {
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "inactive", "graduated");
        statusCombo.setValue("active");
        statusCombo.getStyleClass().add("form-field");
        statusCombo.setPrefHeight(35);
        statusCombo.setMaxWidth(Double.MAX_VALUE);

        return statusCombo;
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
        studentCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateStudentCode();
        });

        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateFirstName();
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateLastName();
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateEmail();
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            validatePhone();
        });

        dateOfBirthPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateDateOfBirth();
        });

        semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSemester();
        });
    }

    private boolean validateStudentCode() {
        String code = studentCodeField.getText();

        if (FormValidator.isEmpty(code)) {
            showError(studentCodeField, studentCodeError, "El código de estudiante es obligatorio");
            return false;
        }

        if (!FormValidator.isAlphanumeric(code)) {
            showError(studentCodeField, studentCodeError, "El código solo puede contener letras y números");
            return false;
        }

        if (!FormValidator.hasMinLength(code, 3)) {
            showError(studentCodeField, studentCodeError, "El código debe tener al menos 3 caracteres");
            return false;
        }

        if (!FormValidator.hasMaxLength(code, 20)) {
            showError(studentCodeField, studentCodeError, "El código no puede exceder 20 caracteres");
            return false;
        }

        clearError(studentCodeField, studentCodeError);
        return true;
    }

    private boolean validateFirstName() {
        String name = firstNameField.getText();

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
        String lastName = lastNameField.getText();

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

    private boolean validateEmail() {
        String email = emailField.getText();

        if (FormValidator.isEmpty(email)) {
            showError(emailField, emailError, "El correo electrónico es obligatorio");
            return false;
        }

        if (!FormValidator.isValidEmail(email)) {
            showError(emailField, emailError, FormValidator.getEmailErrorMessage());
            return false;
        }

        clearError(emailField, emailError);
        return true;
    }

    private boolean validatePhone() {
        String phone = phoneField.getText();

        if (FormValidator.isEmpty(phone)) {
            clearError(phoneField, phoneError);
            return true;
        }

        if (!FormValidator.isValidPhone(phone)) {
            showError(phoneField, phoneError, FormValidator.getPhoneErrorMessage());
            return false;
        }

        clearError(phoneField, phoneError);
        return true;
    }

    private boolean validateDateOfBirth() {
        LocalDate date = dateOfBirthPicker.getValue();

        if (date == null) {
            clearError(dateOfBirthPicker, dateOfBirthError);
            return true;
        }

        if (!FormValidator.isValidDateOfBirth(date)) {
            showError(dateOfBirthPicker, dateOfBirthError, FormValidator.getDateErrorMessage());
            return false;
        }

        clearError(dateOfBirthPicker, dateOfBirthError);
        return true;
    }

    private boolean validateSemester() {
        if (semesterCombo.getValue() == null) {
            showError(semesterCombo, semesterError, "Debe seleccionar un semestre");
            return false;
        }

        Integer semester = semesterCombo.getValue();
        if (semester < 1 || semester > 12) {
            showError(semesterCombo, semesterError, "El semestre debe estar entre 1 y 12");
            return false;
        }

        clearError(semesterCombo, semesterError);
        return true;
    }

    private boolean validateAll() {
        boolean valid = true;

        valid &= validateStudentCode();
        valid &= validateFirstName();
        valid &= validateLastName();
        valid &= validateEmail();
        valid &= validatePhone();
        valid &= validateDateOfBirth();
        valid &= validateSemester();

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
        // Obtener el botón de guardar
        Button saveButton = (Button) getDialogPane().lookupButton(
                getDialogPane().getButtonTypes().stream()
                        .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                        .findFirst()
                        .orElse(null)
        );

        if (saveButton != null) {
            // Manejar el evento del botón guardar
            saveButton.addEventFilter(ActionEvent.ACTION, event -> {
                // Validar antes de cerrar el diálogo
                if (!validateAll()) {
                    // Mostrar alerta de validación
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Validación");
                    alert.setHeaderText("Errores en el formulario");
                    alert.setContentText("Por favor corrija los errores indicados antes de continuar.");
                    alert.showAndWait();

                    // Consumir el evento para evitar que el diálogo se cierre
                    event.consume();
                }
            });
        }

        setResultConverter(buttonType -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // En este punto, la validación ya pasó gracias al event filter
                try {
                    Student student;

                    if (isEditMode) {
                        // Modo edición - actualizar estudiante existente
                        student = existingStudent;
                        student.setFirstName(firstNameField.getText().trim());
                        student.setLastName(lastNameField.getText().trim());
                        student.setEmail(emailField.getText().trim());
                        student.setPhone(phoneField.getText().trim());
                        student.setDateOfBirth(dateOfBirthPicker.getValue());
                        student.setSemester(semesterCombo.getValue());
                        student.setAddress(addressArea.getText().trim());
                        student.setStatus(statusCombo.getValue());

                        Student updatedStudent = studentController.updateStudent(student);

                        // Mostrar mensaje de éxito
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Estudiante actualizado");
                        successAlert.setContentText("Los cambios se guardaron correctamente.");
                        successAlert.showAndWait();

                        return updatedStudent;
                    } else {
                        // Modo creación - nuevo estudiante
                        student = studentController.createStudent(
                                studentCodeField.getText().trim(),
                                firstNameField.getText().trim(),
                                lastNameField.getText().trim(),
                                emailField.getText().trim());

                        // Establecer campos opcionales
                        if (!FormValidator.isEmpty(phoneField.getText())) {
                            student.setPhone(phoneField.getText().trim());
                        }
                        if (dateOfBirthPicker.getValue() != null) {
                            student.setDateOfBirth(dateOfBirthPicker.getValue());
                        }
                        if (!FormValidator.isEmpty(addressArea.getText())) {
                            student.setAddress(addressArea.getText().trim());
                        }
                        student.setSemester(semesterCombo.getValue());

                        // Actualizar con campos opcionales
                        if (student.getPhone() != null || student.getDateOfBirth() != null
                                || student.getAddress() != null || student.getSemester() != null) {
                            student = studentController.updateStudent(student);
                        }

                        // Mostrar mensaje de éxito
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Estudiante registrado");
                        successAlert.setContentText(student.getFullName() + " ha sido registrado correctamente.");
                        successAlert.showAndWait();

                        return student;
                    }
                } catch (IllegalArgumentException e) {
                    // Errores de negocio (código duplicado, email duplicado, etc.)
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de negocio");
                    alert.setHeaderText("No se pudo guardar el estudiante");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null; // Mantener el diálogo abierto
                } catch (Exception e) {
                    // Error inesperado
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error del sistema");
                    alert.setHeaderText("Error inesperado");
                    alert.setContentText("Ocurrió un error al guardar el estudiante: " + e.getMessage());
                    alert.showAndWait();
                    return null; // Mantener el diálogo abierto
                }
            }
            return null; // Para cancelar
        });
    }

    private void loadExistingData() {
        if (existingStudent != null) {
            studentCodeField.setText(existingStudent.getStudentCode());
            firstNameField.setText(existingStudent.getFirstName());
            lastNameField.setText(existingStudent.getLastName());
            emailField.setText(existingStudent.getEmail());

            if (existingStudent.getPhone() != null) {
                phoneField.setText(existingStudent.getPhone());
            }

            if (existingStudent.getDateOfBirth() != null) {
                dateOfBirthPicker.setValue(existingStudent.getDateOfBirth());
            }

            if (existingStudent.getAddress() != null) {
                addressArea.setText(existingStudent.getAddress());
            }

            if (existingStudent.getSemester() != null) {
                semesterCombo.setValue(existingStudent.getSemester());
            }

            if (statusCombo != null && existingStudent.getStatus() != null) {
                statusCombo.setValue(existingStudent.getStatus());
            }
        }
    }
}