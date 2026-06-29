package com.controlador;

import com.DB.ConexionDB;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML private ComboBox<String> cbFiltrosProductos;
    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<Producto>               tablaProductos;
    @FXML private TableColumn<Producto, Integer>    colId;
    @FXML private TableColumn<Producto, String>     colNombre;
    @FXML private TableColumn<Producto, String>     colCategoria;
    @FXML private TableColumn<Producto, Double>     colPrecio;
    @FXML private TableColumn<Producto, Integer>    colStock;
    @FXML private TableColumn<Producto, String>     colEstado;
    @FXML private TableColumn<Producto, Void>       colAcciones;

    private ObservableList<Producto> listaProductos;
    private final ProductoDAO productoDAO = new ProductoDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbFiltrosProductos.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Categoria", "IDs");
        cbFiltrosProductos.setOnAction(e -> aplicarFiltro(cbFiltrosProductos.getValue()));

        if (colId       != null) colId      .setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        if (colNombre   != null) colNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colPrecio   != null) colPrecio  .setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colStock    != null) colStock   .setCellValueFactory(new PropertyValueFactory<>("stock"));
        if (colCategoria!= null) colCategoria.setCellValueFactory(c -> new SimpleStringProperty("General"));
        if (colEstado   != null) colEstado  .setCellValueFactory(c -> {
            int stock = c.getValue().getStock();
            return new SimpleStringProperty(stock == 0 ? "Sin Stock" : stock < 5 ? "Bajo Stock" : "Activo");
        });

        configurarColumnaAcciones();
        cargarDatosTabla();
        txtBuscarProducto.textProperty().addListener((obs, old, nuevo) -> filtrarProductos(nuevo));
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
                btnEditar  .getStyleClass().add("boton-editar");
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

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
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
            if (p != null && actualizarProductoBD(p)) {
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
                if (eliminarProductoBD(prod.getId_producto())) {
                    listaProductos.remove(prod);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Producto eliminado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.\nVerifica que no tenga ventas asociadas.");
                }
            }
        });
    }

    // ============================================================
    //  BASE DE DATOS
    // ============================================================
    private boolean actualizarProductoBD(Producto p) {
        String sql = "UPDATE producto SET nombre=?, precio=?, stock=?, codigo_barras=? WHERE id_producto=?";
        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt   (3, p.getStock());
            ps.setString(4, p.getCodigo_barras());
            ps.setInt   (5, p.getId_producto());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean eliminarProductoBD(int id) {
        // Eliminar referencias en detalle_venta y detalle_carrito primero
        String[] sqls = {
                "DELETE FROM oferta WHERE id_producto=?",
                "DELETE FROM movimiento_inventario WHERE id_producto=?",
                "DELETE FROM detalle_carrito WHERE id_producto=?",
                "DELETE FROM detalle_venta WHERE id_producto=?",
                "DELETE FROM producto WHERE id_producto=?"
        };
        try (Connection cn = ConexionDB.conectar()) {
            cn.setAutoCommit(false);
            for (String sql : sqls) {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }
            cn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  CARGA Y FILTROS
    // ============================================================
    private void cargarDatosTabla() {
        if (tablaProductos == null) return;
        List<Producto> lista = productoDAO.obtenerTodos();
        listaProductos = FXCollections.observableArrayList(lista);
        tablaProductos.setItems(listaProductos);
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

                        || (p.getCodigo_barras() != null &&
                        p.getCodigo_barras().toLowerCase().contains(lower))
        );

        tablaProductos.setItems(filtrado);
    }

    @FXML
    public void abrirNuevoProducto(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoproducto-view.fxml"));
            javafx.scene.Parent vista = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panel = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}