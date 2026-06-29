package com.controlador;

import com.DB.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.*;
import java.util.*;

public class ControladorConsultas implements Initializable {

    // CONSULTAR VENTAS
    @FXML private DatePicker dateInicioVentas;
    @FXML private DatePicker dateFinVentas;
    @FXML private Button btnConsultarVentas;

    // CONSULTAR PRODUCTOS
    // CONSULTAR PRODUCTOS
    @FXML private ComboBox<String> cbTipoProducto;
    @FXML private Spinner<Integer> spinnerStockMinimo;
    @FXML private Button btnConsultarProductos;

    // CONSULTAR EMPLEADOS
    @FXML private ComboBox<String> cbDepartamento;
    @FXML private ComboBox<String> cbEstadoEmpleado;
    @FXML private Button btnConsultarEmpleados;

    // CONSULTA GENERAL
    @FXML private ComboBox<String> cbTipoConsulta;
    @FXML private TextField txtParametroConsulta;
    @FXML private Button btnConsultarGeneral;

    // PANEL DE RESULTADOS (nuevo en el FXML)
    @FXML private VBox panelResultados;
    @FXML private Label lblTituloResultado;
    @FXML private Label lblMensajeVacio;
    @FXML private TableView<Map<String, Object>> tablaResultados;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarCategorias();
        cargarDepartamentos();
        cargarEstados();
        cargarTiposConsulta();
        configurarSpinner();

        // Ocultar panel de resultados al inicio
        panelResultados.setVisible(false);
        panelResultados.setManaged(false);

