/*

 * Gestionamos la interfaz gráfica para el control del inventario general y las tarjetas de Kardex por artículo.
 * Hemos unificado la lógica de la vista aplicando el Principio de Inversión de Dependencias (DIP) mediante el uso
 * exclusivo de las abstracciones IProductoDAO e IMovimientoInventarioDAO. Además, delegamos toda la lógica de
 * consultas (filtrado) y actualizaciones de stock directamente a los DAOs, eliminando las sentencias SQL
 * quemadas en el controlador y corrigiendo los errores de variables obsoletas.
 */
package com.controlador;

import com.dao.IProductoDAO;
import com.dao.ProductoDAO;
import com.dao.IMovimientoInventarioDAO;
import com.dao.MovimientoInventarioDAO;
import com.modelo.Producto;
import com.modelo.MovimientoInventario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ControladorInventario implements Initializable {

    // ── Tabla superior: Productos ─────────────────────────────────
    @FXML private TableView<Producto>            tablaProductos;
    @FXML private TableColumn<Producto, Integer> colProdId;
    @FXML private TableColumn<Producto, String>  colProdCodigo;
    @FXML private TableColumn<Producto, String>  colProdNombre;
    @FXML private TableColumn<Producto, Double>  colProdPrecio;
    @FXML private TableColumn<Producto, Integer> colProdStock;
    @FXML private TextField                      txtBuscarProducto;

    // ── Tabla inferior: Kardex ────────────────────────────────────
    @FXML private Label                                   lblTituloKardex;
    @FXML private TableView<MovimientoInventario>         tablaKardex;
    @FXML private TableColumn<MovimientoInventario, String> colKarFecha;
    @FXML private TableColumn<MovimientoInventario, String> colKarTipo;
    @FXML private TableColumn<MovimientoInventario, Integer> colKarCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colKarDescripcion;

    // ── Formulario lateral ────────────────────────────────────────
    @FXML private TextField   txtProductoSeleccionado;
    @FXML private ComboBox<String> cbTipoMovimiento;
    @FXML private TextField   txtCantidad;
    @FXML private TextArea    txtDescripcionMovimiento;

    // Aquí centralizamos las abstracciones inyectadas para cumplir con SOLID
    private final IProductoDAO productoDAO;
    private final IMovimientoInventarioDAO movimientoInventarioDAO;

    // Conservamos las listas observables de JavaFX y las variables operativas internas de la clase
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<MovimientoInventario> listaKardex = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private int idProductoSeleccionado = -1;

    public ControladorInventario() {
        this.productoDAO = new ProductoDAO();
        this.movimientoInventarioDAO = new MovimientoInventarioDAO();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbTipoMovimiento.getItems().addAll("ENTRADA", "SALIDA", "AJUSTE", "MERMA");

        // ── Columnas Productos ────────────────────────────────────
        colProdId    .setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        colProdCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo_barras"));
        colProdNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProdPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colProdStock .setCellValueFactory(new PropertyValueFactory<>("stock"));
        tablaProductos.setItems(listaProductos);

        // ── Columnas Kardex ───────────────────────────────────────
        colKarFecha.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getFecha() != null ? c.getValue().getFecha().format(FMT) : "--"));
        colKarTipo       .setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        colKarCantidad   .setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colKarDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaKardex.setItems(listaKardex);

        // ── Al seleccionar producto → cargar kardex y formulario ──
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) seleccionarProducto(nuevo);
        });

        // ── Búsqueda en tiempo real ───────────────────────────────
        txtBuscarProducto.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null || nuevo.isBlank()) cargarInventario();
            else filtrarInventario(nuevo);
        });

        cargarInventario();
    }

    // ── Selección de fila → formulario + kardex ───────────────────
    private void seleccionarProducto(Producto p) {
        idProductoSeleccionado = p.getId_producto();
        txtProductoSeleccionado.setText(p.getNombre());
        lblTituloKardex.setText("Kardex: " + p.getNombre());
        cargarKardex(idProductoSeleccionado);
    }

    // ── Guardar movimiento en BD + actualizar stock ───────────────
    @FXML
    public void guardarMovimiento() {
        if (idProductoSeleccionado == -1) {
            alerta("Selecciona un producto de la tabla primero."); return;
        }
        String tipo  = cbTipoMovimiento.getValue();
        String cantStr = txtCantidad.getText().trim();
        String desc  = txtDescripcionMovimiento.getText().trim();

        if (tipo == null || cantStr.isEmpty() || desc.isEmpty()) {
            alerta("Completa todos los campos: tipo, cantidad y descripción."); return;
        }
        int cantidad;
        try { cantidad = Integer.parseInt(cantStr); }
        catch (NumberFormatException e) { alerta("La cantidad debe ser un número entero."); return; }
        if (cantidad <= 0) { alerta("La cantidad debe ser mayor a 0."); return; }

        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdProducto(idProductoSeleccionado);
        mov.setTipoMovimiento(tipo);
        mov.setCantidad(cantidad);
        mov.setDescripcion(desc);

        // Usamos la abstracción inyectada en lugar del 'movDAO'
        if (!movimientoInventarioDAO.registrarMovimiento(mov)) {
            alerta("No se pudo registrar el movimiento."); return;
        }

        // Actualizar stock delegando en el DAO en lugar de usar SQL raw
        actualizarStock(tipo, cantidad, idProductoSeleccionado);

        info("Movimiento registrado correctamente.");
        cargarInventario();
        cargarKardex(idProductoSeleccionado);
        limpiarCamposMovimiento();
    }

    private void actualizarStock(String tipo, int cantidad, int idProducto) {
        Producto p = productoDAO.buscarPorId(idProducto);
        if (p == null) return;

        // Delegamos la manipulación de base de datos a IProductoDAO.
        // Como 'actualizarStock' del ProductoDAO resta existencias por defecto:
        switch (tipo) {
            case "ENTRADA":
                // Restar un negativo equivale a sumar
                productoDAO.actualizarStock(idProducto, -cantidad);
                break;
            case "SALIDA":
            case "MERMA":
                productoDAO.actualizarStock(idProducto, cantidad);
                break;
            case "AJUSTE":
                // Calculamos la diferencia matemática para llegar al stock deseado
                int diferencia = p.getStock() - cantidad;
                productoDAO.actualizarStock(idProducto, diferencia);
                break;
        }
    }

    // ── Búsqueda por nombre o código ─────────────────────────────
    @FXML
    public void buscarProducto() {
        filtrarInventario(txtBuscarProducto.getText().trim());
    }

    private void filtrarInventario(String texto) {
        if (texto == null || texto.isBlank()) {
            cargarInventario();
            return;
        }
        // Usamos la abstracción inyectada, eliminando el SQL raw del controlador
        listaProductos.setAll(productoDAO.buscarPorNombre(texto));
    }

    // ── Limpiar formulario lateral ────────────────────────────────
    @FXML
    public void limpiarFormulario() {
        tablaProductos.getSelectionModel().clearSelection();
        txtProductoSeleccionado.clear();
        idProductoSeleccionado = -1;
        lblTituloKardex.setText("Kardex: Seleccione un producto");
        listaKardex.clear();
        limpiarCamposMovimiento();
    }

    private void limpiarCamposMovimiento() {
        cbTipoMovimiento.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtDescripcionMovimiento.clear();
    }

    private void cargarInventario() {
        listaProductos.setAll(productoDAO.obtenerTodos());
    }

    private void cargarKardex(int idProducto) {
        // Usamos la abstracción inyectada en lugar del 'movDAO'
        listaKardex.setAll(movimientoInventarioDAO.listarPorProducto(idProducto));
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}