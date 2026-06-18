package com.controlador;

import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorEmpleados implements Initializable {

    @FXML private Button btnNuevoEmpleado;
    @FXML private TextField txtBuscarEmpleado;
    @FXML private Button btnFiltros;
    @FXML private ComboBox<String> cbFiltrosEmpleados;

    // --- VARIABLES DE LA TABLA ---
    @FXML private TableView<UsuarioPersonal> tablaEmpleados;
    @FXML private TableColumn<UsuarioPersonal, String> colNombre;
    @FXML private TableColumn<UsuarioPersonal, String> colEmail;
    @FXML private TableColumn<UsuarioPersonal, String> colRol;

    private ObservableList<UsuarioPersonal> listaEmpleados;
    private UsuarioPersonalDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        usuarioDAO = new UsuarioPersonalDAO();

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosEmpleados.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Administrador - Rol", "Empleado - Rol");

        // CONFIGURAR COLUMNAS
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Transformamos el ID del rol (1, 2, 3) a texto visual
        if (colRol != null) {
            colRol.setCellValueFactory(cellData -> {
                int rol = cellData.getValue().getIdRol();
                String nombreRol = rol == 1 ? "Administrador" : (rol == 2 ? "Vendedor" : "Almacén");
                return new SimpleStringProperty(nombreRol);
            });
        }

        cargarDatosTabla();
    }

    private void cargarDatosTabla() {
        if (tablaEmpleados != null) {
            List<UsuarioPersonal> usuariosBD = usuarioDAO.obtenerTodos();
            listaEmpleados = FXCollections.observableArrayList(usuariosBD);
            tablaEmpleados.setItems(listaEmpleados);
        }
    }

    @FXML
    public void abrirNuevoEmpleado(javafx.event.ActionEvent event) {
        try {
            // RUTA CORREGIDA AQUÍ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoempleado-view.fxml"));
            javafx.scene.Parent vistaNuevoEmpleado = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaNuevoEmpleado);
        } catch (java.io.IOException e) {
            System.err.println("Error al cargar la vista de nuevo empleado");
            e.printStackTrace();
        }
    }
}