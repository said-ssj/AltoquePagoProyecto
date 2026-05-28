package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorConsultas implements Initializable {

    // CONSULTAR VENTAS
    @FXML private DatePicker dateInicioVentas;
    @FXML private DatePicker dateFinVentas;
    @FXML private Button btnConsultarVentas;

    // CONSULTAR PRODUCTOS
    @FXML private ComboBox<String> cbCategoriaProducto;
    @FXML private Spinner<Integer> spinnerStockMinimo;
    @FXML private Button btnConsultarProductos;

    // CONSULTAR EMPLEADOS
    @FXML private ComboBox<String> cbDepartamento;
    @FXML private ComboBox<String> cbEstadoEmpleado;
    @FXML private Button btnConsultarEmpleados;

    // CONSULTA GENERAL
    @FXML private ComboBox<String> cbTipoConsulta;
    @FXML private TextField txtParametroConsulta;
    @FXML private Button btnConsultarGeneral;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarCategorias();
        cargarDepartamentos();
        cargarEstados();
        cargarTiposConsulta();
        configurarSpinner();
    }

    // ============================================================
    //                  INICIALIZACIÓN DE COMBOS
    // ============================================================

    private void cargarCategorias() {
        cbCategoriaProducto.getItems().addAll(
                "Todas las categorías",
                "Computadoras",
                "Accesorios",
                "Monitores"
        );
        cbCategoriaProducto.setValue("Todas las categorías");
    }

    private void cargarDepartamentos() {
        cbDepartamento.getItems().addAll(
                "Todos los departamentos",
                "Ventas",
                "Finanzas",
                "IT"
        );
        cbDepartamento.setValue("Todos los departamentos");
    }

    private void cargarEstados() {
        cbEstadoEmpleado.getItems().addAll(
                "Todos",
                "Activo",
                "Inactivo"
        );
        cbEstadoEmpleado.setValue("Todos");
    }

    private void cargarTiposConsulta() {
        cbTipoConsulta.getItems().addAll(
                "Ventas por cliente",
                "Ventas por producto",
                "Ventas por empleado"
        );
        cbTipoConsulta.setValue("Ventas por cliente");
    }

    private void configurarSpinner() {
        spinnerStockMinimo.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0)
        );
    }

    // ============================================================
    // ACCIONES DE LOS BOTONES
    // ============================================================

    @FXML
    public void consultarVentas() {
        String fechaInicio = dateInicioVentas.getValue() != null
                ? dateInicioVentas.getValue().toString() : "";
        String fechaFin = dateFinVentas.getValue() != null
                ? dateFinVentas.getValue().toString() : "";

        System.out.println("Consultando ventas desde: " + fechaInicio + " hasta: " + fechaFin);

        // Conectar con la lógica de negocio / base de datos

    }

    @FXML
    public void consultarProductos() {
        String categoria = cbCategoriaProducto.getValue();
        int stockMinimo = spinnerStockMinimo.getValue();

        System.out.println("Consultando productos - Categoría: " + categoria
                + " | Stock mínimo: " + stockMinimo);

        // Conectar con la lógica de negocio / base de datos

    }

    @FXML
    public void consultarEmpleados() {
        String departamento = cbDepartamento.getValue();
        String estado = cbEstadoEmpleado.getValue();

        System.out.println("Consultando empleados - Departamento: " + departamento
                + " | Estado: " + estado);

        // Conectar con la lógica de negocio / base de datos

    }

    @FXML
    public void consultarGeneral() {
        String tipo = cbTipoConsulta.getValue();
        String parametro = txtParametroConsulta.getText().trim();

        System.out.println("Consulta general - Tipo: " + tipo
                + " | Parámetro: " + parametro);

        // Conectar con la lógica de negocio / base de datos

    }

    @FXML
    public void abrirFiltros() {
        System.out.println("Abrir panel de filtros");

        // Conectar con la lógica de negocio / base de datos

    }
}

