/*

En este controlador gestionamos la vista de reportes y consultas dinámicas del sistema.

Hemos aplicado el Principio de Inversión de Dependencias (SOLID / DIP) extrayendo

absolutamente todas las sentencias SQL y la lógica de ResultSet hacia una nueva

abstracción (IConsultaDAO). Ahora nuestra interfaz recibe estructuras de datos

limpias (List), manteniendo el diseño robusto, escalable y 100% libre de acoplamiento.
*/
package com.controlador;

import com.dao.IConsultaDAO;
import com.dao.ConsultaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ControladorConsultas implements Initializable {

    // CONSULTAR VENTAS
    @FXML private DatePicker dateInicioVentas;
    @FXML private DatePicker dateFinVentas;
    @FXML private Button btnConsultarVentas;

    // CONSULTAR PRODUCTOS
    @FXML private ComboBox<String> cbTipoProducto;
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

    // PANEL DE RESULTADOS
    @FXML private VBox panelResultados;
    @FXML private Label lblTituloResultado;
    @FXML private Label lblMensajeVacio;
    @FXML private TableView<Map<String, Object>> tablaResultados;

    // Abstracción de datos (SOLID)
    private final IConsultaDAO consultaDAO;

    public ControladorConsultas() {
        this.consultaDAO = new ConsultaDAO();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarCategorias();
        cargarDepartamentos();
        cargarEstados();
        cargarTiposConsulta();
        configurarSpinner();

        // Ocultar panel de resultados al inicio
        panelResultados.setVisible(false);
        panelResultados.setManaged(false);

        // Conectar botones con sus acciones
        btnConsultarVentas.setOnAction(e -> consultarVentas());
        btnConsultarProductos.setOnAction(e -> consultarProductos());
        btnConsultarEmpleados.setOnAction(e -> consultarEmpleados());
        btnConsultarGeneral.setOnAction(e -> consultarGeneral());
    }

// ============================================================
//  INICIALIZACIÓN DE COMBOS
// ============================================================

    private void cargarCategorias() {
        cbTipoProducto.getItems().addAll(
                "Todas las categorías",
                "Computadoras",
                "Accesorios",
                "Monitores"
        );
        cbTipoProducto.setValue("Todas las categorías");
    }

    private void cargarDepartamentos() {
        cbDepartamento.getItems().add("Todos los departamentos");

        List<String> areas = consultaDAO.obtenerDepartamentos();
        if (areas.isEmpty()) {
            cbDepartamento.getItems().addAll("Ventas", "Finanzas", "IT");
        } else {
            cbDepartamento.getItems().addAll(areas);
        }

        cbDepartamento.setValue("Todos los departamentos");
    }

    private void cargarEstados() {
        cbEstadoEmpleado.getItems().addAll("Todos", "Activo", "Inactivo");
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
//  HELPER: MOSTRAR TABLA CON RESULTADOS GENÉRICOS
// ============================================================

    /**
     * Construye dinámicamente las columnas y filas de la TableView basándose
     * en una lista de mapas (donde cada mapa es una fila de la BD).
     */
    private void mostrarResultados(String titulo, List<Map<String, Object>> datos) {
        tablaResultados.getColumns().clear();
        tablaResultados.getItems().clear();

        if (datos != null && !datos.isEmpty()) {
            Map<String, Object> primerRegistro = datos.get(0);

            for (String nombreCol : primerRegistro.keySet()) {
                TableColumn<Map<String, Object>, Object> col = new TableColumn<>(nombreCol.replace("_", " ").toUpperCase());

                col.setCellValueFactory(cd -> {
                    Map<String, Object> fila = cd.getValue();
                    Object valor = (fila != null) ? fila.get(nombreCol) : null;
                    return new javafx.beans.property.SimpleObjectProperty<>(valor);
                });

                col.setMinWidth(100);
                col.setPrefWidth(120);
                tablaResultados.getColumns().add(col);
            }

            ObservableList<Map<String, Object>> filas = FXCollections.observableArrayList(datos);
            tablaResultados.setItems(filas);
            lblTituloResultado.setText(titulo + "  (" + filas.size() + " registros)");
        } else {
            lblTituloResultado.setText(titulo + "  (0 registros)");
        }

        boolean sinDatos = (datos == null || datos.isEmpty());
        lblMensajeVacio.setVisible(sinDatos);
        lblMensajeVacio.setManaged(sinDatos);
        tablaResultados.setVisible(!sinDatos);
        tablaResultados.setManaged(!sinDatos);

        panelResultados.setVisible(true);
        panelResultados.setManaged(true);
    }

// ============================================================
//  CONSULTAS DE NEGOCIO
// ============================================================

    @FXML
    public void consultarVentas() {
        String fechaInicio = dateInicioVentas.getValue() != null ? dateInicioVentas.getValue().toString() : null;
        String fechaFin = dateFinVentas.getValue() != null ? dateFinVentas.getValue().toString() : null;

        List<Map<String, Object>> resultados = consultaDAO.consultarVentas(fechaInicio, fechaFin);
        mostrarResultados("Ventas", resultados);
    }

    @FXML
    public void consultarProductos() {
        int stockMinimo = spinnerStockMinimo.getValue();
        String tipo = cbTipoProducto.getValue();

        List<Map<String, Object>> resultados = consultaDAO.consultarProductos(stockMinimo, tipo);
        mostrarResultados("Productos (stock ≥ " + stockMinimo + ")", resultados);
    }

    @FXML
    public void consultarEmpleados() {
        String departamento = cbDepartamento.getValue();
        String estado = cbEstadoEmpleado.getValue();

        List<Map<String, Object>> resultados = consultaDAO.consultarEmpleados(departamento, estado);
        mostrarResultados("Empleados", resultados);
    }

    @FXML
    public void consultarGeneral() {
        String parametro = txtParametroConsulta.getText().trim();
        String tipo = cbTipoConsulta.getValue();

        if (tipo == null) {
            mostrarAlerta("Tipo no reconocido", "Seleccione un tipo de consulta válido.");
            return;
        }

        List<Map<String, Object>> resultados = consultaDAO.consultarGeneral(tipo, parametro);
        mostrarResultados(tipo, resultados);
    }

// ============================================================
//  UTILIDADES
// ============================================================

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    }