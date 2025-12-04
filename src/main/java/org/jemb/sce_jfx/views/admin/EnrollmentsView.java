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
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.models.Enrollment;

import java.time.format.DateTimeFormatter;

public class EnrollmentsView extends VBox {

    private final int rowsPerPage = 12;

    private TableView<Enrollment> enrollmentsTable;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> semesterFilter;
    private TextField academicYearField;
    private EnrollmentController enrollmentController;
    private Pagination pagination;
    private Label paginationInfo;

    private ObservableList<Enrollment> masterEnrollmentsList = FXCollections.observableArrayList();
    private ObservableList<Enrollment> currentDisplayedList = FXCollections.observableArrayList();

    public EnrollmentsView() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm());

        enrollmentController = new EnrollmentController();

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

        loadEnrollments();
    }

    private VBox createHeader() {
        Label title = new Label("Inscripciones");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona las inscripciones de estudiantes a materias");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createSearchAndFilters() {
        searchField = new TextField();
        searchField.setPromptText("Buscar por estudiante o materia...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todos", "Inscritos", "Completados", "Dados de baja");
        statusFilter.setValue("Todos");
        statusFilter.setPrefWidth(150);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        semesterFilter = new ComboBox<>();
        semesterFilter.getItems().addAll("Todos", "Semestre 1", "Semestre 2", "Semestre 3", "Semestre 4", "Semestre 5", "Semestre 6", "Semestre 7", "Semestre 8", "Semestre 9", "Semestre 10");
        semesterFilter.setValue("Todos");
        semesterFilter.setPrefWidth(130);
        semesterFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        academicYearField = new TextField();
        academicYearField.setPromptText("Año académico");
        academicYearField.setPrefWidth(130);
        academicYearField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        Button newEnrollmentBtn = new Button("+ Nueva Inscripción");
        newEnrollmentBtn.getStyleClass().add("primary-button");
        newEnrollmentBtn.setOnAction(e -> showNewEnrollmentDialog());

        HBox filterRow = new HBox(15, searchField, statusFilter, semesterFilter, academicYearField, newEnrollmentBtn);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        VBox filtersContainer = new VBox(10, filterRow);
        return filtersContainer;
    }

    private VBox createTableWithPagination() {
        enrollmentsTable = new TableView<>();
        enrollmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Enrollment, Void> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<Enrollment, Void>() {
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
        indexCol.setPrefWidth(50);

        TableColumn<Enrollment, String> studentCol = new TableColumn<>("ESTUDIANTE");
        studentCol.setCellValueFactory(c -> {
            Enrollment e = c.getValue();
            if (e.getStudent() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        e.getStudent().getFullName() + " (" + e.getStudent().getStudentCode() + ")");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        studentCol.setPrefWidth(200);

        TableColumn<Enrollment, String> subjectCol = new TableColumn<>("MATERIA");
        subjectCol.setCellValueFactory(c -> {
            Enrollment e = c.getValue();
            if (e.getSubject() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        e.getSubject().getName() + " (" + e.getSubject().getSubjectCode() + ")");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        subjectCol.setPrefWidth(200);

        TableColumn<Enrollment, String> academicYearCol = new TableColumn<>("AÑO ACADÉMICO");
        academicYearCol.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        academicYearCol.setPrefWidth(120);

        TableColumn<Enrollment, Integer> semesterCol = new TableColumn<>("SEMESTRE");
        semesterCol.setCellValueFactory(new PropertyValueFactory<>("semester"));
        semesterCol.setPrefWidth(90);

        TableColumn<Enrollment, String> dateCol = new TableColumn<>("FECHA INSCRIPCIÓN");
        dateCol.setCellValueFactory(c -> {
            Enrollment e = c.getValue();
            if (e.getEnrollmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        e.getEnrollmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        dateCol.setPrefWidth(130);

        TableColumn<Enrollment, String> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Enrollment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    switch (item) {
                        case "enrolled":
                            setText("Inscrito");
                            setStyle("-fx-text-fill: #10b981;");
                            break;
                        case "completed":
                            setText("Completado");
                            setStyle("-fx-text-fill: #3b82f6;");
                            break;
                        case "dropped":
                            setText("Dado de baja");
                            setStyle("-fx-text-fill: #ef4444;");
                            break;
                        default:
                            setText(item);
                            setStyle("");
                    }
                }
                setAlignment(Pos.CENTER);
            }
        });
        statusCol.setPrefWidth(120);

        TableColumn<Enrollment, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<Enrollment, Void>() {

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
                editBtn.setTooltip(new Tooltip("Editar materia"));
                editBtn.setOnAction(e -> {
                    Enrollment enrollment = getTableView().getItems().get(getIndex());
                    showEditEnrollmentDialog(enrollment);
                });

                ImageView iconDelete = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/delete.png"))
                );
                iconDelete.setFitWidth(16);
                iconDelete.setFitHeight(16);
                deleteBtn = new Button("", iconDelete);
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setTooltip(new Tooltip("Eliminar materia"));
                deleteBtn.setOnAction(e -> {
                    Enrollment enrollment = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(enrollment);
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

        enrollmentsTable.getColumns().addAll(
                indexCol, studentCol, subjectCol, academicYearCol, semesterCol, dateCol, statusCol, actionsCol);

        paginationInfo = new Label();

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForPage(newVal.intValue());
        });

        VBox tableContainer = new VBox(15, enrollmentsTable, paginationInfo, pagination);
        VBox.setVgrow(enrollmentsTable, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadEnrollments() {
        masterEnrollmentsList.setAll(enrollmentController.getAllEnrollments());
        filterAndPaginate();
    }

    private void filterAndPaginate() {
        String search = searchField.getText().toLowerCase();
        String statusFilterValue = statusFilter.getValue();
        String semesterFilterValue = semesterFilter.getValue();
        String academicYear = academicYearField.getText().toLowerCase();

        ObservableList<Enrollment> filtered = FXCollections.observableArrayList();

        for (Enrollment e : masterEnrollmentsList) {
            boolean matchesSearch = search.isEmpty() ||
                    (e.getStudent() != null && e.getStudent().getFullName().toLowerCase().contains(search)) ||
                    (e.getStudent() != null && e.getStudent().getStudentCode().toLowerCase().contains(search)) ||
                    (e.getSubject() != null && e.getSubject().getName().toLowerCase().contains(search)) ||
                    (e.getSubject() != null && e.getSubject().getSubjectCode().toLowerCase().contains(search));

            boolean matchesStatus = statusFilterValue.equals("Todos") ||
                    (statusFilterValue.equals("Inscritos") && e.isEnrolled()) ||
                    (statusFilterValue.equals("Completados") && e.isCompleted()) ||
                    (statusFilterValue.equals("Dados de baja") && e.isDropped());

            boolean matchesSemester = semesterFilterValue.equals("Todos") ||
                    (semesterFilterValue.equals("Semestre 1") && e.getSemester() == 1) ||
                    (semesterFilterValue.equals("Semestre 2") && e.getSemester() == 2);

            boolean matchesAcademicYear = academicYear.isEmpty() ||
                    (e.getAcademicYear() != null && e.getAcademicYear().toLowerCase().contains(academicYear));

            if (matchesSearch && matchesStatus && matchesSemester && matchesAcademicYear) {
                filtered.add(e);
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
            enrollmentsTable.setItems(FXCollections.observableArrayList());
            paginationInfo.setText("No hay inscripciones para mostrar");
        } else {
            enrollmentsTable.setItems(FXCollections.observableArrayList(
                    currentDisplayedList.subList(from, to)));

            paginationInfo.setText(
                    String.format("Mostrando %d-%d de %d inscripciones",
                            from + 1, to, currentDisplayedList.size()));
        }
    }

    private void showNewEnrollmentDialog() {
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(null);
        dialog.showAndWait().ifPresent(enrollment -> {
            if (enrollment != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Inscripción registrada");
                ok.setContentText("La inscripción se agregó correctamente.");
                ok.showAndWait();
                loadEnrollments();
            }
        });
    }

    private void showEditEnrollmentDialog(Enrollment enrollment) {
        EnrollmentFormDialog dialog = new EnrollmentFormDialog(enrollment);
        dialog.showAndWait().ifPresent(updated -> {
            if (updated != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Inscripción actualizada");
                ok.setContentText("Los cambios se guardaron correctamente.");
                ok.showAndWait();
                loadEnrollments();
            }
        });
    }

    private void showDeleteConfirmation(Enrollment enrollment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("¿Eliminar inscripción?");

        String studentName = enrollment.getStudent() != null ? enrollment.getStudent().getFullName() : "Estudiante";
        String subjectName = enrollment.getSubject() != null ? enrollment.getSubject().getName() : "Materia";

        alert.setContentText(
                "¿Deseas eliminar la inscripción de " + studentName +
                        " en " + subjectName + "?");

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    enrollmentController.deleteEnrollment(enrollment.getId());

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Inscripción eliminada");
                    ok.showAndWait();

                    loadEnrollments();
                } catch (Exception e) {
                    showError("Error al eliminar inscripción: " + e.getMessage());
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