        // Conectar botones con sus acciones
        btnConsultarVentas.setOnAction(e -> consultarVentas());
        btnConsultarProductos.setOnAction(e -> consultarProductos());
        btnConsultarEmpleados.setOnAction(e -> consultarEmpleados());
        btnConsultarGeneral.setOnAction(e -> consultarGeneral());
    }

    // ============================================================
    //  INICIALIZACIÓN DE COMBOS
    // ============================================================

    private void cargarCategorias() {
        cbTipoProducto.getItems().addAll(
                "Todas las categorías",
                "Computadoras",
                "Accesorios",
                "Monitores"
        );
        cbTipoProducto.setValue("Todas las categorías");
    }

    private void cargarDepartamentos() {
        // Cargar áreas reales desde la BD
        cbDepartamento.getItems().add("Todos los departamentos");
        try (Connection cn = ConexionDB.conectar()) {
            if (cn != null) {
                String sql = "SELECT DISTINCT area FROM usuario_personal ORDER BY area";
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String area = rs.getString("area");
                    if (area != null && !area.isBlank()) {
                        cbDepartamento.getItems().add(area);
                    }
                }
            }
        } catch (Exception e) {
            // Si falla la BD, usar valores por defecto
            cbDepartamento.getItems().addAll("Ventas", "Finanzas", "IT");
        }
        cbDepartamento.setValue("Todos los departamentos");
    }

    private void cargarEstados() {
        cbEstadoEmpleado.getItems().addAll(
                "Todos",
                "Activo",
                "Inactivo"
        );
        cbEstadoEmpleado.setValue("Todos");
    }

    private void cargarTiposConsulta() {
        cbTipoConsulta.getItems().addAll(
                "Ventas por cliente",
                "Ventas por producto",
                "Ventas por empleado"
        );
        cbTipoConsulta.setValue("Ventas por cliente");
    }

    private void configurarSpinner() {
        spinnerStockMinimo.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0)
        );
    }

    // ============================================================
    //  HELPER: MOSTRAR TABLA CON RESULTADOS GENÉRICOS
    // ============================================================

    /**
     * Recibe el ResultSet de cualquier consulta y construye dinámicamente
     * las columnas y filas de la TableView.
     */
    private void mostrarResultados(String titulo, ResultSet rs) throws SQLException {
        tablaResultados.getColumns().clear();
        tablaResultados.getItems().clear();

        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();

        // Crear columnas dinámicamente
        for (int i = 1; i <= columnas; i++) {
            String nombreCol = meta.getColumnLabel(i);
            TableColumn<Map<String, Object>, Object> col = new TableColumn<>(nombreCol.replace("_", " ").toUpperCase());
            col.setCellValueFactory(new MapValueFactory(nombreCol));
            col.setMinWidth(100);
            col.setPrefWidth(120);
            tablaResultados.getColumns().add(col);
        }

        // Cargar filas
        ObservableList<Map<String, Object>> filas = FXCollections.observableArrayList();
        while (rs.next()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            for (int i = 1; i <= columnas; i++) {
                fila.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            filas.add(fila);
        }

        tablaResultados.setItems(filas);
        lblTituloResultado.setText(titulo + "  (" + filas.size() + " registros)");

        boolean sinDatos = filas.isEmpty();
        lblMensajeVacio.setVisible(sinDatos);
        lblMensajeVacio.setManaged(sinDatos);
        tablaResultados.setVisible(!sinDatos);
        tablaResultados.setManaged(!sinDatos);

        panelResultados.setVisible(true);
        panelResultados.setManaged(true);
    }

    // ============================================================
    //  CONSULTA DE VENTAS (por rango de fechas)
    // ============================================================

    @FXML
    public void consultarVentas() {
        String fechaInicio = dateInicioVentas.getValue() != null
                ? dateInicioVentas.getValue().toString() : null;
        String fechaFin = dateFinVentas.getValue() != null
                ? dateFinVentas.getValue().toString() : null;

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                mostrarAlerta("Sin conexión", "No se pudo conectar a la base de datos.");
                return;
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT " +
                            "  v.id_venta       AS id_venta, " +
                            "  DATE(v.fecha)    AS fecha, " +
                            "  CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS cliente, " +
                            "  COUNT(dv.id_producto) AS productos, " +
                            "  v.total          AS total, " +
                            "  IFNULL(p.estado, 'PENDIENTE') AS estado_pago " +
                            "FROM venta v " +
                            "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                            "LEFT  JOIN detalle_venta dv ON v.id_venta = dv.id_venta " +
                            "LEFT  JOIN pago p ON v.id_venta = p.id_venta "
            );

            List<String> condiciones = new ArrayList<>();
            if (fechaInicio != null) condiciones.add("DATE(v.fecha) >= ?");
            if (fechaFin    != null) condiciones.add("DATE(v.fecha) <= ?");
            if (!condiciones.isEmpty()) sql.append("WHERE ").append(String.join(" AND ", condiciones)).append(" ");

            sql.append("GROUP BY v.id_venta, v.fecha, c.nombre, c.apellido, v.total, p.estado ")
                    .append("ORDER BY v.fecha DESC");

            PreparedStatement ps = cn.prepareStatement(sql.toString());
            int idx = 1;
            if (fechaInicio != null) ps.setString(idx++, fechaInicio);
            if (fechaFin    != null) ps.setString(idx,   fechaFin);

            mostrarResultados("Ventas", ps.executeQuery());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al consultar ventas:\n" + e.getMessage());
        }
    }

    // ============================================================
    //  CONSULTA DE PRODUCTOS (por stock mínimo)
    // ============================================================

    @FXML
    public void consultarProductos() {

        int stockMinimo = spinnerStockMinimo.getValue();
        String tipo = cbTipoProducto.getValue();

        try (Connection cn = ConexionDB.conectar()) {

            if (cn == null) {
                mostrarAlerta("Sin conexión", "No se pudo conectar a la base de datos.");
                return;
            }

            String sql =
                    "SELECT " +
                            "  id_producto AS id, " +
                            "  codigo_barras AS codigo, " +
                            "  nombre, " +
                            "  precio, " +
                            "  stock " +
                            "FROM producto " +
                            "WHERE stock >= ? ";

            if (!tipo.equals("Todas las categorías")) {
                sql += "AND categoria = ? ";
            }

            sql += "ORDER BY nombre ASC";

            PreparedStatement ps = cn.prepareStatement(sql);

            ps.setInt(1, stockMinimo);

            if (!tipo.equals("Todas las categorías")) {
                ps.setString(2, tipo);
            }

            mostrarResultados(
                    "Productos (stock ≥ " + stockMinimo + ")",
                    ps.executeQuery()
            );

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(
                    "Error",
                    "Ocurrió un error al consultar productos:\n" + e.getMessage()
            );
        }
    }

    // ============================================================
    //  CONSULTA DE EMPLEADOS (por área y estado)
    //  Nota: la tabla usuario_personal no tiene columna 'estado',
    //        se usa antecedentes/tipo_contrato como indicador.
    // ============================================================

    @FXML
    public void consultarEmpleados() {
        String departamento = cbDepartamento.getValue();
        String estado       = cbEstadoEmpleado.getValue();

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                mostrarAlerta("Sin conexión", "No se pudo conectar a la base de datos.");
                return;
            }

            StringBuilder sql = new StringBuilder(
                    "SELECT " +
                            "  u.id_usuario     AS id, " +
                            "  u.nombre, " +
                            "  u.email, " +
                            "  r.nombre_rol     AS rol, " +
                            "  u.area, " +
                            "  u.tipo_contrato, " +
                            "  u.telefono, " +
                            "  u.fecha_inicio " +
                            "FROM usuario_personal u " +
                            "LEFT JOIN rol r ON u.id_rol = r.id_rol "
            );

            List<String> condiciones = new ArrayList<>();
            if (!"Todos los departamentos".equals(departamento)) {
                condiciones.add("u.area = ?");
            }
            // 'estado' no existe en la tabla; lo omitimos sin romper la UI
            if (!condiciones.isEmpty()) {
                sql.append("WHERE ").append(String.join(" AND ", condiciones)).append(" ");
            }
            sql.append("ORDER BY u.nombre ASC");

            PreparedStatement ps = cn.prepareStatement(sql.toString());
            if (!"Todos los departamentos".equals(departamento)) {
                ps.setString(1, departamento);
            }

            mostrarResultados("Empleados", ps.executeQuery());

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al consultar empleados:\n" + e.getMessage());
        }
    }

    // ============================================================
    //  CONSULTA GENERAL
    // ============================================================

    @FXML
    public void consultarGeneral() {
        String parametro = txtParametroConsulta.getText().trim();
        String tipo      = cbTipoConsulta.getValue();

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                mostrarAlerta("Sin conexión", "No se pudo conectar a la base de datos.");
                return;
            }

            PreparedStatement ps;

            switch (tipo) {
                case "Ventas por cliente" -> {
                    String sql =
                            "SELECT " +
                                    "  CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS cliente, " +
                                    "  COUNT(v.id_venta)  AS total_ventas, " +
                                    "  SUM(v.total)       AS monto_total " +
                                    "FROM venta v " +
                                    "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                                    (parametro.isBlank() ? "" : "WHERE c.nombre LIKE ? OR c.apellido LIKE ? ") +
                                    "GROUP BY c.id_cliente, c.nombre, c.apellido " +
                                    "ORDER BY monto_total DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) {
                        ps.setString(1, "%" + parametro + "%");
                        ps.setString(2, "%" + parametro + "%");
                    }
                    mostrarResultados("Ventas por cliente", ps.executeQuery());
                }
                case "Ventas por producto" -> {
                    String sql =
                            "SELECT " +
                                    "  p.nombre            AS producto, " +
                                    "  SUM(dv.cantidad)    AS unidades_vendidas, " +
                                    "  SUM(dv.subtotal)    AS ingreso_total " +
                                    "FROM detalle_venta dv " +
                                    "INNER JOIN producto p ON dv.id_producto = p.id_producto " +
                                    (parametro.isBlank() ? "" : "WHERE p.nombre LIKE ? ") +
                                    "GROUP BY p.id_producto, p.nombre " +
                                    "ORDER BY ingreso_total DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) ps.setString(1, "%" + parametro + "%");
                    mostrarResultados("Ventas por producto", ps.executeQuery());
                }
                case "Ventas por empleado" -> {
                    // Relaciona ventas con usuario_personal a través del reporte
                    String sql =
                            "SELECT " +
                                    "  u.nombre            AS empleado, " +
                                    "  u.area, " +
                                    "  COUNT(r.id_reporte) AS reportes_generados " +
                                    "FROM usuario_personal u " +
                                    "LEFT JOIN reporte r ON u.id_usuario = r.id_usuario " +
                                    (parametro.isBlank() ? "" : "WHERE u.nombre LIKE ? ") +
                                    "GROUP BY u.id_usuario, u.nombre, u.area " +
                                    "ORDER BY reportes_generados DESC";
                    ps = cn.prepareStatement(sql);
                    if (!parametro.isBlank()) ps.setString(1, "%" + parametro + "%");
                    mostrarResultados("Ventas por empleado", ps.executeQuery());
                }
                default -> mostrarAlerta("Tipo no reconocido", "Seleccione un tipo de consulta válido.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error en la consulta general:\n" + e.getMessage());
        }
    }

    // ============================================================
    //  UTILIDADES
    // ============================================================

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}