package org.jemb.sce_jfx.views.teacher;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jemb.sce_jfx.App;
import org.jemb.sce_jfx.utils.UserSession;
import org.jemb.sce_jfx.views.ProfileView;

import java.util.HashMap;
import java.util.Map;

public class TeacherMainView extends HBox {
    private SidebarTeacher sidebar;
    private Map<String, Node> views;
    private StackPane contentArea;
    private String currentView;

    public TeacherMainView() {
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
        views.put("Mis Materias", new MySubjectsView());
        views.put("Calificaciones", new GradesView());
        views.put("Reportes", new ReportsView());
        views.put("Gráficas de Rendimiento", new PerformanceChartsView());
        views.put("Mi Perfil", new ProfileView());
    }

    private void setupLayout() {
        sidebar = new SidebarTeacher();
        sidebar.setViewChangeListener(this::switchView);

        switchToView("Mis Materias");

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

            refreshCurrentView(view);
        }
    }

    private void refreshCurrentView(Node view) {
        try {
            var refreshMethod = view.getClass().getMethod("refresh");
            refreshMethod.invoke(view);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
            System.err.println("Error al refrescar la vista: " + e.getMessage());
            e.printStackTrace();
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
