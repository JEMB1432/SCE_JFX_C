package org.jemb.sce_jfx.views.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.StudentController;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.TeacherSubject;

import java.util.List;

/**
 * Diálogo para inscribir estudiantes en una materia
 */
public class EnrollStudentDialog extends Dialog<Enrollment> {

    private final TeacherSubject teacherSubject;
    private final StudentController studentController;
    private final EnrollmentController enrollmentController;

    private ComboBox<Student> studentComboBox;
    private TextField academicYearField;
    private TextField semesterField;
    private TextField subjectField;

    public EnrollStudentDialog(TeacherSubject teacherSubject) {
        this.teacherSubject = teacherSubject;
        this.studentController = new StudentController();
        this.enrollmentController = new EnrollmentController();

        setTitle("Inscribir Estudiante");
        setHeaderText("Inscribir estudiante en: " + teacherSubject.getSubject().getName());

        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm());

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        DialogUtils.setDialogIcon(this);

        VBox content = createContent();
        getDialogPane().setContent(content);

        getDialogPane().setPrefSize(500, 300);

        setupValidation();
        setResultConverter(this::convertResult);

        loadStudents();
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Configurar columnas para que se expandan correctamente
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setPrefWidth(120);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(250);
        col2.setPrefWidth(350);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        // Materia (readonly)
        Label subjectLabel = new Label("Materia:");
        subjectLabel.getStyleClass().add("form-label");
        subjectLabel.setMinWidth(Region.USE_PREF_SIZE);

        subjectField = new TextField();
        subjectField.setText(teacherSubject.getSubject().getName() + " (" +
                teacherSubject.getSubject().getSubjectCode() + ")");
        subjectField.setEditable(false);
        subjectField.getStyleClass().add("form-field");

        // Año Académico (readonly, precargado)
        Label yearLabel = new Label("Año Académico:");
        yearLabel.getStyleClass().add("form-label");
        yearLabel.setMinWidth(Region.USE_PREF_SIZE);

        academicYearField = new TextField();
        academicYearField.setText(teacherSubject.getAcademicYear());
        academicYearField.setEditable(false);
        academicYearField.getStyleClass().add("form-field");

        // Semestre (readonly, precargado)
        Label semesterLabel = new Label("Semestre:");
        semesterLabel.getStyleClass().add("form-label");
        semesterLabel.setMinWidth(Region.USE_PREF_SIZE);

        semesterField = new TextField();
        semesterField.setText(String.valueOf(teacherSubject.getSemester()));
        semesterField.setEditable(false);
        semesterField.getStyleClass().add("form-field");

        // Estudiante (ComboBox con búsqueda)
        Label studentLabel = new Label("Estudiante:");
        studentLabel.getStyleClass().add("form-label");
        studentLabel.setMinWidth(Region.USE_PREF_SIZE);

        studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Selecciona un estudiante");
        studentComboBox.getStyleClass().add("combo-box");
        studentComboBox.setMaxWidth(Double.MAX_VALUE);
        studentComboBox.setEditable(true); // Permite búsqueda
        GridPane.setHgrow(studentComboBox, Priority.ALWAYS);

        // Agregar a grid
        int row = 0;
        grid.add(subjectLabel, 0, row);
        grid.add(subjectField, 1, row);

        row++;
        grid.add(yearLabel, 0, row);
        grid.add(academicYearField, 1, row);

        row++;
        grid.add(semesterLabel, 0, row);
        grid.add(semesterField, 1, row);

        row++;
        grid.add(studentLabel, 0, row);
        grid.add(studentComboBox, 1, row);

        container.getChildren().add(grid);
        return container;
    }

    private void loadStudents() {
        try {
            List<Student> allStudents = studentController.getAllStudents();

            // Filtrar solo estudiantes activos
            List<Student> activeStudents = allStudents.stream()
                    .filter(Student::isActive)
                    .toList();

            studentComboBox.getItems().setAll(activeStudents);

            // Configurar el display del ComboBox
            studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
                @Override
                public String toString(Student student) {
                    if (student == null)
                        return "";
                    return student.getFullName() + " (" + student.getStudentCode() + ")";
                }

                @Override
                public Student fromString(String string) {
                    return studentComboBox.getItems().stream()
                            .filter(s -> (s.getFullName() + " (" + s.getStudentCode() + ")").equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });

        } catch (Exception e) {
            showError("Error al cargar estudiantes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupValidation() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        // Deshabilitar OK hasta que se seleccione un estudiante
        okButton.setDisable(true);

        studentComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });
    }

    private Enrollment convertResult(ButtonType buttonType) {
        if (buttonType != ButtonType.OK) {
            return null;
        }

        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent == null) {
            return null;
        }

        try {
            // Inscribir estudiante usando el controller
            Enrollment enrollment = enrollmentController.enrollStudent(
                    selectedStudent.getId(),
                    teacherSubject.getSubjectId(),
                    teacherSubject.getAcademicYear(),
                    teacherSubject.getSemester());

            showSuccess("Estudiante inscrito correctamente");
            return enrollment;

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return null;
        } catch (Exception e) {
            showError("Error al inscribir estudiante: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
