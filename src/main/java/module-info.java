module com.example.altoquepagoproyecto {
        requires javafx.controls;
        requires javafx.fxml;

        // El permiso MySQL
        requires java.sql;
        opens com.controlador to javafx.fxml;
        exports com.controlador;
        exports com.modelo;
        }