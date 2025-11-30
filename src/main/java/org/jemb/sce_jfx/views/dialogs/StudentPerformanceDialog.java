package org.jemb.sce_jfx.views.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
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
        setHeaderText("Rendimiento de " + student.getFullName());

        // Configurar botones
        getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // Cargar estilos
        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm());

        // Crear contenido
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Configurar tamaño
        getDialogPane().setPrefSize(950, 650);
        getDialogPane().setMinSize(800, 500);
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Verificar si el estudiante tiene calificaciones
        if (!performanceService.hasGrades(student.getId())) {
            Label noDataLabel = new Label("Este estudiante aún no tiene calificaciones registradas.");
            noDataLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
            noDataLabel.setStyle("-fx-text-fill: #6b7280;");
            container.getChildren().add(noDataLabel);
            return container;
        }

        // Crear pestañas con gráficos
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        subjectChartTab = createSubjectChartTab();
        evaluationTypeChartTab = createEvaluationTypeChartTab();
        progressChartTab = createProgressChartTab();

        tabPane.getTabs().addAll(subjectChartTab, evaluationTypeChartTab, progressChartTab);

        // Información general
        Label infoLabel = createInfoLabel();

        container.getChildren().addAll(infoLabel, tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);

        return container;
    }

    private Label createInfoLabel() {
        double overallAvg = performanceService.getOverallAverage(student.getId());
        String avgText = String.format("Promedio General: %.2f", overallAvg);

        Label label = new Label(avgText);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setStyle("-fx-text-fill: #1f2937; -fx-padding: 10 0;");

        return label;
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

        // Aplicar colores a las barras
        barChart.setStyle("-fx-bar-fill: #3b82f6;");

        VBox container = new VBox(barChart);
        container.setPadding(new Insets(10));
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

        // Mostrar porcentajes
        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.1f", data.getPieValue());
            Tooltip tooltip = new Tooltip(data.getName() + "\nCalificación: " + percentage);
            Tooltip.install(data.getNode(), tooltip);
        });

        VBox container = new VBox(pieChart);
        container.setPadding(new Insets(10));
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

        // Agregar tooltips
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
        container.setPadding(new Insets(10));
        VBox.setVgrow(lineChart, javafx.scene.layout.Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }
}
