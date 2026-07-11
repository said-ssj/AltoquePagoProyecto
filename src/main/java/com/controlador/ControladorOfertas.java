package com.controlador;

import com.dao.OfertaDAO;
import com.dao.ProductoDAO;
import com.modelo.Oferta;
import com.modelo.Producto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ControladorOfertas implements Initializable {

    // ── Tabla ────────────────────────────────────────────────────
    @FXML private TableView<Oferta>            tablaOfertas;
    @FXML private TableColumn<Oferta, Integer> colId;
    @FXML private TableColumn<Oferta, String>  colProducto;
    @FXML private TableColumn<Oferta, String>  colDescripcion;
    @FXML private TableColumn<Oferta, Double>  colDescuento;
    @FXML private TableColumn<Oferta, String>  colInicio;
    @FXML private TableColumn<Oferta, String>  colFin;
    @FXML private TableColumn<Oferta, String>  colEstado;

    // ── Buscador ─────────────────────────────────────────────────
    @FXML private TextField txtBuscar;

    // ── Formulario lateral ───────────────────────────────────────
    @FXML private TextField  txtBuscarProductoOfertas;
    @FXML private TextField  txtDescripcion;
    @FXML private TextField  txtDescuento;
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private CheckBox   chkEstado;

    private final OfertaDAO  ofertaDAO  = new OfertaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Oferta> listaOfertas = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ContextMenu  popupBusqueda;
    private Producto     productoSeleccionado;
    private Oferta       ofertaEnEdicion;   // null = nueva, != null = editando

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarBusquedaProducto();
        cargarDatosTabla();

        // Al hacer clic en una fila → cargar en formulario para editar
        tablaOfertas.getSelectionModel().selectedItemProperty().addListener((obs, old, nueva) -> {
            if (nueva != null) cargarEnFormulario(nueva);
        });

        // Búsqueda en tiempo real en la tabla
        txtBuscar.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null || nuevo.isBlank()) cargarDatosTabla();
            else filtrarTabla(nuevo);
        });
    }

    // ============================================================
    //  CONFIGURAR COLUMNAS
    // ============================================================
    private void configurarColumnas() {
        colId         .setCellValueFactory(new PropertyValueFactory<>("id_oferta"));
        colProducto   .setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescuento  .setCellValueFactory(new PropertyValueFactory<>("descuento"));

        colInicio.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFechaInicio() != null
                        ? c.getValue().getFechaInicio().format(FMT) : "--"));
        colFin.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFechaFin() != null
                        ? c.getValue().getFechaFin().format(FMT) : "--"));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstadoTexto()));

        // Badge de color para estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setPadding(new Insets(3, 10, 3, 10));
                badge.setStyle("-fx-background-radius:10px;-fx-font-size:11px;-fx-font-weight:bold;" +
                        ("Activa".equals(item)
                                ? "-fx-background-color:#dcfce7;-fx-text-fill:#166534;"
                                : "-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;"));
                setGraphic(badge); setText(null);
            }
        });

        // Columna de acciones: Editar + Eliminar
        TableColumn<Oferta, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(180);
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏ Editar");
            private final Button btnEliminar = new Button("🗑 Eliminar");
            private final HBox   caja        = new HBox(8, btnEditar, btnEliminar);
            {
                caja.setAlignment(Pos.CENTER);
                btnEditar  .getStyleClass().add("boton-editar");
                btnEliminar.getStyleClass().add("boton-eliminar");
                btnEditar.setOnAction(e -> cargarEnFormulario(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarOferta(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : caja);
            }
        });
        tablaOfertas.getColumns().add(colAcciones);
        tablaOfertas.setItems(listaOfertas);
    }

    // ============================================================
    //  BÚSQUEDA AUTOCOMPLETE DE PRODUCTO (panel lateral)
    // ============================================================
    private void configurarBusquedaProducto() {
        popupBusqueda = new ContextMenu();
        popupBusqueda.setPrefWidth(280);

        txtBuscarProductoOfertas.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null || nuevo.isBlank()) {
                popupBusqueda.hide();
                if (ofertaEnEdicion == null) productoSeleccionado = null;
                return;
            }
            if (productoSeleccionado != null && nuevo.equals(productoSeleccionado.getNombre())) return;
            if (nuevo.trim().length() >= 2) {
                List<Producto> resultados = productoDAO.buscarPorNombre(nuevo.trim());
                if (!resultados.isEmpty()) {
                    mostrarResultadosProducto(resultados);
                    if (!popupBusqueda.isShowing())
                        popupBusqueda.show(txtBuscarProductoOfertas, Side.BOTTOM, 0, 0);
                } else {
                    popupBusqueda.hide();
                }
            } else {
                popupBusqueda.hide();
            }
        });
    }

    private void mostrarResultadosProducto(List<Producto> resultados) {
        popupBusqueda.getItems().clear();
        for (Producto p : resultados) {
            HBox fila = new HBox(10);
            Label lblNombre = new Label(p.getNombre());
            lblNombre.setStyle("-fx-text-fill:#1e293b;-fx-font-size:14px;");
            Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
            Label lblStock = new Label("Stock: " + p.getStock());
            lblStock.setStyle("-fx-text-fill:#64748b;-fx-font-size:12px;");
            fila.getChildren().addAll(lblNombre, spacer, lblStock);
            CustomMenuItem item = new CustomMenuItem(fila);
            item.setHideOnClick(true);
            item.setOnAction(e -> seleccionarProducto(p));
            popupBusqueda.getItems().add(item);
        }
    }

    private void seleccionarProducto(Producto p) {
        productoSeleccionado = p;
        txtBuscarProductoOfertas.setText(p.getNombre());
        popupBusqueda.hide();
    }

    // ============================================================
    //  CARGA / FILTRO DE LA TABLA
    // ============================================================
    private void cargarDatosTabla() {
        listaOfertas.setAll(ofertaDAO.listarTodas());
    }

    private void filtrarTabla(String texto) {
        listaOfertas.setAll(ofertaDAO.buscar(texto));
    }

    // ============================================================
    //  CARGAR OFERTA EN EL FORMULARIO PARA EDITAR
    // ============================================================
    private void cargarEnFormulario(Oferta o) {
        ofertaEnEdicion = o;
        // Buscar nombre del producto para mostrarlo en el campo
        txtBuscarProductoOfertas.setText(o.getNombreProducto());
        productoSeleccionado = new Producto(
                o.getId_producto(), null, o.getNombreProducto(), 0, 0);
        txtDescripcion.setText(o.getDescripcion());
        txtDescuento  .setText(String.valueOf(o.getDescuento()));
        dpInicio      .setValue(o.getFechaInicio());
        dpFin         .setValue(o.getFechaFin());
        chkEstado     .setSelected(o.isEstado());
    }

    // ============================================================
    //  GUARDAR (INSERT o UPDATE según modo)
    // ============================================================
    @FXML
    public void guardarOferta() {
        if (productoSeleccionado == null) {
            alerta("Selecciona un producto de la lista desplegable."); return;
        }
        String desc = txtDescripcion.getText().trim();
        String dtoStr = txtDescuento.getText().trim();
        if (desc.isEmpty() || dtoStr.isEmpty()) {
            alerta("Completa la descripción y el descuento."); return;
        }
        double dto;
        try { dto = Double.parseDouble(dtoStr); }
        catch (NumberFormatException e) { alerta("El descuento debe ser un número válido."); return; }

        Oferta o = ofertaEnEdicion != null ? ofertaEnEdicion : new Oferta();
        o.setId_producto(productoSeleccionado.getId_producto());
        o.setNombreProducto(productoSeleccionado.getNombre());
        o.setDescripcion(desc);
        o.setDescuento(dto);
        o.setFechaInicio(dpInicio.getValue());
        o.setFechaFin(dpFin.getValue());
        o.setEstado(chkEstado.isSelected());

        boolean ok;
        if (ofertaEnEdicion != null) {
            ok = ofertaDAO.actualizar(o);
        } else {
            ok = ofertaDAO.insertar(o);
        }

        if (ok) {
            info(ofertaEnEdicion != null ? "Oferta actualizada." : "Oferta guardada.");
            limpiarFormulario();
            cargarDatosTabla();
        } else {
            alerta("No se pudo guardar la oferta en la base de datos.");
        }
    }

    private void eliminarOferta(Oferta o) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar oferta");
        confirm.setHeaderText("¿Eliminar la oferta de '" + o.getNombreProducto() + "'?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (ofertaDAO.eliminar(o.getId_oferta())) {
                    listaOfertas.remove(o);
                    limpiarFormulario();
                    info("Oferta eliminada.");
                } else {
                    alerta("No se pudo eliminar la oferta.");
                }
            }
        });
    }

    @FXML
    public void limpiarFormulario() {
        ofertaEnEdicion = null;
        productoSeleccionado = null;
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
        String t = txtBuscar.getText().trim();
        if (t.isBlank()) cargarDatosTabla(); else filtrarTabla(t);
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
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