package org.jemb.sce_jfx.views.admin;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jemb.sce_jfx.App;
import org.jemb.sce_jfx.utils.UserSession;
import org.jemb.sce_jfx.views.ProfileView;

import java.util.HashMap;
import java.util.Map;

public class AdminMainView extends HBox {
    private Sidebar sidebar;
    private Map<String, Node> views;
    private StackPane contentArea;
    private String currentView;

    public AdminMainView() {
        views = new HashMap<>();
        contentArea = new StackPane();
        initializeViews();
        setupLayout();
        applyStyles();

        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
    }

    private void initializeViews() {
        views.put("Dashboard", new DashboardView());
        views.put("Estudiantes", new StudentsView());
        views.put("Materias", new SubjectsView());
        views.put("Asignar Materias", new TeacherSubjectsView());
        views.put("Inscripciones", new EnrollmentsView());
        views.put("Usuarios", new UsersView());
        views.put("Mi Perfil", new ProfileView());
    }

    private void setupLayout() {
        sidebar = new Sidebar();
        sidebar.setViewChangeListener(this::switchView);

        switchToView("Estudiantes");

        getChildren().addAll(sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        setSpacing(0);

        contentArea.prefWidthProperty().bind(widthProperty().subtract(sidebar.widthProperty()));
        contentArea.prefHeightProperty().bind(heightProperty());
    }

    private void applyStyles() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm());
        contentArea.getStyleClass().add("content-area");
        this.getStyleClass().add("admin-main-view");
    }

    private void switchView(String viewName) {
        if (viewName.equals("logout")) {
            handleLogout();
            return;
        }

        switchToView(viewName);
    }

    private void switchToView(String viewName) {
        Node view = views.get(viewName);

        if (view != null && !viewName.equals(currentView)) {
            contentArea.getChildren().clear();

            contentArea.getChildren().add(view);
            currentView = viewName;
        }
    }

    private void handleLogout() {
        UserSession.getInstance().clear();

        Platform.runLater(() -> {
            goBackToLogin();
        });
    }

    private void goBackToLogin() {
        Platform.runLater(() -> {
            Stage stage = (Stage) this.getScene().getWindow();
            App.showLoginView(stage);
        });
    }
}