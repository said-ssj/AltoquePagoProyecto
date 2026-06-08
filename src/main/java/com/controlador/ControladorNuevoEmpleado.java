package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ControladorNuevoEmpleado {

    @FXML private Button btnVolver;
    @FXML private Button btnCancelar;
    @FXML private Button btnGuardarEmpleado;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtIdEmpleado;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtFechaNacimiento;
    @FXML private TextField txtPuesto;
    @FXML private TextField txtFechaIngreso;
    @FXML private TextField txtSalario;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextArea txtNotas;

    @FXML private ComboBox<String> cbDepartamento;
    @FXML private ComboBox<String> cbTipoContrato;
    @FXML private ComboBox<String> cbEstado;

    @FXML
    public void initialize() {
        // Llenar datos del ComboBox de Departamento
        cbDepartamento.getItems().addAll(
                "Ventas",
                "Finanzas",
                "IT",
                "Recursos Humanos",
                "Operaciones",
                "Marketing"
        );

        // Llenar datos del ComboBox de Tipo de Contrato
        cbTipoContrato.getItems().addAll(
                "Tiempo Completo",
                "Medio Tiempo",
                "Por Contrato",
                "Temporal"
        );

        // Llenar datos del ComboBox de Estado
        cbEstado.getItems().addAll(
                "Activo",
                "Inactivo",
                "En Licencia"
        );
    }

    @FXML
    public void abrirEmpleados(javafx.event.ActionEvent event) {
        try {
            // Regresamos a la vista de la tabla de empleados
            FXMLLoader loader = new FXMLLoader(getClass().getResource("empleados-view.fxml"));
            javafx.scene.Parent vistaEmpleados = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaEmpleados);
        } catch (IOException e) {
            System.err.println("Error al volver a la vista de empleados");
            e.printStackTrace();
        }
    }
}