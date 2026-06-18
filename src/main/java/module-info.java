module com.example.altoquepagoproyecto {
    requires javafx.controls;
    requires javafx.fxml;
    requires mysql.connector.j;

    // El permiso MySQL
    requires java.sql;
    requires org.slf4j;
    requires java.net.http;
    requires com.google.gson;
    opens com.controlador to javafx.fxml;
    exports com.controlador;
    exports com.modelo;
}