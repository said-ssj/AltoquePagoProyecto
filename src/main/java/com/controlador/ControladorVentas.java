package com.controlador;

import com.DB.ConexionDB;
import com.dao.VentaDAO;
import com.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ControladorVentas {

    @FXML private TableView<Venta>            tablaVentas;
    @FXML private TableColumn<Venta, String>  colId;
    @FXML private TableColumn<Venta, String>  colFecha;
    @FXML private TableColumn<Venta, String>  colCliente;
    @FXML private TableColumn<Venta, Integer> colProductos;
    @FXML private TableColumn<Venta, Double>  colTotal;
    @FXML private TableColumn<Venta, String>  colEstado;
    @FXML private TableColumn<Venta, String>  colMetodoPago;
    @FXML private TableColumn<Venta, Void>    colAcciones;

    @FXML private ComboBox<String> cbFiltrosVentas;
    @FXML private TextField        txtBuscarVenta;

    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();
    private final VentaDAO ventaDAO = new VentaDAO();

    @FXML
    public void initialize() {
        cbFiltrosVentas.getItems().addAll("Hoy", "Últimos 7 días", "Últimos 30 días", "Todos");

        // ── Columnas básicas ──────────────────────────────────────
        colId       .setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha    .setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente  .setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("productos"));

        // ── Total formateado ──────────────────────────────────────
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("S/ %,.2f", item));
            }
        });

        // ── Método de pago ────────────────────────────────────────
        if (colMetodoPago != null)
            colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));

        // ── Estado con badge de colores ───────────────────────────
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                Label badge = new Label(item);
                badge.setPadding(new Insets(4, 12, 4, 12));
                String base = "-fx-background-radius:12px;-fx-font-size:12px;-fx-font-weight:bold;";
                badge.setStyle(base + switch (item.toUpperCase()) {
                    case "PAGADO"    -> "-fx-background-color:#dcfce7;-fx-text-fill:#166534;";
                    case "PENDIENTE" -> "-fx-background-color:#fef9c3;-fx-text-fill:#854d0e;";
                    case "RECHAZADO" -> "-fx-background-color:#fad1d0;-fx-text-fill:#a61511;";
                    default          -> "-fx-background-color:#e2e8f0;-fx-text-fill:#475569;";
                });
                setGraphic(badge); setText(null);
            }
        });

        configurarColumnaAcciones();

        listaVentas.addAll(ventaDAO.listarVentas());
        tablaVentas.setItems(listaVentas);

        if (txtBuscarVenta != null)
            txtBuscarVenta.textProperty().addListener((obs, old, nuevo) -> filtrarVentas(nuevo));
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
                btnEditar  .setOnAction(e -> abrirDialogoEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarYEliminar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    // ============================================================
    //  DIÁLOGO EDICIÓN: cliente, estado, método de pago
    // ============================================================
    private void abrirDialogoEdicion(Venta venta) {
        Dialog<Venta> dialog = new Dialog<>();
        dialog.setTitle("Editar Venta #" + venta.getId());
        dialog.setHeaderText(null);

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(440);

        // ── Cargar clientes desde la BD ──────────────────────────
        List<String[]> clientes = ventaDAO.listarClientes();
        Map<String, Integer> mapaClientes = new LinkedHashMap<>();
        ComboBox<String> fCliente = new ComboBox<>();
        fCliente.setEditable(true);
        for (String[] c : clientes) {
            mapaClientes.put(c[1], Integer.parseInt(c[0]));
            fCliente.getItems().add(c[1]);
        }
        fCliente.setValue(venta.getCliente());
        fCliente.setMaxWidth(Double.MAX_VALUE);

        // ── Estado ───────────────────────────────────────────────
        ComboBox<String> fEstado = new ComboBox<>();
        fEstado.getItems().addAll("PENDIENTE", "PAGADO", "RECHAZADO");
        fEstado.setValue(venta.getEstado() != null ? venta.getEstado().toUpperCase() : "PENDIENTE");
        fEstado.setMaxWidth(Double.MAX_VALUE);

        // ── Método de pago ────────────────────────────────────────
        ComboBox<String> fMetodo = new ComboBox<>();
        fMetodo.getItems().addAll("YAPE", "PLIN", "TARJETA");
        String metodoActual = venta.getMetodoPago();
        fMetodo.setValue(metodoActual != null && !metodoActual.equals("N/A") ? metodoActual : "TARJETA");
        fMetodo.setMaxWidth(Double.MAX_VALUE);

        // ── Info extra (solo lectura) ─────────────────────────────
        Label lblInfo = new Label(
                "ID: " + venta.getId() + "   |   Fecha: " + venta.getFecha() +
                        "   |   Total: S/ " + String.format("%,.2f", venta.getTotal())
        );
        lblInfo.setStyle("-fx-font-size:12px;-fx-text-fill:#64748b;");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(14);
        grid.setPadding(new Insets(20, 24, 10, 24));
        grid.addRow(0, lblInfo);
        grid.addRow(1, new Label("Cliente:"),         fCliente);
        grid.addRow(2, new Label("Estado del pago:"), fEstado);
        grid.addRow(3, new Label("Método de pago:"),  fMetodo);
        // Que los combos se extiendan
        javafx.scene.layout.GridPane.setHgrow(fCliente, javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.GridPane.setHgrow(fEstado,  javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.GridPane.setHgrow(fMetodo,  javafx.scene.layout.Priority.ALWAYS);
        javafx.scene.layout.GridPane.setColumnSpan(lblInfo, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                String nombreCliente = fCliente.getValue().trim();

                venta.setCliente(nombreCliente);

                Integer nuevoIdCliente = mapaClientes.get(nombreCliente);

                if (nuevoIdCliente == null) {
                    nuevoIdCliente = insertarNuevoCliente(nombreCliente);
                }

                if (nuevoIdCliente != -1) {
                    venta.setIdCliente(nuevoIdCliente);
                }
                venta.setEstado(fEstado.getValue());
                venta.setMetodoPago(fMetodo.getValue());
                return venta;
            }
            return null;
        });

        Optional<Venta> resultado = dialog.showAndWait();
        resultado.ifPresent(v -> {
            if (actualizarVentaBD(v)) {
                recargarVentas();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Venta actualizada correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la venta.");
            }
        });
    }

    // ============================================================
    //  CONFIRMAR Y ELIMINAR
    // ============================================================
    private void confirmarYEliminar(Venta venta) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar venta");
        confirm.setHeaderText("¿Eliminar venta #" + venta.getId() + "?");
        confirm.setContentText(
                "Cliente: " + venta.getCliente() +
                        "\nTotal: S/ " + String.format("%,.2f", venta.getTotal()) +
                        "\n\nSe eliminarán detalles y pago asociados.\nEsta acción no se puede deshacer."
        );
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (eliminarVentaBD(Integer.parseInt(venta.getId()))) {
                    listaVentas.remove(venta);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Venta eliminada correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la venta.");
                }
            }
        });
    }

    // ============================================================
    //  BASE DE DATOS
    // ============================================================
    private boolean actualizarVentaBD(Venta v) {
        try (Connection cn = ConexionDB.conectar()) {
            // 1. Actualizar cliente en venta
            String sqlVenta = "UPDATE venta SET id_cliente=? WHERE id_venta=?";
            try (PreparedStatement ps = cn.prepareStatement(sqlVenta)) {
                ps.setInt(1, v.getIdCliente());
                ps.setInt(2, Integer.parseInt(v.getId()));
                ps.executeUpdate();
            }
            // 2. Actualizar o insertar pago
            String sqlCheck = "SELECT COUNT(*) FROM pago WHERE id_venta=?";
            int count = 0;
            try (PreparedStatement ps = cn.prepareStatement(sqlCheck)) {
                ps.setInt(1, Integer.parseInt(v.getId()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            }
            if (count > 0) {
                String sqlPago = "UPDATE pago SET estado=?, metodo=? WHERE id_venta=?";
                try (PreparedStatement ps = cn.prepareStatement(sqlPago)) {
                    ps.setString(1, v.getEstado());
                    ps.setString(2, v.getMetodoPago());
                    ps.setInt   (3, Integer.parseInt(v.getId()));
                    ps.executeUpdate();
                }
            } else {
                String sqlIns = "INSERT INTO pago (id_venta, metodo, monto, estado) VALUES (?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(sqlIns)) {
                    ps.setInt   (1, Integer.parseInt(v.getId()));
                    ps.setString(2, v.getMetodoPago());
                    ps.setDouble(3, v.getTotal());
                    ps.setString(4, v.getEstado());
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private int insertarNuevoCliente(String nombreCliente) {

        String sql = """
        INSERT INTO cliente (nombre, tipo_documento)
        VALUES (?, ?)
        """;

        try (
                Connection cn = ConexionDB.conectar();
                PreparedStatement ps = cn.prepareStatement(
                        sql,
                        PreparedStatement.RETURN_GENERATED_KEYS
                )
        ) {

            ps.setString(1, nombreCliente);

            // D = DNI
            ps.setString(2, "D");

            int filas = ps.executeUpdate();

            if (filas > 0) {

                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private boolean eliminarVentaBD(int idVenta) {
        String[] sqls = {
                "DELETE FROM pago WHERE id_venta=?",
                "DELETE FROM comprobante WHERE id_venta=?",
                "DELETE FROM detalle_venta WHERE id_venta=?",
                "DELETE FROM venta WHERE id_venta=?"
        };
        try (Connection cn = ConexionDB.conectar()) {
            cn.setAutoCommit(false);
            for (String sql : sqls) {
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, idVenta); ps.executeUpdate();
                }
            }
            cn.commit(); return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ============================================================
    //  FILTRO / BÚSQUEDA / RECARGA
    // ============================================================
    private void filtrarVentas(String texto) {
        if (texto == null || texto.isBlank()) { tablaVentas.setItems(listaVentas); return; }
        String lower = texto.toLowerCase();
        tablaVentas.setItems(listaVentas.filtered(v ->
                v.getCliente().toLowerCase().contains(lower) ||
                        v.getId().contains(lower) ||
                        v.getFecha().contains(lower) ||
                        (v.getEstado() != null && v.getEstado().toLowerCase().contains(lower))
        ));
    }

    private void recargarVentas() {
        listaVentas.clear();
        listaVentas.addAll(ventaDAO.listarVentas());
    }

    @FXML
    public void abrirNuevaVenta(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevaventa-view.fxml"));
            javafx.scene.Parent vista = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panel = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}