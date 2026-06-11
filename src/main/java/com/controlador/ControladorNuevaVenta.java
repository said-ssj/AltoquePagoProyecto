package com.controlador;

import com.dao.*;
import com.modelo.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ControladorNuevaVenta {

    @FXML private Button btnMostrarDatosCliente;
    @FXML private Button btnCerrarDatosCliente;
    @FXML private VBox panelDatosCliente;

    // Nuevos elementos inyectados del FXML
    @FXML private TextField txtFechaVenta;
    @FXML private ToggleButton btnModoBusqueda;
    @FXML private TextField txtBuscarProducto;
    @FXML private Label lblTotal;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtSubtotal;
    @FXML private Button btnAgregarProducto;
    @FXML private TableView<ItemVenta> tablaDetalleVenta;
    @FXML private TableColumn<ItemVenta,String> colProducto;
    @FXML private TableColumn<ItemVenta,Integer> colCantidad;
    @FXML private TableColumn<ItemVenta,Double> colPrecio;
    @FXML private TableColumn<ItemVenta,Double> colSubtotal;
    @FXML private Button btnEliminarProducto;
    @FXML private Label lblStock;
    @FXML private ComboBox<String> cbMetodoPago;
    @FXML private ComboBox<String> cbEstado;
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private ComboBox<String> cbMoneda;
    @FXML private ComboBox<String> cbDescuentos;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRucDni;
    @FXML private TextField txtCorreoCliente;
    @FXML private TextField txtTelefonoCliente;
    @FXML private TextArea txtObservacion;

    private ObservableList<ItemVenta> listaItems = FXCollections.observableArrayList();
    private double totalVenta = 0.0;
    private List<String> productosVenta = new ArrayList<>();
    private List<DetalleVenta> detallesVenta = new ArrayList<>();

    @FXML
    public void initialize() {

        // Ocultar panel de cliente al inicio (Asegurarnos de que arranque cerrado)
        panelDatosCliente.setVisible(false);
        panelDatosCliente.setManaged(false);

        // Lógica del botón Desplegable del Cliente
        btnMostrarDatosCliente.setOnAction(e -> {
            panelDatosCliente.setVisible(true);
            panelDatosCliente.setManaged(true);
            btnMostrarDatosCliente.setVisible(false);
            btnMostrarDatosCliente.setManaged(false);
        });

        btnCerrarDatosCliente.setOnAction(e -> {
            panelDatosCliente.setVisible(false);
            panelDatosCliente.setManaged(false);
            btnMostrarDatosCliente.setVisible(true);
            btnMostrarDatosCliente.setManaged(true);
        });

        // Poner la fecha actual automáticamente
        txtFechaVenta.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Lógica interactiva del Buscador vs Código de Barras
        configurarModoCodigoBarras(); // Arranca por defecto en modo código de barras

        btnModoBusqueda.setOnAction(e -> {
            if (btnModoBusqueda.isSelected()) {
                // Cambió a Modo Búsqueda por Texto
                configurarModoBusqueda();
            } else {
                // Regresó a Modo Código de Barras
                configurarModoCodigoBarras();
            }
        });

        // Restricción para que solo acepte números cuando está en modo escáner
        txtBuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnModoBusqueda.isSelected()) { // Si no está presionado (Modo Escáner)
                if (!newValue.matches("\\d*")) { // Si detecta una letra o símbolo
                    txtBuscarProducto.setText(newValue.replaceAll("[^\\d]", "")); // Lo borra automáticamente
                }
            }
        });

        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotal()
        );

        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotal()
        );

        btnAgregarProducto.setOnAction(e -> agregarProducto());

        txtBuscarProducto.setOnAction(e -> buscarProducto());

        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));

        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        tablaDetalleVenta.setItems(listaItems);

        tablaDetalleVenta.getSelectionModel().getSelectedItem();

        btnEliminarProducto.setOnAction(e -> eliminarProducto());

        cbMetodoPago.getItems().addAll("YAPE",
                "PLIN",
                "TARJETA"
        );
        cbMetodoPago.getSelectionModel().selectFirst();

        cbEstado.getItems().addAll(
                "PAGADO",
                "PENDIENTE",
                "RECHAZADO"
        );
        cbEstado.getSelectionModel().selectFirst();

        cbTipoDocumento.getItems().addAll(
                "BOLETA",
                "FACTURA"
        );
        cbTipoDocumento.getSelectionModel().selectFirst();

        cbMoneda.getItems().addAll(
                "SOLES",
                "DÓLARES"
        );
        cbMoneda.getSelectionModel().selectFirst();

        cbDescuentos.getItems().addAll(
                "SIN DESCUENTO",
                "5%",
                "10%",
                "15%"
        );
        cbDescuentos.getSelectionModel().selectFirst();
    }

    // Funciones de ayuda para cambiar el aspecto visual según el modo
    private void configurarModoCodigoBarras() {
        btnModoBusqueda.setText("🔍 Modo Búsqueda");
        txtBuscarProducto.setPromptText("||||| Escanear código de barras...");

        Platform.runLater(() -> txtBuscarProducto.requestFocus());
    }

    private void configurarModoBusqueda() {
        btnModoBusqueda.setText("||||| Modo Código de Barras");
        txtBuscarProducto.setPromptText("🔍 Escribe el nombre del producto...");
    }

    private void buscarProducto() {
        String texto = txtBuscarProducto.getText();
        ProductoDAO dao = new ProductoDAO();
        if(btnModoBusqueda.isSelected()){
            productoSeleccionado = dao.buscarPorNombre(texto);
        }else{
            productoSeleccionado = dao.buscarPorCodigo(texto);
        }
        if(productoSeleccionado != null){
            txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio())
            );
            txtCantidad.setText("1");
            lblStock.setText("" + productoSeleccionado.getStock());
            System.out.println("Producto encontrado: " + productoSeleccionado.getNombre()
            );
        }else{
            System.out.println("Producto no encontrado");
        }
    }

    private void calcularSubtotal() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double precio = Double.parseDouble(txtPrecio.getText());
            double subtotal = cantidad * precio;
            txtSubtotal.setText(String.format(java.util.Locale.US, "%.2f", subtotal)
            );
        } catch (Exception e) {
            txtSubtotal.setText("0.00");
        }
    }

    private void agregarProducto() {
            System.out.println("BOTON AGREGAR PRESIONADO");
            try {
                String producto = productoSeleccionado.getNombre();
                String cantidad = txtCantidad.getText();
                String precio = txtPrecio.getText();
                double subtotal = Double.parseDouble(txtSubtotal.getText());
                int cantidadSolicitada = Integer.parseInt(txtCantidad.getText());
                if(cantidadSolicitada > productoSeleccionado.getStock()){
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Stock insuficiente");
                        alert.setHeaderText(null);
                        alert.setContentText("No hay suficiente stock disponible.");
                        alert.showAndWait();
                        return;
                    }
                totalVenta += subtotal;
                lblTotal.setText("S/ " + String.format(java.util.Locale.US, "%.2f", totalVenta));
                productosVenta.add(producto + " | Cantidad: " + cantidad + " | Precio: " + precio);
                detallesVenta.add(new DetalleVenta(productoSeleccionado.getId_producto(),
                                Integer.parseInt(cantidad),
                                Double.parseDouble(precio),
                                subtotal
                        )
                );
                listaItems.add(new ItemVenta(producto,
                                Integer.parseInt(cantidad),
                                Double.parseDouble(precio),
                                subtotal
                        )
                );
                System.out.println("Detalles guardados: " + detallesVenta.size());
                System.out.println(productosVenta);
                System.out.println("Total acumulado = " + totalVenta);
                txtBuscarProducto.clear();
                txtCantidad.clear();
                txtPrecio.clear();
                txtSubtotal.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @FXML
    public void abrirVentas(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ventas-view.fxml"));
            javafx.scene.Parent vistaVentas = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vistaVentas);

        } catch (IOException e) {
            System.err.println("Error al volver a la vista de ventas");
            e.printStackTrace();
        }
    }

    @FXML
    private void guardarVenta() {
        System.out.println("Botón Guardar Venta presionado");
        if (detallesVenta.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Venta vacía");
            alert.setHeaderText(null);
            alert.setContentText("Debe agregar al menos un producto.");
            alert.showAndWait();
            return;
        }
        try {
            ClienteDAO clienteDAO = new ClienteDAO();
            Cliente cliente = new Cliente(
                    txtRazonSocial.getText(),
                    txtTelefonoCliente.getText(),
                    txtCorreoCliente.getText()
            );
            int idCliente = clienteDAO.guardarCliente(cliente);

            double total = totalVenta;
            VentaDAO ventaDAO = new VentaDAO();
            int idVenta = ventaDAO.guardarVenta(idCliente, total);

            DetalleVentaDAO detalleDAO = new DetalleVentaDAO();
            ProductoDAO productoDAO = new ProductoDAO();

            for (DetalleVenta d : detallesVenta) {
                detalleDAO.guardarDetalle(
                        idVenta,
                        d.getIdProducto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                );

                productoDAO.actualizarStock(
                        d.getIdProducto(),
                        d.getCantidad()
                );
            }

            PagoDAO pagoDAO = new PagoDAO();
            pagoDAO.guardarPago(
                    idVenta,
                    cbMetodoPago.getValue(),
                    total,
                    cbEstado.getValue()
            );

            ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
            comprobanteDAO.guardarComprobante(
                    idVenta,
                    cbTipoDocumento.getValue()
            );


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Venta registrada");
            alert.setHeaderText(null);
            alert.setContentText("La venta fue registrada correctamente.");
            alert.showAndWait();

            limpiarFormulario();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Producto productoSeleccionado;

    private void eliminarProducto() {
        ItemVenta item = tablaDetalleVenta.getSelectionModel().getSelectedItem();
        if(item == null){
            return;
        }
        totalVenta -= item.getSubtotal();
        lblTotal.setText("S/ " + String.format("%.2f", totalVenta)
        );
        listaItems.remove(item);
    }

    private void limpiarFormulario() {
        totalVenta = 0.0;
        lblTotal.setText("S/ 0.00");
        detallesVenta.clear();
        listaItems.clear();
        productosVenta.clear();
        txtBuscarProducto.clear();
        txtCantidad.clear();
        txtPrecio.clear();
        txtSubtotal.clear();
        txtRazonSocial.clear();
        txtRucDni.clear();
        txtCorreoCliente.clear();
        txtTelefonoCliente.clear();
        txtObservacion.clear();
        productoSeleccionado = null;
    }
}
