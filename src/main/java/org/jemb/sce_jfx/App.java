package org.jemb.sce_jfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.views.LoginView;
import org.jemb.sce_jfx.views.admin.AdminMainView;
import org.jemb.sce_jfx.views.teacher.TeacherMainView;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            DatabaseConfig.initialize();
        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }

        LoginView loginView = new LoginView();
        
        loginView.setOnLoginSuccess(() -> {
            User currentUser = loginView.getAuthService().getCurrentUser();
            if (currentUser != null) {
                org.jemb.sce_jfx.utils.UserSession.getInstance().setCurrentUser(currentUser);
                
                if (currentUser.isAdmin()) {
                    loadAdminView(stage);
                } else if (currentUser.isTeacher()) {
                    loadTeacherView(stage);
                } else {
                    // TODO: Cargar vista de asistente
                    System.out.println("Vista de asistente próximamente");
                }
            }
        });

        Scene scene = new Scene(loginView);
        stage.setTitle("Sistema de Control de Estudiantes - Iniciar Sesión");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConfig.close();
    }

    private static void loadAdminView(Stage stage) {
        AdminMainView adminView = new AdminMainView();
        Scene adminScene = new Scene(adminView);
        stage.setScene(adminScene);
        stage.setTitle("Sistema de Control de Estudiantes - Administrador");
        stage.setMaximized(true);
    }

    private static void loadTeacherView(Stage stage) {
        TeacherMainView teacherMainView = new TeacherMainView();
        Scene teacherScene = new Scene(teacherMainView);
        stage.setScene(teacherScene);
        stage.setTitle("Sistema de Control de Estudiantes - Docente");
        stage.setMaximized(true);
    }

    public static void showLoginView(Stage stage) {
        org.jemb.sce_jfx.utils.UserSession.getInstance().clear();

        boolean wasMaximized = stage.isMaximized();
        double width = stage.getWidth();
        double height = stage.getHeight();

        LoginView loginView = new LoginView();

        loginView.setOnLoginSuccess(() -> {
            User currentUser = loginView.getAuthService().getCurrentUser();
            if (currentUser != null) {
                org.jemb.sce_jfx.utils.UserSession.getInstance().setCurrentUser(currentUser);

                if (currentUser.isAdmin()) {
                    loadAdminView(stage);
                } else if (currentUser.isTeacher()) {
                    loadTeacherView(stage);
                } else {
                    System.out.println("Vista de asistente próximamente");
                }
            }
        });

        Scene scene = new Scene(loginView, width, height);
        stage.setTitle("Sistema de Control de Estudiantes - Iniciar Sesión");
        stage.setScene(scene);

        stage.setMaximized(wasMaximized);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
