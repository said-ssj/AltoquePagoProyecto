package com.controlador;

import com.dao.ConfiguracionDAO;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.modelo.Configuracion;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.print.PrintServiceLookup;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ControladorConfiguracion implements Initializable {

    // ── Datos de la empresa ───────────────────────────────────────
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRuc;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;

    // ── Impresión ─────────────────────────────────────────────────
    @FXML private ComboBox<String> cbImpresoras;
    @FXML private ComboBox<String> cbTamañoPapel;
    @FXML private TextArea         txtMensajeTicket;

    // ── Preferencias kiosko ───────────────────────────────────────
    @FXML private CheckBox chkSonidoEscaner;
    @FXML private CheckBox chkModoOscuro;
    @FXML private CheckBox chkImpresionAutomatica;

    private final ConfiguracionDAO dao = new ConfiguracionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Detectar impresoras reales del sistema
        cbImpresoras.getItems().add("Ninguna");
        try {
            for (var srv : PrintServiceLookup.lookupPrintServices(null, null)) {
                cbImpresoras.getItems().add(srv.getName());
            }
        } catch (Exception ignored) {}
        // Fallback por si no hay impresoras detectadas
        if (cbImpresoras.getItems().size() == 1) {
            cbImpresoras.getItems().addAll("EPSON TM-T20III Receipt", "POS-80C Thermal Printer");
        }

        cbTamañoPapel.getItems().addAll("58mm", "80mm");

        cargarConfiguracionActual();
    }

    // ============================================================
    //  CARGAR CONFIGURACIÓN DESDE EL .properties
    // ============================================================
    private void cargarConfiguracionActual() {
        Configuracion config = dao.cargarConfiguracion();

        txtRazonSocial.setText(config.getRazonSocial() != null ? config.getRazonSocial() : "");
        txtRuc        .setText(config.getRuc()         != null ? config.getRuc()         : "");
        txtDireccion  .setText(config.getDireccion()   != null ? config.getDireccion()   : "");
        txtTelefono   .setText(config.getTelefono()    != null ? config.getTelefono()    : "");

        String imp = config.getImpresora();
        if (imp != null && cbImpresoras.getItems().contains(imp)) cbImpresoras.setValue(imp);
        else cbImpresoras.getSelectionModel().selectFirst();

        String papel = config.getTamañoPapel();
        cbTamañoPapel.setValue(papel != null ? papel : "80mm");

        txtMensajeTicket.setText(config.getMensajeTicket() != null ? config.getMensajeTicket() : "");

        chkSonidoEscaner     .setSelected(config.isSonidoEscaner());
        chkModoOscuro        .setSelected(config.isModoOscuro());
        chkImpresionAutomatica.setSelected(config.isImpresionAuto());
    }

    // ============================================================
    //  GUARDAR CONFIGURACIÓN EN .properties Y CONFIRMAR
    // ============================================================
    @FXML
    public void guardarConfiguracion() {
        String razonSocial = txtRazonSocial.getText().trim();
        String ruc         = txtRuc.getText().trim();

        if (razonSocial.isEmpty() || ruc.isEmpty()) {
            alerta("La Razón Social y el RUC son obligatorios."); return;
        }

        Configuracion config = new Configuracion();
        config.setRazonSocial(razonSocial);
        config.setRuc(ruc);
        config.setDireccion(txtDireccion.getText().trim());
        config.setTelefono(txtTelefono.getText().trim());
        config.setImpresora(cbImpresoras.getValue());
        config.setTamañoPapel(cbTamañoPapel.getValue());
        config.setMensajeTicket(txtMensajeTicket.getText().trim());
        config.setSonidoEscaner(chkSonidoEscaner.isSelected());
        config.setModoOscuro(chkModoOscuro.isSelected());
        config.setImpresionAuto(chkImpresionAutomatica.isSelected());

        if (dao.guardarConfiguracion(config)) {
            info("Configuración guardada correctamente.\nLos cambios se aplicarán en el próximo comprobante.");
        } else {
            alerta("No se pudo guardar la configuración.");
        }
    }

    // ============================================================
    //  DESCARGAR COMPROBANTE DE PRUEBA (PDF) CON LOS DATOS ACTUALES
    // ============================================================
    @FXML
    public void descargarComprobante() {
        String razonSocial = txtRazonSocial.getText().trim();
        String ruc         = txtRuc.getText().trim();
        String direccion   = txtDireccion.getText().trim();
        String telefono    = txtTelefono.getText().trim();
        String mensaje     = txtMensajeTicket.getText().trim();

        if (razonSocial.isEmpty()) {
            alerta("Ingresa la Razón Social antes de descargar el comprobante."); return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar comprobante de prueba");
        fc.setInitialFileName("comprobante_prueba_" +
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmm").format(LocalDateTime.now()) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File archivo = fc.showSaveDialog(obtenerStage());
        if (archivo == null) return;

        try {
            generarComprobantePDF(archivo, razonSocial, ruc, direccion, telefono, mensaje);
            info("Comprobante generado:\n" + archivo.getAbsolutePath());
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(archivo);
        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error al generar el comprobante:\n" + e.getMessage());
        }
    }

    private void generarComprobantePDF(File archivo, String razonSocial, String ruc,
                                       String direccion, String telefono, String mensaje) throws Exception {
        Document doc = new Document(new Rectangle(226, 600), 15, 15, 20, 20); // ~80mm
        PdfWriter.getInstance(doc, new FileOutputStream(archivo));
        doc.open();

        Color azul = new Color(37, 99, 235);

        Font fEmpresa  = new Font(Font.HELVETICA, 14, Font.BOLD,   azul);
        Font fTitulo   = new Font(Font.HELVETICA, 11, Font.BOLD,   Color.DARK_GRAY);
        Font fNormal   = new Font(Font.HELVETICA, 9,  Font.NORMAL, Color.DARK_GRAY);
        Font fPie      = new Font(Font.HELVETICA, 8,  Font.ITALIC, Color.GRAY);

        // ── Cabecera ──────────────────────────────────────────────
        Paragraph pEmpresa = new Paragraph(razonSocial, fEmpresa);
        pEmpresa.setAlignment(Element.ALIGN_CENTER);
        pEmpresa.setSpacingAfter(3);
        doc.add(pEmpresa);

        if (!ruc.isEmpty()) {
            Paragraph pRuc = new Paragraph("RUC: " + ruc, fNormal);
            pRuc.setAlignment(Element.ALIGN_CENTER);
            doc.add(pRuc);
        }
        if (!direccion.isEmpty()) {
            Paragraph pDir = new Paragraph(direccion, fNormal);
            pDir.setAlignment(Element.ALIGN_CENTER);
            doc.add(pDir);
        }
        if (!telefono.isEmpty()) {
            Paragraph pTel = new Paragraph("Tel: " + telefono, fNormal);
            pTel.setAlignment(Element.ALIGN_CENTER);
            pTel.setSpacingAfter(8);
            doc.add(pTel);
        }

        // ── Línea separadora ──────────────────────────────────────
        doc.add(new Paragraph("─────────────────────────────", fNormal));

        // ── Tipo y número ─────────────────────────────────────────
        Paragraph pTipo = new Paragraph("BOLETA DE VENTA", fTitulo);
        pTipo.setAlignment(Element.ALIGN_CENTER);
        doc.add(pTipo);
        Paragraph pNum = new Paragraph("B001-00001", fNormal);
        pNum.setAlignment(Element.ALIGN_CENTER);
        pNum.setSpacingAfter(6);
        doc.add(pNum);

        doc.add(new Paragraph("Fecha: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now()), fNormal));
        doc.add(new Paragraph("Cliente: Consumidor Final", fNormal));
        doc.add(new Paragraph("─────────────────────────────", fNormal));

        // ── Tabla de productos de muestra ─────────────────────────
        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{5f, 2f, 3f});

        for (String h : new String[]{"Producto", "Cant.", "Subtotal"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 8, Font.BOLD)));
            c.setBorder(Rectangle.BOTTOM);
            c.setPadding(3);
            tabla.addCell(c);
        }
        String[][] items = {
                {"Producto Ejemplo A", "2", "S/ 10.00"},
                {"Producto Ejemplo B", "1", "S/ 5.50"}
        };
        for (String[] row : items) {
            for (String cell : row) {
                PdfPCell c = new PdfPCell(new Phrase(cell, fNormal));
                c.setBorder(Rectangle.NO_BORDER);
                c.setPadding(3);
                tabla.addCell(c);
            }
        }
        doc.add(tabla);
        doc.add(new Paragraph("─────────────────────────────", fNormal));

        // ── Totales ───────────────────────────────────────────────
        doc.add(new Paragraph("Subtotal:     S/ 15.50", fNormal));
        doc.add(new Paragraph("IGV (18%):    S/  2.79", fNormal));
        Paragraph pTotal = new Paragraph("TOTAL:        S/ 18.29", fTitulo);
        pTotal.setSpacingAfter(8);
        doc.add(pTotal);

        doc.add(new Paragraph("─────────────────────────────", fNormal));

        // ── Pie de comprobante (mensaje de configuración) ─────────
        if (!mensaje.isEmpty()) {
            Paragraph pMensaje = new Paragraph(mensaje, fPie);
            pMensaje.setAlignment(Element.ALIGN_CENTER);
            pMensaje.setSpacingBefore(6);
            doc.add(pMensaje);
        }

        doc.close();
    }

    @FXML
    public void restablecerValores() {
        cargarConfiguracionActual();
        info("Cambios descartados. Se restauró la configuración guardada.");
    }

    private Stage obtenerStage() {
        if (txtRazonSocial != null && txtRazonSocial.getScene() != null)
            return (Stage) txtRazonSocial.getScene().getWindow();
        return null;
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
}
