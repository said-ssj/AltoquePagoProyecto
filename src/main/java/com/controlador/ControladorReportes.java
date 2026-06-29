package com.controlador;

import com.DB.ConexionDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de Reportes.
 *
 * LÓGICA DE "REPORTES RECIENTES":
 *   - Ventas  → solo el MES actual (del 1° del mes hasta hoy).
 *   - Inventario → solo la SEMANA actual (lunes – hoy).
 *   - Empleados  → solo la SEMANA actual (lunes – hoy).
 *
 * Todo lo anterior a esos rangos aparece en el panel "Historial de Reportes"
 * (arriba, cerca del botón Generar).
 */
public class ControladorReportes implements Initializable {

    // ─── Botones de generación ───────────────────────────────────────────────
    @FXML private Button btnGenerarVentas;
    @FXML private Button btnGenerarInventario;
    @FXML private Button btnGenerarPersonalizado;  // reportes de empleados

    // ─── Contenedor de HISTORIAL (reportes viejos) ───────────────────────────
    @FXML private VBox contenedorHistorial;

    // ─── Contenedor de REPORTES RECIENTES ────────────────────────────────────
    @FXML private VBox contenedorRecientes;

    // ─── Separador que se muestra cuando hay historial ───────────────────────
    @FXML private Label lblTituloHistorial;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FILE  = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnGenerarVentas.setOnAction(e -> generarReporteVentas());
        btnGenerarInventario.setOnAction(e -> generarReporteInventario());
        btnGenerarPersonalizado.setOnAction(e -> generarReporteEmpleados());

