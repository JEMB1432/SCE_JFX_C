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
import org.jemb.sce_jfx.controllers.UserController;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.utils.UserSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UsersView extends VBox {

    private final int rowsPerPage = 12;

    private TableView<User> usersTable;
    private TextField searchField;
    private ComboBox<String> roleFilter;
    private ComboBox<String> statusFilter;
    private UserController controller;
    private Pagination pagination;
    private Label paginationInfo;

    private ObservableList<User> masterList = FXCollections.observableArrayList();
    private ObservableList<User> currentDisplayedList = FXCollections.observableArrayList();

    public UsersView() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm());

        controller = new UserController();

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

        loadUsers();
    }

    private VBox createHeader() {
        Label title = new Label("Gestión de Usuarios");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Administra usuarios del sistema (administradores, profesores, asistentes)");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createSearchAndFilters() {
        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre o email...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(350);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> filterAndPaginate());
            pause.playFromStart();
        });

        roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Todos los roles", "Administrador", "Profesor", "Asistente");
        roleFilter.setValue("Todos los roles");
        roleFilter.setPrefWidth(160);
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todos", "Activos", "Inactivos");
        statusFilter.setValue("Todos");
        statusFilter.setPrefWidth(130);
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndPaginate());

        Button newUserBtn = new Button("+ Nuevo Usuario");
        newUserBtn.setTooltip(new Tooltip("Agregar Usuario"));
        newUserBtn.getStyleClass().add("primary-button");
        newUserBtn.setOnAction(e -> showNewUserDialog());

        HBox searchRow = new HBox(15, searchField, roleFilter, statusFilter, newUserBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        return searchRow;
    }

    private VBox createTableWithPagination() {
        usersTable = new TableView<>();
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Void> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<User, Void>() {
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

        TableColumn<User, String> nameCol = new TableColumn<>("NOMBRE");
        nameCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(user.getFullName());
        });
        nameCol.setPrefWidth(200);

        TableColumn<User, String> emailCol = new TableColumn<>("EMAIL");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);

        TableColumn<User, String> roleCol = new TableColumn<>("ROL");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String roleText;
                    String styleClass;
                    switch (item) {
                        case "admin":
                            roleText = "Admin";
                            styleClass = "role-admin";
                            break;
                        case "teacher":
                            roleText = "Profesor";
                            styleClass = "role-teacher";
                            break;
                        case "assistant":
                            roleText = "Asistente";
                            styleClass = "role-assistant";
                            break;
                        default:
                            roleText = item;
                            styleClass = "role-default";
                    }
                    Label roleLabel = new Label(roleText);
                    roleLabel.getStyleClass().addAll("badge", styleClass);
                    setGraphic(roleLabel);
                }
            }
        });
        roleCol.setPrefWidth(120);

        TableColumn<User, Boolean> statusCol = new TableColumn<>("ESTADO");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(item ? "Activo" : "Inactivo");
                    statusLabel.getStyleClass().add(item ? "status-active" : "status-inactive");
                    setGraphic(statusLabel);
                }
                setAlignment(Pos.CENTER);
            }
        });
        statusCol.setPrefWidth(100);

        TableColumn<User, LocalDateTime> lastLoginCol = new TableColumn<>("ÚLTIMO LOGIN");
        lastLoginCol.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        lastLoginCol.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("-");
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    setText(item.format(formatter));
                }
            }
        });
        lastLoginCol.setPrefWidth(150);

        TableColumn<User, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<User, Void>() {

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
                    User user = getTableView().getItems().get(getIndex());
                    showEditUserDialog(user);
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
                    User user = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(user);
                });

                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());

                    User currentUser = UserSession.getInstance().getCurrentUser();
                    if (currentUser != null && user.getId().equals(currentUser.getId())) {
                        deleteBtn.setDisable(true);
                        deleteBtn.setStyle("-fx-opacity: 0.5;");
                    } else {
                        deleteBtn.setDisable(false);
                        deleteBtn.setStyle("");
                    }

                    HBox buttons = new HBox(8, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        usersTable.getColumns().addAll(
                indexCol, nameCol, emailCol, roleCol, statusCol, lastLoginCol, actionsCol);

        paginationInfo = new Label();

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForPage(newVal.intValue());
        });

        VBox tableContainer = new VBox(15, usersTable, paginationInfo, pagination);
        VBox.setVgrow(usersTable, Priority.ALWAYS);

        return tableContainer;
    }

    private void loadUsers() {
        masterList.setAll(controller.getAllUsers());
        filterAndPaginate();
    }

    private void filterAndPaginate() {
        String search = searchField.getText().toLowerCase();
        String roleFilterValue = roleFilter.getValue();
        String statusFilterValue = statusFilter.getValue();

        ObservableList<User> filtered = FXCollections.observableArrayList();

        for (User user : masterList) {
            boolean matchesSearch = search.isEmpty() ||
                    user.getFullName().toLowerCase().contains(search) ||
                    user.getEmail().toLowerCase().contains(search);

            boolean matchesRole = roleFilterValue.equals("Todos los roles");
            if (!matchesRole) {
                switch (roleFilterValue) {
                    case "Administrador":
                        matchesRole = user.isAdmin();
                        break;
                    case "Profesor":
                        matchesRole = user.isTeacher();
                        break;
                    case "Asistente":
                        matchesRole = user.isAssistant();
                        break;
                }
            }

            boolean matchesStatus = statusFilterValue.equals("Todos") ||
                    (statusFilterValue.equals("Activos") && user.isActive()) ||
                    (statusFilterValue.equals("Inactivos") && !user.isActive());

            if (matchesSearch && matchesRole && matchesStatus) {
                filtered.add(user);
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
            usersTable.setItems(FXCollections.observableArrayList());
            paginationInfo.setText("No hay usuarios para mostrar");
        } else {
            usersTable.setItems(FXCollections.observableArrayList(
                    currentDisplayedList.subList(from, to)));

            paginationInfo.setText(
                    String.format("Mostrando %d-%d de %d usuarios",
                            from + 1, to, currentDisplayedList.size()));
        }
    }

    private void showNewUserDialog() {
        UserFormDialog dialog = new UserFormDialog(null);
        dialog.showAndWait().ifPresent(user -> {
            if (user != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Usuario creado");
                ok.setContentText("El usuario se creó correctamente.");
                ok.showAndWait();
                loadUsers();
            }
        });
    }

    private void showEditUserDialog(User user) {
        UserFormDialog dialog = new UserFormDialog(user);
        dialog.showAndWait().ifPresent(updated -> {
            if (updated != null) {
                Alert ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setHeaderText("Usuario actualizado");
                ok.setContentText("Los cambios se guardaron correctamente.");
                ok.showAndWait();
                loadUsers();
            }
        });
    }

    private void showDeleteConfirmation(User user) {
        // Verificar que no sea el usuario actual
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null && user.getId().equals(currentUser.getId())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Operación no permitida");
            alert.setContentText("No puedes eliminar tu propio usuario.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("¿Eliminar usuario?");
        alert.setContentText(
                "¿Deseas eliminar al usuario " + user.getFullName() +
                        " (" + user.getEmail() + ")?\n\nEsta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                try {
                    controller.deleteUser(user.getId());

                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText("Usuario eliminado");
                    ok.showAndWait();

                    loadUsers();
                } catch (Exception e) {
                    showError("Error al eliminar usuario: " + e.getMessage());
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
