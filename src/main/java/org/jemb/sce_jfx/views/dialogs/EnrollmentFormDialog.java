package org.jemb.sce_jfx.views.dialogs;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.StudentController;
import org.jemb.sce_jfx.controllers.SubjectController;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.*;
import org.jemb.sce_jfx.utils.FormValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentFormDialog extends Dialog<Enrollment> {
    private final EnrollmentController enrollmentController;
    private final StudentController studentController;
    private final SubjectController subjectController;
    private final TeacherSubjectController teacherSubjectController;
    private final Enrollment existingEnrollment;
    private final boolean isEditMode;

    // Campos del formulario
    private ComboBox<Student> studentCombo;
    private ComboBox<Subject> subjectCombo;
    private ComboBox<User> teacherCombo;
    private TextField academicYearField;
    private ComboBox<Integer> semesterCombo;
    private DatePicker enrollmentDatePicker;
    private ComboBox<String> statusCombo;

    // Labels de error
    private Label studentError;
    private Label subjectError;
    private Label teacherError;
    private Label academicYearError;
    private Label semesterError;
    private Label enrollmentDateError;

    public EnrollmentFormDialog(Enrollment enrollment) {
        this.enrollmentController = new EnrollmentController();
        this.studentController = new StudentController();
        this.subjectController = new SubjectController();
        this.teacherSubjectController = new TeacherSubjectController();
        this.existingEnrollment = enrollment;
        this.isEditMode = (enrollment != null);

        initializeDialog();
        createCompactForm();
        setupValidation();
        setupResult();

        DialogUtils.setDialogIcon(this);
        // Cargar materias inicialmente (todas las activas si no hay estudiante
        // seleccionado)
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
        teacherError = createErrorLabel();
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
        } else {
            VBox teacherSection = createLabeledField("Profesor *",
                    createTeacherCombo(),
                    teacherError);
            formGrid.add(teacherSection, 1, 2);
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

        Label noteLabel = new Label("* Campos obligatorios \n- Debe de llenar los campos en orden");
        noteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-style: italic;");
        formContainer.getChildren().add(noteLabel);

        getDialogPane().setContent(formContainer);
    }

    private ComboBox<User> createTeacherCombo() {
        teacherCombo = new ComboBox<>();
        teacherCombo.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName());
                }
            }
        });
        teacherCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFirstName() + " " + item.getLastName());
                }
            }
        });
        teacherCombo.setPromptText("Seleccione un profesor");
        teacherCombo.getStyleClass().add("form-field");
        teacherCombo.setPrefHeight(35);
        teacherCombo.setMaxWidth(Double.MAX_VALUE);
        return teacherCombo;
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

        // Obtener estudiantes activos
        List<Student> activeStudents = studentController.getAllStudents().stream()
                .filter(Student::isActive)
                .collect(Collectors.toList());

        studentCombo.getItems().addAll(activeStudents);

        // Configurar cómo se muestran los estudiantes
        studentCombo.setCellFactory(param -> new ListCell<Student>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s - %s (Sem: %d)",
                            item.getStudentCode(),
                            item.getFullName(),
                            item.getCurrentSemester() != null ? item.getCurrentSemester() : 1));
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
                    setText(String.format("%s - %s (Sem: %d)",
                            item.getStudentCode(),
                            item.getFullName(),
                            item.getCurrentSemester() != null ? item.getCurrentSemester() : 1));
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
                    String semesterInfo = item.getSemesterAvailable() != null
                            ? " (Sem: " + item.getSemesterAvailable() + ")"
                            : "";
                    setText(item.getSubjectCode() + " - " + item.getName() + semesterInfo);
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
                    String semesterInfo = item.getSemesterAvailable() != null
                            ? " (Sem: " + item.getSemesterAvailable() + ")"
                            : "";
                    setText(item.getSubjectCode() + " - " + item.getName() + semesterInfo);
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
        semesterCombo.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        semesterCombo.setPromptText("Seleccione semestre");
        semesterCombo.getStyleClass().add("form-field");
        semesterCombo.setPrefHeight(35);
        semesterCombo.setMaxWidth(Double.MAX_VALUE);
        semesterCombo.setDisable(true);
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
        statusCombo.getItems().addAll("Inscrito", "Completado", "Dado de baja");
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

        List<Subject> allSubjects = subjectController.getAllSubjects().stream()
                .filter(Subject::isActive)
                .collect(Collectors.toList());

        if (selectedStudent == null) {
            subjectCombo.getItems().addAll(allSubjects);
            return;
        }

        Integer studentSemester = selectedStudent.getCurrentSemester() != null
                ? selectedStudent.getCurrentSemester()
                : 1;

        List<Subject> availableSubjects = allSubjects.stream()
                .filter(subject -> {
                    Integer subjectSemester = subject.getSemesterAvailable();
                    return subjectSemester == null || subjectSemester.equals(studentSemester);
                })
                .collect(Collectors.toList());

        subjectCombo.getItems().addAll(availableSubjects);

        if (isEditMode && existingEnrollment != null && existingEnrollment.getSubject() != null) {
            Subject currentSubject = existingEnrollment.getSubject();
            if (!availableSubjects.contains(currentSubject)) {
                subjectCombo.getItems().add(0, currentSubject);
            }
        }
    }

    private void loadTeachersForSubject() {
        if (isEditMode || teacherCombo == null) {
            return;
        }
        teacherCombo.getItems().clear();
        Subject selectedSubject = subjectCombo.getValue();
        String academicYear = academicYearField.getText().trim();
        Integer semester = semesterCombo.getValue();
        if (selectedSubject == null || academicYear.isEmpty() || semester == null) {
            return;
        }
        // Validar formato de año antes de buscar
        if (!academicYear.matches("\\d{4}-\\d{4}")) {
            return;
        }
        try {
            // Obtener asignaciones de profesores para esta materia
            List<TeacherSubject> assignments = teacherSubjectController
                    .getAssignmentsBySubject(selectedSubject.getId());
            // Filtrar por año académico y semestre
            List<User> availableTeachers = assignments.stream()
                    .filter(ts -> ts.getAcademicYear().equals(academicYear) &&
                            ts.getSemester() == semester &&
                            ts.getStatus().equals("active"))
                    .map(TeacherSubject::getTeacher)
                    .filter(teacher -> teacher != null)
                    .distinct()
                    .collect(Collectors.toList());
            teacherCombo.getItems().addAll(availableTeachers);
            // Si solo hay un profesor, seleccionarlo automáticamente
            if (availableTeachers.size() == 1) {
                teacherCombo.setValue(availableTeachers.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupValidation() {
        studentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateStudent();
            updateSubjectCombo();
            setDefaultSemesterBasedOnStudent(); // Establecer semestre automáticamente
            subjectCombo.setValue(null);
            if (teacherCombo != null) {
                teacherCombo.setValue(null);
            }
        });

        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSubject();
            if (!isEditMode) {
                loadTeachersForSubject();
            }
        });

        academicYearField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAcademicYear();
            if (!isEditMode) {
                loadTeachersForSubject();
            }
        });

        semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSemester();
            if (!isEditMode) {
                loadTeachersForSubject();
            }
        });

        if (!isEditMode && teacherCombo != null) {
            teacherCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                validateTeacher();
            });
        }

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

        // Validar que el estudiante pueda inscribirse en esta materia
        Student selectedStudent = studentCombo.getValue();
        Subject selectedSubject = subjectCombo.getValue();

        if (selectedStudent != null && selectedSubject != null) {
            if (!selectedStudent.canEnrollInSubject(selectedSubject)) {
                Integer requiredSemester = selectedSubject.getSemesterAvailable();
                Integer studentSemester = selectedStudent.getCurrentSemester() != null ?
                        selectedStudent.getCurrentSemester() : 1;

                if (requiredSemester != null) {
                    showError(subjectCombo, subjectError,
                            "El estudiante está en el semestre " + studentSemester +
                                    ". Esta materia está disponible a partir del semestre " + requiredSemester);
                } else {
                    showError(subjectCombo, subjectError,
                            "El estudiante no puede inscribirse en esta materia.");
                }
                return false;
            }
        }

        clearError(subjectCombo, subjectError);
        return true;
    }

    private boolean validateTeacher() {
        if (isEditMode) {
            return true;
        }
        if (teacherCombo == null || teacherCombo.getValue() == null) {
            if (teacherCombo != null) {
                showError(teacherCombo, teacherError, "Debe seleccionar un profesor");
            }
            return false;
        }
        clearError(teacherCombo, teacherError);
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
        if (semester < 1) {
            showError(semesterCombo, semesterError, "El semestre debe ser mayor a 0");
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
        valid &= validateTeacher();
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
                    Enrollment enrollment;

                    if (isEditMode) {
                        enrollment = existingEnrollment;
                        enrollment.setAcademicYear(academicYearField.getText().trim());
                        enrollment.setSemester(semesterCombo.getValue());
                        enrollment.setEnrollmentDate(enrollmentDatePicker.getValue());

                        if (statusCombo != null) {
                            String status = statusCombo.getValue().toLowerCase();
                            switch (status) {
                                case "inscrito":
                                    status = "enrrolled";
                                    break;
                                case "completado":
                                    status = "completed";
                                    break;
                                case "dado de baja":
                                    status = "dropped";
                                    break;
                                default:
                                    status = "enrolled";
                            }
                            enrollment.setStatus(status);
                        }

                        Enrollment updatedEnrollment = enrollmentController.updateEnrollment(enrollment);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Éxito");
                        successAlert.setHeaderText("Inscripción actualizada");
                        successAlert.setContentText("Los cambios se guardaron correctamente.");
                        successAlert.showAndWait();

                        return updatedEnrollment;
                    } else {
                        String teacherId = teacherCombo.getValue().getId();
                        enrollment = enrollmentController.enrollStudent(
                                studentCombo.getValue().getId(),
                                subjectCombo.getValue().getId(),
                                teacherId,
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

    private void setDefaultSemesterBasedOnStudent() {
        Student selectedStudent = studentCombo.getValue();
        if (selectedStudent != null && !isEditMode) {
            // Establecer el semestre del combo igual al semestre del estudiante
            Integer studentSemester = selectedStudent.getCurrentSemester();
            if (studentSemester != null && studentSemester >= 1 && studentSemester <= 10) {
                semesterCombo.setValue(studentSemester);
            } else {
                semesterCombo.setValue(1);
            }
        }
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

            if (existingEnrollment.getSemester() != null) {
                semesterCombo.setValue(existingEnrollment.getSemester());
            }

            if (existingEnrollment.getEnrollmentDate() != null) {
                enrollmentDatePicker.setValue(existingEnrollment.getEnrollmentDate());
            }

            if (statusCombo != null && existingEnrollment.getStatus() != null) {
                statusCombo.setValue(existingEnrollment.getStatus());
            }
        }
    }
}