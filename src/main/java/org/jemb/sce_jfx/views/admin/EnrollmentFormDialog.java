package org.jemb.sce_jfx.views.admin;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.StudentController;
import org.jemb.sce_jfx.controllers.SubjectController;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.Subject;
import org.jemb.sce_jfx.utils.FormValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Diálogo para crear y editar inscripciones con diseño compacto en dos columnas
 */
public class EnrollmentFormDialog extends Dialog<Enrollment> {
    private final EnrollmentController enrollmentController;
    private final StudentController studentController;
    private final SubjectController subjectController;
    private final Enrollment existingEnrollment;
    private final boolean isEditMode;

    // Campos del formulario
    private ComboBox<Student> studentCombo;
    private ComboBox<Subject> subjectCombo;
    private TextField academicYearField;
    private ComboBox<Integer> semesterCombo;
    private DatePicker enrollmentDatePicker;
    private ComboBox<String> statusCombo;

    // Labels de error
    private Label studentError;
    private Label subjectError;
    private Label academicYearError;
    private Label semesterError;
    private Label enrollmentDateError;

    public EnrollmentFormDialog(Enrollment enrollment) {
        this.enrollmentController = new EnrollmentController();
        this.studentController = new StudentController();
        this.subjectController = new SubjectController();
        this.existingEnrollment = enrollment;
        this.isEditMode = (enrollment != null);

        initializeDialog();
        createCompactForm();
        setupValidation();
        setupResult();

        // Cargar materias inicialmente (todas las activas si no hay estudiante seleccionado)
        updateSubjectCombo();

        if (isEditMode) {
            loadExistingData();
        }
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Inscripción" : "Nueva Inscripción");
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

    private void createCompactForm() {
        VBox formContainer = new VBox(12);
        formContainer.setPadding(new Insets(16));
        formContainer.setPrefWidth(550);

        Label formTitle = new Label(
                isEditMode ? "Modificar Información de Inscripción" : "Registrar Nueva Inscripción");
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

        studentError = createErrorLabel();
        subjectError = createErrorLabel();
        academicYearError = createErrorLabel();
        semesterError = createErrorLabel();
        enrollmentDateError = createErrorLabel();

        VBox studentSection = createLabeledField("Estudiante *",
                createStudentCombo(),
                studentError);
        formGrid.add(studentSection, 0, 0);

        VBox subjectSection = createLabeledField("Materia *",
                createSubjectCombo(),
                subjectError);
        formGrid.add(subjectSection, 1, 0);

        VBox academicYearSection = createLabeledField("Año Académico *",
                academicYearField = createTextField("Ej: 2024-2025"),
                academicYearError);
        formGrid.add(academicYearSection, 0, 1);

        VBox semesterSection = createLabeledField("Semestre *",
                createSemesterCombo(),
                semesterError);
        formGrid.add(semesterSection, 1, 1);

        VBox enrollmentDateSection = createLabeledField("Fecha de Inscripción",
                enrollmentDatePicker = createDatePicker(),
                enrollmentDateError);
        formGrid.add(enrollmentDateSection, 0, 2);

        if (isEditMode) {
            VBox statusSection = createLabeledField("Estado",
                    createStatusCombo(),
                    createErrorLabel());
            formGrid.add(statusSection, 1, 2);
        }

        if (isEditMode) {
            studentCombo.setDisable(true);
            studentCombo.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
            subjectCombo.setDisable(true);
            subjectCombo.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
        }

        formContainer.getChildren().addAll(
                formTitle,
                new Separator(),
                formGrid);

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

    private ComboBox<Student> createStudentCombo() {
        studentCombo = new ComboBox<>();

        List<Student> activeStudents = studentController.getAllStudents().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        studentCombo.getItems().addAll(activeStudents);

        studentCombo.setCellFactory(param -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentCode() + " - " + item.getFullName());
                }
            }
        });

        studentCombo.setButtonCell(new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getStudentCode() + " - " + item.getFullName());
                }
            }
        });

        studentCombo.setPromptText("Seleccione un estudiante");
        studentCombo.getStyleClass().add("form-field");
        studentCombo.setPrefHeight(35);
        studentCombo.setMaxWidth(Double.MAX_VALUE);

        return studentCombo;
    }

    private ComboBox<Subject> createSubjectCombo() {
        subjectCombo = new ComboBox<>();

        subjectCombo.setCellFactory(param -> new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSubjectCode() + " - " + item.getName());
                }
            }
        });

        subjectCombo.setButtonCell(new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSubjectCode() + " - " + item.getName());
                }
            }
        });

        subjectCombo.setPromptText("Seleccione una materia");
        subjectCombo.getStyleClass().add("form-field");
        subjectCombo.setPrefHeight(35);
        subjectCombo.setMaxWidth(Double.MAX_VALUE);

        return subjectCombo;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-field");
        field.setPrefHeight(35);
        return field;
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

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setPromptText("Seleccione la fecha");
        picker.setValue(LocalDate.now());
        picker.getStyleClass().add("form-field");
        picker.setPrefHeight(35);
        return picker;
    }

    private ComboBox<String> createStatusCombo() {
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("enrolled", "completed", "dropped");
        statusCombo.setValue("enrolled");
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

    private void updateSubjectCombo() {
        subjectCombo.getItems().clear();
        
        Student selectedStudent = studentCombo.getValue();
        List<Subject> availableSubjects;
        
        if (selectedStudent != null && selectedStudent.getSemester() != null) {
            // Filtrar materias según el semestre del estudiante
            Integer studentSemester = selectedStudent.getSemester();
            availableSubjects = subjectController.getAllSubjects().stream()
                    .filter(Subject::isActive)
                    .filter(subject -> {
                        // Si la materia no tiene semestre disponible, se muestra
                        if (subject.getSemesterAvailable() == null) {
                            return true;
                        }
                        // Solo mostrar materias disponibles para el semestre del estudiante o anteriores
                        return studentSemester >= subject.getSemesterAvailable();
                    })
                    .collect(Collectors.toList());
        } else {
            // Si no hay estudiante seleccionado o no tiene semestre, mostrar todas las materias activas
            availableSubjects = subjectController.getAllSubjects().stream()
                    .filter(Subject::isActive)
                    .collect(Collectors.toList());
        }
        
        subjectCombo.getItems().addAll(availableSubjects);
        
        // Si solo hay una materia disponible, seleccionarla automáticamente
        if (availableSubjects.size() == 1) {
            subjectCombo.setValue(availableSubjects.get(0));
        }
    }

    private void setupValidation() {
        studentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateStudent();
            // Actualizar materias disponibles cuando cambia el estudiante
            updateSubjectCombo();
            // Limpiar selección de materia si cambia el estudiante
            subjectCombo.setValue(null);
        });

        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSubject();
        });

        academicYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAcademicYear();
        });

        semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSemester();
        });

        enrollmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateEnrollmentDate();
        });
    }

    private boolean validateStudent() {
        if (studentCombo.getValue() == null) {
            showError(studentCombo, studentError, "Debe seleccionar un estudiante");
            return false;
        }

        clearError(studentCombo, studentError);
        return true;
    }

    private boolean validateSubject() {
        if (subjectCombo.getValue() == null) {
            showError(subjectCombo, subjectError, "Debe seleccionar una materia");
            return false;
        }

        clearError(subjectCombo, subjectError);
        return true;
    }

    private boolean validateAcademicYear() {
        String academicYear = academicYearField.getText();

        if (FormValidator.isEmpty(academicYear)) {
            showError(academicYearField, academicYearError, "El año académico es obligatorio");
            return false;
        }

        if (!academicYear.matches("\\d{4}-\\d{4}")) {
            showError(academicYearField, academicYearError, "Formato inválido. Use: YYYY-YYYY (ej: 2024-2025)");
            return false;
        }

        String[] years = academicYear.split("-");
        try {
            int year1 = Integer.parseInt(years[0]);
            int year2 = Integer.parseInt(years[1]);
            if (year2 != year1 + 1) {
                showError(academicYearField, academicYearError, "El segundo año debe ser consecutivo (ej: 2024-2025)");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(academicYearField, academicYearError, "Año inválido");
            return false;
        }

        clearError(academicYearField, academicYearError);
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

    private boolean validateEnrollmentDate() {
        LocalDate date = enrollmentDatePicker.getValue();

        if (date == null) {
            clearError(enrollmentDatePicker, enrollmentDateError);
            return true;
        }

        if (date.isAfter(LocalDate.now())) {
            showError(enrollmentDatePicker, enrollmentDateError, "La fecha no puede ser futura");
            return false;
        }

        clearError(enrollmentDatePicker, enrollmentDateError);
        return true;
    }

    private boolean validateAll() {
        boolean valid = true;

        valid &= validateStudent();
        valid &= validateSubject();
        valid &= validateAcademicYear();
        valid &= validateSemester();
        valid &= validateEnrollmentDate();

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
                    Enrollment enrollment;

                    if (isEditMode) {
                        enrollment = existingEnrollment;
                        enrollment.setAcademicYear(academicYearField.getText().trim());
                        enrollment.setSemester(semesterCombo.getValue());
                        enrollment.setEnrollmentDate(enrollmentDatePicker.getValue());

                        if (statusCombo != null) {
                            enrollment.setStatus(statusCombo.getValue());
                        }

                        Enrollment updatedEnrollment = enrollmentController.updateEnrollment(enrollment);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Inscripción actualizada");
                        successAlert.setContentText("Los cambios se guardaron correctamente.");
                        successAlert.showAndWait();

                        return updatedEnrollment;
                    } else {
                        enrollment = enrollmentController.enrollStudent(
                                studentCombo.getValue().getId(),
                                subjectCombo.getValue().getId(),
                                academicYearField.getText().trim(),
                                semesterCombo.getValue()
                        );

                        // Actualizar fecha de inscripción si es diferente a la actual
                        LocalDate selectedDate = enrollmentDatePicker.getValue();
                        if (selectedDate != null && !selectedDate.equals(enrollment.getEnrollmentDate())) {
                            enrollment.setEnrollmentDate(selectedDate);
                            enrollment = enrollmentController.updateEnrollment(enrollment);
                        }

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Inscripción registrada");
                        successAlert.setContentText("La inscripción ha sido registrada correctamente.");
                        successAlert.showAndWait();

                        return enrollment;
                    }
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de negocio");
                    alert.setHeaderText("No se pudo guardar la inscripción");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    return null;
                } catch (Exception e) {
                    // Error inesperado
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error del sistema");
                    alert.setHeaderText("Error inesperado");
                    alert.setContentText("Ocurrió un error al guardar la inscripción: " + e.getMessage());
                    alert.showAndWait();
                    return null;
                }
            }
            return null;
        });
    }

    private void loadExistingData() {
        if (existingEnrollment != null) {
            if (existingEnrollment.getStudent() != null) {
                studentCombo.setValue(existingEnrollment.getStudent());
                // Actualizar materias disponibles según el estudiante
                updateSubjectCombo();
            }

            if (existingEnrollment.getSubject() != null) {
                subjectCombo.setValue(existingEnrollment.getSubject());
            }

            if (existingEnrollment.getAcademicYear() != null) {
                academicYearField.setText(existingEnrollment.getAcademicYear());
            }

            semesterCombo.setValue(existingEnrollment.getSemester());

            if (existingEnrollment.getEnrollmentDate() != null) {
                enrollmentDatePicker.setValue(existingEnrollment.getEnrollmentDate());
            }

            if (statusCombo != null && existingEnrollment.getStatus() != null) {
                statusCombo.setValue(existingEnrollment.getStatus());
            }
        }
    }
}