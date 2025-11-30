package org.jemb.sce_jfx.views.teacher;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vista del Dashboard del Profesor
 * TODO: Implementar estadísticas y resumen de materias
 */
public class TeacherDashboardView extends VBox {

    public TeacherDashboardView() {
        setPadding(new Insets(30));
        setSpacing(20);
       setAlignment(Pos.CENTER);

        Label title = new Label("Dashboard del Profesor");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Vista en desarrollo - próximamente");
        subtitle.setFont(Font.font("System", 16));
        subtitle.setStyle("-fx-text-fill: #6b7280;");

        getChildren().addAll(title, subtitle);
    }
}
