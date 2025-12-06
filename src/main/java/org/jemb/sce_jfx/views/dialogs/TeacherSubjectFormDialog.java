package org.jemb.sce_jfx.views.dialogs;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.SubjectController;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.dao.UserDAO;
import org.jemb.sce_jfx.models.Subject;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.models.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Diálogo para crear y editar asignaciones profesor-materia
 */
public class TeacherSubjectFormDialog extends Dialog<TeacherSubject> {
    private final TeacherSubjectController controller;
    private final SubjectController subjectController;
    private final UserDAO userDAO;
    private final TeacherSubject existingAssignment;
    private final boolean isEditMode;

    private ComboBox<User> teacherCombo;
    private ComboBox<Subject> subjectCombo;
    private TextField academicYearField;
    private TextField semesterField;
    private ComboBox<String> statusCombo;

    private Label teacherError;
    private Label subjectError;
    private Label academicYearError;
    private Label semesterError;

    public TeacherSubjectFormDialog(TeacherSubject assignment) {
        this.controller = new TeacherSubjectController();
        this.subjectController = new SubjectController();
        this.userDAO = new UserDAO();
        this.existingAssignment = assignment;
        this.isEditMode = (assignment != null);

        initializeDialog();
        createForm();
        setupValidation();
        setupResult();

        DialogUtils.setDialogIcon(this);

        if (isEditMode) {
            loadExistingData();
        }
    }

