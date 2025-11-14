module org.jemb.sce_jfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires jbcrypt;

    opens org.jemb.sce_jfx to javafx.fxml;
    exports org.jemb.sce_jfx;
    exports org.jemb.sce_jfx.views;
    exports org.jemb.sce_jfx.services;
    exports org.jemb.sce_jfx.models;
}