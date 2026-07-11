package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import com.dao.ProductoDAO;
import com.modelo.Producto;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import java.util.List;

// Si ya tienes tus modelos, impórtalos aquí:
// import com.modelo.Oferta;
// import com.modelo.Producto;

public class ControladorOfertas implements Initializable {

    // --- Elementos de la Tabla ---
    @FXML private TableView<?> tablaOfertas; // Reemplazar <?> por <Oferta>
    @FXML private TableColumn<?, Integer> colId;
    @FXML private TableColumn<?, String> colProducto;
    @FXML private TableColumn<?, String> colDescripcion;
    @FXML private TableColumn<?, Double> colDescuento;
    @FXML private TableColumn<?, String> colInicio;
    @FXML private TableColumn<?, String> colFin;
    @FXML private TableColumn<?, String> colEstado;

    @FXML private TextField txtBuscar;

    // --- Elementos del Formulario Lateral ---
    @FXML private TextField txtBuscarProductoOfertas; // Lo ideal es ComboBox<Producto>
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtDescuento;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private CheckBox chkEstado;

    private ContextMenu popupBusqueda;
    private Producto productoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Inicializar el menú flotante
        popupBusqueda = new ContextMenu();
        popupBusqueda.setPrefWidth(280); // Mismo ancho que tu TextField

        // 2. Listener que detecta cuando escribes en el campo de texto
        txtBuscarProductoOfertas.textProperty().addListener((observable, oldValue, newValue) -> {
            // Si borran el texto, escondemos el menú y limpiamos la selección
            if (newValue == null || newValue.trim().isEmpty()) {
                popupBusqueda.hide();
                productoSeleccionado = null;
                return;
            }

            // Evitar que busque de nuevo justo después de que el usuario hace clic en una sugerencia
            if (productoSeleccionado != null && newValue.equals(productoSeleccionado.getNombre())) {
                return;
            }

            // Si escribió 2 o más letras, vamos a la Base de Datos
            if (newValue.trim().length() >= 2) {
                ProductoDAO dao = new ProductoDAO();
                List<Producto> resultados = dao.buscarPorNombre(newValue.trim());

                if (!resultados.isEmpty()) {
                    mostrarResultadosVisuales(resultados);
                    if (!popupBusqueda.isShowing()) {
                        popupBusqueda.show(txtBuscarProductoOfertas, javafx.geometry.Side.BOTTOM, 0, 0);
                    }
                } else {
                    popupBusqueda.hide();
                }
            } else {
                popupBusqueda.hide();
            }
        });

        cargarDatosTabla();

        // 4. Listener para cuando se selecciona una fila en la tabla para editar
        tablaOfertas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetalleOferta(newSelection);
            }
        });


    }

    @FXML
    public void guardarOferta() {
        String descripcion = txtDescripcion.getText();
        String descuentoStr = txtDescuento.getText();
        LocalDate fechaInicio = dpInicio.getValue();
        LocalDate fechaFin = dpFin.getValue();
        boolean estado = chkEstado.isSelected();

        // Validación básica
        if (descripcion.isEmpty() || descuentoStr.isEmpty() || productoSeleccionado == null) {
            System.out.println("Debe seleccionar un producto válido de la lista y llenar todos los campos.");
            return;
        }

        try {
            double descuento = Double.parseDouble(descuentoStr);

            System.out.println("Oferta guardada correctamente: " + descripcion);
            limpiarFormulario();
            cargarDatosTabla(); // Refrescar la tabla


        } catch (NumberFormatException e) {
            System.out.println("El descuento debe ser un valor numérico válido.");
        }

        productoSeleccionado = null;
    }

    @FXML
    public void limpiarFormulario() {
        txtBuscarProductoOfertas.clear();
        txtDescripcion.clear();
        txtDescuento.clear();
        dpInicio.setValue(null);
        dpFin.setValue(null);
        chkEstado.setSelected(true);
        tablaOfertas.getSelectionModel().clearSelection();
    }

    @FXML
    public void buscarOferta() {
        String texto = txtBuscar.getText();
        System.out.println("Buscando oferta por: " + texto);
    }

    private void cargarDatosTabla() {
    }

    private void mostrarDetalleOferta(Object ofertaSeleccionada) {
    }

    private void mostrarResultadosVisuales(List<Producto> resultados) {
        popupBusqueda.getItems().clear();

        for (Producto prod : resultados) {
            HBox hbox = new HBox();
            hbox.setSpacing(10);

            // Nombre del producto
            Label lblNombre = new Label(prod.getNombre());
            lblNombre.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

            // Espaciador para tirar el stock a la derecha
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Indicador de Stock
            Label lblStockItem = new Label("Stock: " + prod.getStock());
            lblStockItem.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

            hbox.getChildren().addAll(lblNombre, spacer, lblStockItem);

            CustomMenuItem item = new CustomMenuItem(hbox);
            item.setHideOnClick(true);

            // Cuando el usuario haga clic en esta opción:
            item.setOnAction(event -> seleccionarProductoParaOferta(prod));

            popupBusqueda.getItems().add(item);
        }
    }

    private void seleccionarProductoParaOferta(Producto prod) {
        this.productoSeleccionado = prod; // Guardamos el objeto completo (con su ID)
        txtBuscarProductoOfertas.setText(prod.getNombre()); // Ponemos el nombre en el TextField
        popupBusqueda.hide(); // Escondemos la lista
    }
}