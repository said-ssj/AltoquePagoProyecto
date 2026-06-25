package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorInventario implements Initializable {

    // --- Elementos de la Tabla Superior (Productos) ---
    @FXML private TableView<?> tablaProductos; // Reemplazar <?> por <Producto>
    @FXML private TableColumn<?, Integer> colProdId;
    @FXML private TableColumn<?, String> colProdCodigo;
    @FXML private TableColumn<?, String> colProdNombre;
    @FXML private TableColumn<?, Double> colProdPrecio;
    @FXML private TableColumn<?, Integer> colProdStock;
    @FXML private TextField txtBuscarProducto;

    // --- Elementos de la Tabla Inferior (Kardex) ---
    @FXML private Label lblTituloKardex;
    @FXML private TableView<?> tablaKardex; // Reemplazar <?> por <MovimientoInventario>
    @FXML private TableColumn<?, String> colKarFecha;
    @FXML private TableColumn<?, String> colKarTipo;
    @FXML private TableColumn<?, Integer> colKarCantidad;
    @FXML private TableColumn<?, String> colKarDescripcion;

    // --- Elementos del Formulario Lateral (Nuevo Movimiento) ---
    @FXML private TextField txtProductoSeleccionado;
    @FXML private ComboBox<String> cbTipoMovimiento;
    @FXML private TextField txtCantidad;
    @FXML private TextArea txtDescripcionMovimiento;

    // Variable para almacenar temporalmente el ID del producto seleccionado
    private int idProductoSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar ComboBox de Tipos de Movimiento
        cbTipoMovimiento.getItems().addAll("ENTRADA", "SALIDA", "AJUSTE", "MERMA");

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                seleccionarProducto(newSelection);
            }
        });
        cargarInventario();
    }

    private void seleccionarProducto(Object productoObj) {
        // Simulación temporal:
        txtProductoSeleccionado.setText("Producto Seleccionado");
        lblTituloKardex.setText("Kardex del Producto Seleccionado");
        // Llamar a la base de datos para cargar los movimientos de este ID
        cargarKardex(idProductoSeleccionado);
    }

    @FXML
    public void guardarMovimiento() {
        if (idProductoSeleccionado == -1) {
            System.out.println("Error: Debe seleccionar un producto de la tabla primero.");
            return;
        }

        String tipo = cbTipoMovimiento.getValue();
        String cantidadStr = txtCantidad.getText();
        String descripcion = txtDescripcionMovimiento.getText();

        if (tipo == null || cantidadStr.isEmpty() || descripcion.isEmpty()) {
            System.out.println("Error: Complete todos los campos.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                System.out.println("Error: La cantidad debe ser mayor a 0.");
                return;
            }

            System.out.println("Movimiento registrado con éxito.");

            // Refrescar ambas tablas para ver los cambios reflejados
            cargarInventario();
            cargarKardex(idProductoSeleccionado);
            limpiarCamposMovimiento();

        } catch (NumberFormatException e) {
            System.out.println("Error: La cantidad debe ser un número entero válido.");
        }
    }

    @FXML
    public void buscarProducto() {
        String texto = txtBuscarProducto.getText();
        System.out.println("Buscando inventario por: " + texto);
        // Filtrar la tablaProductos usando tu DAO
    }

    @FXML
    public void limpiarFormulario() {
        tablaProductos.getSelectionModel().clearSelection();
        txtProductoSeleccionado.clear();
        idProductoSeleccionado = -1;
        lblTituloKardex.setText("Kardex: Seleccione un producto");
        tablaKardex.getItems().clear(); // Vaciar tabla inferior
        limpiarCamposMovimiento();
    }

    private void limpiarCamposMovimiento() {
        cbTipoMovimiento.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtDescripcionMovimiento.clear();
    }

    private void cargarInventario() {
        // Llenar la tablaProductos con productoDAO.listarTodos()
    }

    private void cargarKardex(int idProducto) {
        // Llenar la tablaKardex con movimientoDAO.listarPorProducto(idProducto)
    }
}