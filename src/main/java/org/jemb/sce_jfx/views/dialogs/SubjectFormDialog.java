package org.jemb.sce_jfx.views.dialogs;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.SubjectController;
import org.jemb.sce_jfx.models.Subject;
import org.jemb.sce_jfx.utils.FormValidator;

/**
 * Diálogo para crear y editar materias con diseño compacto en dos columnas
 */
public class SubjectFormDialog extends Dialog<Subject> {
    private final SubjectController subjectController;
    private final Subject existingSubject;
    private final boolean isEditMode;

    // Campos del formulario
    private TextField subjectCodeField;
    private TextField nameField;
    private TextArea descriptionArea;
    private Spinner<Integer> creditsSpinner;
    private Spinner<Integer> hoursPerWeekSpinner;
    private ComboBox<Integer> semesterCombo;
    private ComboBox<String> statusCombo;

    // Labels de error
    private Label subjectCodeError;
    private Label nameError;
    private Label creditsError;
    private Label hoursPerWeekError;
    private Label semesterError;

    public SubjectFormDialog(Subject subject) {
        this.subjectController = new SubjectController();
        this.existingSubject = subject;
        this.isEditMode = (subject != null);

        initializeDialog();
        createCompactForm();
        setupValidation();
        setupResult();

        DialogUtils.setDialogIcon(this);

        if (isEditMode) {
            loadExistingData();
        }
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Materia" : "Nueva Materia");
        setHeaderText(null);

        // Aplicar estilos
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/dialogs.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm()
        );

        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Estilizar botones
        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.getStyleClass().addAll("primary-button");

        Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("cancel-button");
    }

    private void createCompactForm() {
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(16));
        formContainer.setPrefWidth(550); // Ancho reducido

        // Título del formulario
        Label formTitle = new Label(isEditMode ? "Modificar Información de Materia" : "Registrar Nueva Materia");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.getStyleClass().add("form-title");

        // Grid principal para dos columnas
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        // Configurar columnas
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        formGrid.getColumnConstraints().addAll(col1, col2);

        // === PRIMERA FILA: Código y Nombre ===
        // Columna 1: Código
        VBox codeSection = createLabeledField("Código de Materia *",
                subjectCodeField = createTextField("Ej: MAT001"),
                subjectCodeError = createErrorLabel());
        formGrid.add(codeSection, 0, 0);

        // Columna 2: Nombre
        VBox nameSection = createLabeledField("Nombre *",
                nameField = createTextField("Ingrese el nombre"),
                nameError = createErrorLabel());
        formGrid.add(nameSection, 1, 0);

        // === SEGUNDA FILA: Créditos y Horas ===
        // Columna 1: Créditos
        VBox creditsSection = createLabeledField("Créditos *",
                createCreditsSpinner(),
                creditsError = createErrorLabel());
        formGrid.add(creditsSection, 0, 1);

        // Columna 2: Horas por semana
        VBox hoursSection = createLabeledField("Horas por Semana *",
                createHoursPerWeekSpinner(),
                hoursPerWeekError = createErrorLabel());
        formGrid.add(hoursSection, 1, 1);

        // === TERCERA FILA: Semestre y Estado ===
        // Columna 1: Semestre
        VBox semesterSection = createLabeledField("Semestre Disponible",
                createSemesterCombo(),
                semesterError = createErrorLabel());
        formGrid.add(semesterSection, 0, 2);

        // Columna 2: Estado (solo en edición)
        if (isEditMode) {
            VBox statusSection = createLabeledField("Estado",
                    createStatusCombo(),
                    createErrorLabel()); // Error label vacío para alineación
            formGrid.add(statusSection, 1, 2);
        }

        // === CUARTA FILA: Descripción (ocupa ambas columnas) ===
        VBox descriptionSection = new VBox(6);
        Label descriptionLabel = new Label("Descripción");
        descriptionLabel.getStyleClass().add("form-label");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Ingrese una descripción de la materia (opcional)");
        descriptionArea.setPrefRowCount(2); // Menos filas para compactar
        descriptionArea.setPrefHeight(60);
        descriptionArea.getStyleClass().add("form-field");

        descriptionSection.getChildren().addAll(descriptionLabel, descriptionArea);
        formGrid.add(descriptionSection, 0, 3, 2, 1); // Ocupa 2 columnas

        // Deshabilitar código en modo edición
        if (isEditMode) {
            subjectCodeField.setDisable(true);
            subjectCodeField.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
        }

        // Agregar todos los componentes al contenedor principal
        formContainer.getChildren().addAll(
                formTitle,
                new Separator(),
                formGrid
        );

        // Nota sobre campos obligatorios
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

    private Spinner<Integer> createCreditsSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 3);
        creditsSpinner = new Spinner<>(factory);
        creditsSpinner.setEditable(true);
        creditsSpinner.getStyleClass().add("form-field");
        creditsSpinner.setPrefHeight(35);

        // Hacer que el spinner ocupe todo el ancho disponible
        creditsSpinner.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(creditsSpinner, Priority.ALWAYS);

        return creditsSpinner;
    }

    private Spinner<Integer> createHoursPerWeekSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 40, 4);
        hoursPerWeekSpinner = new Spinner<>(factory);
        hoursPerWeekSpinner.setEditable(true);
        hoursPerWeekSpinner.getStyleClass().add("form-field");
        hoursPerWeekSpinner.setPrefHeight(35);
        hoursPerWeekSpinner.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(hoursPerWeekSpinner, Priority.ALWAYS);

        return hoursPerWeekSpinner;
    }

    private ComboBox<Integer> createSemesterCombo() {
        semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        semesterCombo.setPromptText("Seleccione semestre");
        semesterCombo.getStyleClass().add("form-field");
        semesterCombo.setPrefHeight(35);
        semesterCombo.setMaxWidth(Double.MAX_VALUE);

        return semesterCombo;
    }

    private ComboBox<String> createStatusCombo() {
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "inactive");
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
        // Validación en tiempo real para código de materia
        subjectCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSubjectCode();
        });

        // Validación en tiempo real para nombre
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateName();
        });

        // Validación para créditos
        creditsSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateCredits();
        });

        // Validación para horas por semana
        hoursPerWeekSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateHoursPerWeek();
        });

        // Validación para semestre
        if (semesterCombo != null) {
            semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateSemester();
            });
        }
    }

    // Los métodos de validación (validateSubjectCode, validateName, etc.)
    // se mantienen igual que en tu código original...
    private boolean validateSubjectCode() {
        String code = subjectCodeField.getText();

        if (FormValidator.isEmpty(code)) {
            showError(subjectCodeField, subjectCodeError, "El código de materia es obligatorio");
            return false;
        }

        if (!FormValidator.isAlphanumeric(code)) {
            showError(subjectCodeField, subjectCodeError, "El código solo puede contener letras y números");
            return false;
        }

        if (!FormValidator.hasMinLength(code, 3)) {
            showError(subjectCodeField, subjectCodeError, "El código debe tener al menos 3 caracteres");
            return false;
        }

        if (!FormValidator.hasMaxLength(code, 20)) {
            showError(subjectCodeField, subjectCodeError, "El código no puede exceder 20 caracteres");
            return false;
        }

        clearError(subjectCodeField, subjectCodeError);
        return true;
    }

    private boolean validateName() {
        String name = nameField.getText();

        if (FormValidator.isEmpty(name)) {
            showError(nameField, nameError, "El nombre es obligatorio");
            return false;
        }

        if (!FormValidator.hasMinLength(name, 3)) {
            showError(nameField, nameError, "El nombre debe tener al menos 3 caracteres");
            return false;
        }

        if (!FormValidator.hasMaxLength(name, 200)) {
            showError(nameField, nameError, "El nombre no puede exceder 200 caracteres");
            return false;
        }

        clearError(nameField, nameError);
        return true;
    }

    private boolean validateCredits() {
        Integer credits = creditsSpinner.getValue();

        if (credits == null || credits <= 0) {
            showError(creditsSpinner, creditsError, "Los créditos deben ser mayores a 0");
            return false;
        }

        if (credits > 20) {
            showError(creditsSpinner, creditsError, "Los créditos no pueden ser mayores a 20");
            return false;
        }

        clearError(creditsSpinner, creditsError);
        return true;
    }

    private boolean validateHoursPerWeek() {
        Integer hours = hoursPerWeekSpinner.getValue();

        if (hours == null || hours <= 0) {
            showError(hoursPerWeekSpinner, hoursPerWeekError, "Las horas por semana deben ser mayores a 0");
            return false;
        }

        if (hours > 40) {
            showError(hoursPerWeekSpinner, hoursPerWeekError, "Las horas por semana no pueden ser mayores a 40");
            return false;
        }

        clearError(hoursPerWeekSpinner, hoursPerWeekError);
        return true;
    }

    private boolean validateSemester() {
        Integer semester = semesterCombo.getValue();

        // El semestre es opcional
        if (semester == null) {
            clearError(semesterCombo, semesterError);
            return true;
        }

        if (semester < 1 || semester > 12) {
            showError(semesterCombo, semesterError, "El semestre debe estar entre 1 y 12");
            return false;
        }

        clearError(semesterCombo, semesterError);
        return true;
    }

    private boolean validateAll() {
        boolean valid = true;

        valid &= validateSubjectCode();
        valid &= validateName();
        valid &= validateCredits();
        valid &= validateHoursPerWeek();
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
                    Subject subject;

                    if (isEditMode) {
                        subject = existingSubject;
                        subject.setName(nameField.getText().trim());
                        subject.setDescription(descriptionArea.getText().trim());
                        subject.setCredits(creditsSpinner.getValue());
                        subject.setHoursPerWeek(hoursPerWeekSpinner.getValue());
                        subject.setSemesterAvailable(semesterCombo.getValue());
                        subject.setStatus(statusCombo.getValue());

                        Subject updatedSubject = subjectController.updateSubject(subject);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Materia actualizada");
                        successAlert.setContentText("Los cambios se guardaron correctamente.");
                        successAlert.showAndWait();

                        return updatedSubject;
                    } else {
                        subject = subjectController.createSubject(
                                subjectCodeField.getText().trim(),
                                nameField.getText().trim(),
                                creditsSpinner.getValue());

                        if (!FormValidator.isEmpty(descriptionArea.getText())) {
                            subject.setDescription(descriptionArea.getText().trim());
                        }
                        subject.setHoursPerWeek(hoursPerWeekSpinner.getValue());
                        if (semesterCombo.getValue() != null) {
                            subject.setSemesterAvailable(semesterCombo.getValue());
                        }

                        if (subject.getDescription() != null || subject.getSemesterAvailable() != null) {
                            subject = subjectController.updateSubject(subject);
                        }

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Materia registrada");
                        successAlert.setContentText(subject.getName() + " ha sido registrada correctamente.");
                        successAlert.showAndWait();

                        return subject;
                    }
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de negocio");
                    alert.setHeaderText("No se pudo guardar la materia");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                } catch (Exception e) {
                    // Error inesperado
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error del sistema");
                    alert.setHeaderText("Error inesperado");
                    alert.setContentText("Ocurrió un error al guardar la materia: " + e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }

    private void loadExistingData() {
        if (existingSubject != null) {
            subjectCodeField.setText(existingSubject.getSubjectCode());
            nameField.setText(existingSubject.getName());

            if (existingSubject.getDescription() != null) {
                descriptionArea.setText(existingSubject.getDescription());
            }

            creditsSpinner.getValueFactory().setValue(existingSubject.getCredits());
            hoursPerWeekSpinner.getValueFactory().setValue(existingSubject.getHoursPerWeek());

            if (existingSubject.getSemesterAvailable() != null) {
                semesterCombo.setValue(existingSubject.getSemesterAvailable());
            }

            if (statusCombo != null && existingSubject.getStatus() != null) {
                statusCombo.setValue(existingSubject.getStatus());
            }
        }
    }
}