        cargarReportesRecientes();
    }

    // =========================================================================
    //  GENERACIÓN DE REPORTES (crean CSV y guardan en tabla 'reporte')
    // =========================================================================

    @FXML
    public void generarReporteVentas() {
        LocalDate hoy    = LocalDate.now();
        LocalDate inicio = hoy.withDayOfMonth(1);          // 1° del mes actual

        String sql =
                "SELECT v.id_venta, DATE(v.fecha) AS fecha, " +
                        "  CONCAT(c.nombre,' ',IFNULL(c.apellido,'')) AS cliente, " +
                        "  v.total, IFNULL(p.estado,'PENDIENTE') AS estado_pago " +
                        "FROM venta v " +
                        "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                        "LEFT  JOIN pago   p ON v.id_venta   = p.id_venta " +
                        "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                        "ORDER BY v.fecha DESC";

        exportarCSV(sql, inicio, hoy,
                "reporte_ventas_" + FMT_FILE.format(LocalDateTime.now()) + ".csv",
                "Reporte de Ventas Mensual",
                "Ventas");
    }

    @FXML
    public void generarReporteInventario() {
        LocalDate hoy   = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);

        String sql =
                "SELECT p.id_producto, p.codigo_barras, p.nombre, p.precio, p.stock, " +
                        "  CASE WHEN p.stock = 0 THEN 'SIN STOCK' " +
                        "       WHEN p.stock < 5 THEN 'BAJO STOCK' " +
                        "       ELSE 'OK' END AS estado_stock " +
                        "FROM producto p " +
                        "LEFT JOIN movimiento_inventario mi ON p.id_producto = mi.id_producto " +
                        "  AND DATE(mi.fecha) BETWEEN ? AND ? " +
                        "GROUP BY p.id_producto, p.codigo_barras, p.nombre, p.precio, p.stock " +
                        "ORDER BY p.stock ASC";

        exportarCSV(sql, lunes, hoy,
                "reporte_inventario_" + FMT_FILE.format(LocalDateTime.now()) + ".csv",
                "Reporte de Inventario Semanal",
                "Inventario");
    }

    @FXML
    public void generarReporteEmpleados() {
        LocalDate hoy   = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);

        // Para empleados usamos el rango de fecha_inicio de la semana
        // (muestra todos los empleados; el filtro semanal aplica a movimientos)
        String sql =
                "SELECT u.id_usuario, u.nombre, u.email, r.nombre_rol AS rol, " +
                        "  u.area, u.tipo_contrato, u.telefono, u.fecha_inicio, u.salario_base " +
                        "FROM usuario_personal u " +
                        "LEFT JOIN rol r ON u.id_rol = r.id_rol " +
                        "ORDER BY u.area, u.nombre";

        exportarCSV(sql, lunes, hoy,
                "reporte_empleados_" + FMT_FILE.format(LocalDateTime.now()) + ".csv",
                "Reporte de Empleados Semanal",
                "Empleados");
    }

    // =========================================================================
    //  LÓGICA INTERNA: EXPORTAR CSV + GUARDAR EN BD
    // =========================================================================

    private void exportarCSV(String sql, LocalDate inicio, LocalDate fin,
                             String nombreArchivo, String tituloReporte, String tipoReporte) {

        // 1. Elegir carpeta de destino
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de destino");
        File carpeta = chooser.showDialog(obtenerStage());
        if (carpeta == null) return;   // usuario canceló

        File archivo = new File(carpeta, nombreArchivo);

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Sin conexión",
                        "No se pudo conectar a la base de datos.");
                return;
            }

            PreparedStatement ps = cn.prepareStatement(sql);
            // Solo pasamos los parámetros si la query los tiene (contiene '?')
            if (sql.contains("?")) {
                ps.setDate(1, Date.valueOf(inicio));
                ps.setDate(2, Date.valueOf(fin));
            }

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            try (PrintWriter pw = new PrintWriter(new FileWriter(archivo))) {
                // Encabezado
                List<String> headers = new ArrayList<>();
                for (int i = 1; i <= cols; i++) headers.add(meta.getColumnLabel(i));
                pw.println(String.join(",", headers));

                // Filas
                int filas = 0;
                while (rs.next()) {
                    List<String> valores = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) {
                        Object val = rs.getObject(i);
                        String celda = val == null ? "" : val.toString().replace(",", ";");
                        valores.add("\"" + celda + "\"");
                    }
                    pw.println(String.join(",", valores));
                    filas++;
                }

                // 2. Guardar registro en tabla reporte (id_usuario = 1 como genérico)
                guardarRegistroReporte(cn, tipoReporte);

                mostrarAlerta(Alert.AlertType.INFORMATION, "Reporte generado",
                        "Se exportaron " + filas + " registros.\nArchivo: " + archivo.getAbsolutePath());

                // 3. Refrescar la lista de recientes
                cargarReportesRecientes();
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al exportar",
                    "No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    private void guardarRegistroReporte(Connection cn, String tipo) {
        try {
            // Obtiene el primer usuario disponible como generador del reporte
            String sqlUser = "SELECT id_usuario FROM usuario_personal LIMIT 1";
            PreparedStatement ps0 = cn.prepareStatement(sqlUser);
            ResultSet rs0 = ps0.executeQuery();
            int idUsuario = rs0.next() ? rs0.getInt(1) : 1;

            String sqlInsert = "INSERT INTO reporte (id_usuario, tipo_reporte, fecha) VALUES (?, ?, NOW())";
            PreparedStatement ps = cn.prepareStatement(sqlInsert);
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ps.executeUpdate();
        } catch (Exception e) {
            // No bloquea el flujo si falla el registro
            System.err.println("Aviso: no se pudo registrar el reporte en BD: " + e.getMessage());
        }
    }

    // =========================================================================
    //  CARGAR REPORTES RECIENTES Y HISTORIAL DESDE LA BD
    // =========================================================================

    /**
     * Recientes:
     *   - Ventas    → creados este MES (fecha >= 1° mes actual)
     *   - Inventario → creados esta SEMANA (fecha >= lunes de esta semana)
     *   - Empleados  → creados esta SEMANA
     *
     * El resto va al contenedor de Historial.
     */
    private void cargarReportesRecientes() {
        if (contenedorRecientes == null || contenedorHistorial == null) return;

        contenedorRecientes.getChildren().clear();
        contenedorHistorial.getChildren().clear();

        LocalDate hoy   = LocalDate.now();
        LocalDate lunes = hoy.with(java.time.DayOfWeek.MONDAY);
        LocalDate primeroDeMes = hoy.withDayOfMonth(1);

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return;

            String sql =
                    "SELECT r.id_reporte, r.tipo_reporte, r.fecha " +
                            "FROM reporte r " +
                            "ORDER BY r.fecha DESC " +
                            "LIMIT 50";

            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String tipo  = rs.getString("tipo_reporte");
                Timestamp ts = rs.getTimestamp("fecha");
                LocalDate fechaRep = ts.toLocalDateTime().toLocalDate();

                boolean esReciente = false;
                if ("Ventas".equalsIgnoreCase(tipo) && !fechaRep.isBefore(primeroDeMes)) {
                    esReciente = true;
                } else if (("Inventario".equalsIgnoreCase(tipo) || "Empleados".equalsIgnoreCase(tipo))
                        && !fechaRep.isBefore(lunes)) {
                    esReciente = true;
                }

                HBox fila = crearFilaReporte(tipo, ts.toLocalDateTime(), esReciente);
                if (esReciente) {
                    contenedorRecientes.getChildren().add(fila);
                } else {
                    contenedorHistorial.getChildren().add(fila);
                }
            }

            // Mostrar/ocultar título del historial
            if (lblTituloHistorial != null) {
                boolean hayHistorial = !contenedorHistorial.getChildren().isEmpty();
                lblTituloHistorial.setVisible(hayHistorial);
                lblTituloHistorial.setManaged(hayHistorial);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Construye una fila de reporte visualmente igual a las que tiene el FXML original,
     * pero generada dinámicamente con los datos de la BD.
     */
    private HBox crearFilaReporte(String tipo, LocalDateTime fecha, boolean reciente) {
        HBox fila = new HBox(12);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        fila.getStyleClass().add("item-lista");
        fila.setPadding(new javafx.geometry.Insets(12, 0, 12, 0));

        // Información textual
        VBox info = new VBox(4);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        HBox cabecera = new HBox(8);
        cabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        String titulo;
        String badgeStyle;
        switch (tipo.toLowerCase()) {
            case "ventas" -> {
                titulo     = "Reporte de Ventas Mensual";
                badgeStyle = "badge-azul";
            }
            case "inventario" -> {
                titulo     = "Reporte de Inventario Semanal";
                badgeStyle = "badge-verde";
            }
            case "empleados" -> {
                titulo     = "Reporte de Empleados Semanal";
                badgeStyle = "badge-morada";
            }
            default -> {
                titulo     = "Reporte: " + tipo;
                badgeStyle = "badge-azul";
            }
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("texto-primario-bold");

        Label badge = new Label(tipo);
        badge.getStyleClass().add(badgeStyle);

        cabecera.getChildren().addAll(lblTitulo, badge);

        Label lblDesc = new Label(obtenerDescripcion(tipo));
        lblDesc.getStyleClass().add("texto-secundario");

        Label lblFecha = new Label(fecha.format(FMT_FECHA));
        lblFecha.getStyleClass().add("texto-fecha");

        info.getChildren().addAll(cabecera, lblDesc, lblFecha);

        // Botón Descargar (re-genera el reporte en el momento)
        Button btnDescargar = new Button("Descargar");
        btnDescargar.getStyleClass().add("boton-descargar");
        btnDescargar.setOnAction(e -> descargarReporte(tipo));

        fila.getChildren().addAll(info, btnDescargar);
        return fila;
    }

    private String obtenerDescripcion(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "ventas"      -> "Ventas del mes con desglose por producto y cliente";
            case "inventario"  -> "Estado del inventario y productos con bajo stock (semana actual)";
            case "empleados"   -> "Listado de empleados activos con área y contrato (semana actual)";
            default            -> "Reporte generado por el sistema";
        };
    }

    /** Re-descarga el reporte del tipo indicado usando el rango vigente. */
    private void descargarReporte(String tipo) {
        switch (tipo.toLowerCase()) {
            case "ventas"     -> generarReporteVentas();
            case "inventario" -> generarReporteInventario();
            case "empleados"  -> generarReporteEmpleados();
            default           -> mostrarAlerta(Alert.AlertType.WARNING,
                    "Tipo no soportado", "No se puede re-descargar el tipo: " + tipo);
        }
    }

    // =========================================================================
    //  UTILIDADES
    // =========================================================================

    private Stage obtenerStage() {
        if (btnGenerarVentas != null && btnGenerarVentas.getScene() != null) {
            return (Stage) btnGenerarVentas.getScene().getWindow();
        }
        return null;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}