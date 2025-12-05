package org.jemb.sce_jfx.views.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.models.Student;
import org.jemb.sce_jfx.models.StudentPerformanceData;
import org.jemb.sce_jfx.models.SubjectPerformance;
import org.jemb.sce_jfx.services.PerformanceService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Diálogo que muestra gráficos de rendimiento académico de un estudiante
 */
public class StudentPerformanceDialog extends Dialog<ButtonType> {

    private final Student student;
    private final PerformanceService performanceService;

    private Tab subjectChartTab;
    private Tab evaluationTypeChartTab;
    private Tab progressChartTab;

    public StudentPerformanceDialog(Student student) {
        this.student = student;
        this.performanceService = new PerformanceService();

        setTitle("Rendimiento Académico");
        setHeaderText(null);

        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/components/dialogs.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tabs.css").toExternalForm());

        VBox content = createContent();
        DialogUtils.setDialogIcon(this);
        getDialogPane().setContent(content);

        getDialogPane().setPrefSize(1000, 700);
        getDialogPane().setMinSize(900, 600);

        getDialogPane().getStyleClass().add("dialog-pane");

        Button closeButton = (Button) getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeButton != null) {
            closeButton.getStyleClass().addAll("cancel-button");
        }
    }

    private VBox createContent() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(24));
        container.setStyle("-fx-background-color: white;");

        VBox header = createCustomHeader();

        if (!performanceService.hasGrades(student.getId())) {
            VBox emptyState = createEmptyState();
            container.getChildren().addAll(header, emptyState);
            return container;
        }

        HBox statsBox = createStatsBox();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("tab-pane");

        subjectChartTab = createSubjectChartTab();
        evaluationTypeChartTab = createEvaluationTypeChartTab();
        progressChartTab = createProgressChartTab();

        tabPane.getTabs().addAll(subjectChartTab, evaluationTypeChartTab, progressChartTab);

        container.getChildren().addAll(header, statsBox, tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);

        return container;
    }

    /**
     * Header personalizado con título y nombre del estudiante
     */
    private VBox createCustomHeader() {
        VBox header = new VBox(4);

        Label titleLabel = new Label("Rendimiento Académico");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label studentLabel = new Label(student.getFullName());
        studentLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280;");

        header.getChildren().addAll(titleLabel, studentLabel);

        return header;
    }

    /**
     * Estado vacío cuando no hay calificaciones
     */
    private VBox createEmptyState() {
        VBox emptyState = new VBox(12);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60, 20, 60, 20));
        emptyState.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 12px;");

        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("Sin calificaciones registradas");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subMessage = new Label("Este estudiante aún no tiene calificaciones en el sistema");
        subMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        emptyState.getChildren().addAll(icon, message, subMessage);
        VBox.setVgrow(emptyState, javafx.scene.layout.Priority.ALWAYS);

        return emptyState;
    }

    /**
     * Caja de estadísticas con cards
     */
    private HBox createStatsBox() {
        HBox statsBox = new HBox(16);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        double overallAvg = performanceService.getOverallAverage(student.getId());
        List<SubjectPerformance> subjects = performanceService.getSubjectSummary(student.getId());

        // Card de promedio general
        VBox avgCard = createStatCard("Promedio General", String.format("%.2f", overallAvg), "#2f5856");

        // Card de materias
        VBox subjectsCard = createStatCard("Materias", String.valueOf(subjects.size()), "#1C7C54");

        // Card de mejor materia
        if (!subjects.isEmpty()) {
            SubjectPerformance best = subjects.stream()
                    .max((s1, s2) -> Double.compare(s1.getAverageScore(), s2.getAverageScore()))
                    .orElse(subjects.get(0));

            VBox bestCard = createStatCard("Mejor Materia",
                    best.getSubjectCode() + " (" + String.format("%.1f", best.getAverageScore()) + ")",
                    "#10b981");

            statsBox.getChildren().addAll(avgCard, subjectsCard, bestCard);
        } else {
            statsBox.getChildren().addAll(avgCard, subjectsCard);
        }

        return statsBox;
    }

    /**
     * Crea una tarjeta de estadística
     */
    private VBox createStatCard(String label, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-color: #e6e6e6; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-border-width: 1px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.05), 8, 0, 0, 2);"
        );
        card.setPrefWidth(200);
        card.setMinHeight(80);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-font-weight: 500;");

        Label valueText = new Label(value);
        valueText.setStyle(
                "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: " + accentColor + ";"
        );

        card.getChildren().addAll(labelText, valueText);

        return card;
    }

    /**
     * Pestaña 1: Gráfico de barras con promedio por materia
     */
    private Tab createSubjectChartTab() {
        Tab tab = new Tab("Por Materia");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);

        xAxis.setLabel("Materias");
        yAxis.setLabel("Calificación Promedio");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Rendimiento por Materia");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Promedio");

        List<SubjectPerformance> subjects = performanceService.getSubjectSummary(student.getId());

        for (SubjectPerformance subject : subjects) {
            String label = subject.getSubjectCode();
            XYChart.Data<String, Number> data = new XYChart.Data<>(label, subject.getAverageScore());
            series.getData().add(data);
        }

        barChart.getData().add(series);

        VBox container = new VBox(barChart);
        container.setPadding(new Insets(20));
        VBox.setVgrow(barChart, javafx.scene.layout.Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    /**
     * Pestaña 2: Gráfico circular con distribución por tipo de evaluación
     */
    private Tab createEvaluationTypeChartTab() {
        Tab tab = new Tab("Por Tipo de Evaluación");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Promedio por Tipo de Evaluación");
        pieChart.setLegendSide(Side.RIGHT);

        Map<String, Double> evaluationSummary = performanceService.getEvaluationTypeSummary(student.getId());

        evaluationSummary.forEach((type, avg) -> {
            PieChart.Data slice = new PieChart.Data(
                    String.format("%s (%.1f)", type, avg),
                    avg);
            pieChart.getData().add(slice);
        });

        // Mostrar tooltips con información detallada
        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.1f", data.getPieValue());
            Tooltip tooltip = new Tooltip(data.getName() + "\nCalificación: " + percentage);
            Tooltip.install(data.getNode(), tooltip);
        });

        VBox container = new VBox(pieChart);
        container.setPadding(new Insets(20));
        VBox.setVgrow(pieChart, javafx.scene.layout.Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    /**
     * Pestaña 3: Gráfico de líneas con progreso temporal
     */
    private Tab createProgressChartTab() {
        Tab tab = new Tab("Progreso Temporal");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);

        xAxis.setLabel("Fecha");
        yAxis.setLabel("Calificación");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Evolución de Calificaciones");
        lineChart.setCreateSymbols(true);

        List<StudentPerformanceData> progressData = performanceService.getProgressOverTime(student.getId());

        // Agrupar por materia
        Map<String, XYChart.Series<String, Number>> seriesMap = new java.util.HashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (StudentPerformanceData data : progressData) {
            String subjectName = data.getSubjectCode();

            XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(subjectName,
                    k -> {
                        XYChart.Series<String, Number> s = new XYChart.Series<>();
                        s.setName(subjectName);
                        return s;
                    });

            String dateLabel = data.getGradedAt().format(dateFormatter);
            String label = dateLabel + " - " + data.getEvaluationType();

            XYChart.Data<String, Number> point = new XYChart.Data<>(label, data.getScore());
            series.getData().add(point);
        }

        lineChart.getData().addAll(seriesMap.values());

        // Agregar tooltips con estilo personalizado
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Tooltip tooltip = new Tooltip(
                        series.getName() + "\n" +
                                data.getXValue() + "\n" +
                                "Calificación: " + String.format("%.1f", data.getYValue()));
                Tooltip.install(data.getNode(), tooltip);
            }
        }

        VBox container = new VBox(lineChart);
        container.setPadding(new Insets(20));
        VBox.setVgrow(lineChart, javafx.scene.layout.Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }
}