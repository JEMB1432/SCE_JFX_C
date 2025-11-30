package org.jemb.sce_jfx.views.teacher;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class TeacherDashboardView extends VBox {
public TeacherDashboardView() {
 // Cargar CSS necesarios según componentes usados. Asumo un 'teacher.css' o uso de 'admin.css' si no hay uno específico.
 getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                 // Aquí deberías usar el CSS específico para docente si lo tienes, o admin.css si es compartido
                getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm()
                 );
getStyleClass().add("dashboard-view");

 setPadding(new Insets(30));
 setSpacing(20);

 // Título (Adaptado al rol de Docente)
Label title = new Label("Dashboard Docente");
 title.getStyleClass().add("view-title");
 title.setFont(Font.font("System", FontWeight.BOLD, 28));

Label subtitle = new Label("Estadísticas académicas de tus materias y estudiantes");
subtitle.getStyleClass().add("view-subtitle");

VBox header = new VBox(8, title, subtitle);
header.setAlignment(Pos.CENTER_LEFT);

// Tarjetas de estadísticas (Adaptadas al rol de Docente)
HBox statsCards = createTeacherStatsCards();

// Contenido principal
VBox content = new VBox(20, header, statsCards);
content.setAlignment(Pos.TOP_LEFT);

getChildren().add(content);
 }

 private HBox createTeacherStatsCards() {
 HBox cards = new HBox(20);
 cards.setAlignment(Pos.CENTER_LEFT);

 // Tarjeta 1: Total Estudiantes a Cargo (Estudiantes inscritos en sus materias)
 VBox card1 = createStatCard("Estudiantes Totales", "0", "#305252");
 // Tarjeta 2: Total Materias Asignadas
 VBox card2 = createStatCard("Materias Asignadas", "0", "#1C7C54");
 // Tarjeta 3: Calificaciones Pendientes (Cuántas calificaciones faltan por registrar/modificar)
 VBox card3 = createStatCard("Calificaciones Ptes.", "0", "#FF7043");
 // Tarjeta 4: Promedio Grupal General (El promedio de todos sus estudiantes en sus materias)
 VBox card4 = createStatCard("Promedio Grupal", "0.00", "#305252");

 cards.getChildren().addAll(card1, card2, card3, card4);
 return cards;
}

// El método para crear la tarjeta es el mismo para mantener la consistencia visual
 private VBox createStatCard(String title, String value, String color) {
 VBox card = new VBox(10);
 card.getStyleClass().add("stat-card");
 card.setPadding(new Insets(20));
 card.setPrefWidth(200);
 card.setPrefHeight(120);

 Label titleLabel = new Label(title);
 titleLabel.getStyleClass().add("stat-title");

 Label valueLabel = new Label(value);
 valueLabel.getStyleClass().add("stat-value");
 valueLabel.setStyle("-fx-text-fill: " + color + ";");

 card.getChildren().addAll(titleLabel, valueLabel);
 return card;
}
}