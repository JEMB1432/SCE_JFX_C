package org.jemb.sce_jfx.views.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.EvaluationTypeController;
import org.jemb.sce_jfx.controllers.GradeController;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.*;
import org.jemb.sce_jfx.utils.UserSession;
import org.jemb.sce_jfx.views.dialogs.GradeFormDialog;

import java.util.List;

/**
 * Vista de Calificaciones para profesores
 * Incluye sidebar interno con materias y tabla dinámica de estudiantes con
 * evaluaciones
 */
public class GradesView extends HBox {

    private final TeacherSubjectController teacherSubjectController;
    private final EnrollmentController enrollmentController;
    private final EvaluationTypeController evaluationTypeController;
    private final GradeController gradeController;
    private final User currentUser;

    // Sidebar components
    private ListView<TeacherSubject> subjectsListView;
    private ObservableList<TeacherSubject> subjectsList;

    // Main content components
    private VBox contentArea;
    private Label subjectTitleLabel;
    private TableView<Enrollment> studentsTable;
    private ObservableList<Enrollment> enrollmentsList;

    private TeacherSubject selectedSubject;
    private List<EvaluationType> evaluationTypes;

    public GradesView() {
        teacherSubjectController = new TeacherSubjectController();
        enrollmentController = new EnrollmentController();
        evaluationTypeController = new EvaluationTypeController();
        gradeController = new GradeController();
        currentUser = UserSession.getInstance().getCurrentUser();

        subjectsList = FXCollections.observableArrayList();
        enrollmentsList = FXCollections.observableArrayList();

        // Cargar estilos
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/sidebar.css").toExternalForm());

        setupLayout();
        loadSubjects();
    }

    private void setupLayout() {
        // Sidebar izquierdo
        VBox sidebar = createSidebar();
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(280);
        sidebar.setMaxWidth(280);

        // Área de contenido principal
        contentArea = createContentArea();
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        getChildren().addAll(sidebar, contentArea);
        setSpacing(0);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0;");

        // Título del sidebar
        Label title = new Label("Mis Materias");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #111827;");

        // ListView de materias
        subjectsListView = new ListView<>(subjectsList);
        subjectsListView.setCellFactory(listView -> new SubjectListCell());
        subjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onSubjectSelected(newVal));
        VBox.setVgrow(subjectsListView, Priority.ALWAYS);

