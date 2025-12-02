package org.jemb.sce_jfx.views.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jemb.sce_jfx.controllers.EnrollmentController;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.*;
import org.jemb.sce_jfx.services.PerformanceService;
import org.jemb.sce_jfx.utils.UserSession;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Vista de gráficas de rendimiento para profesores
 * Muestra estadísticas agregadas de todos los estudiantes de las materias del profesor
 */
public class PerformanceChartsView extends VBox {

    private final TeacherSubjectController teacherSubjectController;
    private final EnrollmentController enrollmentController;
    private final PerformanceService performanceService;
    private final User currentUser;

    private ComboBox<TeacherSubject> subjectCombo;
    private ObservableList<TeacherSubject> subjectsList;

    private VBox chartsContainer;
    private Label statsLabel;

    public PerformanceChartsView() {
        teacherSubjectController = new TeacherSubjectController();
        enrollmentController = new EnrollmentController();
        performanceService = new PerformanceService();
        currentUser = UserSession.getInstance().getCurrentUser();
        subjectsList = FXCollections.observableArrayList();

        // Cargar estilos
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm());

        setPadding(new Insets(30));
        setSpacing(20);

        getChildren().addAll(
                createHeader(),
                createFiltersSection(),
                createChartsSection());

        loadSubjects();
    }

    private VBox createHeader() {
        Label title = new Label("Gráficas de Rendimiento");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Visualiza el rendimiento académico de tus estudiantes");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox createFiltersSection() {
        Label subjectLabel = new Label("Materia:");
        subjectLabel.getStyleClass().add("form-label");

        subjectCombo = new ComboBox<>(subjectsList);
        subjectCombo.setPrefWidth(400);
        subjectCombo.setCellFactory(listView -> new ListCell<TeacherSubject>() {
            @Override
            protected void updateItem(TeacherSubject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getSubject() == null) {
                    setText(null);
                } else {
                    String text = item.getSubject().getSubjectCode() + " - " +
                            item.getSubject().getName() +
                            " (" + item.getAcademicYear() + " - Sem " + item.getSemester() + ")";
                    setText(text);
                }
            }
        });
        subjectCombo.setButtonCell(new ListCell<TeacherSubject>() {
            @Override
            protected void updateItem(TeacherSubject item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getSubject() == null) {
                    setText("Selecciona una materia");
                } else {
                    String text = item.getSubject().getSubjectCode() + " - " +
                            item.getSubject().getName();
                    setText(text);
                }
            }
        });
        subjectCombo.getStyleClass().add("filter-combo");
        subjectCombo.setOnAction(e -> onSubjectSelected());

        Button refreshButton = new Button("🔄 Actualizar");
        refreshButton.getStyleClass().add("secondary-button");
        refreshButton.setOnAction(e -> {
            loadSubjects();
            onSubjectSelected();
        });

        HBox filters = new HBox(15, subjectLabel, subjectCombo, refreshButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 0, 10, 0));

        return filters;
    }

    private ScrollPane createChartsSection() {
        chartsContainer = new VBox(20);
        chartsContainer.setPadding(new Insets(20));

        statsLabel = new Label("Selecciona una materia para ver las gráficas de rendimiento");
        statsLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        statsLabel.setStyle("-fx-text-fill: #6b7280;");
        chartsContainer.getChildren().add(statsLabel);

        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return scrollPane;
    }

    private void loadSubjects() {
        if (currentUser == null) {
            showError("No se pudo obtener el usuario actual");
            return;
        }

        try {
            var subjects = teacherSubjectController.getActiveAssignmentsByTeacher(currentUser.getId());
            subjectsList.setAll(subjects);

            if (!subjects.isEmpty()) {
                subjectCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onSubjectSelected() {
        TeacherSubject selected = subjectCombo.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getSubject() == null) {
            chartsContainer.getChildren().clear();
            statsLabel.setText("Selecciona una materia para ver las gráficas de rendimiento");
            chartsContainer.getChildren().add(statsLabel);
            return;
        }

        try {
            // Obtener estudiantes inscritos en esta materia
            List<Enrollment> bySubject =
                    enrollmentController.getEnrollmentsBySubject(selected.getSubjectId());

            List<Enrollment> enrollments = bySubject.stream()
                    .filter(e -> e.getAcademicYear().equals(selected.getAcademicYear()))
                    .filter(e -> e.getSemester() == selected.getSemester()).toList();


            if (enrollments.isEmpty()) {
                chartsContainer.getChildren().clear();
                Label noDataLabel = new Label("No hay estudiantes inscritos en esta materia");
                noDataLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
                noDataLabel.setStyle("-fx-text-fill: #6b7280;");
                chartsContainer.getChildren().add(noDataLabel);
                return;
            }

            // Generar gráficas
            generateCharts(selected, enrollments);

        } catch (Exception e) {
            showError("Error al cargar datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateCharts(TeacherSubject teacherSubject, List<Enrollment> enrollments) {
        chartsContainer.getChildren().clear();

        // Estadísticas generales
        statsLabel = createStatsLabel(teacherSubject, enrollments);
        chartsContainer.getChildren().add(statsLabel);

        // Obtener datos de rendimiento de todos los estudiantes
        List<StudentPerformanceData> allPerformanceData = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            List<StudentPerformanceData> studentData = performanceService.getStudentPerformance(
                    enrollment.getStudentId());
            allPerformanceData.addAll(studentData);
        }

        // Filtrar solo datos de la materia seleccionada
        String subjectId = teacherSubject.getSubjectId();
        List<StudentPerformanceData> subjectData = allPerformanceData.stream()
                .filter(data -> data.getSubjectId().equals(subjectId))
                .collect(toList());

        if (subjectData.isEmpty()) {
            Label noGradesLabel = new Label("No hay calificaciones registradas para esta materia");
            noGradesLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
            noGradesLabel.setStyle("-fx-text-fill: #6b7280;");
            chartsContainer.getChildren().add(noGradesLabel);
            return;
        }

        // Crear pestañas con diferentes gráficas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefHeight(600);

        Tab averageTab = createAverageChartTab(subjectData, enrollments);
        Tab distributionTab = createDistributionChartTab(subjectData);
        Tab evaluationTypeTab = createEvaluationTypeChartTab(subjectData);
        Tab progressTab = createProgressChartTab(subjectData);

        tabPane.getTabs().addAll(averageTab, distributionTab, evaluationTypeTab, progressTab);

        chartsContainer.getChildren().add(tabPane);
    }

    private Label createStatsLabel(TeacherSubject teacherSubject, List<Enrollment> enrollments) {
        String subjectName = teacherSubject.getSubject() != null ?
                teacherSubject.getSubject().getName() : "Materia";

        int totalStudents = enrollments.size();

        // Calcular promedio general
        double overallAvg = 0.0;
        int studentsWithGrades = 0;

        for (Enrollment enrollment : enrollments) {
            if (performanceService.hasGrades(enrollment.getStudentId())) {
                double studentAvg = performanceService.getOverallAverage(enrollment.getStudentId());
                overallAvg += studentAvg;
                studentsWithGrades++;
            }
        }

        if (studentsWithGrades > 0) {
            overallAvg /= studentsWithGrades;
        }

        String statsText = String.format(
                "%s | Estudiantes: %d | Promedio General: %.2f",
                subjectName, totalStudents, overallAvg);

        Label label = new Label(statsText);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        label.setStyle("-fx-text-fill: #1f2937; -fx-padding: 10 0;");

        return label;
    }

    /**
     * Gráfica 1: Promedio por estudiante
     */
    private Tab createAverageChartTab(List<StudentPerformanceData> subjectData, List<Enrollment> enrollments) {
        Tab tab = new Tab("Promedio por Estudiante");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Estudiantes");
        yAxis.setLabel("Calificación Promedio");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Promedio de Calificaciones por Estudiante");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Promedio");

        // Calcular promedio por estudiante
        Map<String, List<Double>> studentScores = new LinkedHashMap<>();
        Map<String, String> studentNames = new HashMap<>();

        for (StudentPerformanceData data : subjectData) {
            String studentId = data.getStudentId();
            String studentName = data.getFullName();

            studentNames.put(studentId, studentName);
            studentScores.computeIfAbsent(studentId, k -> new ArrayList<>()).add(data.getScore());
        }

        // Crear datos para el gráfico
        for (Map.Entry<String, List<Double>> entry : studentScores.entrySet()) {
            String studentId = entry.getKey();
            List<Double> scores = entry.getValue();

            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            String label = studentNames.get(studentId);

            // Truncar nombre si es muy largo
            if (label.length() > 20) {
                label = label.substring(0, 17) + "...";
            }

            XYChart.Data<String, Number> data = new XYChart.Data<>(label, avg);
            series.getData().add(data);
        }

        barChart.getData().add(series);
        barChart.setStyle("-fx-bar-fill: #3b82f6;");

        VBox container = new VBox(barChart);
        container.setPadding(new Insets(10));
        VBox.setVgrow(barChart, Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    /**
     * Gráfica 2: Distribución de calificaciones
     */
    private Tab createDistributionChartTab(List<StudentPerformanceData> subjectData) {
        Tab tab = new Tab("Distribución de Calificaciones");

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribución de Calificaciones");
        pieChart.setLegendSide(Side.RIGHT);

        // Categorizar calificaciones
        int excellent = 0; // 90-100
        int good = 0;      // 80-89
        int average = 0;   // 70-79
        int below = 0;     // <70

        for (StudentPerformanceData data : subjectData) {
            double score = data.getScore();
            if (score >= 90) excellent++;
            else if (score >= 80) good++;
            else if (score >= 70) average++;
            else below++;
        }

        if (excellent > 0) {
            pieChart.getData().add(new PieChart.Data("Excelente (90-100)", excellent));
        }
        if (good > 0) {
            pieChart.getData().add(new PieChart.Data("Bueno (80-89)", good));
        }
        if (average > 0) {
            pieChart.getData().add(new PieChart.Data("Regular (70-79)", average));
        }
        if (below > 0) {
            pieChart.getData().add(new PieChart.Data("Bajo (<70)", below));
        }

        // Agregar tooltips
        pieChart.getData().forEach(data -> {
            int count = (int) data.getPieValue();
            Tooltip tooltip = new Tooltip(data.getName() + "\nCantidad: " + count);
            Tooltip.install(data.getNode(), tooltip);
        });

        VBox container = new VBox(pieChart);
        container.setPadding(new Insets(10));
        VBox.setVgrow(pieChart, Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    /**
     * Gráfica 3: Promedio por tipo de evaluación
     */
    private Tab createEvaluationTypeChartTab(List<StudentPerformanceData> subjectData) {
        Tab tab = new Tab("Por Tipo de Evaluación");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Tipos de Evaluación");
        yAxis.setLabel("Calificación Promedio");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Promedio por Tipo de Evaluación");
        barChart.setLegendVisible(false);

        // Agrupar por tipo de evaluación
        Map<String, List<Double>> evaluationScores = subjectData.stream()
                .collect(Collectors.groupingBy(
                        StudentPerformanceData::getEvaluationType,
                        Collectors.mapping(StudentPerformanceData::getScore, toList())));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Promedio");

        evaluationScores.forEach((type, scores) -> {
            double avg = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            XYChart.Data<String, Number> data = new XYChart.Data<>(type, avg);
            series.getData().add(data);
        });

        barChart.getData().add(series);
        barChart.setStyle("-fx-bar-fill: #10b981;");

        VBox container = new VBox(barChart);
        container.setPadding(new Insets(10));
        VBox.setVgrow(barChart, Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    /**
     * Gráfica 4: Progreso temporal
     */
    private Tab createProgressChartTab(List<StudentPerformanceData> subjectData) {
        Tab tab = new Tab("Progreso Temporal");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Fecha");
        yAxis.setLabel("Calificación");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Evolución de Calificaciones");
        lineChart.setCreateSymbols(true);

        // Ordenar por fecha
        subjectData.sort(Comparator.comparing(StudentPerformanceData::getGradedAt));

        // Agrupar por tipo de evaluación
        Map<String, XYChart.Series<String, Number>> seriesMap = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        for (StudentPerformanceData data : subjectData) {
            String evalType = data.getEvaluationType();

            XYChart.Series<String, Number> series = seriesMap.computeIfAbsent(evalType,
                    k -> {
                        XYChart.Series<String, Number> s = new XYChart.Series<>();
                        s.setName(evalType);
                        return s;
                    });

            String dateLabel = data.getGradedAt().format(dateFormatter);
            XYChart.Data<String, Number> point = new XYChart.Data<>(dateLabel, data.getScore());
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
        VBox.setVgrow(lineChart, Priority.ALWAYS);

        tab.setContent(container);
        return tab;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}