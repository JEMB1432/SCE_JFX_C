package org.jemb.sce_jfx.views.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.jemb.sce_jfx.controllers.EvaluationTypeController;
import org.jemb.sce_jfx.models.EvaluationType;
import org.jemb.sce_jfx.models.TeacherSubject;

import java.util.Optional;

/**
 * Diálogo para gestionar tipos de evaluación de una materia
 * Incluye validación de que los pesos sumen 100%
 */
public class EvaluationTypesDialog extends Dialog<ButtonType> {

    private final TeacherSubject teacherSubject;
    private final EvaluationTypeController evaluationTypeController;

    private TableView<EvaluationType> typesTable;
    private ObservableList<EvaluationType> typesList;
    private ProgressBar weightProgressBar;
    private Label weightLabel;

    public EvaluationTypesDialog(TeacherSubject teacherSubject) {
        this.teacherSubject = teacherSubject;
        this.evaluationTypeController = new EvaluationTypeController();
        this.typesList = FXCollections.observableArrayList();

        setTitle("Tipos de Evaluación");
        setHeaderText("Gestionar evaluaciones de: " + teacherSubject.getSubject().getName());

        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/org/jemb/sce_jfx/styles/common/base.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/tables.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/buttons.css").toExternalForm(),
                getClass().getResource("/org/jemb/sce_jfx/styles/common/forms.css").toExternalForm());

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = createContent();
        DialogUtils.setDialogIcon(this);
        getDialogPane().setContent(content);

        getDialogPane().setPrefSize(800, 600);

        loadEvaluationTypes();
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Progress bar de peso total
        VBox weightSection = createWeightSection();

        // Botón agregar
        Button addButton = new Button("➕ Agregar Tipo de Evaluación");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> showAddDialog());

        // Tabla
        typesTable = createTable();

        VBox.setVgrow(typesTable, Priority.ALWAYS);
        container.getChildren().addAll(weightSection, addButton, typesTable);

        return container;
    }

    private VBox createWeightSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10));
        section.setStyle(
                "-fx-background-color: #f9fafb; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");

        Label title = new Label("Peso Total de Evaluaciones");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        weightProgressBar = new ProgressBar(0);
        weightProgressBar.setPrefWidth(Double.MAX_VALUE);
        weightProgressBar.setPrefHeight(25);

        weightLabel = new Label("0.00% de 100%");
        weightLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        section.getChildren().addAll(title, weightProgressBar, weightLabel);
        return section;
    }

    private TableView<EvaluationType> createTable() {
        TableView<EvaluationType> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No hay tipos de evaluación configurados"));

        // Columna: Nombre
        TableColumn<EvaluationType, String> nameCol = new TableColumn<>("NOMBRE");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        // Columna: Peso
        TableColumn<EvaluationType, Double> weightCol = new TableColumn<>("PESO (%)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double weight, boolean empty) {
                super.updateItem(weight, empty);
                if (empty || weight == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", weight));
                }
            }
        });
        weightCol.setPrefWidth(100);

        // Columna: Puntaje Máximo
        TableColumn<EvaluationType, Double> maxScoreCol = new TableColumn<>("PUNTAJE MÁX.");
        maxScoreCol.setCellValueFactory(new PropertyValueFactory<>("maxScore"));
        maxScoreCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f", score));
                }
            }
        });
        maxScoreCol.setPrefWidth(120);

        // Columna: Orden
        TableColumn<EvaluationType, Integer> orderCol = new TableColumn<>("ORDEN");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("evaluationOrder"));
        orderCol.setPrefWidth(80);

        // Columna: Es Final
        TableColumn<EvaluationType, Boolean> finalCol = new TableColumn<>("EXAMEN FINAL");
        finalCol.setCellValueFactory(new PropertyValueFactory<>("finalExam"));
        finalCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean isFinal, boolean empty) {
                super.updateItem(isFinal, empty);
                if (empty || isFinal == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(isFinal ? "Sí" : "No");
                    badge.getStyleClass().addAll("badge", isFinal ? "status-active" : "status-inactive");
                    setGraphic(badge);
                }
            }
        });
        finalCol.setPrefWidth(120);

        // Columna: Acciones
        TableColumn<EvaluationType, Void> actionsCol = new TableColumn<>("ACCIONES");
        actionsCol.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Editar");
            private final Button deleteBtn = new Button("Eliminar");

            {
                editBtn.getStyleClass().add("edit-button");
                deleteBtn.getStyleClass().add("delete-button");

                editBtn.setOnAction(e -> {
                    EvaluationType type = getTableView().getItems().get(getIndex());
                    showEditDialog(type);
                });

                deleteBtn.setOnAction(e -> {
                    EvaluationType type = getTableView().getItems().get(getIndex());
                    confirmDelete(type);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, editBtn, deleteBtn);
                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(180);

        table.getColumns().addAll(nameCol, weightCol, maxScoreCol, orderCol, finalCol, actionsCol);
        table.setItems(typesList);

        return table;
    }

    private void loadEvaluationTypes() {
        try {
            var types = evaluationTypeController.getEvaluationTypesBySubject(teacherSubject.getSubjectId());
            typesList.setAll(types);
            updateWeightDisplay();
        } catch (Exception e) {
            showError("Error al cargar tipos de evaluación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateWeightDisplay() {
        try {
            double totalWeight = evaluationTypeController.getTotalWeightBySubject(teacherSubject.getSubjectId());
            weightProgressBar.setProgress(totalWeight / 100.0);
            weightLabel.setText(String.format("%.2f%% de 100%%", totalWeight));

            // Cambiar color según el peso
            if (totalWeight < 100) {
                weightProgressBar.setStyle("-fx-accent: #f59e0b;"); // Amarillo/naranja
            } else if (totalWeight == 100) {
                weightProgressBar.setStyle("-fx-accent: #10b981;"); // Verde
            } else {
                weightProgressBar.setStyle("-fx-accent: #dc2626;"); // Rojo
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddDialog() {
        EvaluationTypeFormDialog dialog = new EvaluationTypeFormDialog(teacherSubject, null);
        Optional<EvaluationType> result = dialog.showAndWait();

        if (result.isPresent()) {
            loadEvaluationTypes();
        }
    }

    private void showEditDialog(EvaluationType evaluationType) {
        EvaluationTypeFormDialog dialog = new EvaluationTypeFormDialog(teacherSubject, evaluationType);
        Optional<EvaluationType> result = dialog.showAndWait();

        if (result.isPresent()) {
            loadEvaluationTypes();
        }
    }

    private void confirmDelete(EvaluationType evaluationType) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar tipo de evaluación?");
        alert.setContentText("Se eliminará: " + evaluationType.getName() + "\n¿Desea continuar?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                evaluationTypeController.deleteEvaluationType(evaluationType.getId());
                showSuccess("Tipo de evaluación eliminado correctamente");
                loadEvaluationTypes();
            } catch (Exception e) {
                showError("Error al eliminar: " + e.getMessage());
                e.printStackTrace();
            }
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
