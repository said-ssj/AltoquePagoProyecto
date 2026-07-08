package com.controlador;

import com.DB.ConexionDB;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorReportes implements Initializable {

    @FXML private Button btnGenerarVentas;
    @FXML private Button btnGenerarInventario;
    @FXML private Button btnGenerarPersonalizado;   // Empleados

    @FXML private VBox contenedorHistorial;
    @FXML private VBox contenedorRecientes;
    @FXML private Label lblTituloHistorial;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_FILE  = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter FMT_TITULO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores de marca para encabezados de tabla en el PDF
    private static final Color COLOR_VENTAS     = new Color(37, 99, 235);   // azul
    private static final Color COLOR_INVENTARIO = new Color(22, 163, 74);   // verde
    private static final Color COLOR_EMPLEADOS  = new Color(147, 51, 234);  // morado

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnGenerarVentas.setOnAction(e -> iniciarGeneracion("Ventas"));
        btnGenerarInventario.setOnAction(e -> iniciarGeneracion("Inventario"));
        btnGenerarPersonalizado.setOnAction(e -> iniciarGeneracion("Empleados"));

        cargarReportesRecientes();
    }

    // ============================================================
    //  PASO 1: DIÁLOGO PARA ELEGIR EL PERIODO (Mensual / Semanal / Personalizado)
    // ============================================================
    private void iniciarGeneracion(String tipoReporte) {
        Dialog<Object[]> dialog = new Dialog<>();
        dialog.setTitle("Generar Reporte de " + tipoReporte);
        dialog.setHeaderText("Selecciona el periodo del reporte");

        ButtonType btnContinuar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnContinuar, ButtonType.CANCEL);

        ToggleGroup grupo = new ToggleGroup();
        RadioButton rbMensual = new RadioButton("Mensual (este mes)");
        RadioButton rbSemanal = new RadioButton("Semanal (esta semana)");
        RadioButton rbPersonalizado = new RadioButton("Personalizado (elige un rango de fechas, incluye periodos antiguos)");
        rbMensual.setToggleGroup(grupo);
        rbSemanal.setToggleGroup(grupo);
        rbPersonalizado.setToggleGroup(grupo);

        // Por defecto: Ventas → mensual, Inventario/Empleados → semanal
        if ("Ventas".equals(tipoReporte)) rbMensual.setSelected(true);
        else rbSemanal.setSelected(true);

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusMonths(1));
        DatePicker dpFin = new DatePicker(LocalDate.now());
        HBox filaFechas = new HBox(10, new Label("Desde:"), dpInicio, new Label("Hasta:"), dpFin);
        filaFechas.setDisable(true); // solo se habilita si eligen "Personalizado"

        rbPersonalizado.selectedProperty().addListener((obs, viejo, seleccionado) -> filaFechas.setDisable(!seleccionado));

        VBox caja = new VBox(12, rbMensual, rbSemanal, rbPersonalizado, filaFechas);
        caja.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(caja);

        // Validación: si eligen Personalizado, ambas fechas deben existir y "Desde" no puede ser posterior a "Hasta"
        Node botonOk = dialog.getDialogPane().lookupButton(btnContinuar);
        botonOk.addEventFilter(javafx.event.ActionEvent.ACTION, evento -> {
            if (rbPersonalizado.isSelected()) {
                LocalDate ini = dpInicio.getValue();
                LocalDate fin = dpFin.getValue();
                if (ini == null || fin == null || ini.isAfter(fin)) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Rango inválido",
                            "Selecciona una fecha 'Desde' y 'Hasta' válidas (Desde no puede ser posterior a Hasta).");
                    evento.consume();
                }
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != btnContinuar) return null;
            if (rbPersonalizado.isSelected()) {
                return new Object[]{"PERSONALIZADO", dpInicio.getValue(), dpFin.getValue()};
            }
            String periodo = rbMensual.isSelected() ? "MENSUAL" : "SEMANAL";
            return new Object[]{periodo, null, null};
        });

        Optional<Object[]> resultado = dialog.showAndWait();
        resultado.ifPresent(datos -> generarReporte(
                tipoReporte,
                (String) datos[0],
                (LocalDate) datos[1],
                (LocalDate) datos[2]
        ));
    }

    // ============================================================
    //  PASO 2: GENERAR EL REPORTE SEGÚN TIPO Y PERIODO ELEGIDO
    // ============================================================
    private void generarReporte(String tipoReporte, String periodo, LocalDate inicioPersonalizado, LocalDate finPersonalizado) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio;
        LocalDate fin;

        switch (periodo) {
            case "PERSONALIZADO" -> {
                inicio = inicioPersonalizado;
                fin = finPersonalizado;
            }
            case "MENSUAL" -> {
                inicio = hoy.withDayOfMonth(1);
                fin = hoy;
            }
            default -> { // SEMANAL
                inicio = hoy.with(java.time.DayOfWeek.MONDAY);
                fin = hoy;
            }
        }

        String sql;
        String titulo;
        Color colorTema;

        String etiquetaPeriodo = switch (periodo) {
            case "MENSUAL" -> "Mensual";
            case "SEMANAL" -> "Semanal";
            default -> "Personalizado";
        };

        switch (tipoReporte) {
            case "Ventas" -> {
                sql =
                        "SELECT v.id_venta AS 'ID', DATE(v.fecha) AS 'Fecha', " +
                                "  CONCAT(c.nombre,' ',IFNULL(c.apellido,'')) AS 'Cliente', " +
                                "  v.total AS 'Total (S/)', IFNULL(p.estado,'PENDIENTE') AS 'Estado' " +
                                "FROM venta v " +
                                "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                                "LEFT JOIN pago p ON v.id_venta = p.id_venta " +
                                "WHERE DATE(v.fecha) BETWEEN ? AND ? " +
                                "ORDER BY v.fecha DESC";
                titulo = "Reporte de Ventas " + etiquetaPeriodo;
                colorTema = COLOR_VENTAS;
            }
            case "Inventario" -> {
                sql =
                        "SELECT p.id_producto AS 'ID', p.codigo_barras AS 'Código', p.nombre AS 'Producto', " +
                                "  p.precio AS 'Precio (S/)', p.stock AS 'Stock', " +
                                "  CASE WHEN p.stock = 0 THEN 'SIN STOCK' " +
                                "       WHEN p.stock < 5 THEN 'BAJO STOCK' " +
                                "       ELSE 'OK' END AS 'Estado' " +
                                "FROM producto p " +
                                "LEFT JOIN movimiento_inventario mi ON p.id_producto = mi.id_producto " +
                                "  AND DATE(mi.fecha) BETWEEN ? AND ? " +
                                "GROUP BY p.id_producto, p.codigo_barras, p.nombre, p.precio, p.stock " +
                                "ORDER BY p.stock ASC";
                titulo = "Reporte de Inventario " + etiquetaPeriodo;
                colorTema = COLOR_INVENTARIO;
            }
            case "Empleados" -> {
                sql =
                        "SELECT u.id_usuario AS 'ID', u.nombre AS 'Nombre', u.email AS 'Email', " +
                                "  r.nombre_rol AS 'Rol', u.area AS 'Área', u.tipo_contrato AS 'Contrato', " +
                                "  u.telefono AS 'Teléfono' " +
                                "FROM usuario_personal u " +
                                "LEFT JOIN rol r ON u.id_rol = r.id_rol " +
                                "ORDER BY u.area, u.nombre";
                titulo = "Reporte de Empleados " + etiquetaPeriodo;
                colorTema = COLOR_EMPLEADOS;
            }
            default -> { return; }
        }

        String nombreArchivo = "reporte_" + tipoReporte.toLowerCase() + "_" +
                FMT_FILE.format(LocalDateTime.now()) + ".pdf";

        exportarPDF(sql, inicio, fin, nombreArchivo, titulo, tipoReporte, periodo, colorTema);
    }

    // ============================================================
    //  GENERAR EL ARCHIVO PDF CON TÍTULO + TABLA
    // ============================================================
    private void exportarPDF(String sql, LocalDate inicio, LocalDate fin,
                             String nombreArchivo, String tituloReporte,
                             String tipoReporte, String periodo, Color colorTema) {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de destino");
        File carpeta = chooser.showDialog(obtenerStage());
        if (carpeta == null) return;

        File archivo = new File(carpeta, nombreArchivo);

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Sin conexión", "No se pudo conectar a la base de datos.");
                return;
            }

            PreparedStatement ps = cn.prepareStatement(sql);
            if (sql.contains("?")) {
                ps.setDate(1, Date.valueOf(inicio));
                ps.setDate(2, Date.valueOf(fin));
            }

            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= cols; i++) headers.add(meta.getColumnLabel(i));

            List<String[]> filas = new ArrayList<>();
            while (rs.next()) {
                String[] fila = new String[cols];
                for (int i = 1; i <= cols; i++) {
                    Object val = rs.getObject(i);
                    fila[i - 1] = (val == null) ? "" : val.toString();
                }
                filas.add(fila);
            }

            construirPDF(archivo, tituloReporte, headers, filas, inicio, fin, colorTema);

            guardarRegistroReporte(cn, tipoReporte);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Reporte generado",
                    "Se exportaron " + filas.size() + " registros.\nArchivo: " + archivo.getAbsolutePath());

            cargarReportesRecientes();

            // Abrir el PDF automáticamente si el sistema lo soporta
            try {
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(archivo);
            } catch (Exception ignored) { }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error al exportar",
                    "No se pudo generar el reporte:\n" + e.getMessage());
        }
    }

    /** Construye el documento PDF con título dinámico, rango de fechas y tabla de datos. */
    private void construirPDF(File archivo, String titulo, List<String> headers,
                              List<String[]> filas, LocalDate inicio, LocalDate fin,
                              Color colorTema) throws Exception {

        Document documento = new Document(PageSize.A4.rotate(), 30, 30, 40, 30);
        PdfWriter.getInstance(documento, new FileOutputStream(archivo));
        documento.open();

        Font fontTitulo  = new Font(Font.HELVETICA, 20, Font.BOLD, colorTema);
        Font fontSub     = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY);
        Font fontHeader  = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font fontCelda   = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);

        // ── Título dinámico ──────────────────────────────────────
        Paragraph pTitulo = new Paragraph(titulo, fontTitulo);
        pTitulo.setAlignment(Element.ALIGN_LEFT);
        pTitulo.setSpacingAfter(4);
        documento.add(pTitulo);

        // ── Subtítulo: rango de fechas + fecha de generación ─────
        String rango = "Periodo: " + FMT_TITULO.format(inicio) + " — " + FMT_TITULO.format(fin);
        String generado = "Generado el: " + FMT_FECHA.format(LocalDateTime.now());
        Paragraph pSub = new Paragraph(rango + "      |      " + generado, fontSub);
        pSub.setSpacingAfter(16);
        documento.add(pSub);

        // ── Tabla ─────────────────────────────────────────────────
        if (filas.isEmpty()) {
            Paragraph vacio = new Paragraph("No se encontraron registros para el periodo seleccionado.",
                    new Font(Font.HELVETICA, 11, Font.ITALIC, Color.GRAY));
            documento.add(vacio);
        } else {
            int numCols = headers.size();
            PdfPTable tabla = new PdfPTable(numCols);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(5);

            // Encabezados
            for (String h : headers) {
                PdfPCell celda = new PdfPCell(new Phrase(h, fontHeader));
                celda.setBackgroundColor(colorTema);
                celda.setPadding(6);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabla.addCell(celda);
            }

            // Filas con color alterno
            boolean alterna = false;
            for (String[] fila : filas) {
                Color fondo = alterna ? new Color(245, 247, 250) : Color.WHITE;
                for (String valor : fila) {
                    PdfPCell celda = new PdfPCell(new Phrase(valor, fontCelda));
                    celda.setBackgroundColor(fondo);
                    celda.setPadding(5);
                    tabla.addCell(celda);
                }
                alterna = !alterna;
            }

            documento.add(tabla);

            // Pie con total de registros
            Paragraph pie = new Paragraph("\nTotal de registros: " + filas.size(),
                    new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY));
            documento.add(pie);
        }

        documento.close();
    }

    private void guardarRegistroReporte(Connection cn, String tipo) {
        try {
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
            System.err.println("Aviso: no se pudo registrar el reporte en BD: " + e.getMessage());
        }
    }

    // ============================================================
    //  CARGAR REPORTES RECIENTES Y HISTORIAL DESDE LA BD
    // ============================================================
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

                HBox fila = crearFilaReporte(tipo, ts.toLocalDateTime());
                if (esReciente) contenedorRecientes.getChildren().add(fila);
                else contenedorHistorial.getChildren().add(fila);
            }

            if (lblTituloHistorial != null) {
                boolean hayHistorial = !contenedorHistorial.getChildren().isEmpty();
                lblTituloHistorial.setVisible(hayHistorial);
                lblTituloHistorial.setManaged(hayHistorial);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox crearFilaReporte(String tipo, LocalDateTime fecha) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("item-lista");
        fila.setPadding(new Insets(12, 0, 12, 0));

        VBox info = new VBox(4);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        HBox cabecera = new HBox(8);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        String titulo;
        String badgeStyle;
        switch (tipo.toLowerCase()) {
            case "ventas" -> { titulo = "Reporte de Ventas";     badgeStyle = "badge-azul"; }
            case "inventario" -> { titulo = "Reporte de Inventario"; badgeStyle = "badge-verde"; }
            case "empleados" -> { titulo = "Reporte de Empleados";  badgeStyle = "badge-morada"; }
            default -> { titulo = "Reporte: " + tipo; badgeStyle = "badge-azul"; }
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("texto-primario-bold");

        Label badge = new Label(tipo + " · PDF");
        badge.getStyleClass().add(badgeStyle);

        cabecera.getChildren().addAll(lblTitulo, badge);

        Label lblDesc = new Label(obtenerDescripcion(tipo));
        lblDesc.getStyleClass().add("texto-secundario");

        Label lblFecha = new Label(fecha.format(FMT_FECHA));
        lblFecha.getStyleClass().add("texto-fecha");

        info.getChildren().addAll(cabecera, lblDesc, lblFecha);

        Button btnDescargar = new Button("Descargar");
        btnDescargar.getStyleClass().add("boton-descargar");
        btnDescargar.setOnAction(e -> iniciarGeneracion(normalizarTipo(tipo)));

        fila.getChildren().addAll(info, btnDescargar);
        return fila;
    }

    private String normalizarTipo(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "ventas" -> "Ventas";
            case "inventario" -> "Inventario";
            case "empleados" -> "Empleados";
            default -> tipo;
        };
    }

    private String obtenerDescripcion(String tipo) {
        return switch (tipo.toLowerCase()) {
            case "ventas"      -> "Ventas con desglose por producto y cliente (PDF)";
            case "inventario"  -> "Estado del inventario y productos con bajo stock (PDF)";
            case "empleados"   -> "Listado de empleados con área y contrato (PDF)";
            default            -> "Reporte generado por el sistema";
        };
    }

    // ============================================================
    //  UTILIDADES
    // ============================================================
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
