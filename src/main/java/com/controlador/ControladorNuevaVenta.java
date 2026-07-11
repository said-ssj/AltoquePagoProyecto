package com.controlador;

import com.dao.*;
import com.google.gson.JsonObject;
import com.modelo.*;
import com.servicio.ApiSunatServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import com.dao.ConfiguracionDAO;
import com.modelo.Configuracion;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.awt.Desktop;

public class ControladorNuevaVenta {

    @FXML private Button btnMostrarDatosCliente;
    @FXML private Button btnCerrarDatosCliente;
    @FXML private VBox panelDatosCliente;

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
    @FXML private Button btnGuardarCliente;
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
    @FXML private TextField txtDireccion;

    private String ubigeoActual = "";
    private ContextMenu popupBusqueda;
    private ObservableList<ItemVenta> listaItems = FXCollections.observableArrayList();
    private double totalVenta = 0.0;
    private List<String> productosVenta = new ArrayList<>();
    private List<DetalleVenta> detallesVenta = new ArrayList<>();
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {

        txtPrecio.setEditable(false);
        txtSubtotal.setEditable(false);

        panelDatosCliente.setVisible(false);
        panelDatosCliente.setManaged(false);

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

        txtFechaVenta.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        configurarModoCodigoBarras();

        btnModoBusqueda.setOnAction(e -> {
            if (btnModoBusqueda.isSelected()) {
                configurarModoBusqueda();
            } else {
                configurarModoCodigoBarras();
            }
        });

        txtBuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnModoBusqueda.isSelected()) {
                if (!newValue.matches("\\d*")) {
                    txtBuscarProducto.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotal());
        txtPrecio.textProperty().addListener((obs, oldVal, newVal) -> calcularSubtotal());
        btnAgregarProducto.setOnAction(e -> agregarProducto());
        txtBuscarProducto.setOnAction(e -> buscarProducto());

        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        tablaDetalleVenta.setItems(listaItems);

        btnEliminarProducto.setOnAction(e -> eliminarProducto());

        cbMetodoPago.getItems().addAll("YAPE", "PLIN", "TARJETA");
        cbMetodoPago.getSelectionModel().selectFirst();

        cbEstado.getItems().addAll("PAGADO", "PENDIENTE", "RECHAZADO");
        cbEstado.getSelectionModel().selectFirst();

        cbTipoDocumento.getItems().addAll("BOLETA", "FACTURA");
        cbTipoDocumento.getSelectionModel().selectFirst();

        cbMoneda.getItems().addAll("SOLES", "DÓLARES");
        cbMoneda.getSelectionModel().selectFirst();

        cbDescuentos.getItems().addAll("SIN DESCUENTO", "5%", "10%", "15%");
        cbDescuentos.getSelectionModel().selectFirst();

        popupBusqueda = new ContextMenu();
        popupBusqueda.setPrefWidth(300);

        txtBuscarProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!btnModoBusqueda.isSelected()) {
                if (!newValue.matches("\\d*")) {
                    txtBuscarProducto.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (popupBusqueda.isShowing()) popupBusqueda.hide();

            } else {
                if (newValue.trim().length() >= 2) {
                    ProductoDAO dao = new ProductoDAO();
                    List<Producto> resultados = dao.buscarPorNombre(newValue.trim());

                    if (!resultados.isEmpty()) {
                        mostrarResultados(resultados);
                        if (!popupBusqueda.isShowing()) {
                            popupBusqueda.show(txtBuscarProducto, javafx.geometry.Side.BOTTOM, 0, 0);
                        }
                    } else {
                        popupBusqueda.hide();
                    }
                } else {
                    popupBusqueda.hide();
                }
            }
        });

        txtRucDni.setOnAction(e -> buscarDatosCliente());
        btnGuardarCliente.setOnAction(e -> guardarDatosClienteTemporal());

        javafx.beans.value.ChangeListener<String> detectorDeDatos = (obs, oldV, newV) -> {
            boolean hayDatos = !txtRucDni.getText().trim().isEmpty() ||
                    !txtRazonSocial.getText().trim().isEmpty() ||
                    (txtDireccion != null && !txtDireccion.getText().trim().isEmpty());

            if (hayDatos) {
                if (!btnGuardarCliente.getStyleClass().contains("btn-cliente-activo")) {
                    btnGuardarCliente.getStyleClass().add("btn-cliente-activo");
                }
            } else {
                btnGuardarCliente.getStyleClass().remove("btn-cliente-activo");
            }
        };

        txtRucDni.textProperty().addListener(detectorDeDatos);
        txtRazonSocial.textProperty().addListener(detectorDeDatos);
        if (txtDireccion != null) txtDireccion.textProperty().addListener(detectorDeDatos);
    }

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
        if (texto == null || texto.trim().isEmpty()) return;

        ProductoDAO dao = new ProductoDAO();

        if (btnModoBusqueda.isSelected()) {
            List<Producto> resultados = dao.buscarPorNombre(texto.trim());
            if (resultados != null && !resultados.isEmpty()) {
                productoSeleccionado = resultados.get(0);
            } else {
                productoSeleccionado = null;
            }
        } else {
            productoSeleccionado = dao.buscarPorCodigo(texto.trim());
        }

        if (productoSeleccionado != null) {
            txtBuscarProducto.setText(productoSeleccionado.getNombre());
            txtPrecio.setText(String.valueOf(productoSeleccionado.getPrecio()));
            txtCantidad.setText("1");
            lblStock.setText(String.valueOf(productoSeleccionado.getStock()));

            double subtotal = productoSeleccionado.getPrecio() * 1;
            txtSubtotal.setText(String.format(java.util.Locale.US, "%.2f", subtotal));

            System.out.println("Producto encontrado: " + productoSeleccionado.getNombre());

            if (popupBusqueda != null && popupBusqueda.isShowing()) {
                popupBusqueda.hide();
            }
        } else {
            System.out.println("Producto no encontrado");
            lblStock.setText("0");
            txtPrecio.clear();
            txtSubtotal.clear();
        }
    }

    private void mostrarResultados(List<Producto> resultados) {
        popupBusqueda.getItems().clear();

        for (Producto prod : resultados) {
            HBox hbox = new HBox();
            hbox.setSpacing(10);

            Label lblNombre = new Label(prod.getNombre() + " - S/ " + prod.getPrecio());
            lblNombre.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblStockItem = new Label("Stock: " + prod.getStock());
            lblStockItem.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

            hbox.getChildren().addAll(lblNombre, spacer, lblStockItem);

            CustomMenuItem item = new CustomMenuItem(hbox);
            item.setHideOnClick(true);

            item.setOnAction(event -> seleccionarProductoParaVenta(prod));

            popupBusqueda.getItems().add(item);
        }
    }

    private void seleccionarProductoParaVenta(Producto prod) {
        this.productoSeleccionado = prod;

        txtBuscarProducto.setText(prod.getNombre());
        txtPrecio.setText(String.valueOf(prod.getPrecio()));
        txtCantidad.setText("1");

        double subtotal = prod.getPrecio() * 1;
        txtSubtotal.setText(String.format(java.util.Locale.US, "%.2f", subtotal));
        lblStock.setText(String.valueOf(prod.getStock()));

        popupBusqueda.hide();
    }

    private void calcularSubtotal() {
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText());
            double precio = Double.parseDouble(txtPrecio.getText());
            double subtotal = cantidad * precio;
            txtSubtotal.setText(String.format(java.util.Locale.US, "%.2f", subtotal));
        } catch (Exception e) {
            txtSubtotal.setText("0.00");
        }
    }

    private void agregarProducto() {
        System.out.println("BOTON AGREGAR PRESIONADO");

        if (productoSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Producto no seleccionado");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, escanea o busca un producto válido antes de agregarlo a la lista.");
            alert.showAndWait();
            return;
        }

        try {
            String producto = productoSeleccionado.getNombre();
            String cantidad = txtCantidad.getText();
            String precio = txtPrecio.getText();
            double subtotal = Double.parseDouble(txtSubtotal.getText());
            int cantidadSolicitada = Integer.parseInt(txtCantidad.getText());

            int cantidadYaEnCarrito = 0;
            for (DetalleVenta d : detallesVenta) {
                if (d.getIdProducto() == productoSeleccionado.getId_producto()) {
                    cantidadYaEnCarrito += d.getCantidad();
                }
            }

            if ((cantidadSolicitada + cantidadYaEnCarrito) > productoSeleccionado.getStock()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Stock insuficiente");
                alert.setHeaderText(null);
                alert.setContentText("No puedes agregar esta cantidad. Ya tienes " + cantidadYaEnCarrito +
                        " en el carrito y el stock total es " + productoSeleccionado.getStock() + ".");
                alert.showAndWait();
                return;
            }

            if (cantidadSolicitada <= 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cantidad inválida");
                alert.setHeaderText(null);
                alert.setContentText("La cantidad a vender debe ser al menos 1.");
                alert.showAndWait();
                return;
            }

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

            // Utilizamos el constructor actualizado de 5 parámetros de DetalleVenta
            detallesVenta.add(new DetalleVenta(
                    productoSeleccionado.getId_producto(),
                    productoSeleccionado.getNombre(), // Se pasa el nombre para la impresión
                    Integer.parseInt(cantidad),
                    Double.parseDouble(precio),
                    subtotal
            ));

            listaItems.add(new ItemVenta(
                    producto,
                    Integer.parseInt(cantidad),
                    Double.parseDouble(precio),
                    subtotal
            ));

            System.out.println("Detalles guardados: " + detallesVenta.size());
            System.out.println("Total acumulado = " + totalVenta);

            txtBuscarProducto.clear();
            txtCantidad.clear();
            txtPrecio.clear();
            txtSubtotal.clear();

            productoSeleccionado = null;
            lblStock.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirVentas(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/ventas-view.fxml"));
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

            String documentoIngresado = txtRucDni.getText() != null ? txtRucDni.getText().trim() : "";
            String tipoDoc;
            String dni = null;
            String ruc = null;
            String nombreAsumido;

            if (documentoIngresado.isEmpty()) {
                tipoDoc = "0";
                dni = "00000000";
                nombreAsumido = "CLIENTES VARIOS";
                cbTipoDocumento.setValue("BOLETA");
            } else {
                tipoDoc = (documentoIngresado.length() == 11) ? "6" : "1";
                dni = (tipoDoc.equals("1")) ? documentoIngresado : null;
                ruc = (tipoDoc.equals("6")) ? documentoIngresado : null;
                nombreAsumido = txtRazonSocial.getText() != null ? txtRazonSocial.getText() : "";
            }

            String correo = txtCorreoCliente.getText() != null ? txtCorreoCliente.getText() : "";
            String telefono = txtTelefonoCliente.getText() != null ? txtTelefonoCliente.getText() : "";
            String observacion = txtObservacion.getText() != null ? txtObservacion.getText() : "";
            String direccion = txtDireccion.getText() != null ? txtDireccion.getText() : "";

            Cliente cliente = new Cliente(
                    nombreAsumido, "", nombreAsumido, correo, dni, ruc,
                    telefono, direccion, ubigeoActual, tipoDoc, observacion
            );

            int idCliente = clienteDAO.guardarCliente(cliente);

            if (idCliente == -1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error en Base de Datos");
                alert.setHeaderText(null);
                alert.setContentText("No se pudo registrar el cliente. La venta ha sido cancelada.");
                alert.showAndWait();
                return;
            }

            double total = totalVenta;
            String descuentoSeleccionado = cbDescuentos.getValue();

            if (descuentoSeleccionado != null && !descuentoSeleccionado.equals("SIN DESCUENTO")) {
                try {
                    String porcentajeStr = descuentoSeleccionado.replace("%", "").trim();
                    double porcentaje = Double.parseDouble(porcentajeStr);
                    double montoDescuento = total * (porcentaje / 100.0);
                    total = total - montoDescuento;
                } catch (Exception ex) {
                    System.err.println("Error al aplicar el descuento: " + ex.getMessage());
                }
            }

            VentaDAO ventaDAO = new VentaDAO();
            int idVenta = ventaDAO.guardarVenta(idCliente, total);

            DetalleVentaDAO detalleDAO = new DetalleVentaDAO();
            ProductoDAO productoDAO = new ProductoDAO();

            for (DetalleVenta d : detallesVenta) {
                detalleDAO.guardarDetalle(idVenta, d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
                productoDAO.actualizarStock(d.getIdProducto(), d.getCantidad());
            }

            PagoDAO pagoDAO = new PagoDAO();
            pagoDAO.guardarPago(idVenta, cbMetodoPago.getValue(), total, cbEstado.getValue());

            ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
            comprobanteDAO.guardarComprobante(idVenta, cbTipoDocumento.getValue());

            // ==============================================================
            // ALERTA PERSONALIZADA: ELEGIR TIPO DE IMPRESIÓN Y ABRIR PDF
            // ==============================================================
            Alert alertaImpresion = new Alert(Alert.AlertType.CONFIRMATION);
            alertaImpresion.setTitle("Venta Registrada Exitosamente");
            alertaImpresion.setHeaderText("La venta N° " + idVenta + " se ha guardado.");
            alertaImpresion.setContentText("Seleccione el formato para visualizar e imprimir el comprobante:");

            ButtonType btnA4 = new ButtonType("Formato A4");
            ButtonType btnTicket = new ButtonType("Ticket 80mm");
            ButtonType btnOmitir = new ButtonType("Cerrar / Omitir", ButtonBar.ButtonData.CANCEL_CLOSE);

            alertaImpresion.getButtonTypes().setAll(btnTicket, btnA4, btnOmitir);

            Optional<ButtonType> resultado = alertaImpresion.showAndWait();

            if (resultado.isPresent() && resultado.get() != btnOmitir) {

                Venta ventaPDF = new Venta();
                ventaPDF.setId(idVenta);
                ventaPDF.setTotal(total);
                ventaPDF.setProductos(detallesVenta.size());
                ventaPDF.setCliente(nombreAsumido);
                ventaPDF.setMetodoPago(cbMetodoPago.getValue());
                ventaPDF.setEstado(cbTipoDocumento.getValue());

                java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
                java.time.format.DateTimeFormatter formato = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                ventaPDF.setFecha(ahora.format(formato));

                ventaPDF.setDetalles(detallesVenta);

                // Declaramos la variable leyendo el ComboBox
                boolean esFacturaSel = cbTipoDocumento.getValue().equals("FACTURA");
                String archivoNombre = (esFacturaSel ? "Factura_" : "Boleta_") + idVenta + ".pdf";

                // 2. Generamos el archivo según el formato elegido
                if (resultado.get() == btnTicket) {
                    com.servicio.ComprobanteImpresionServicio.emitirEImprimirTicketKiosko(ventaPDF);
                } else if (resultado.get() == btnA4) {
                    com.servicio.ComprobanteImpresionServicio.emitirFormatoA4(ventaPDF);
                }

                // 3. Abrir el PDF automáticamente
                try {
                    String rutaCarpeta = System.getProperty("user.dir") + File.separator + "Tickets";
                    File archivoPdf = new File(rutaCarpeta + File.separator + archivoNombre);

                    if (archivoPdf.exists()) {
                        Desktop.getDesktop().open(archivoPdf);
                    }
                } catch (Exception ex) {
                    System.err.println("Error al intentar abrir el PDF automáticamente.");
                }
            }

            limpiarFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error inesperado");
            alert.setHeaderText(null);
            alert.setContentText("Ocurrió un error grave al guardar la venta. Revisa los logs.");
            alert.showAndWait();
        }
    }

    private void eliminarProducto() {
        int index = tablaDetalleVenta.getSelectionModel().getSelectedIndex();
        if(index < 0){ return; }

        ItemVenta item = listaItems.get(index);
        totalVenta -= item.getSubtotal();

        if (totalVenta < 0.01) { totalVenta = 0.0; }

        lblTotal.setText("S/ " + String.format(java.util.Locale.US, "%.2f", totalVenta));

        listaItems.remove(index);
        detallesVenta.remove(index);
        productosVenta.remove(index);

        System.out.println("Producto eliminado correctamente. Ítems restantes en BD: " + detallesVenta.size());
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

        if(txtDireccion != null) {
            txtDireccion.clear();
        }
        ubigeoActual = "";
    }

    private void buscarDatosCliente() {
        String documento = txtRucDni.getText() != null ? txtRucDni.getText().trim() : "";

        if (documento.isEmpty()) {
            txtRazonSocial.setText("CLIENTES VARIOS");
            cbTipoDocumento.setValue("BOLETA");
            return;
        }

        if (documento.length() != 8 && documento.length() != 11) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("El documento debe tener 8 (DNI) o 11 (RUC) dígitos.");
            alert.show();
            return;
        }

        new Thread(() -> {
            JsonObject datos = ApiSunatServicio.consultarDocumento(documento);

            Platform.runLater(() -> {
                if (datos != null) {
                    if (documento.length() == 8) {
                        txtRazonSocial.setText(datos.get("nombre_completo").getAsString());
                        cbTipoDocumento.setValue("BOLETA");

                        if(txtDireccion != null) txtDireccion.setText("");
                        ubigeoActual = "";

                    } else if (documento.length() == 11) {
                        txtRazonSocial.setText(datos.get("nombre_o_razon_social").getAsString());
                        cbTipoDocumento.setValue("FACTURA");

                        if (datos.has("direccion_completa") && !datos.get("direccion_completa").isJsonNull()) {
                            if (txtDireccion != null) {
                                txtDireccion.setText(datos.get("direccion_completa").getAsString());
                            }
                        } else {
                            if (txtDireccion != null) {
                                txtDireccion.setText("");
                            }
                        }

                        if (datos.has("ubigeo_sunat") && !datos.get("ubigeo_sunat").isJsonNull()) {
                            ubigeoActual = datos.get("ubigeo_sunat").getAsString();
                        } else {
                            ubigeoActual = "";
                        }

                        String estado = datos.has("estado") && !datos.get("estado").isJsonNull() ? datos.get("estado").getAsString() : "";
                        String condicion = datos.has("condicion") && !datos.get("condicion").isJsonNull() ? datos.get("condicion").getAsString() : "";

                        if (!estado.equals("ACTIVO") || !condicion.equals("HABIDO")) {
                            Alert alertaEstado = new Alert(Alert.AlertType.WARNING);
                            alertaEstado.setTitle("Advertencia SUNAT");
                            alertaEstado.setHeaderText("Problema con el RUC");
                            alertaEstado.setContentText("El RUC ingresado se encuentra en estado: " + estado + " y condición: " + condicion + ". Confirma si deseas proceder con la factura.");
                            alertaEstado.show();
                        }
                    }
                } else {
                    txtRazonSocial.setText("");
                    if(txtDireccion != null) txtDireccion.setText("");
                    ubigeoActual = "";

                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("No se encontraron datos. Verifica el DNI/RUC o tu conexión.");
                    alert.show();
                }
            });
        }).start();
    }

    private void guardarDatosClienteTemporal() {
        if (txtRucDni.getText() == null || txtRucDni.getText().trim().isEmpty()) {
            txtRucDni.setText("00000000");
        }
        if (txtRazonSocial.getText() == null || txtRazonSocial.getText().trim().isEmpty()) {
            txtRazonSocial.setText("CLIENTES VARIOS");
            cbTipoDocumento.setValue("BOLETA");
        }

        panelDatosCliente.setVisible(false);
        panelDatosCliente.setManaged(false);
        btnMostrarDatosCliente.setVisible(true);
        btnMostrarDatosCliente.setManaged(true);
    }
}