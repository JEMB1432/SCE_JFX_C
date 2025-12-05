package org.jemb.sce_jfx.views.dialogs;

import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DialogUtils {
    public static void setDialogIcon(Dialog<?> dialog) {
        dialog.setOnShown(event -> {
            Platform.runLater(() -> {
                Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage) {
                    Stage stage = (Stage) window;
                    try {
                        stage.getIcons().add(
                                new Image(DialogUtils.class.getResourceAsStream("/org/jemb/sce_jfx/icons/logoDark.png"))
                        );
                        stage.setResizable(true);
                    } catch (Exception e) {
                        System.err.println("Error al cargar icono del diálogo: " + e.getMessage());
                    }
                }
            });
        });
    }
}
