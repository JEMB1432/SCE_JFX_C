package org.jemb.sce_jfx.views.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jemb.sce_jfx.controllers.GradeController;
import org.jemb.sce_jfx.models.Enrollment;
import org.jemb.sce_jfx.models.EvaluationType;
import org.jemb.sce_jfx.models.Grade;

import java.util.List;
import java.util.Optional;

/**
 * Diálogo para calificar a un estudiante en un tipo de evaluación específico
 */
public class GradeFormDialog extends Dialog<Grade> {

    private final Enrollment enrollment;
    private final List<EvaluationType> evaluationTypes;
    private final GradeController gradeController;

    private ComboBox<EvaluationType> evaluationTypeComboBox;
    private Spinner<Double> scoreSpinner;
    private TextArea commentsArea;
    private Label maxScoreLabel;

    public GradeFormDialog(Enrollment enrollment, List<EvaluationType> evaluationTypes) {
        this.enrollment = enrollment;
        this.evaluationTypes = evaluationTypes;
        this.gradeController = new GradeController();

        setTitle("Calificar Estudiante");
        setHeaderText("Estudiante: " + enrollment.getStudent().getFullName());

        // Estilos
        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm());

        // Botones
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Contenido
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Tamaño
        getDialogPane().setPrefSize(550, 400);

        // Validación
        setupValidation();
        setResultConverter(this::convertResult);
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Configurar columnas
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        col1.setPrefWidth(150);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setMinWidth(250);
        col2.setPrefWidth(300);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        // Tipo de Evaluación
        Label evalTypeLabel = new Label("Tipo de Evaluación:");
        evalTypeLabel.getStyleClass().add("form-label");
        evalTypeLabel.setMinWidth(Region.USE_PREF_SIZE);

        evaluationTypeComboBox = new ComboBox<>();
        evaluationTypeComboBox.getItems().setAll(evaluationTypes);
        evaluationTypeComboBox.setPromptText("Selecciona un tipo");
        evaluationTypeComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(evaluationTypeComboBox, Priority.ALWAYS);

        // Convertidor para mostrar nombres
        evaluationTypeComboBox.setConverter(new javafx.util.StringConverter<EvaluationType>() {
            @Override
            public String toString(EvaluationType type) {
                if (type == null)
                    return "";
                return type.getName() + String.format(" (%.0f%% - Máx: %.0f pts)",
                        type.getWeight(), type.getMaxScore());
            }

            @Override
            public EvaluationType fromString(String string) {
                return null;
            }
        });

        // Al seleccionar tipo de evaluación, cargar calificación existente
        evaluationTypeComboBox.setOnAction(e -> loadExistingGrade());

        // Puntuación
        Label scoreLabel = new Label("Puntuación:");
        scoreLabel.getStyleClass().add("form-label");
        scoreLabel.setMinWidth(Region.USE_PREF_SIZE);

        scoreSpinner = new Spinner<>(0.0, 100.0, 0.0, 1.0);
        scoreSpinner.setEditable(true);
        scoreSpinner.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(scoreSpinner, Priority.ALWAYS);

        maxScoreLabel = new Label("de 100.00 pts");
        maxScoreLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        // Comentarios
        Label commentsLabel = new Label("Comentarios:");
        commentsLabel.getStyleClass().add("form-label");
        commentsLabel.setMinWidth(Region.USE_PREF_SIZE);

        commentsArea = new TextArea();
        commentsArea.setPromptText("Comentarios opcionales sobre la calificación...");
        commentsArea.setPrefRowCount(4);
        commentsArea.setWrapText(true);
        GridPane.setHgrow(commentsArea, Priority.ALWAYS);

        // Agregar a grid
        int row = 0;
        grid.add(evalTypeLabel, 0, row);
        grid.add(evaluationTypeComboBox, 1, row);

        row++;
        grid.add(scoreLabel, 0, row);
        VBox scoreBox = new VBox(5, scoreSpinner, maxScoreLabel);
        grid.add(scoreBox, 1, row);

        row++;
        grid.add(commentsLabel, 0, row);
        grid.add(commentsArea, 1, row);

        container.getChildren().add(grid);
        return container;
    }

    private void loadExistingGrade() {
        EvaluationType selectedType = evaluationTypeComboBox.getValue();
        if (selectedType == null)
            return;

        // Actualizar el label de puntuación máxima
        maxScoreLabel.setText(String.format("de %.0f pts", selectedType.getMaxScore()));

        // Actualizar el rango del spinner
        SpinnerValueFactory.DoubleSpinnerValueFactory factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0,
                selectedType.getMaxScore(), 0.0, 1.0);
        scoreSpinner.setValueFactory(factory);

        // Buscar calificación existente
        try {
            Optional<Grade> existingGrade = gradeController.getGradeByEnrollmentAndEvaluationType(
                    enrollment.getId(),
                    selectedType.getId());

            if (existingGrade.isPresent()) {
                Grade grade = existingGrade.get();
                if (grade.getScore() != null) {
                    scoreSpinner.getValueFactory().setValue(grade.getScore());
                }
                if (grade.getComments() != null) {
                    commentsArea.setText(grade.getComments());
                }
            } else {
                // Limpiar campos si no hay calificación
                scoreSpinner.getValueFactory().setValue(0.0);
                commentsArea.setText("");
            }
        } catch (Exception e) {
            showError("Error al cargar calificación existente: " + e.getMessage());
        }
    }

    private void setupValidation() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        // Deshabilitar OK hasta que se seleccione tipo de evaluación
        okButton.setDisable(true);

        evaluationTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null);
        });
    }

    private Grade convertResult(ButtonType buttonType) {
        if (buttonType != ButtonType.OK) {
            return null;
        }

        EvaluationType selectedType = evaluationTypeComboBox.getValue();
        if (selectedType == null) {
            return null;
        }

        try {
            double score = scoreSpinner.getValue();
            String comments = commentsArea.getText().trim();
            comments = comments.isEmpty() ? null : comments;

            // Registrar o actualizar calificación
            Grade grade = gradeController.recordGrade(
                    enrollment.getId(),
                    selectedType.getId(),
                    score,
                    comments);

            showSuccess("Calificación registrada correctamente");
            return grade;

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return null;
        } catch (Exception e) {
            showError("Error al registrar calificación: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
