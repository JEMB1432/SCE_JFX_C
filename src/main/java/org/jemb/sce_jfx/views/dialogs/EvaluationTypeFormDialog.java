package org.jemb.sce_jfx.views.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jemb.sce_jfx.controllers.EvaluationTypeController;
import org.jemb.sce_jfx.models.EvaluationType;
import org.jemb.sce_jfx.models.TeacherSubject;
import org.jemb.sce_jfx.utils.UserSession;

/**
 * Formulario para agregar o editar un tipo de evaluación
 */
public class EvaluationTypeFormDialog extends Dialog<EvaluationType> {

    private final TeacherSubject teacherSubject;
    private final EvaluationType evaluationType;
    private final EvaluationTypeController controller;

    private TextField nameField;
    private Spinner<Double> weightSpinner;
    private Spinner<Double> maxScoreSpinner;
    private Spinner<Integer> orderSpinner;
    private CheckBox isFinalCheckBox;

    public EvaluationTypeFormDialog(TeacherSubject teacherSubject, EvaluationType evaluationType) {
        this.teacherSubject = teacherSubject;
        this.evaluationType = evaluationType;
        this.controller = new EvaluationTypeController();

        boolean isEdit = (evaluationType != null);
        setTitle(isEdit ? "Editar Tipo de Evaluación" : "Nuevo Tipo de Evaluación");
        setHeaderText("Materia: " + teacherSubject.getSubject().getName());

        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm());

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = createContent();
        DialogUtils.setDialogIcon(this);
        getDialogPane().setContent(content);

        getDialogPane().setPrefSize(500, 400);

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
        col2.setMinWidth(200);
        col2.setPrefWidth(250);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        // Nombre
        Label nameLabel = new Label("Nombre:");
        nameLabel.getStyleClass().add("form-label");
        nameLabel.setMinWidth(Region.USE_PREF_SIZE);

        nameField = new TextField();
        nameField.setPromptText("Ej: Examen Parcial 1");
        nameField.getStyleClass().add("form-field");
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        if (evaluationType != null) {
            nameField.setText(evaluationType.getName());
        }

        // Peso
        Label weightLabel = new Label("Peso (%):");
        weightLabel.getStyleClass().add("form-label");
        weightLabel.setMinWidth(Region.USE_PREF_SIZE);

        weightSpinner = new Spinner<>(0.0, 100.0, 0.0, 5.0);
        weightSpinner.setEditable(true);
        weightSpinner.getStyleClass().add("spinner");
        weightSpinner.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(weightSpinner, Priority.ALWAYS);
        if (evaluationType != null) {
            weightSpinner.getValueFactory().setValue(evaluationType.getWeight());
        }

        // Puntaje Máximo
        Label maxScoreLabel = new Label("Puntaje Máximo:");
        maxScoreLabel.getStyleClass().add("form-label");
        maxScoreLabel.setMinWidth(Region.USE_PREF_SIZE);

        maxScoreSpinner = new Spinner<>(1.0, 1000.0, 100.0, 10.0);
        maxScoreSpinner.setEditable(true);
        maxScoreSpinner.getStyleClass().add("spinner");
        maxScoreSpinner.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(maxScoreSpinner, Priority.ALWAYS);
        if (evaluationType != null) {
            maxScoreSpinner.getValueFactory().setValue(evaluationType.getMaxScore());
        }

        // Orden
        Label orderLabel = new Label("Orden:");
        orderLabel.getStyleClass().add("form-label");
        orderLabel.setMinWidth(Region.USE_PREF_SIZE);

        orderSpinner = new Spinner<>(1, 100, 1, 1);
        orderSpinner.setEditable(true);
        orderSpinner.getStyleClass().add("spinner");
        orderSpinner.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(orderSpinner, Priority.ALWAYS);
        if (evaluationType != null && evaluationType.getEvaluationOrder() != null) {
            orderSpinner.getValueFactory().setValue(evaluationType.getEvaluationOrder());
        }

        // Es Examen Final
        isFinalCheckBox = new CheckBox("Es examen final");
        isFinalCheckBox.setStyle("-fx-font-size: 14px;");
        if (evaluationType != null) {
            isFinalCheckBox.setSelected(evaluationType.isFinalExam());
        }

        // Agregar a grid
        int row = 0;
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row);

        row++;
        grid.add(weightLabel, 0, row);
        grid.add(weightSpinner, 1, row);

        row++;
        grid.add(maxScoreLabel, 0, row);
        grid.add(maxScoreSpinner, 1, row);

        row++;
        grid.add(orderLabel, 0, row);
        grid.add(orderSpinner, 1, row);

        row++;
        grid.add(isFinalCheckBox, 1, row);

        container.getChildren().add(grid);
        return container;
    }

    private void setupValidation() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        // Validar nombre no vacío
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(newVal == null || newVal.trim().isEmpty());
        });

        // Inicializar validación
        okButton.setDisable(nameField.getText().trim().isEmpty());
    }

    private EvaluationType convertResult(ButtonType buttonType) {
        if (buttonType != ButtonType.OK) {
            return null;
        }

        try {
            String name = nameField.getText().trim();
            double weight = weightSpinner.getValue();
            double maxScore = maxScoreSpinner.getValue();
            int order = orderSpinner.getValue();
            boolean isFinal = isFinalCheckBox.isSelected();

            if (evaluationType == null) {
                // Crear nuevo
                String teacherId = UserSession.getInstance().getCurrentUser().getId();

                EvaluationType saved = controller.createEvaluationType(
                        teacherSubject.getSubjectId(),
                        name,
                        weight,
                        teacherId);

                // Actualizar campos adicionales
                saved.setMaxScore(maxScore);
                saved.setEvaluationOrder(order);
                saved.setFinalExam(isFinal);
                controller.updateEvaluationType(saved);

                showSuccess("Tipo de evaluación creado correctamente");
                return saved;

            } else {
                // Editar existente
                evaluationType.setName(name);
                evaluationType.setWeight(weight);
                evaluationType.setMaxScore(maxScore);
                evaluationType.setEvaluationOrder(order);
                evaluationType.setFinalExam(isFinal);

                EvaluationType updated = controller.updateEvaluationType(evaluationType);
                showSuccess("Tipo de evaluación actualizado correctamente");
                return updated;
            }

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return null;
        } catch (Exception e) {
            showError("Error al guardar: " + e.getMessage());
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
