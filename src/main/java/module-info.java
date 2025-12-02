module org.jemb.sce_jfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires jbcrypt;

    // iText7 modules - AGREGAR ESTOS
    requires kernel;
    requires layout;
    requires io; // ← FALTABA ESTE
    requires commons; // ← Y ESTE TAMBIÉN

    opens org.jemb.sce_jfx to javafx.fxml;
    exports org.jemb.sce_jfx;
    exports org.jemb.sce_jfx.views;
    exports org.jemb.sce_jfx.services;
    exports org.jemb.sce_jfx.models;
    exports org.jemb.sce_jfx.utils;
}