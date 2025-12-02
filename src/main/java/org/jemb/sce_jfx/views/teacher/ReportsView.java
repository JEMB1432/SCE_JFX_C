package org.jemb.sce_jfx.views.teacher;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jemb.sce_jfx.controllers.TeacherSubjectController;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.models.User;
import org.jemb.sce_jfx.services.PDFReportService;
import org.jemb.sce_jfx.utils.UserSession;

import java.io.File;

/**
 * Vista de Reportes para profesores
 * Permite generar reportes en PDF de calificaciones por materia
 */
public class ReportsView extends VBox {

    private final TeacherSubjectController teacherSubjectController;
    private final PDFReportService pdfReportService;
    private final User currentUser;

    private ComboBox<TeacherSubject> subjectComboBox;
    private ObservableList<TeacherSubject> subjectsList;
    private Button generateReportButton;
    private Label statusLabel;

    public ReportsView() {
        teacherSubjectController = new TeacherSubjectController();
        pdfReportService = new PDFReportService();
        currentUser = UserSession.getInstance().getCurrentUser();
        subjectsList = FXCollections.observableArrayList();

        // Cargar estilos
        getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm());

        setPadding(new Insets(30));
        setSpacing(20);

        getChildren().addAll(
                createHeader(),
                createReportForm(),
                createStatusSection());

        loadSubjects();
    }

    private VBox createHeader() {
        Label title = new Label("Generar Reportes");
        title.getStyleClass().add("view-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));

        Label subtitle = new Label("Genera reportes en PDF de calificaciones por materia");
        subtitle.getStyleClass().add("view-subtitle");

        VBox header = new VBox(8, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createReportForm() {
        VBox form = new VBox(20);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #f9fafb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Información del reporte
        Label infoLabel = new Label("Selecciona una materia para generar el reporte de calificaciones:");
        infoLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        infoLabel.setWrapText(true);

        // Selector de materia
        HBox subjectSelector = new HBox(15);
        subjectSelector.setAlignment(Pos.CENTER_LEFT);

        Label subjectLabel = new Label("Materia:");
        subjectLabel.getStyleClass().add("form-label");
        subjectLabel.setMinWidth(100);

        subjectComboBox = new ComboBox<>(subjectsList);
        subjectComboBox.setPrefWidth(400);
        subjectComboBox.setPromptText("Selecciona una materia");
        subjectComboBox.setCellFactory(listView -> new TeacherSubjectListCell());
        subjectComboBox.setButtonCell(new TeacherSubjectListCell());

        subjectSelector.getChildren().addAll(subjectLabel, subjectComboBox);

        // Botón de generar reporte
        generateReportButton = new Button("📄 Generar Reporte PDF");
        generateReportButton.getStyleClass().add("primary-button");
        generateReportButton.setPrefWidth(200);
        generateReportButton.setDisable(true);
        generateReportButton.setOnAction(e -> generateReport());

        // Habilitar botón cuando se seleccione una materia
        subjectComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            generateReportButton.setDisable(newVal == null);
        });

        HBox buttonContainer = new HBox(generateReportButton);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        form.getChildren().addAll(infoLabel, subjectSelector, buttonContainer);

        return form;
    }

    private VBox createStatusSection() {
        VBox statusSection = new VBox(10);
        statusSection.setPadding(new Insets(20));
        statusSection.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label statusTitle = new Label("Información del Reporte");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        statusLabel = new Label("Selecciona una materia para ver los detalles del reporte.");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #6b7280;");

        VBox infoBox = new VBox(10);
        infoBox.getChildren().addAll(
                new Label("• El reporte incluirá todas las calificaciones de los estudiantes inscritos"),
                new Label("• Se mostrarán los tipos de evaluación configurados para la materia"),
                new Label("• Se calculará la nota final y estadísticas generales"),
                new Label("• El archivo PDF se guardará en la ubicación que elijas"));

        for (var child : infoBox.getChildren()) {
            if (child instanceof Label) {
                ((Label) child).setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            }
        }

        statusSection.getChildren().addAll(statusTitle, statusLabel, infoBox);

        return statusSection;
    }

    private void loadSubjects() {
        if (currentUser == null) {
            showError("No se pudo obtener el usuario actual");
            return;
        }

        try {
            var subjects = teacherSubjectController.getActiveAssignmentsByTeacher(currentUser.getId());
            subjectsList.setAll(subjects);

            if (subjects.isEmpty()) {
                statusLabel.setText("No tienes materias asignadas activas.");
                statusLabel.setStyle("-fx-text-fill: #dc2626;");
            }
        } catch (Exception e) {
            showError("Error al cargar materias: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void generateReport() {
        TeacherSubject selectedSubject = subjectComboBox.getValue();
        if (selectedSubject == null) {
            showError("Por favor selecciona una materia");
            return;
        }

        try {
            // Mostrar diálogo para elegir ubicación
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccionar ubicación para guardar el reporte");
            File selectedDirectory = directoryChooser.showDialog(this.getScene().getWindow());

            if (selectedDirectory == null) {
                return; // Usuario canceló
            }

            statusLabel.setText("Generando reporte...");
            statusLabel.setStyle("-fx-text-fill: #2563eb;");

            // Generar el reporte
            File reportFile = pdfReportService.generateSubjectGradesReport(selectedSubject, currentUser);

            // Mover el archivo a la ubicación seleccionada
            File destination = new File(selectedDirectory, reportFile.getName());
            if (reportFile.renameTo(destination)) {
                statusLabel.setText("✓ Reporte generado exitosamente: " + destination.getName());
                statusLabel.setStyle("-fx-text-fill: #10b981;");

                // Mostrar diálogo de éxito
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Reporte Generado");
                successAlert.setHeaderText(null);
                successAlert.setContentText("El reporte se ha generado exitosamente en:\n" + destination.getAbsolutePath());
                successAlert.showAndWait();
            } else {
                // Si no se pudo mover, al menos el archivo está en la ubicación actual
                statusLabel.setText("✓ Reporte generado: " + reportFile.getAbsolutePath());
                statusLabel.setStyle("-fx-text-fill: #10b981;");

                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Reporte Generado");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("El reporte se ha generado en:\n" + reportFile.getAbsolutePath());
                infoAlert.showAndWait();
            }

        } catch (Exception e) {
            showError("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error al generar el reporte. Por favor intenta nuevamente.");
            statusLabel.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Custom ListCell para mostrar materias en el ComboBox
     */
    private static class TeacherSubjectListCell extends ListCell<TeacherSubject> {
        @Override
        protected void updateItem(TeacherSubject item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                String text = item.getSubject().getName() + " (" + item.getSubject().getSubjectCode() + ") - "
                        + item.getAcademicYear() + " Sem " + item.getSemester();
                setText(text);
            }
        }
    }
}

