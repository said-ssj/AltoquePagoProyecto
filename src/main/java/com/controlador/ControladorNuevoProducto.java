package com.controlador;
import com.dao.ProductoDAO;

import com.modelo.Producto;

import javafx.application.Platform;

import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;

import javafx.scene.control.Alert;

import javafx.scene.control.Button;

import javafx.scene.control.ComboBox;

import javafx.scene.control.TextArea;

import javafx.scene.control.TextField;

import javafx.scene.control.ToggleButton;



import java.io.IOException;
public class ControladorNuevoProducto {
    // ── Botones ──────────────────────────────────────────────────
    @FXML private Button     btnVolver;
    @FXML private Button     btnCancelar;
    @FXML private Button     btnGuardarProducto;
    @FXML private ToggleButton btnModoBusqueda;
    // ── Campos de texto ──────────────────────────────────────────
    @FXML private TextField  txtCodigoBarras;
    @FXML private TextField  txtNombre;
    @FXML private TextField  txtIDproducto;
    @FXML private TextField  txtPrecioVenta;
    @FXML private TextField  txtStockInicial;
    @FXML private TextField  txtStockMinimo;
    @FXML private TextArea   txtDescripcion;
    // ── Combos ───────────────────────────────────────────────────
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbUnidadMedida;
    @FXML private ComboBox<String> cbProveedor;
    @FXML private ComboBox<String> cbEstado;
    private final ProductoDAO productoDAO = new ProductoDAO();
    // ============================================================
    //  INICIALIZACIÓN
    // ============================================================
    @FXML
    public void initialize() {
        // Llenar combos
        cbCategoria.getItems().addAll(
                "Computadoras", "Accesorios", "Monitores",
                "Periféricos", "Impresoras", "Almacenamiento", "Otro"
        );
        cbUnidadMedida.getItems().addAll("Unidades", "Cajas", "Metros", "Litros", "Kilogramos");
        cbProveedor.getItems().addAll("Proveedor A", "Proveedor B", "Proveedor C");
        cbEstado.getItems().addAll("Activo", "Inactivo");
        cbEstado.setValue("Activo");
        // Guardar arranca deshabilitado
        btnGuardarProducto.setDisable(true);
        btnGuardarProducto.setOnAction(e -> guardarProducto());
        // Modo escáner por defecto
        configurarModoCodigoBarras();
        // Toggle de modo
        btnModoBusqueda.setOnAction(e -> {
            if (btnModoBusqueda.isSelected()) {
                configurarModoBusqueda();
            } else {
                configurarModoCodigoBarras();
            }
            resetearEstadoCampo();
        });
        // Listener: busca en BD con cada cambio del campo código
        txtCodigoBarras.textProperty().addListener((obs, oldVal, newVal) -> {
            // Modo escáner: solo dígitos
            if (!btnModoBusqueda.isSelected() && !newVal.matches("\\d*")) {
                txtCodigoBarras.setText(newVal.replaceAll("[^\\d]", ""));
                return;
            }
            // Campo vacío → reset visual
            if (newVal.trim().isEmpty()) {
                resetearEstadoCampo();
                return;
            }
            // Consultar BD y pintar verde o rojo
            buscarCodigoEnBD(newVal.trim());
        });
    }



    // ============================================================
    //  BÚSQUEDA EN BASE DE DATOS → VERDE / ROJO
    // ============================================================
    private void buscarCodigoEnBD(String codigo) {
        // Usamos el método correcto que definimos en ProductoDAO
        boolean existe = productoDAO.existeCodigo(codigo);

        txtCodigoBarras.getStyleClass().removeAll(
                "text-field-busqueda",
                "text-field-busqueda-encontrado",
                "text-field-busqueda-no-encontrado"
        );

        if (existe) {
            // Si existe, es un error. Rojo y bloquear.
            txtCodigoBarras.getStyleClass().add("text-field-busqueda-no-encontrado");
            btnGuardarProducto.setDisable(true);
            mostrarAlerta(Alert.AlertType.WARNING, "Código Duplicado", "Este código de barras ya está registrado en el sistema.");
        } else {
            // Si no existe, es perfecto. Verde y permitir guardar.
            txtCodigoBarras.getStyleClass().add("text-field-busqueda-encontrado");
            btnGuardarProducto.setDisable(false);
        }
    }


