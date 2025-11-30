package org.jemb.sce_jfx.views.admin;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.utils.UserSession;

import java.util.Arrays;
import java.util.function.Consumer;

public class Sidebar extends VBox {
    private Button btnDashboard;
    private Button btnEstudiantes;
    private Button btnMaterias;
    private Button btnAsignacionMaterias;
    private Button btnInscripciones;
    private Button btnCalificaciones;
    private Button btnReportes;
    private Button btnUsuarios;
    private Button btnConfiguracion;
    private Button btnProfile;

    private User currentUser;
    private Consumer<String> viewChangeListener;

    public void setViewChangeListener(Consumer<String> listener) {
        this.viewChangeListener = listener;
    }

    public Sidebar() {
        // Cargar CSS necesarios según componentes usados (botones)
        getStylesheets().addAll(
            getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
            getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
            getClass().getResource("/org/jemb/sce_jfx/styles/components/sidebar.css").toExternalForm()
        );
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
        SVGPath logoIcon = new SVGPath();
        logoIcon.setContent("M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9zm6.82 6L12 12.72L5.18 9L12 5.28zM17 16l-5 2.72L7 16v-3.73L12 15l5-2.73z");
        logoIcon.getStyleClass().add("logo-icon");

        SVGPath scaledLogoIcon = createScaledSvgIcon(
                "M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9zm6.82 6L12 12.72L5.18 9L12 5.28zM17 16l-5 2.72L7 16v-3.73L12 15l5-2.73z",
                20
        );

        Label logoText = new Label("SCE");
        logoText.getStyleClass().add("logo-text");

        HBox logo = new HBox(10, scaledLogoIcon, logoText);
        logo.getStyleClass().add("logo");
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setPadding(new Insets(0, 20, 20, 20));

        this.getChildren().add(logo);
    }

    private void createUserInfo() {
        // Avatar placeholder
        ImageView userAvatar = new ImageView();
        userAvatar.setFitWidth(40);
        userAvatar.setFitHeight(40);
        userAvatar.getStyleClass().add("user-avatar");

        Circle clip = new Circle(20, 20, 20);
        userAvatar.setClip(clip);

        String userName = currentUser != null ?
                currentUser.getFirstName() + " " + currentUser.getLastName() : "Usuario";
        Label userNameLabel = new Label(userName);
        userNameLabel.getStyleClass().add("user-name");

        String userRole = currentUser != null ?
                getRoleDisplayName(currentUser.getRole()) : "Sin rol";
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
        if (role == null) return "Sin rol";
        return switch (role.toLowerCase()) {
            case "admin" -> "Administrador";
            case "teacher" -> "Profesor";
            case "assistant" -> "Asistente";
            default -> role;
        };
    }