        sidebar.getChildren().addAll(title, subjectsListView);
        return sidebar;
    }

    private VBox createContentArea() {
        VBox area = new VBox(20);
        area.setPadding(new Insets(30));
        VBox.setVgrow(area, Priority.ALWAYS);

        // Header
        subjectTitleLabel = new Label("Selecciona una materia");
        subjectTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        subjectTitleLabel.getStyleClass().add("view-title");

        Label subtitle = new Label("Gestiona las calificaciones de tus estudiantes");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, subjectTitleLabel, subtitle);

        // Tabla (inicialmente vacía)
        studentsTable = new TableView<>();
        studentsTable.setPlaceholder(new Label("Selecciona una materia para ver los estudiantes"));
        VBox.setVgrow(studentsTable, Priority.ALWAYS);

        area.getChildren().addAll(header, studentsTable);
        return area;
    }

    private void loadSubjects() {
        if (currentUser == null) {
            showError("No se pudo obtener el usuario actual");
            return;
        }

        try {
            var subjects = teacherSubjectController.getActiveAssignmentsByTeacher(currentUser.getId());
            subjectsList.setAll(subjects);

            // Seleccionar la primera materia si existe
            if (!subjects.isEmpty()) {
                subjectsListView.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onSubjectSelected(TeacherSubject subject) {
        if (subject == null)
            return;

        selectedSubject = subject;
        subjectTitleLabel.setText(subject.getSubject().getName());

        // Cargar evaluation types de la materia
        try {
            evaluationTypes = evaluationTypeController.getEvaluationTypesBySubject(subject.getSubjectId());
        } catch (Exception e) {
            evaluationTypes = List.of();
            showError("Error al cargar tipos de evaluación: " + e.getMessage());
        }

        // Crear tabla dinámica
        createDynamicTable();

        // Cargar estudiantes inscritos
        loadEnrollments();
    }

    @SuppressWarnings("unchecked")
    private void createDynamicTable() {
        studentsTable.getColumns().clear();
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columna: Código de Estudiante
        TableColumn<Enrollment, String> codeCol = new TableColumn<>("CÓDIGO");
        codeCol.setCellValueFactory(data -> {
            Enrollment enrollment = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    enrollment.getStudent() != null ? enrollment.getStudent().getStudentCode() : "N/A");
        });
        codeCol.setPrefWidth(100);

        // Columna: Nombre del Estudiante
        TableColumn<Enrollment, String> nameCol = new TableColumn<>("ESTUDIANTE");
        nameCol.setCellValueFactory(data -> {
            Enrollment enrollment = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    enrollment.getStudent() != null ? enrollment.getStudent().getFullName() : "N/A");
        });
        nameCol.setPrefWidth(200);

        studentsTable.getColumns().addAll(codeCol, nameCol);

        // Columnas dinámicas por cada evaluation type
        for (EvaluationType evalType : evaluationTypes) {
            TableColumn<Enrollment, String> evalCol = new TableColumn<>(evalType.getName());
            evalCol.setCellValueFactory(data -> {
                Enrollment enrollment = data.getValue();
                Grade grade = findGrade(enrollment.getId(), evalType.getId());
                String value = grade != null ? String.format("%.2f", grade.getScore()) : "-";
                return new javafx.beans.property.SimpleStringProperty(value);
            });
            evalCol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String score, boolean empty) {
                    super.updateItem(score, empty);
                    if (empty || score == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(score);
                        setStyle(score.equals("-") ? "-fx-text-fill: #9ca3af;"
                                : "-fx-text-fill: #111827; -fx-font-weight: bold;");
                    }
                }
            });
            evalCol.setPrefWidth(120);
            studentsTable.getColumns().add(evalCol);
        }

        // Columna: Nota Final
        TableColumn<Enrollment, String> finalCol = new TableColumn<>("NOTA FINAL");
        finalCol.setCellValueFactory(data -> {
            Enrollment enrollment = data.getValue();
            double finalGrade = gradeController.calculateFinalGrade(enrollment.getId());
            String value = finalGrade > 0 ? String.format("%.2f", finalGrade) : "-";
            return new javafx.beans.property.SimpleStringProperty(value);
        });
        finalCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || grade == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(grade);
                    if (!grade.equals("-")) {
                        double value = Double.parseDouble(grade);
                        String color = value >= 70 ? "#10b981" : "#dc2626";
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }
                }
            }
        });
        finalCol.setPrefWidth(120);

        // Columna: Acciones
        TableColumn<Enrollment, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button gradeButton = new Button("Calificar");

            {
                gradeButton.getStyleClass().add("primary-button");
                gradeButton.setOnAction(e -> {
                    Enrollment enrollment = getTableView().getItems().get(getIndex());
                    showGradeDialog(enrollment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(gradeButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        studentsTable.getColumns().addAll(finalCol, actionsCol);
    }

    private void loadEnrollments() {
        if (selectedSubject == null)
            return;

        try {
            var enrollments = enrollmentController.getEnrollmentsBySubject(selectedSubject.getSubjectId());

            // Filtrar por periodo
            var filtered = enrollments.stream()
                    .filter(e -> e.getAcademicYear().equals(selectedSubject.getAcademicYear()) &&
                            e.getSemester() == selectedSubject.getSemester())
                    .toList();

            enrollmentsList.setAll(filtered);
            studentsTable.setItems(enrollmentsList);
        } catch (Exception e) {
            showError("Error al cargar estudiantes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Grade findGrade(String enrollmentId, String evaluationTypeId) {
        try {
            var grades = gradeController.getGradesByEnrollment(enrollmentId);
            return grades.stream()
                    .filter(g -> g.getEvaluationTypeId().equals(evaluationTypeId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void showGradeDialog(Enrollment enrollment) {
        if (selectedSubject == null || evaluationTypes.isEmpty()) {
            showError("No hay tipos de evaluación configurados para esta materia");
            return;
        }

        GradeFormDialog dialog = new GradeFormDialog(enrollment, evaluationTypes);
        dialog.showAndWait();

        // Refrescar tabla después de calificar
        loadEnrollments();
        studentsTable.refresh();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Custom ListCell para mostrar materias en el sidebar
     */
    private static class SubjectListCell extends ListCell<TeacherSubject> {
        @Override
        protected void updateItem(TeacherSubject item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                VBox content = new VBox(4);

                Label nameLabel = new Label(item.getSubject().getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label codeLabel = new Label(item.getSubject().getSubjectCode());
                codeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

                Label periodLabel = new Label(item.getAcademicYear() + " - Sem " + item.getSemester());
                periodLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");

                content.getChildren().addAll(nameLabel, codeLabel, periodLabel);
                setGraphic(content);

                // Estilos de selección y hover
                if (isSelected()) {
                    setStyle("-fx-background-color: #2f5856;");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
                    codeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #d1d5db;");
                    periodLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #d1d5db;");
                } else {
                    setStyle("");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                    codeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
                    periodLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;");
                }

                setOnMouseEntered(e -> {
                    if (!isSelected()) {
                        setStyle("-fx-background-color: #f3f4f6;");
                    }
                });

                setOnMouseExited(e -> {
                    if (!isSelected()) {
                        setStyle("");
                    }
                });
            }
        }
    }
}