    private void initializeDialog() {
        setTitle(isEditMode ? "Editar Asignación" : "Nueva Asignación");
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

        Label formTitle = new Label(isEditMode ? "Modificar Asignación" : "Nueva Asignación Profesor-Materia");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.getStyleClass().add("form-title");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(10, 0, 10, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(100);
        formGrid.getColumnConstraints().add(col1);

        // Profesor ComboBox
        VBox teacherSection = createLabeledField("Profesor *",
                teacherCombo = createTeacherCombo(),
                teacherError = createErrorLabel());
        formGrid.add(teacherSection, 0, 0);

        // Materia ComboBox
        VBox subjectSection = createLabeledField("Materia *",
                subjectCombo = createSubjectCombo(),
                subjectError = createErrorLabel());
        formGrid.add(subjectSection, 0, 1);

        // Año Académico
        VBox yearSection = createLabeledField("Año Académico *",
                academicYearField = createTextField("Ej: 2024-2025"),
                academicYearError = createErrorLabel());
        formGrid.add(yearSection, 0, 2);

        // Semestre (automático y deshabilitado)
        VBox semesterSection = createLabeledField("Semestre (automático)",
                semesterField = createSemesterField(),
                semesterError = createErrorLabel());
        formGrid.add(semesterSection, 0, 3);

        // Estado (solo en modo edición)
        if (isEditMode) {
            VBox statusSection = createLabeledField("Estado",
                    createStatusCombo(),
                    createErrorLabel());
            formGrid.add(statusSection, 0, 4);
        }

        formContainer.getChildren().addAll(
                formTitle,
                new Separator(),
                formGrid);

        Label noteLabel = new Label("* Campos obligatorios\n" +
                "El semestre se establece automáticamente según la materia seleccionada");
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

    private TextField createSemesterField() {
        TextField field = new TextField();
        field.setPromptText("Se establece automáticamente");
        field.getStyleClass().add("form-field");
        field.setPrefHeight(35);
        field.setEditable(false);
        field.setDisable(true);
        field.setStyle("-fx-opacity: 0.7; -fx-background-color: #f0f0f0;");
        return field;
    }

    private ComboBox<User> createTeacherCombo() {
        ComboBox<User> combo = new ComboBox<>();

        // Cargar solo usuarios con rol de teacher
        List<User> teachers = userDAO.findByRole("teacher");
        combo.getItems().addAll(teachers);

        // Configurar cómo se muestra el profesor
        combo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFullName() + " (" + user.getEmail() + ")");
                }
            }
        });

        combo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFullName() + " (" + user.getEmail() + ")");
                }
            }
        });

        combo.getStyleClass().add("form-field");
        combo.setPrefHeight(35);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText("Seleccione un profesor");

        return combo;
    }

    private ComboBox<Subject> createSubjectCombo() {
        ComboBox<Subject> combo = new ComboBox<>();

        // Cargar solo materias activas
        List<Subject> activeSubjects = subjectController.getAllSubjects().stream()
                .filter(Subject::isActive)
                .sorted((s1, s2) -> {
                    // Ordenar por semestre disponible para mejor visualización
                    Integer sem1 = s1.getSemesterAvailable() != null ? s1.getSemesterAvailable() : 999;
                    Integer sem2 = s2.getSemesterAvailable() != null ? s2.getSemesterAvailable() : 999;
                    return sem1.compareTo(sem2);
                })
                .collect(Collectors.toList());
        combo.getItems().addAll(activeSubjects);

        // Configurar cómo se muestra la materia (con semestre)
        combo.setButtonCell(new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);
                if (empty || subject == null) {
                    setText(null);
                } else {
                    String semesterInfo = subject.getSemesterAvailable() != null
                            ? " - Sem: " + subject.getSemesterAvailable()
                            : " - Sin semestre asignado";
                    setText(subject.getName() + " (" + subject.getSubjectCode() + ")" + semesterInfo);
                }
            }
        });

        combo.setCellFactory(lv -> new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);
                if (empty || subject == null) {
                    setText(null);
                } else {
                    String semesterInfo = subject.getSemesterAvailable() != null
                            ? " - Sem: " + subject.getSemesterAvailable()
                            : " - Sin semestre asignado";
                    setText(subject.getName() + " (" + subject.getSubjectCode() + ")" + semesterInfo);
                }
            }
        });

        combo.getStyleClass().add("form-field");
        combo.setPrefHeight(35);
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setPromptText("Seleccione una materia");

        return combo;
    }

    private ComboBox<String> createStatusCombo() {
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("active", "inactive", "completed");
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
        teacherCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateTeacher());

        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateSubject();
            updateSemesterField();
        });

        academicYearField.textProperty().addListener((obs, oldVal, newVal) -> validateAcademicYear());
    }

    private void updateSemesterField() {
        Subject selectedSubject = subjectCombo.getValue();

        if (selectedSubject == null) {
            semesterField.clear();
            semesterField.setPromptText("Seleccione una materia primero");
            return;
        }

        Integer semesterAvailable = selectedSubject.getSemesterAvailable();

        if (semesterAvailable != null) {
            semesterField.setText(String.valueOf(semesterAvailable));
        } else {
            // Si la materia no tiene semestre definido
            semesterField.setText("1");
            showWarning("La materia seleccionada no tiene semestre definido. Se asignará el semestre 1 por defecto.");
        }
    }

    private boolean validateTeacher() {
        if (teacherCombo.getValue() == null) {
            showError(teacherCombo, teacherError, "Debe seleccionar un profesor");
            return false;
        }
        clearError(teacherCombo, teacherError);
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
        String year = academicYearField.getText().trim();

        if (year.isEmpty()) {
            showError(academicYearField, academicYearError, "El año académico es obligatorio");
            return false;
        }

        // Validar formato: YYYY-YYYY
        if (!year.matches("^\\d{4}-\\d{4}$")) {
            showError(academicYearField, academicYearError, "Formato inválido. Use: 2024-2025");
            return false;
        }

        try {
            String[] parts = year.split("-");
            int year1 = Integer.parseInt(parts[0]);
            int year2 = Integer.parseInt(parts[1]);

            if (year2 != year1 + 1) {
                showError(academicYearField, academicYearError, "El segundo año debe ser consecutivo al primero");
                return false;
            }
        } catch (Exception e) {
            showError(academicYearField, academicYearError, "Formato de año inválido");
            return false;
        }

        clearError(academicYearField, academicYearError);
        return true;
    }

    private boolean validateSemester() {
        String semesterText = semesterField.getText().trim();

        if (semesterText.isEmpty()) {
            showError(semesterField, semesterError, "El semestre es obligatorio (se establece automáticamente)");
            return false;
        }

        try {
            int semester = Integer.parseInt(semesterText);
            if (semester < 1) {
                showError(semesterField, semesterError, "El semestre debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(semesterField, semesterError, "El semestre debe ser un número entero");
            return false;
        }

        clearError(semesterField, semesterError);
        return true;
    }

    private boolean validateAll() {
        boolean valid = true;
        valid &= validateTeacher();
        valid &= validateSubject();
        valid &= validateAcademicYear();
        valid &= validateSemester();
        return valid;
    }

    private void showError(Control field, Label errorLabel, String message) {
        field.getStyleClass().add("error");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError(Control field, Label errorLabel) {
        field.getStyleClass().remove("error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
                    User teacher = teacherCombo.getValue();
                    Subject subject = subjectCombo.getValue();
                    String academicYear = academicYearField.getText().trim();
                    int semester = Integer.parseInt(semesterField.getText().trim());

                    TeacherSubject assignment;

                    if (isEditMode) {
                        // Modo edición
                        assignment = existingAssignment;
                        assignment.setTeacherId(teacher.getId());
                        assignment.setSubjectId(subject.getId());
                        assignment.setAcademicYear(academicYear);
                        assignment.setSemester(semester);
                        assignment.setStatus(statusCombo.getValue());

                        // Actualizar referencias para mostrar correctamente
                        assignment.setTeacher(teacher);
                        assignment.setSubject(subject);

                        assignment = controller.updateAssignment(assignment);
                    } else {
                        // Modo creación
                        assignment = controller.createAssignment(
                                teacher.getId(),
                                subject.getId(),
                                academicYear,
                                semester);

                        // Establecer referencias para mostrar correctamente
                        assignment.setTeacher(teacher);
                        assignment.setSubject(subject);
                    }

                    return assignment;
                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("No se pudo guardar la asignación");
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
        if (existingAssignment != null) {
            // Seleccionar profesor
            if (existingAssignment.getTeacher() != null) {
                teacherCombo.getItems().stream()
                        .filter(u -> u.getId().equals(existingAssignment.getTeacherId()))
                        .findFirst()
                        .ifPresent(teacherCombo::setValue);
            }

            // Seleccionar materia
            if (existingAssignment.getSubject() != null) {
                subjectCombo.getItems().stream()
                        .filter(s -> s.getId().equals(existingAssignment.getSubjectId()))
                        .findFirst()
                        .ifPresent(subjectCombo::setValue);
            }

            academicYearField.setText(existingAssignment.getAcademicYear());
            semesterField.setText(String.valueOf(existingAssignment.getSemester()));

            if (statusCombo != null) {
                statusCombo.setValue(existingAssignment.getStatus());
            }
        }
    }
}