    private void createMenu() {
        // Botones del menú con iconos SVG usando escalado preciso
        btnDashboard = crearMenuButton("Dashboard",
                "M3 12a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1zm0 8a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1v-4a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1zm10 0a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1v-8a1 1 0 0 0-1-1h-6a1 1 0 0 0-1 1zm1-17a1 1 0 0 0-1 1v4a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1z");

        btnEstudiantes = crearMenuButton("Estudiantes",
                "M10.5 16a5.5 5.5 0 1 0 0-11a5.5 5.5 0 0 0 0 11M23 16a4 4 0 1 0 0-8a4 4 0 0 0 0 8M5 18a3 3 0 0 0-3 3v.15S2 27 10.5 27s8.5-5.85 8.5-5.85V21a3 3 0 0 0-3-3zm18 7c-1.456 0-2.608-.198-3.521-.513c.432-.7.68-1.375.82-1.92a6.4 6.4 0 0 0 .193-1.196l.004-.107l.001-.042V21a4.48 4.48 0 0 0-1.145-3h8.241A2.406 2.406 0 0 1 30 20.406S30 25 23 25");

        btnMaterias = crearMenuButton("Materias",
                "M20.75 16.714a1 1 0 0 1-.014.143a.75.75 0 0 1-.736.893H6a1.25 1.25 0 1 0 0 2.5h14a.75.75 0 0 1 0 1.5H6A2.75 2.75 0 0 1 3.25 19V5A2.75 2.75 0 0 1 6 2.25h13.4c.746 0 1.35.604 1.35 1.35zM9 6.25a.75.75 0 0 0 0 1.5h6a.75.75 0 0 0 0-1.5z");

        btnAsignacionMaterias = crearMenuButton("Asignar Materias",
                "M2.06935 5.00839C2 5.37595 2 5.81722 2 6.69975V13.75C2 17.5212 2 19.4069 3.17157 20.5784C4.34315 21.75 6.22876 21.75 10 21.75H14C17.7712 21.75 19.6569 21.75 20.8284 20.5784C22 19.4069 22 17.5212 22 13.75V11.5479C22 8.91554 22 7.59935 21.2305 6.74383C21.1598 6.66514 21.0849 6.59024 21.0062 6.51946C20.1506 5.75 18.8345 5.75 16.2021 5.75H15.8284C14.6747 5.75 14.0979 5.75 13.5604 5.59678C13.2651 5.5126 12.9804 5.39471 12.7121 5.24543C12.2237 4.97367 11.8158 4.56578 11 3.75L10.4497 3.19975C10.1763 2.92633 10.0396 2.78961 9.89594 2.67051C9.27652 2.15704 8.51665 1.84229 7.71557 1.76738C7.52976 1.75 7.33642 1.75 6.94975 1.75C6.06722 1.75 5.62595 1.75 5.25839 1.81935C3.64031 2.12464 2.37464 3.39031 2.06935 5.00839ZM12 11C12.4142 11 12.75 11.3358 12.75 11.75V13H14C14.4142 13 14.75 13.3358 14.75 13.75C14.75 14.1642 14.4142 14.5 14 14.5H12.75V15.75C12.75 16.1642 12.4142 16.5 12 16.5C11.5858 16.5 11.25 16.1642 11.25 15.75V14.5H10C9.58579 14.5 9.25 14.1642 9.25 13.75C9.25 13.3358 9.58579 13 10 13H11.25V11.75C11.25 11.3358 11.5858 11 12 11Z");

        btnInscripciones = crearMenuButton("Inscripciones",
                "M 15.5547 53.125 L 40.4453 53.125 C 45.2969 53.125 47.7109 50.6640 47.7109 45.7890 L 47.7109 24.5078 L 30.7422 24.5078 C 27.7422 24.5078 26.3359 23.0781 26.3359 20.0781 L 26.3359 2.8750 L 15.5547 2.8750 C 10.7266 2.8750 8.2891 5.3594 8.2891 10.2344 L 8.2891 45.7890 C 8.2891 50.6875 10.7266 53.125 15.5547 53.125 Z M 30.8125 21.2969 L 47.4531 21.2969 C 47.2891 20.3359 46.6094 19.3984 45.5078 18.2500 L 32.5703 5.1015 C 31.4922 3.9766 30.5078 3.2969 29.5234 3.1328 L 29.5234 20.0313 C 29.5234 20.875 29.9687 21.2969 30.8125 21.2969 Z M 18.9766 34.6562 C 18.0156 34.6562 17.3359 33.9766 17.3359 33.0625 C 17.3359 32.1484 18.0156 31.4687 18.9766 31.4687 L 37.0469 31.4687 C 37.9844 31.4687 38.7109 32.1484 38.7109 33.0625 C 38.7109 33.9766 37.9844 34.6562 37.0469 34.6562 Z M 18.9766 43.5859 C 18.0156 43.5859 17.3359 42.9062 17.3359 41.9922 C 17.3359 41.0781 18.0156 40.3984 18.9766 40.3984 L 37.0469 40.3984 C 37.9844 40.3984 38.7109 41.0781 38.7109 41.9922 C 38.7109 42.9062 37.9844 43.5859 37.0469 43.5859 Z");

        btnCalificaciones = crearMenuButton("Calificaciones",
                "M12.688 4.411h6.625v8.828h4.411L16 22.067l-7.724-8.828h4.411zM0 25.38h32v2.203H0z");

        btnReportes = crearMenuButton("Reportes",
                "M26.688 2.88c2.55 0 3.992.37 5.686 2.03l.196.197l12.938 13.148c1.552 1.59 2.044 2.764 2.167 4.835l.024.537l.01.579l.002 21.588c0 4.778-2.319 7.237-6.978 7.333l-.288.003h-24.89c-4.732 0-7.167-2.341-7.263-7.045l-.003-.291V10.239c0-4.777 2.341-7.259 6.979-7.356l.287-.003zm-.516 3.773H15.766c-2.36 0-3.62 1.252-3.7 3.435l-.004.222v35.414c0 2.268 1.186 3.548 3.45 3.628l.23.005h24.516c2.359 0 3.598-1.208 3.676-3.41l.004-.223V24.677h-13.36c-2.823 0-4.32-1.371-4.403-4.157l-.003-.25zm-8.046 26.282c.863 0 1.562.7 1.562 1.563v10.94c0 .862-.7 1.562-1.562 1.562h-1.563C15.7 47 15 46.3 15 45.437v-10.94c0-.862.7-1.562 1.563-1.562zm14.065 4.688c.863 0 1.562.7 1.562 1.563v6.251c0 .863-.7 1.563-1.562 1.563h-1.563c-.863 0-1.563-.7-1.563-1.563v-6.25c0-.864.7-1.564 1.563-1.564zm-7.033-9.376c.863 0 1.563.7 1.563 1.562v15.628c0 .863-.7 1.563-1.563 1.563h-1.563c-.863 0-1.562-.7-1.562-1.563V29.81c0-.863.7-1.562 1.562-1.562zM29.711 7.38v12.445c0 .883.332 1.267 1.133 1.309l.156.004h12.210z");

        btnUsuarios = crearMenuButton("Usuarios",
                "M11 21H4C4 17.4735 6.60771 14.5561 10 14.0709M19.8726 15.2038C19.8044 15.2079 19.7357 15.21 19.6667 15.21C18.6422 15.21 17.7077 14.7524 17 14C16.2923 14.7524 15.3578 15.2099 14.3333 15.2099C14.2643 15.2099 14.1956 15.2078 14.1274 15.2037C14.0442 15.5853 14 15.9855 14 16.3979C14 18.6121 15.2748 20.4725 17 21C18.7252 20.4725 20 18.6121 20 16.3979C20 15.9855 19.9558 15.5853 19.8726 15.2038ZM15 7C15 9.20914 13.2091 11 11 11C8.79086 11 7 9.20914 7 7C7 4.79086 8.79086 3 11 3C13.2091 3 15 4.79086 15 7Z");

        btnConfiguracion = crearMenuButton("Configuración",
                "M10.825 22q-.675 0-1.162-.45t-.588-1.1L8.85 18.8q-.325-.125-.612-.3t-.563-.375l-1.55.65q-.625.275-1.25.05t-.975-.8l-1.175-2.05q-.35-.575-.2-1.225t.675-1.075l1.325-1Q4.5 12.5 4.5 12.337v-.675q0-.162.025-.337l-1.325-1Q2.675 9.9 2.525 9.25t.2-1.225L3.9 5.975q.35-.575.975-.8t1.25.05l1.55.65q.275-.2.575-.375t.6-.3l.225-1.65q.1-.65.588-1.1T10.825 2h2.35q.675 0 1.163.45t.587 1.1l.225 1.65q.325.125.613.3t.562.375l1.55-.65q.625-.275 1.25-.05t.975.8l1.175 2.05q.35.575.2 1.225t-.675 1.075l-1.325 1q.025.175.025.338v.674q0 .163-.05.338l1.325 1q.525.425.675 1.075t-.2 1.225l-1.2 2.05q-.35.575-.975.8t-1.25-.05l-1.5-.65q-.275.2-.575.375t-.6.3l-.225 1.65q-.1.65-.587 1.1t-1.163.45zm1.225-6.5q1.45 0 2.475-1.025T15.55 12t-1.025-2.475T12.05 8.5q-1.475 0-2.488 1.025T8.55 12t1.013 2.475T12.05 15.5");

        btnProfile = crearMenuButton("Mi Perfil",
                "M8 7a4 4 0 1 1 8 0a4 4 0 0 1-8 0m0 6a5 5 0 0 0-5 5a3 3 0 0 0 3 3h12a3 3 0 0 0 3-3a5 5 0 0 0-5-5z");

        btnDashboard.getStyleClass().add("active");

        // Crear contenedor para los botones del menú
        VBox menuButtons = new VBox(5, btnDashboard, btnEstudiantes, btnMaterias, btnAsignacionMaterias,
                btnInscripciones, btnUsuarios, btnProfile);
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
        double originalAspectRatio = bounds.getWidth() / bounds.getHeight();

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
        Arrays.asList(btnDashboard, btnEstudiantes, btnMaterias, btnInscripciones, btnAsignacionMaterias,
                        btnCalificaciones, btnReportes, btnUsuarios, btnConfiguracion, btnProfile)
                .forEach(btn -> btn.getStyleClass().remove("active"));
        activeButton.getStyleClass().add("active");
    }
}