package com.controlador;

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
import java.util.ResourceBundle;

public class ControladorProductos implements Initializable {

    @FXML
    private ComboBox<String> cbFiltrosProductos;

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevoproducto-view.fxml"));
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
