module com.example.altoquepagoproyecto {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires mysql.connector.j;
    requires java.sql;
    requires org.slf4j;
    requires java.net.http;
    requires com.google.gson;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires com.github.librepdf.openpdf;

    opens com.controlador to javafx.fxml;
    opens com.modelo to javafx.base;
    exports com.controlador;
    exports com.modelo;
}
