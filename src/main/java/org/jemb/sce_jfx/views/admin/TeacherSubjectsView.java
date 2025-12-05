package org.jemb.sce_jfx.views.admin;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.views.dialogs.TeacherSubjectFormDialog;

public class TeacherSubjectsView extends VBox {

    private final int rowsPerPage = 12;

    private TableView<TeacherSubject> assignmentsTable;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> academicYearFilter;
    private TextField semesterFilterField;
    private TeacherSubjectController controller;
    private Pagination pagination;
    private Label paginationInfo;

    private ObservableList<TeacherSubject> masterList = FXCollections.observableArrayList();
    private ObservableList<TeacherSubject> currentDisplayedList = FXCollections.observableArrayList();

    public TeacherSubjectsView() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm()
        );

        controller = new TeacherSubjectController();

        setPadding(new Insets(30));
        setSpacing(20);

        pagination = new Pagination();
        pagination.setPageFactory(pageIndex -> null);

        VBox content = new VBox(20);
        content.getChildren().addAll(
                createHeader(),
                createSearchAndFilters(),
                createTableWithPagination());

        getChildren().add(content);

        loadAssignments();
    }

    public void refresh() {
        loadAssignments();
    }

    private VBox createHeader() {
        Label title = new Label("Asignaciones Profesor-Materia");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona las asignaciones de profesores a materias");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createSearchAndFilters() {
        searchField = new TextField();
        searchField.setPromptText("Buscar por profesor o materia...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todos", "Activos", "Inactivos", "Completados");
        statusFilter.setValue("Todos");
        statusFilter.setPrefWidth(140);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        academicYearFilter = new ComboBox<>();
        academicYearFilter.getItems().add("Todos los años");
        academicYearFilter.setValue("Todos los años");
        academicYearFilter.setPrefWidth(150);

        academicYearFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(event -> filterAndPaginate());
                pause.playFromStart();
            }
        });

        Label semesterLabel = new Label("Semestre:");
        semesterFilterField = new TextField();
        semesterFilterField.setPromptText("Todos");
        semesterFilterField.setPrefWidth(80);
        semesterFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        HBox semesterBox = new HBox(5, semesterLabel, semesterFilterField);
        semesterBox.setAlignment(Pos.CENTER_LEFT);

        Button newAssignmentBtn = new Button("+ Nueva Asignación");
        newAssignmentBtn.setTooltip(new Tooltip("Asignar una materia a un profesor"));
        newAssignmentBtn.getStyleClass().add("primary-button");
        newAssignmentBtn.setOnAction(e -> showNewAssignmentDialog());

        HBox searchRow = new HBox(15, searchField, statusFilter, academicYearFilter, semesterBox, newAssignmentBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        return searchRow;
    }

    private VBox createTableWithPagination() {
        assignmentsTable = new TableView<>();
        assignmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TeacherSubject, Void> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<TeacherSubject, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else {
                    int currentPage = pagination.getCurrentPageIndex();
                    int index = getIndex();
                    setText(String.valueOf(currentPage * rowsPerPage + index + 1));
                }
            }
        });
        indexCol.setPrefWidth(20);
        indexCol.setStyle("-fx-alignment: CENTER; -fx-text-fill: #6b7280;");

        TableColumn<TeacherSubject, String> teacherCol = new TableColumn<>("PROFESOR");
        teacherCol.setCellValueFactory(cellData -> {
            TeacherSubject ts = cellData.getValue();
            if (ts.getTeacher() != null) {
                return new javafx.beans.property.SimpleStringProperty(ts.getTeacher().getFullName());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        teacherCol.setPrefWidth(170);

        TableColumn<TeacherSubject, String> subjectCol = new TableColumn<>("MATERIA");
        subjectCol.setCellValueFactory(cellData -> {
            TeacherSubject ts = cellData.getValue();
            if (ts.getSubject() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        ts.getSubject().getName() + " (" + ts.getSubject().getSubjectCode() + ")");
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        subjectCol.setPrefWidth(220);

        TableColumn<TeacherSubject, String> yearCol = new TableColumn<>("AÑO ACADÉMICO");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        yearCol.setPrefWidth(130);

        TableColumn<TeacherSubject, Integer> semesterCol = new TableColumn<>("SEMESTRE");
        semesterCol.setCellValueFactory(new PropertyValueFactory<>("semester"));
        semesterCol.setCellFactory(column -> new TableCell<TeacherSubject, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + "°");
                }
            }
        });
        semesterCol.setPrefWidth(90);

        TableColumn<TeacherSubject, String> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<TeacherSubject, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String statusText;
                    String styleClass;
                    switch (item) {
                        case "active":
                            statusText = "Activo";
                            styleClass = "status-active";
                            break;
                        case "completed":
                            statusText = "Completado";
                            styleClass = "status-completed";
                            break;
                        default:
                            statusText = "Inactivo";
                            styleClass = "status-inactive";
                    }
                    Label statusLabel = new Label(statusText);
                    statusLabel.getStyleClass().add(styleClass);
                    setGraphic(statusLabel);
                }
                setAlignment(Pos.CENTER);
            }
        });
        statusCol.setPrefWidth(110);

        TableColumn<TeacherSubject, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<TeacherSubject, Void>() {

            private final Button editBtn;
            private final Button deleteBtn;
            {
                ImageView iconEdit = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/edit.png"))
                );
                iconEdit.setFitWidth(16);
                iconEdit.setFitHeight(16);
                editBtn = new Button("", iconEdit);
                editBtn.getStyleClass().add("edit-button");
                editBtn.setTooltip(new Tooltip("Editar asignación"));
                editBtn.setOnAction(e -> {
                    TeacherSubject assignment = getTableView().getItems().get(getIndex());
                    showEditAssignmentDialog(assignment);
                });

                ImageView iconDelete = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/delete.png"))
                );
                iconDelete.setFitWidth(16);
                iconDelete.setFitHeight(16);
                deleteBtn = new Button("", iconDelete);
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setTooltip(new Tooltip("Eliminar asignación"));
                deleteBtn.setOnAction(e -> {
                    TeacherSubject assignment = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(assignment);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    HBox buttons = new HBox(8, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        assignmentsTable.getColumns().addAll(
                indexCol, teacherCol, subjectCol, yearCol, semesterCol, statusCol, actionsCol);

        paginationInfo = new Label();

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForPage(newVal.intValue());
        });

        VBox tableContainer = new VBox(15, assignmentsTable, paginationInfo, pagination);
        VBox.setVgrow(assignmentsTable, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadAssignments() {
        academicYearFilter.valueProperty().removeListener((obs, oldVal, newVal) -> filterAndPaginate());

        masterList.setAll(controller.getAllAssignments());

        academicYearFilter.getItems().clear();
        academicYearFilter.getItems().add("Todos los años");
        masterList.stream()
                .map(TeacherSubject::getAcademicYear)
                .distinct()
                .sorted()
                .forEach(year -> academicYearFilter.getItems().add(year));

        academicYearFilter.setValue("Todos los años");

        academicYearFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.setOnFinished(event -> filterAndPaginate());
                pause.playFromStart();
            }
        });

        filterAndPaginate();
    }

    private void filterAndPaginate() {
        String search = searchField.getText().toLowerCase();
        String statusFilterValue = statusFilter.getValue();
        String yearFilterValue = academicYearFilter.getValue();
        String semesterText = semesterFilterField.getText().trim();

        if (yearFilterValue == null) {
            yearFilterValue = "Todos los años";
        }
        if (statusFilterValue == null) {
            statusFilterValue = "Todos";
        }

        ObservableList<TeacherSubject> filtered = FXCollections.observableArrayList();

        for (TeacherSubject ts : masterList) {
            boolean matchesSearch = search.isEmpty();
            if (!matchesSearch && ts.getTeacher() != null) {
                matchesSearch = ts.getTeacher().getFullName().toLowerCase().contains(search);
            }
            if (!matchesSearch && ts.getSubject() != null) {
                matchesSearch = ts.getSubject().getName().toLowerCase().contains(search) ||
                        ts.getSubject().getSubjectCode().toLowerCase().contains(search);
            }

            boolean matchesStatus = statusFilterValue.equals("Todos") ||
                    (statusFilterValue.equals("Activos") && ts.isActive()) ||
                    (statusFilterValue.equals("Inactivos") && "inactive".equals(ts.getStatus())) ||
                    (statusFilterValue.equals("Completados") && ts.isCompleted());

            boolean matchesYear = yearFilterValue.equals("Todos los años") ||
                    yearFilterValue.equals(ts.getAcademicYear());

            boolean matchesSemester = true;
            if (!semesterText.isEmpty()) {
                try {
                    int filterSemester = Integer.parseInt(semesterText);
                    matchesSemester = ts.getSemester() == filterSemester;
                } catch (NumberFormatException e) {
                    matchesSemester = false;
                }
            }

            if (matchesSearch && matchesStatus && matchesYear && matchesSemester) {
                filtered.add(ts);
            }
        }

        currentDisplayedList.setAll(filtered);
        updatePagination();
    }

    private void updatePagination() {
        int itemCount = currentDisplayedList.size();
        int pages = (int) Math.ceil((double) itemCount / rowsPerPage);

        pagination.setPageCount(pages > 0 ? pages : 1);

        if (pagination.getCurrentPageIndex() >= pages && pages > 0) {
            pagination.setCurrentPageIndex(0);
        }

        updateTableForPage(pagination.getCurrentPageIndex());
    }

    private void updateTableForPage(int pageIndex) {
        int from = pageIndex * rowsPerPage;
        int to = Math.min(from + rowsPerPage, currentDisplayedList.size());

        if (currentDisplayedList.isEmpty()) {
            assignmentsTable.setItems(FXCollections.observableArrayList());
            paginationInfo.setText("No hay asignaciones para mostrar");
        } else {
            assignmentsTable.setItems(FXCollections.observableArrayList(
                    currentDisplayedList.subList(from, to)));

            paginationInfo.setText(
                    String.format("Mostrando %d-%d de %d asignaciones",
                            from + 1, to, currentDisplayedList.size()));
        }
    }

    private void showNewAssignmentDialog() {
        TeacherSubjectFormDialog dialog = new TeacherSubjectFormDialog(null);
        dialog.showAndWait().ifPresent(assignment -> {
            if (assignment != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Asignación registrada");
                ok.setContentText("La asignación se creó correctamente.");
                ok.showAndWait();
                loadAssignments();
            }
        });
    }

    private void showEditAssignmentDialog(TeacherSubject assignment) {
        TeacherSubjectFormDialog dialog = new TeacherSubjectFormDialog(assignment);
        dialog.showAndWait().ifPresent(updated -> {
            if (updated != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Asignación actualizada");
                ok.setContentText("Los cambios se guardaron correctamente.");
                ok.showAndWait();
                loadAssignments();
            }
        });
    }

    private void showDeleteConfirmation(TeacherSubject assignment) {
        String teacherName = assignment.getTeacher() != null ? assignment.getTeacher().getFullName() : "N/A";
        String subjectName = assignment.getSubject() != null ? assignment.getSubject().getName() : "N/A";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("¿Eliminar asignación?");
        alert.setContentText(
                "¿Deseas eliminar la asignación de " + teacherName +
                        " a la materia " + subjectName + "?");

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    controller.deleteAssignment(assignment.getId());

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Asignación eliminada");
                    ok.showAndWait();

                    loadAssignments();
                } catch (Exception e) {
                    showError("Error al eliminar asignación: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }
}