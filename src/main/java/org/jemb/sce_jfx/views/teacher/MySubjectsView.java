package org.jemb.sce_jfx.views.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.utils.UserSession;
import org.jemb.sce_jfx.views.dialogs.EnrollStudentDialog;
import org.jemb.sce_jfx.views.dialogs.EvaluationTypesDialog;

/**
 * Vista de Mis Materias para profesores
 * Permite gestionar materias asignadas, inscribir estudiantes y manejar
 * evaluation types
 */
public class MySubjectsView extends VBox {

    private final TeacherSubjectController teacherSubjectController;
    private final User currentUser;

    private TableView<TeacherSubject> subjectsTable;
    private ObservableList<TeacherSubject> subjectsList;
    private ComboBox<String> periodFilter;

    public MySubjectsView() {
        teacherSubjectController = new TeacherSubjectController();
        currentUser = UserSession.getInstance().getCurrentUser();
        subjectsList = FXCollections.observableArrayList();

        // Cargar estilos
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm());

        setPadding(new Insets(30));
        setSpacing(20);

        getChildren().addAll(
                createHeader(),
                createFiltersSection(),
                createTableSection());

        loadSubjects();
    }

    private VBox createHeader() {
        Label title = new Label("Mis Materias");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona tus materias asignadas, estudiantes inscritos y tipos de evaluación");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createFiltersSection() {
        Label periodLabel = new Label("Periodo:");
        periodLabel.getStyleClass().add("form-label");

        periodFilter = new ComboBox<>();
        periodFilter.getStyleClass().add("filter-combo");
        periodFilter.setPrefWidth(200);
        periodFilter.setPromptText("Todos los periodos");

        // Cargar periodos disponibles
        loadPeriods();

        periodFilter.setOnAction(e -> filterSubjects());

        Button refreshButton = new Button("🔄 Actualizar");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(e -> loadSubjects());

        HBox filters = new HBox(15, periodLabel, periodFilter, refreshButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 0, 10, 0));

        return filters;
    }

    private VBox createTableSection() {
        subjectsTable = new TableView<>();
        subjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        subjectsTable.setPlaceholder(new Label("No tienes materias asignadas"));

        // Columna: Código
        TableColumn<TeacherSubject, String> codeCol = new TableColumn<>("CÓDIGO");
        codeCol.setCellValueFactory(data -> {
            TeacherSubject ts = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    ts.getSubject() != null ? ts.getSubject().getSubjectCode() : "N/A");
        });
        codeCol.setPrefWidth(100);

        // Columna: Nombre de Materia
        TableColumn<TeacherSubject, String> nameCol = new TableColumn<>("MATERIA");
        nameCol.setCellValueFactory(data -> {
            TeacherSubject ts = data.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                    ts.getSubject() != null ? ts.getSubject().getName() : "N/A");
        });
        nameCol.setPrefWidth(250);

        // Columna: Año Académico
        TableColumn<TeacherSubject, String> yearCol = new TableColumn<>("AÑO ACADÉMICO");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        yearCol.setPrefWidth(120);

        // Columna: Semestre
        TableColumn<TeacherSubject, Integer> semesterCol = new TableColumn<>("SEMESTRE");
        semesterCol.setCellValueFactory(new PropertyValueFactory<>("semester"));
        semesterCol.setPrefWidth(100);

        // Columna: Estado
        TableColumn<TeacherSubject, String> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(getStatusText(status));
                    badge.getStyleClass().addAll("badge", "status-" + status);
                    setGraphic(badge);
                }
            }
        });
        statusCol.setPrefWidth(100);

        // Columna: Acciones
        TableColumn<TeacherSubject, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button studentsBtn = new Button("👥 Estudiantes");
            private final Button evalTypesBtn = new Button("📋 Eval. Types");

            {
                studentsBtn.getStyleClass().add("edit-button");
                evalTypesBtn.getStyleClass().add("chart-button");

                studentsBtn.setOnAction(e -> {
                    TeacherSubject ts = getTableView().getItems().get(getIndex());
                    showEnrollStudentDialog(ts);
                });

                evalTypesBtn.setOnAction(e -> {
                    TeacherSubject ts = getTableView().getItems().get(getIndex());
                    showEvaluationTypesDialog(ts);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, studentsBtn, evalTypesBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(250);

        subjectsTable.getColumns().addAll(codeCol, nameCol, yearCol, semesterCol, statusCol, actionsCol);
        subjectsTable.setItems(subjectsList);

        VBox tableContainer = new VBox(subjectsTable);
        VBox.setVgrow(subjectsTable, Priority.ALWAYS);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadSubjects() {
        if (currentUser == null) {
            showError("No se pudo obtener el usuario actual");
            return;
        }

        try {
            var subjects = teacherSubjectController.getActiveAssignmentsByTeacher(currentUser.getId());
            subjectsList.setAll(subjects);
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadPeriods() {
        // Obtener periodos únicos de las materias del profesor
        try {
            var allSubjects = teacherSubjectController.getAssignmentsByTeacher(currentUser.getId());
            var periods = allSubjects.stream()
                    .map(ts -> ts.getAcademicYear() + " - Sem " + ts.getSemester())
                    .distinct()
                    .sorted()
                    .toList();

            periodFilter.getItems().clear();
            periodFilter.getItems().add("Todos");
            periodFilter.getItems().addAll(periods);
            periodFilter.setValue("Todos");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterSubjects() {
        String selectedPeriod = periodFilter.getValue();

        if (selectedPeriod == null || selectedPeriod.equals("Todos")) {
            loadSubjects();
            return;
        }

        try {
            var allSubjects = teacherSubjectController.getActiveAssignmentsByTeacher(currentUser.getId());
            var filtered = allSubjects.stream()
                    .filter(ts -> {
                        String period = ts.getAcademicYear() + " - Sem " + ts.getSemester();
                        return period.equals(selectedPeriod);
                    })
                    .toList();

            subjectsList.setAll(filtered);
        } catch (Exception e) {
            showError("Error al filtrar materias: " + e.getMessage());
        }
    }

    private void showEnrollStudentDialog(TeacherSubject teacherSubject) {
        EnrollStudentDialog dialog = new EnrollStudentDialog(teacherSubject);
        dialog.showAndWait();
        // Recargar para reflejar cambios si es necesario
    }

    private void showEvaluationTypesDialog(TeacherSubject teacherSubject) {
        EvaluationTypesDialog dialog = new EvaluationTypesDialog(teacherSubject);
        dialog.showAndWait();
    }

    private String getStatusText(String status) {
        return switch (status) {
            case "active" -> "Activa";
            case "inactive" -> "Inactiva";
            case "completed" -> "Completada";
            default -> status;
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