    // ============================================================
    //  GUARDAR PRODUCTO EN BASE DE DATOS
    // ============================================================
    private void guardarProducto() {

        // Validar campos obligatorios
        String nombre = txtNombre.getText().trim();
        String codigo = txtCodigoBarras.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Campo requerido", "La descripción del producto no puede estar vacía.");
            txtNombre.requestFocus();
            return;
        }
        if (codigo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Campo requerido", "El código de barras no puede estar vacío.");
            txtCodigoBarras.requestFocus();
            return;
        }

        // Parsear precio y stock con valores por defecto si están vacíos
        double precio = 0.0;
        int    stock  = 0;
        try {
            String precioTexto = txtPrecioVenta.getText().trim();
            if (!precioTexto.isEmpty()) {
                precio = Double.parseDouble(precioTexto);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Precio inválido", "Ingresa un número válido en el campo Precio. Ejemplo: 29.90");
            txtPrecioVenta.requestFocus();
            return;
        }
        try {
            String stockTexto = txtStockInicial.getText().trim();
            if (!stockTexto.isEmpty()) {
                stock = Integer.parseInt(stockTexto);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Stock inválido", "Ingresa un número entero en el campo Stock Inicial.");
            txtStockInicial.requestFocus();
            return;
        }

        // Evitar precio cero o negativo
        if (precio <= 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Precio inválido", "El precio de venta debe ser mayor a 0.");
            txtPrecioVenta.requestFocus();
            return;
        }

        // Evitar stock negativo
        if (stock < 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Stock inválido", "El stock inicial no puede ser un número negativo.");
            txtStockInicial.requestFocus();
            return;
        }

        // Construir el objeto Producto
        // Nota:
        // Usamos el constructor con parámetros que ya hay en el DAO.
        // Ponemos "0" en el ID porque MySQL lo autogenerará (Auto Increment).
        Producto nuevo = new Producto(0, codigo, nombre, precio, stock);

        // Llamar al DAO
        boolean exito = productoDAO.guardarProducto(nuevo);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Producto guardado",
                    "El producto \"" + nombre + "\" fue registrado correctamente.");
            limpiarFormulario();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al guardar",
                    "No se pudo guardar el producto. Verifica la conexión a la base de datos.");
        }
    }


    // ============================================================
    //  RESET VISUAL DEL CAMPO (estado azul neutral)
    // ============================================================

    private void resetearEstadoCampo() {
        txtCodigoBarras.getStyleClass().removeAll(
                "text-field-busqueda-encontrado",
                "text-field-busqueda-no-encontrado"
        );

        if (!txtCodigoBarras.getStyleClass().contains("text-field-busqueda")) {
            txtCodigoBarras.getStyleClass().add("text-field-busqueda");
        }
        btnGuardarProducto.setDisable(true);
    }



    // ============================================================
    //  LIMPIAR FORMULARIO DESPUÉS DE GUARDAR
    // ============================================================

    private void limpiarFormulario() {
        txtCodigoBarras.clear();
        txtNombre.clear();
        txtIDproducto.clear();
        txtPrecioVenta.clear();
        txtStockInicial.clear();
        txtStockMinimo.clear();
        txtDescripcion.clear();
        cbCategoria.setValue(null);
        cbUnidadMedida.setValue(null);
        cbProveedor.setValue(null);
        cbEstado.setValue("Activo");
        resetearEstadoCampo();
        Platform.runLater(() -> txtCodigoBarras.requestFocus());

    }



    // ============================================================
    //  MODOS ESCÁNER / BÚSQUEDA
    // ============================================================

    private void configurarModoCodigoBarras() {
        btnModoBusqueda.setText("🔍 Modo Búsqueda");
        txtCodigoBarras.setPromptText("||||| Escanear código de barras...");
        Platform.runLater(() -> txtCodigoBarras.requestFocus());

    }

    private void configurarModoBusqueda() {
        btnModoBusqueda.setText("||||| Modo Código de Barras");
        txtCodigoBarras.setPromptText("🔍 Escribe el nombre o código...");

    }

    // ============================================================
    //  HELPER: mostrar Alert
    // ============================================================
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }



    // ============================================================
    //  NAVEGACIÓN — volver a la lista de productos
    // ============================================================
    @FXML
    public void abrirProductos(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/productos-view.fxml"));
            javafx.scene.Parent vistaProductos = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal =
                    (javafx.scene.layout.BorderPane) boton.getScene().getRoot();
            panelPrincipal.setCenter(vistaProductos);
        } catch (IOException e) {
            System.err.println("Error al volver a la vista de productos");
            e.printStackTrace();
        }
    }
}

