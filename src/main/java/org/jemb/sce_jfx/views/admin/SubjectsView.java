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
import org.jemb.sce_jfx.controllers.SubjectController;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.Subject;

public class SubjectsView extends VBox {

    private final int rowsPerPage = 12;

    private TableView<Subject> subjectsTable;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> semesterFilter;
    private SubjectController subjectController;
    private EnrollmentController enrollmentController;
    private Pagination pagination;
    private Label paginationInfo;

    private ObservableList<Subject> masterSubjectsList = FXCollections.observableArrayList();
    private ObservableList<Subject> currentDisplayedList = FXCollections.observableArrayList();

    public SubjectsView() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm()
        );

        subjectController = new SubjectController();
        enrollmentController = new EnrollmentController();

        setPadding(new Insets(30));
        setSpacing(20);

        pagination = new Pagination();
        pagination.setPageFactory(pageIndex -> null);

        VBox content = new VBox(20);
        content.getChildren().addAll(
                createHeader(),
                createSearchAndFilters(),
                createTableWithPagination()
        );

        getChildren().add(content);

        loadSubjects();
    }

    private VBox createHeader() {
        Label title = new Label("Materias");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Gestiona las materias del sistema");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createSearchAndFilters() {
        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre o código...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(400);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todas", "Activas", "Inactivas");
        statusFilter.setValue("Todas");
        statusFilter.setPrefWidth(150);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        semesterFilter = new ComboBox<>();
        semesterFilter.getItems().addAll("Todos los semestres", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        semesterFilter.setValue("Todos los semestres");
        semesterFilter.setPrefWidth(180);
        semesterFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        Button newSubjectBtn = new Button("+ Nueva Materia");
        newSubjectBtn.setTooltip(new Tooltip("Agregar una nueva materia"));
        newSubjectBtn.getStyleClass().add("primary-button");
        newSubjectBtn.setOnAction(e -> showNewSubjectDialog());

        HBox searchRow = new HBox(15, searchField, statusFilter, semesterFilter, newSubjectBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        return searchRow;
    }

    private VBox createTableWithPagination() {
        subjectsTable = new TableView<>();
        subjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Subject, Void> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<Subject, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else {
                    int currentPage = pagination.getCurrentPageIndex();
                    int index = getIndex();
                    setText(String.valueOf(currentPage * rowsPerPage + index + 1));
                }
            }
        });
        indexCol.setPrefWidth(50);

        TableColumn<Subject, String> codeCol = new TableColumn<>("CÓDIGO");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        codeCol.setPrefWidth(100);

        TableColumn<Subject, String> nameCol = new TableColumn<>("MATERIA");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Subject, Integer> creditsCol = new TableColumn<>("CRÉDITOS");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(100);

        TableColumn<Subject, Integer> semesterCol = new TableColumn<>("SEMESTRE");
        semesterCol.setCellValueFactory(new PropertyValueFactory<>("semesterAvailable"));
        semesterCol.setCellFactory(column -> new TableCell<Subject, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("-");
                } else {
                    setText(item + "°");
                }
            }
        });
        semesterCol.setPrefWidth(100);

        TableColumn<Subject, String> enrolledCol = new TableColumn<>("INSCRITOS");
        enrolledCol.setCellValueFactory(cellData -> {
            Subject subject = cellData.getValue();
            try {
                int count = enrollmentController.getEnrollmentsBySubject(subject.getId())
                        .stream()
                        .filter(e -> e.isEnrolled())
                        .mapToInt(e -> 1)
                        .sum();
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(count));
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("0");
            }
        });
        enrolledCol.setPrefWidth(100);

        TableColumn<Subject, String> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Subject, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item.equals("active") ? "Activa" : "Inactiva");
                    statusLabel.getStyleClass().add(item.equals("active") ? "status-active" : "status-inactive");
                    setGraphic(statusLabel);
                }
            }
        });
        statusCol.setPrefWidth(100);

        TableColumn<Subject, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<Subject, Void>() {

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
                    Subject subject = getTableView().getItems().get(getIndex());
                    showEditSubjectDialog(subject);
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
                    Subject subject = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(subject);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox buttons = new HBox(8, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });

        subjectsTable.getColumns().addAll(
                indexCol, codeCol, nameCol, creditsCol, semesterCol, enrolledCol, statusCol, actionsCol
        );

        paginationInfo = new Label();

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForPage(newVal.intValue());
        });

        VBox tableContainer = new VBox(15, subjectsTable, paginationInfo, pagination);
        VBox.setVgrow(subjectsTable, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadSubjects() {
        masterSubjectsList.setAll(subjectController.getAllSubjects());
        filterAndPaginate();
    }

    private void filterAndPaginate() {
        String search = searchField.getText().toLowerCase();
        String statusFilterValue = statusFilter.getValue();
        String semesterFilterValue = semesterFilter.getValue();

        ObservableList<Subject> filtered = FXCollections.observableArrayList();

        for (Subject s : masterSubjectsList) {
            boolean matchesSearch =
                    search.isEmpty() ||
                            s.getName().toLowerCase().contains(search) ||
                            s.getSubjectCode().toLowerCase().contains(search);

            boolean matchesStatus =
                    statusFilterValue.equals("Todas") ||
                            (statusFilterValue.equals("Activas") && s.isActive()) ||
                            (statusFilterValue.equals("Inactivas") && !s.isActive());

            boolean matchesSemester =
                    semesterFilterValue.equals("Todos los semestres") ||
                            (s.getSemesterAvailable() != null &&
                                    s.getSemesterAvailable().toString().equals(semesterFilterValue));

            if (matchesSearch && matchesStatus && matchesSemester) filtered.add(s);
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
            subjectsTable.setItems(FXCollections.observableArrayList());
            paginationInfo.setText("No hay materias para mostrar");
        } else {
            subjectsTable.setItems(FXCollections.observableArrayList(
                    currentDisplayedList.subList(from, to)
            ));

            paginationInfo.setText(
                    String.format("Mostrando %d-%d de %d materias",
                            from + 1, to, currentDisplayedList.size())
            );
        }
    }

    private void showNewSubjectDialog() {
        SubjectFormDialog dialog = new SubjectFormDialog(null);
        dialog.showAndWait().ifPresent(subject -> {
            if (subject != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Materia registrada");
                ok.setContentText(subject.getName() + " agregada correctamente.");
                ok.showAndWait();
                loadSubjects();
            }
        });
    }

    private void showEditSubjectDialog(Subject subject) {
        SubjectFormDialog dialog = new SubjectFormDialog(subject);
        dialog.showAndWait().ifPresent(updated -> {
            if (updated != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Materia actualizada");
                ok.setContentText("Los cambios se guardaron correctamente.");
                ok.showAndWait();
                loadSubjects();
            }
        });
    }

    private void showDeleteConfirmation(Subject subject) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("¿Eliminar materia?");
        alert.setContentText(
                "¿Deseas eliminar la materia " + subject.getName() +
                        " (" + subject.getSubjectCode() + ")?"
        );

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    subjectController.deleteSubject(subject.getId());

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Materia eliminada");
                    ok.showAndWait();

                    loadSubjects();
                } catch (Exception e) {
                    showError("Error al eliminar materia: " + e.getMessage());
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