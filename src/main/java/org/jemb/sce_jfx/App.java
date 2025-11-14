package org.jemb.sce_jfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jemb.sce_jfx.config.DatabaseConfig;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.views.LoginView;

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
                System.out.println("Login exitoso! Usuario: " + currentUser.getFullName() + 
                                 " (" + currentUser.getEmail() + ") - Rol: " + currentUser.getRole());
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

    public static void main(String[] args) {
        launch(args);
    }
}
