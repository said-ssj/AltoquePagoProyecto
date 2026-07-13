/*
 * Gestionamos la interfaz gráfica para el mantenimiento (CRUD) del catálogo de productos.
 * Hemos reconstruido el controlador desde cero aplicando el Principio de Inversión de Dependencias (DIP).
 * Se eliminaron todas las dependencias a java.sql y las consultas directas (actualizarProductoBD y eliminarProductoBD).
 * Toda la lógica de persistencia y eliminación en cascada ha sido delegada de manera abstracta a la interfaz IProductoDAO.
 */
package com.controlador;

import com.dao.IProductoDAO;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML private ComboBox<String> cbFiltrosProductos;
    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<Producto>               tablaProductos;
    @FXML private TableColumn<Producto, Integer>    colId;
    @FXML private TableColumn<Producto, String>     colCodigo; // Corregido para que coincida con el initialize
    @FXML private TableColumn<Producto, String>     colNombre;
    @FXML private TableColumn<Producto, String>     colCategoria;
    @FXML private TableColumn<Producto, Double>     colPrecio;
    @FXML private TableColumn<Producto, Integer>    colStock;
    @FXML private TableColumn<Producto, String>     colEstado;
    @FXML private TableColumn<Producto, Void>       colAcciones;

    // Elementos del formulario lateral (si los mantienes en tu vista)
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    // ── Abstracción de Datos (SOLID) ──
    private final IProductoDAO productoDAO;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private Producto productoSeleccionado;

    public ControladorProductos() {
        this.productoDAO = new ProductoDAO();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vinculación de columnas
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        if (colCodigo != null) colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo_barras"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        configurarColumnaAcciones();

        if (tablaProductos != null) {
            tablaProductos.setItems(listaProductos);
            tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    seleccionarProducto(newSelection);
                }
            });
        }

        if (txtBuscarProducto != null) {
            txtBuscarProducto.textProperty().addListener((obs, oldVal, newVal) -> filtrarProductos(newVal));
        }

        cargarDatosTabla();
    }

    private void seleccionarProducto(Producto p) {
        productoSeleccionado = p;
        if (txtCodigo != null) txtCodigo.setText(p.getCodigo_barras());
        if (txtNombre != null) txtNombre.setText(p.getNombre());
        if (txtPrecio != null) txtPrecio.setText(String.valueOf(p.getPrecio()));
        if (txtStock != null) txtStock.setText(String.valueOf(p.getStock()));
    }

    @FXML
    public void limpiarFormulario(ActionEvent event) {
        if (txtCodigo != null) txtCodigo.clear();
        if (txtNombre != null) txtNombre.clear();
        if (txtPrecio != null) txtPrecio.clear();
        if (txtStock != null) txtStock.clear();
        productoSeleccionado = null;
        if (tablaProductos != null) tablaProductos.getSelectionModel().clearSelection();
    }

    // ============================================================
    //  COLUMNA DE ACCIONES
    // ============================================================
    private void configurarColumnaAcciones() {
        if (colAcciones == null) return;
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏ Editar");
            private final Button btnEliminar = new Button("🗑 Eliminar");
            private final HBox   contenedor  = new HBox(8, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnEditar.getStyleClass().add("boton-editar");
                btnEliminar.getStyleClass().add("boton-eliminar");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    abrirDialogoEdicion(p);
                });
                btnEliminar.setOnAction(e -> {
                    Producto p = getTableView().getItems().get(getIndex());
                    confirmarYEliminar(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    // ============================================================
    //  DIÁLOGO DE EDICIÓN
    // ============================================================
    private void abrirDialogoEdicion(Producto prod) {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Editar Producto");
        dialog.setHeaderText("Modificar: " + prod.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField fNombre  = new TextField(prod.getNombre());
        TextField fPrecio  = new TextField(String.valueOf(prod.getPrecio()));
        TextField fStock   = new TextField(String.valueOf(prod.getStock()));
        TextField fCodigo  = new TextField(prod.getCodigo_barras() != null ? prod.getCodigo_barras() : "");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nombre:"),        fNombre);
        grid.addRow(1, new Label("Precio (S/):"),   fPrecio);
        grid.addRow(2, new Label("Stock:"),         fStock);
        grid.addRow(3, new Label("Código Barras:"), fCodigo);
        fNombre.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    prod.setNombre(fNombre.getText().trim());
                    prod.setPrecio(Double.parseDouble(fPrecio.getText().trim()));
                    prod.setStock(Integer.parseInt(fStock.getText().trim()));
                    prod.setCodigo_barras(fCodigo.getText().trim());
                    return prod;
                } catch (NumberFormatException ex) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Formato inválido", "Precio y stock deben ser números.");
                    return null;
                }
            }
            return null;
        });

        Optional<Producto> resultado = dialog.showAndWait();
        resultado.ifPresent(p -> {
            // Reemplazamos la conexión directa por la interfaz DAO
            if (p != null && productoDAO.actualizarProducto(p)) {
                cargarDatosTabla();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            } else if (p != null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el producto.");
            }
        });
    }

    // ============================================================
    //  CONFIRMAR Y ELIMINAR
    // ============================================================
    private void confirmarYEliminar(Producto prod) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar producto?");
        confirm.setContentText("Se eliminará: " + prod.getNombre() + "\nEsta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                // Reemplazamos el borrado en cascada crudo por la abstracción del DAO
                if (productoDAO.eliminarProducto(prod.getId_producto())) {
                    listaProductos.remove(prod);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Producto eliminado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.\nVerifica que no tenga ventas asociadas.");
                }
            }
        });
    }

    // ============================================================
    //  CARGA Y FILTROS
    // ============================================================
    private void cargarDatosTabla() {
        if (tablaProductos == null) return;
        // Obtenemos los datos a través del DAO
        listaProductos.setAll(productoDAO.obtenerTodos());
    }

    private void aplicarFiltro(String filtro) {
        if (filtro == null || listaProductos == null) return;
        ObservableList<Producto> sorted = FXCollections.observableArrayList(listaProductos);
        switch (filtro) {
            case "A - Z ⬆" -> sorted.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
            case "Z - A ⬇" -> sorted.sort((a, b) -> b.getNombre().compareToIgnoreCase(a.getNombre()));
            case "IDs"      -> sorted.sort((a, b) -> Integer.compare(a.getId_producto(), b.getId_producto()));
        }
        tablaProductos.setItems(sorted);
    }

    private void filtrarProductos(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaProductos.setItems(listaProductos);
            return;
        }

        String lower = texto.toLowerCase();
        ObservableList<Producto> filtrado = listaProductos.filtered(p ->
                p.getNombre().toLowerCase().contains(lower)
                        || String.valueOf(p.getId_producto()).contains(lower)
                        || (p.getCodigo_barras() != null && p.getCodigo_barras().toLowerCase().contains(lower))
        );

        tablaProductos.setItems(filtrado);
    }

    // ============================================================
    //  NAVEGACIÓN
    // ============================================================
    @FXML
    public void abrirNuevoProducto(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoproducto-view.fxml"));
            Parent vista = loader.load();
            Node boton = (Node) event.getSource();
            BorderPane panel = (BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}