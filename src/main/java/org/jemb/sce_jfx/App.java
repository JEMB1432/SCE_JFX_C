package org.jemb.sce_jfx;

import javafx.application.Application;
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

        Scene scene = new Scene(loginView, 800, 600);
        stage.setTitle("Sistema de Control de Estudiantes - Iniciar Sesión");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConfig.close();
    }

    private void loadAdminView(Stage stage) {
        AdminMainView adminView = new AdminMainView();
        Scene adminScene = new Scene(adminView, 1400, 900);
        stage.setScene(adminScene);
        stage.setTitle("Sistema de Control de Estudiantes - Administrador");
        stage.setMaximized(true);
    }

    private void loadTeacherView(Stage stage) {
        TeacherMainView teacherMainView = new TeacherMainView();
        Scene teacherScene = new Scene(teacherMainView, 1400, 900);
        stage.setScene(teacherScene);
        stage.setTitle("Sistema de Control de Estudiantes - Docente");
        stage.setMaximized(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
