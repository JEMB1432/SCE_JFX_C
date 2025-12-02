package org.jemb.sce_jfx.views.teacher;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.utils.UserSession;

import java.util.Arrays;
import java.util.function.Consumer;

public class SidebarTeacher extends VBox {
    private Button btnDashboard;
    private Button btnMaterias;
    private Button btnCalificaciones;
    private Button btnReportes;
    private Button btnProfile;

    private User currentUser;
    private Consumer<String> viewChangeListener;

    public void setViewChangeListener(Consumer<String> listener) {
        this.viewChangeListener = listener;
    }

    public SidebarTeacher() {
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/sidebar.css").toExternalForm());
        getStyleClass().add("sidebar");

        currentUser = UserSession.getInstance().getCurrentUser();

        createLogo();
        createUserInfo();
        createMenu();

        this.setFillWidth(true);
        this.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);
    }

    private void createLogo() {
        SVGPath scaledLogoIcon = createScaledSvgIcon(
                "M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9zm6.82 6L12 12.72L5.18 9L12 5.28zM17 16l-5 2.72L7 16v-3.73L12 15l5-2.73z",
                20);

        Label logoText = new Label("SCE");
        logoText.getStyleClass().add("logo-text");

        HBox logo = new HBox(10, scaledLogoIcon, logoText);
        logo.getStyleClass().add("logo");
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(0, 20, 20, 20));

        this.getChildren().add(logo);
    }

    private void createUserInfo() {
        ImageView userAvatar = new ImageView();
        userAvatar.setFitWidth(40);
        userAvatar.setFitHeight(40);
        userAvatar.getStyleClass().add("user-avatar");

        Circle clip = new Circle(20, 20, 20);
        userAvatar.setClip(clip);

        String userName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName()
                : "Usuario";
        Label userNameLabel = new Label(userName);
        userNameLabel.getStyleClass().add("user-name");

        String userRole = currentUser != null ? getRoleDisplayName(currentUser.getRole()) : "Sin rol";
        Label userRoleLabel = new Label(userRole);
        userRoleLabel.getStyleClass().add("user-role");

        VBox userDetails = new VBox(2, userNameLabel, userRoleLabel);
        userDetails.setAlignment(Pos.CENTER_LEFT);

        HBox userInfo = new HBox(10, userAvatar, userDetails);
        userInfo.setMaxWidth(Double.MAX_VALUE);
        userInfo.getStyleClass().add("user-info");
        userInfo.setPadding(new Insets(0, 20, 20, 20));

        this.getChildren().add(userInfo);
    }

    private String getRoleDisplayName(String role) {
        if (role == null)
            return "Sin rol";
        return switch (role.toLowerCase()) {
            case "admin" -> "Administrador";
            case "teacher" -> "Profesor";
            case "assistant" -> "Asistente";
            default -> role;
        };
    }

    private void createMenu() {
        btnDashboard = crearMenuButton("Dashboard",
                "M3 13h8V3H3zm0 8h8v-6H3zm10 0h8V11h-8zm0-18v6h8V3z");

        btnMaterias = crearMenuButton("Mis Materias",
                "M20.75 16.714a1 1 0 0 1-.014.143a.75.75 0 0 1-.736.893H6a1.25 1.25 0 1 0 0 2.5h14a.75.75 0 0 1 0 1.5H6A2.75 2.75 0 0 1 3.25 19V5A2.75 2.75 0 0 1 6 2.25h13.4c.746 0 1.35.604 1.35 1.35zM9 6.25a.75.75 0 0 0 0 1.5h6a.75.75 0 0 0 0-1.5z");

        btnCalificaciones = crearMenuButton("Calificaciones",
                "M12.688 4.411h6.625v8.828h4.411L16 22.067l-7.724-8.828h4.411zM0 25.38h32v2.203H0z");

        btnReportes = crearMenuButton("Reportes",
                "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8");

        btnProfile = crearMenuButton("Mi Perfil",
                "M8 7a4 4 0 1 1 8 0a4 4 0 0 1-8 0m0 6a5 5 0 0 0-5 5a3 3 0 0 0 3 3h12a3 3 0 0 0 3-3a5 5 0 0 0-5-5z");

        btnDashboard.getStyleClass().add("active");

        // Crear contenedor para los botones del menú
        VBox menuButtons = new VBox(5,
                btnDashboard,
                btnMaterias,
                btnCalificaciones,
                btnReportes,
                btnProfile);
        menuButtons.getStyleClass().add("sidebar-menu");
        VBox.setVgrow(menuButtons, Priority.ALWAYS);

        // Botón de cerrar sesión
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.getStyleClass().add("primary-button");
        logoutButton.setOnAction(e -> {
            UserSession.getInstance().clear();
            if (viewChangeListener != null) {
                viewChangeListener.accept("logout");
            }
        });

        HBox logoutContainer = new HBox(logoutButton);
        logoutContainer.setAlignment(Pos.BOTTOM_CENTER);
        logoutContainer.getStyleClass().add("logout-container");
        logoutContainer.setPadding(new Insets(0, 0, 20, 0));

        VBox menu = new VBox(menuButtons, logoutContainer);
        menu.setFillWidth(true);
        VBox.setVgrow(menu, Priority.ALWAYS);

        this.getChildren().add(menu);
    }

    private Button crearMenuButton(String text, String svgPath) {
        SVGPath icon = createScaledSvgIcon(svgPath, 20);

        StackPane iconContainer = new StackPane(icon);
        iconContainer.setPrefSize(24, 24);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getStyleClass().add("icon-container");

        Label label = new Label(text);
        label.getStyleClass().add("menu-label");

        HBox content = new HBox(8, iconContainer, label);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPrefHeight(35);
        content.getStyleClass().add("button-content");

        Button button = new Button();
        button.setGraphic(content);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("menu-button");

        button.setOnAction(e -> {
            setActiveButton(button);
            if (viewChangeListener != null) {
                viewChangeListener.accept(text);
            }
        });

        return button;
    }

    private SVGPath createScaledSvgIcon(String path, double desiredSize) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.getStyleClass().addAll("icon", "svg-path");

        Bounds bounds = icon.getBoundsInLocal();

        double scale;
        if (bounds.getWidth() > bounds.getHeight()) {
            scale = desiredSize / bounds.getWidth();
        } else {
            scale = desiredSize / bounds.getHeight();
        }

        icon.setScaleX(scale);
        icon.setScaleY(scale);

        return icon;
    }

    private void setActiveButton(Button activeButton) {
        Arrays.asList(btnDashboard, btnMaterias, btnCalificaciones, btnReportes, btnProfile)
                .forEach(btn -> btn.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }
}