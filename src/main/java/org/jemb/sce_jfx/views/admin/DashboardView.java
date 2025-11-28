package org.jemb.sce_jfx.views.admin;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardView extends VBox {
    public DashboardView() {
        // Cargar CSS necesarios según componentes usados
        getStylesheets().addAll(
            getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
            getClass().getResource("/org/jemb/sce_jfx/styles/admin.css").toExternalForm()
        );
        getStyleClass().add("dashboard-view");

        setPadding(new Insets(30));
        setSpacing(20);

        // Título
        Label title = new Label("Dashboard");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Resumen general del sistema académico");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        // Tarjetas de estadísticas
        HBox statsCards = createStatsCards();

        // Contenido principal
        VBox content = new VBox(20, header, statsCards);
        content.setAlignment(Pos.TOP_LEFT);

        getChildren().add(content);
    }

    private HBox createStatsCards() {
        HBox cards = new HBox(20);
        cards.setAlignment(Pos.CENTER_LEFT);

        // Tarjeta 1: Total Estudiantes
        VBox card1 = createStatCard("Total Estudiantes", "0", "#305252");
        // Tarjeta 2: Total Materias
        VBox card2 = createStatCard("Total Materias", "0", "#1C7C54");
        // Tarjeta 3: Inscripciones Activas
        VBox card3 = createStatCard("Inscripciones Activas", "0", "#FF7043");
        // Tarjeta 4: Promedio General
        VBox card4 = createStatCard("Promedio General", "0.00", "#305252");

        cards.getChildren().addAll(card1, card2, card3, card4);
        return cards;
    }

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

