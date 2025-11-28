package org.jemb.sce_jfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.views.LoginView;
import org.jemb.sce_jfx.views.admin.AdminMainView;

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
                // Guardar usuario en sesión
                org.jemb.sce_jfx.utils.UserSession.getInstance().setCurrentUser(currentUser);
                
                // Cargar vista según el rol
                if (currentUser.isAdmin()) {
                    loadAdminView(stage);
                } else if (currentUser.isTeacher()) {
                    // TODO: Cargar vista de profesor
                    System.out.println("Vista de profesor próximamente");
                } else {
                    // TODO: Cargar vista de asistente
                    System.out.println("Vista de asistente próximamente");
                }
            }
        });

        Scene scene = new Scene(loginView, 1200, 800);
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

    public static void main(String[] args) {
        launch(args);
    }
}
