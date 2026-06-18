package com.controlador;

import com.dao.ProductoDAO;
import com.modelo.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML private ComboBox<String> cbFiltrosProductos;

    // --- VARIABLES DE LA TABLA (Coinciden exactamente con tu FXML) ---
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;

    private ObservableList<Producto> listaProductos;
    private ProductoDAO productoDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productoDAO = new ProductoDAO();

        // CONFIGURACIÓN DEL COMBOBOX
        cbFiltrosProductos.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Categoria -  ", "IDs");
        cbFiltrosProductos.setOnAction(e -> {
            String seleccion = cbFiltrosProductos.getValue();
            System.out.println("Filtrando por: " + seleccion);
        });

        // --- CONEXIÓN DE COLUMNAS A LA CLASE Producto.java ---

        // Estas columnas extraen el dato directamente de los getters de tu clase Producto
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colPrecio != null) colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Como Categoría y Estado no están en tu BD actual, les ponemos un valor visual temporal
        if (colCategoria != null) colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty("General"));
        if (colEstado != null) colEstado.setCellValueFactory(cellData -> new SimpleStringProperty("Activo"));

        // CARGAR DATOS DESDE MYSQL
        cargarDatosTabla();
    }

    @FXML private TableView<Producto> tablaProductos;

    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, String> colEstado;


    @FXML
    public void abrirNuevoProducto(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoproducto-view.fxml"));
            javafx.scene.Parent vistaNuevoProducto = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaNuevoProducto);
        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la vista de nuevo producto");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosProductos.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Categoria", "IDs");
        colId.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        cargarProductos();
    }

    private void cargarProductos() {

        ProductoDAO dao = new ProductoDAO();

        ObservableList<Producto> lista =
                FXCollections.observableArrayList(dao.listarProductos());

        tablaProductos.setItems(lista);
    }

}
