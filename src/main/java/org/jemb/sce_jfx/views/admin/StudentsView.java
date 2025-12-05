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
import org.jemb.sce_jfx.controllers.StudentController;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.views.dialogs.StudentFormDialog;
import org.jemb.sce_jfx.views.dialogs.StudentPerformanceDialog;

import java.time.format.DateTimeFormatter;

public class StudentsView extends VBox {

    private final int rowsPerPage = 12;

    private TableView<Student> studentsTable;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private StudentController studentController;
    private Pagination pagination;
    private Label paginationInfo;

    private ObservableList<Student> masterStudentsList = FXCollections.observableArrayList();
    private ObservableList<Student> currentDisplayedList = FXCollections.observableArrayList();

    public StudentsView() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm());

        studentController = new StudentController();

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

        loadStudents();
    }

    public void refresh() {
        loadStudents();
    }

    private VBox createHeader() {
        Label title = new Label("Estudiantes");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona los estudiantes del sistema");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createSearchAndFilters() {
        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre, código o email...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todos", "Activos", "Inactivos", "Graduados");
        statusFilter.setValue("Todos");
        statusFilter.setPrefWidth(150);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        Button newStudentBtn = new Button("+ Nuevo Estudiante");
        newStudentBtn.setTooltip(new Tooltip("Agregar estudiante"));
        newStudentBtn.getStyleClass().add("primary-button");
        newStudentBtn.setOnAction(e -> showNewStudentDialog());

        HBox searchRow = new HBox(15, searchField, statusFilter, newStudentBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        return searchRow;
    }

    private VBox createTableWithPagination() {
        studentsTable = new TableView<>();
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentsTable.setEditable(false);
        studentsTable.setPrefHeight(800);

        TableColumn<Student, Void> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<Student, Void>() {
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

        TableColumn<Student, String> codeCol = new TableColumn<>("MATRICULA");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("studentCode"));

        TableColumn<Student, String> nameCol = new TableColumn<>("NOMBRE");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFullName()));

        TableColumn<Student, String> emailCol = new TableColumn<>("EMAIL");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Student, String> phoneCol = new TableColumn<>("TELÉFONO");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                }else {
                    setText(item);
                }
            }
        });

        TableColumn<Student, String> enrollmentCol = new TableColumn<>("FECHA INSCRIPCIÓN");
        enrollmentCol.setCellValueFactory(c -> {
            Student s = c.getValue();
            if (s.getEnrollmentDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        s.getEnrollmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        enrollmentCol.setPrefWidth(50);

        TableColumn<Student, String> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item.equals("active") ? "Activo" : "Inactivo");
                    statusLabel.getStyleClass().add(item.equals("active") ? "status-active" : "status-inactive");
                    setGraphic(statusLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(60);

        TableColumn<Student, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<Student, Void>() {

            private final Button editBtn;
            private final Button deleteBtn;
            private final Button chartBtn;

            {
                ImageView iconStat = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/stats.png"))
                );
                iconStat.setFitWidth(16);
                iconStat.setFitHeight(16);
                chartBtn = new Button("", iconStat);
                chartBtn.getStyleClass().add("chart-button");
                chartBtn.setTooltip(new Tooltip("Ver gráficos de rendimiento"));
                chartBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    showPerformanceCharts(student);
                });

                ImageView iconEdit = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/edit.png"))
                );
                iconEdit.setFitWidth(16);
                iconEdit.setFitHeight(16);
                editBtn = new Button("", iconEdit);
                editBtn.getStyleClass().add("edit-button");
                editBtn.setTooltip(new Tooltip("Editar estudiante"));
                editBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    showEditStudentDialog(student);
                });

                ImageView iconDelete = new ImageView(
                        new Image(getClass().getResourceAsStream("/org/jemb/sce_jfx/icons/delete.png"))
                );
                iconDelete.setFitWidth(16);
                iconDelete.setFitHeight(16);
                deleteBtn = new Button("", iconDelete);
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setTooltip(new Tooltip("Eliminar estudiante"));
                deleteBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(student);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    HBox buttons = new HBox(8, chartBtn, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        studentsTable.getColumns().addAll(
                indexCol, codeCol, nameCol, emailCol, phoneCol, enrollmentCol, statusCol, actionsCol);
        paginationInfo = new Label();

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForPage(newVal.intValue());
        });

        VBox tableContainer = new VBox(15, studentsTable, paginationInfo, pagination);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadStudents() {
        masterStudentsList.setAll(studentController.getAllStudents());
        filterAndPaginate();
    }

    private void filterAndPaginate() {
        String search = searchField.getText().toLowerCase();
        String filter = statusFilter.getValue();

        ObservableList<Student> filtered = FXCollections.observableArrayList();

        for (Student s : masterStudentsList) {
            boolean matchesSearch = search.isEmpty() ||
                    s.getFullName().toLowerCase().contains(search) ||
                    s.getStudentCode().toLowerCase().contains(search) ||
                    (s.getEmail() != null && s.getEmail().toLowerCase().contains(search));

            boolean matchesStatus = filter.equals("Todos") ||
                    (filter.equals("Activos") && s.isActive()) ||
                    (filter.equals("Inactivos") && s.getStatus().equals("inactive")) ||
                    (filter.equals("Graduados") && s.isGraduated());

            if (matchesSearch && matchesStatus)
                filtered.add(s);
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
            studentsTable.setItems(FXCollections.observableArrayList());
            paginationInfo.setText("No hay estudiantes para mostrar");
        } else {
            studentsTable.setItems(FXCollections.observableArrayList(
                    currentDisplayedList.subList(from, to)));

            paginationInfo.setText(
                    String.format("Mostrando %d-%d de %d estudiantes",
                            from + 1, to, currentDisplayedList.size()));
        }
    }

    private void showNewStudentDialog() {
        StudentFormDialog dialog = new StudentFormDialog(null);
        dialog.showAndWait().ifPresent(student -> {
            if (student != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Estudiante registrado");
                ok.setContentText(student.getFullName() + " agregado correctamente.");
                ok.showAndWait();
                loadStudents();
            }
        });
    }

    private void showEditStudentDialog(Student student) {
        StudentFormDialog dialog = new StudentFormDialog(student);
        dialog.showAndWait().ifPresent(updated -> {
            if (updated != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Estudiante actualizado");
                ok.setContentText("Los cambios se guardaron correctamente.");
                ok.showAndWait();
                loadStudents();
            }
        });
    }

    private void showDeleteConfirmation(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("¿Eliminar estudiante?");
        alert.setContentText(
                "¿Deseas eliminar a " + student.getFullName() +
                        " (" + student.getStudentCode() + ")?");

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    studentController.deleteStudent(student.getId());

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Estudiante eliminado");
                    ok.showAndWait();

                    loadStudents();
                } catch (Exception e) {
                    showError("Error al eliminar estudiante: " + e.getMessage());
                }
            }
        });
    }

    private void showPerformanceCharts(Student student) {
        StudentPerformanceDialog dialog = new StudentPerformanceDialog(student);
        dialog.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.showAndWait();
    }
}
