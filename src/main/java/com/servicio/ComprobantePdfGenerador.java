/*
 * Nos encargamos exclusivamente del diseño, estructuración y renderizado de los archivos
 * PDF utilizando la librería de bajo nivel iText. Al aislar esta lógica, evitamos mezclar
 * el formateo de datos con la gestión de hardware de impresión, respetando el Principio
 * de Responsabilidad Única (SRP).
 */
package com.servicio;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.dao.ConfiguracionDAO;
import com.modelo.Configuracion;
import com.modelo.Venta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.awt.Color;

public class ComprobantePdfGenerador implements IComprobanteGenerador {

    private static final Logger log = LoggerFactory.getLogger(ComprobantePdfGenerador.class);
    private final ConfiguracionDAO configuracionDAO;
    private final ImpresionHardwareServicio impresionHardware;

    public ComprobantePdfGenerador() {
        this.configuracionDAO = new ConfiguracionDAO();
        // Aquí corregimos el enlace: instanciamos directamente nuestro servicio de hardware aislado
        this.impresionHardware = new ImpresionHardwareServicio();
    }

    @Override
    public void emitirTicketKiosko(Venta venta) {
        String tipoDocVenta = (venta.getEstado() != null) ? venta.getEstado().toUpperCase() : "BOLETA";
        boolean esFactura = tipoDocVenta.equals("FACTURA");
        String prefijoArchivo = esFactura ? "FacturaTicket_" : "BoletaTicket_";
        String serie = esFactura ? "F001-" : "B001-";
        String tituloDocumento = esFactura ? "FACTURA DE VENTA ELECTRONICA" : "BOLETA DE VENTA ELECTRONICA";

        String rutaArchivo = prepararDirectorio() + File.separator + prefijoArchivo + venta.getId() + ".pdf";
        Rectangle pageSize = new Rectangle(226, 800);
        Document documento = new Document(pageSize, 10, 10, 15, 15);

        try {
            Configuracion config = configuracionDAO.cargarConfiguracion();
            String nombreEmpresa = (config != null && config.getRazonSocial() != null && !config.getRazonSocial().isEmpty()) ? config.getRazonSocial() : "FERRETERÍA / MINIMARKET HUAMAN";
            String rucEmpresa = (config != null && config.getRuc() != null && !config.getRuc().isEmpty()) ? config.getRuc() : "10214666087";
            String direccionEmpresa = (config != null && config.getDireccion() != null && !config.getDireccion().isEmpty()) ? config.getDireccion() : "Av. Victorio Gotuzzo 799";

            PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
            documento.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 8);

            Paragraph titulo = new Paragraph(nombreEmpresa.toUpperCase(), fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph ruc = new Paragraph("RUC: " + rucEmpresa, fontNormal);
            ruc.setAlignment(Element.ALIGN_CENTER);
            documento.add(ruc);

            Paragraph dir = new Paragraph(direccionEmpresa, fontNormal);
            dir.setAlignment(Element.ALIGN_CENTER);
            documento.add(dir);

            Paragraph tipoDoc = new Paragraph("\n" + tituloDocumento, fontNegrita);
            tipoDoc.setAlignment(Element.ALIGN_CENTER);
            documento.add(tipoDoc);

            String correlativo = String.format("%08d", Integer.parseInt(venta.getId()));
            Paragraph numDoc = new Paragraph(serie + correlativo, fontTitulo);
            numDoc.setAlignment(Element.ALIGN_CENTER);
            documento.add(numDoc);

            documento.add(new Paragraph("--------------------------------------------------", fontNormal));
            documento.add(new Paragraph("Fecha: " + venta.getFecha(), fontNormal));
            documento.add(new Paragraph(obtenerEtiquetaAtendidoPor(venta), fontNormal));
            documento.add(new Paragraph((esFactura ? "Razón Social: " : "Cliente: ") + (venta.getCliente() != null ? venta.getCliente() : "CLIENTE VARIOS"), fontNormal));
            documento.add(new Paragraph("Forma Pago: " + (venta.getMetodoPago() != null ? venta.getMetodoPago() : "Contado"), fontNormal));
            documento.add(new Paragraph("--------------------------------------------------", fontNormal));

            PdfPTable tablaItems = new PdfPTable(4);
            tablaItems.setWidthPercentage(100);
            tablaItems.setWidths(new float[]{4.5f, 1f, 1.5f, 1.5f});
            String[] cabeceras = {"DESCRIPCION", "CANT", "P.UNIT", "TOTAL"};

            for (String cab : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(cab, fontNegrita));
                cell.setBorder(Rectangle.NO_BORDER);
                if (!cab.equals("DESCRIPCION")) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaItems.addCell(cell);
            }

            if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
                for (com.modelo.DetalleVenta item : venta.getDetalles()) {
                    PdfPCell cDesc = new PdfPCell(new Phrase(item.getNombreProducto(), fontNormal));
                    cDesc.setBorder(Rectangle.NO_BORDER);
                    tablaItems.addCell(cDesc);

                    PdfPCell cCant = new PdfPCell(new Phrase(String.valueOf(item.getCantidad()), fontNormal));
                    cCant.setBorder(Rectangle.NO_BORDER);
                    cCant.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tablaItems.addCell(cCant);

                    PdfPCell cPU = new PdfPCell(new Phrase(String.format("%.2f", item.getPrecioUnitario()), fontNormal));
                    cPU.setBorder(Rectangle.NO_BORDER);
                    cPU.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tablaItems.addCell(cPU);

                    PdfPCell cTotal = new PdfPCell(new Phrase(String.format("%.2f", item.getSubtotal()), fontNormal));
                    cTotal.setBorder(Rectangle.NO_BORDER);
                    cTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tablaItems.addCell(cTotal);
                }
            } else {
                PdfPCell cDesc = new PdfPCell(new Phrase("Artículos varios", fontNormal));
                cDesc.setBorder(Rectangle.NO_BORDER);
                tablaItems.addCell(cDesc);

                PdfPCell v1 = new PdfPCell(new Phrase(String.valueOf(venta.getProductos()), fontNormal));
                v1.setBorder(Rectangle.NO_BORDER);
                v1.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaItems.addCell(v1);

                PdfPCell v2 = new PdfPCell(new Phrase("-", fontNormal));
                v2.setBorder(Rectangle.NO_BORDER);
                v2.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaItems.addCell(v2);

                PdfPCell v3 = new PdfPCell(new Phrase(String.format("%.2f", venta.getTotal()), fontNormal));
                v3.setBorder(Rectangle.NO_BORDER);
                v3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tablaItems.addCell(v3);
            }

            documento.add(tablaItems);
            documento.add(new Paragraph("--------------------------------------------------", fontNormal));

            double totalPagar = venta.getTotal();
            double subtotal = totalPagar / 1.18;
            double igv = totalPagar - subtotal;

            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(100);
            tablaTotales.setWidths(new float[]{3f, 1f});

            agregarFilaTotalTicket(tablaTotales, "OP. GRAVADAS: S/", subtotal, fontNormal);
            agregarFilaTotalTicket(tablaTotales, "OP. EXONERADAS: S/", 0.00, fontNormal);
            agregarFilaTotalTicket(tablaTotales, "I.G.V.: S/", igv, fontNormal);
            agregarFilaTotalTicket(tablaTotales, "IMPORTE TOTAL: S/", totalPagar, fontTitulo);
            documento.add(tablaTotales);

            Paragraph mensajeCierre = new Paragraph("\n¡Gracias por su compra!", fontNormal);
            mensajeCierre.setAlignment(Element.ALIGN_CENTER);
            documento.add(mensajeCierre);

            documento.close();
            log.info("Ticket PDF guardado de forma local en: {}", rutaArchivo);

            // Ahora sí llamamos exclusivamente a nuestro manejador físico aislado
            impresionHardware.enviarColaImpresoraFisica(rutaArchivo);

        } catch (Exception e) {
            log.error("Error fatal al estructurar boleta de 80mm: {}", e.getMessage(), e);
        }
    }

    @Override
    public void emitirFormatoA4(Venta venta) {
        String tipoDocVenta = (venta.getEstado() != null) ? venta.getEstado().toUpperCase() : "BOLETA";
        boolean esFactura = tipoDocVenta.equals("FACTURA");
        String prefijoArchivo = esFactura ? "Factura_" : "Boleta_";
        String serie = esFactura ? "F001-" : "B001-";
        String tituloDocumento = esFactura ? "FACTURA DE VENTA ELECTRONICA" : "BOLETA DE VENTA ELECTRONICA";

        String rutaArchivo = prepararDirectorio() + File.separator + prefijoArchivo + venta.getId() + ".pdf";
        Document documento = new Document(PageSize.A4, 30, 30, 30, 30);

        try {
            Configuracion config = configuracionDAO.cargarConfiguracion();
            String nombreEmpresa = (config != null && config.getRazonSocial() != null && !config.getRazonSocial().isEmpty()) ? config.getRazonSocial() : "FERRETERÍA / MINIMARKET HUAMAN";
            String rucEmpresa = (config != null && config.getRuc() != null && !config.getRuc().isEmpty()) ? config.getRuc() : "10214666087";
            String direccionEmpresa = (config != null && config.getDireccion() != null && !config.getDireccion().isEmpty()) ? config.getDireccion() : "Av. Victorio Gotuzzo 799";

            PdfWriter.getInstance(documento, new FileOutputStream(rutaArchivo));
            documento.open();

            Font fontTituloDoc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontPequena = FontFactory.getFont(FontFactory.HELVETICA, 8);

            PdfPTable tableHeader = new PdfPTable(2);
            tableHeader.setWidthPercentage(100);
            tableHeader.setWidths(new int[]{60, 40});

            PdfPCell cellIzq = new PdfPCell();
            cellIzq.setBorder(Rectangle.NO_BORDER);
            cellIzq.addElement(new Paragraph(nombreEmpresa, fontNegrita));
            cellIzq.addElement(new Paragraph(direccionEmpresa, fontNormal));
            tableHeader.addCell(cellIzq);

            PdfPTable tableRuc = new PdfPTable(1);
            tableRuc.setWidthPercentage(100);

            PdfPCell cRuc = new PdfPCell(new Phrase("RUC: " + rucEmpresa, fontTituloDoc));
            cRuc.setHorizontalAlignment(Element.ALIGN_CENTER);
            cRuc.setPadding(8);
            tableRuc.addCell(cRuc);

            PdfPCell cTipo = new PdfPCell(new Phrase(tituloDocumento, fontNegrita));
            cTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
            cTipo.setPadding(8);
            tableRuc.addCell(cTipo);

            String correlativo = String.format("%08d", Integer.parseInt(venta.getId()));
            PdfPCell cNum = new PdfPCell(new Phrase(serie + correlativo, fontTituloDoc));
            cNum.setHorizontalAlignment(Element.ALIGN_CENTER);
            cNum.setPadding(8);
            tableRuc.addCell(cNum);

            PdfPCell cellDer = new PdfPCell(tableRuc);
            cellDer.setBorder(Rectangle.NO_BORDER);
            tableHeader.addCell(cellDer);
            documento.add(tableHeader);

            documento.add(new Paragraph(" "));

            PdfPTable tableCliente = new PdfPTable(4);
            tableCliente.setWidthPercentage(100);
            tableCliente.setWidths(new float[]{1.5f, 4f, 1.5f, 3f});
            tableCliente.setSpacingAfter(10f);

            agregarCelda(tableCliente, esFactura ? "Razón Social:" : "Cliente:", fontNegrita, false);
            agregarCelda(tableCliente, venta.getCliente() != null ? venta.getCliente() : "CLIENTES VARIOS", fontNormal, true);
            agregarCelda(tableCliente, "Dirección:", fontNegrita, false);
            agregarCelda(tableCliente, "-", fontNormal, true);
            agregarCelda(tableCliente, esFactura ? "RUC:" : "DNI:", fontNegrita, false);
            agregarCelda(tableCliente, "00000000", fontNormal, false);
            agregarCelda(tableCliente, "Forma Pago:", fontNegrita, false);
            agregarCelda(tableCliente, venta.getMetodoPago() != null ? venta.getMetodoPago() : "Contado", fontNormal, false);
            agregarCelda(tableCliente, "Atendido por:", fontNegrita, false);
            agregarCelda(tableCliente, obtenerAtendidoPor(venta), fontNormal, true);

            PdfPTable cuadroClienteOuter = new PdfPTable(1);
            cuadroClienteOuter.setWidthPercentage(100);
            PdfPCell outerCell = new PdfPCell(tableCliente);
            outerCell.setPadding(5);
            cuadroClienteOuter.addCell(outerCell);
            documento.add(cuadroClienteOuter);

            documento.add(new Paragraph(" "));

            PdfPTable tableMeta = new PdfPTable(4);
            tableMeta.setWidthPercentage(100);
            String[] metaHeaders = {"Fecha Emisión", "N° Orden Compra", "Fecha Vencimiento", "Tipo Moneda"};
            for (String h : metaHeaders) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontNegrita));
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setBackgroundColor(new Color(230, 230, 230));
                c.setPadding(5);
                tableMeta.addCell(c);
            }

            String soloFecha = venta.getFecha() != null ? venta.getFecha().split(" ")[0] : "11-07-2026";
            agregarCeldaCentrada(tableMeta, soloFecha, fontNormal);
            agregarCeldaCentrada(tableMeta, "", fontNormal);
            agregarCeldaCentrada(tableMeta, soloFecha, fontNormal);
            agregarCeldaCentrada(tableMeta, "SOLES", fontNormal);
            documento.add(tableMeta);

            documento.add(new Paragraph(" "));

            PdfPTable tableItems = new PdfPTable(7);
            tableItems.setWidthPercentage(100);
            tableItems.setWidths(new float[]{0.8f, 2f, 1f, 1f, 4.2f, 1.5f, 1.5f});
            String[] headers = {"N°", "Codigo", "Cant.", "U.M", "Descripcion", "P.U.", "Importe"};
            for (String header : headers) {
                PdfPCell c = new PdfPCell(new Phrase(header, fontNegrita));
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setBackgroundColor(new Color(230, 230, 230));
                c.setPadding(4);
                tableItems.addCell(c);
            }

            if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
                int index = 1;
                for (com.modelo.DetalleVenta item : venta.getDetalles()) {
                    agregarCeldaCentrada(tableItems, String.valueOf(index++), fontPequena);
                    agregarCeldaCentrada(tableItems, "10100" + item.getIdProducto(), fontPequena);
                    agregarCeldaCentrada(tableItems, String.valueOf(item.getCantidad()), fontPequena);
                    agregarCeldaCentrada(tableItems, "UND", fontPequena);
                    PdfPCell cDesc = new PdfPCell(new Phrase(item.getNombreProducto(), fontPequena));
                    cDesc.setPadding(4);
                    tableItems.addCell(cDesc);
                    agregarCeldaDerecha(tableItems, String.format("%.2f", item.getPrecioUnitario()), fontPequena);
                    agregarCeldaDerecha(tableItems, String.format("%.2f", item.getSubtotal()), fontPequena);
                }
            } else {
                agregarCeldaCentrada(tableItems, "1", fontPequena);
                agregarCeldaCentrada(tableItems, "-", fontPequena);
                agregarCeldaCentrada(tableItems, String.valueOf(venta.getProductos()), fontPequena);
                agregarCeldaCentrada(tableItems, "UND", fontPequena);
                tableItems.addCell(new PdfPCell(new Phrase("Artículos varios", fontPequena)));
                agregarCeldaDerecha(tableItems, "-", fontPequena);
                agregarCeldaDerecha(tableItems, String.format("%.2f", venta.getTotal()), fontPequena);
            }
            documento.add(tableItems);

            documento.add(new Paragraph(" "));

            PdfPTable tableBottom = new PdfPTable(2);
            tableBottom.setWidthPercentage(100);
            tableBottom.setWidths(new int[]{65, 35});

            PdfPCell cellBotIzq = new PdfPCell();
            cellBotIzq.setBorder(Rectangle.NO_BORDER);
            String letrasMonto = convertirNumeroALetras(venta.getTotal());
            cellBotIzq.addElement(new Phrase(letrasMonto, fontNormal));
            cellBotIzq.addElement(new Paragraph("\nRepresentación impresa de la " + tituloDocumento, fontPequena));
            cellBotIzq.addElement(new Paragraph("Consulte en: https://sunat.gob.pe", fontPequena));
            tableBottom.addCell(cellBotIzq);

            double totalPagar = venta.getTotal();
            double subtotal = totalPagar / 1.18;
            double igv = totalPagar - subtotal;

            PdfPTable tableCalc = new PdfPTable(2);
            tableCalc.setWidthPercentage(100);
            tableCalc.setWidths(new float[]{6f, 4f});
            agregarFilaTotalA4(tableCalc, "Op. Gravada", subtotal, fontNormal);
            agregarFilaTotalA4(tableCalc, "Op. Exonerada", 0.00, fontNormal);
            agregarFilaTotalA4(tableCalc, "Op. Gratuita", 0.00, fontNormal);
            agregarFilaTotalA4(tableCalc, "Op. Inafecta", 0.00, fontNormal);
            agregarFilaTotalA4(tableCalc, "I.G.V.", igv, fontNormal);
            agregarFilaTotalA4(tableCalc, "I.C.B.P.E.R", 0.00, fontNormal);
            agregarFilaTotalA4(tableCalc, "Comisión", 0.00, fontNormal);
            agregarFilaTotalA4(tableCalc, "Importe Total", totalPagar, fontNegrita);

            PdfPCell cellBotDer = new PdfPCell(tableCalc);
            cellBotDer.setPadding(0);
            tableBottom.addCell(cellBotDer);
            documento.add(tableBottom);

            documento.close();
            log.info("Documento PDF en formato A4 guardado con éxito en: {}", rutaArchivo);
        } catch (Exception e) {
            log.error("Error fatal en el renderizado del PDF A4: {}", e.getMessage(), e);
        }
    }

    private static String convertirNumeroALetras(double numero) {
        long entero = (long) numero;
        long centavos = Math.round((numero - entero) * 100);
        String sufijo = "CON " + String.format("%02d", centavos) + "/100 SOLES";
        if (entero == 0) return "SON: CERO " + sufijo;
        if (entero == 100) return "SON: CIEN " + sufijo;
        return "SON: " + ConvertirAlgoritmoRec(entero).trim() + " " + sufijo;
    }

    private static String ConvertirAlgoritmoRec(long n) {
        String[] unidades = {"", "UN ", "DOS ", "TRES ", "CUATRO ", "CINCO ", "SEIS ", "SIETE ", "OCHO ", "NUEVE "};
        String[] decenas = {"", "DIEZ ", "VEINTE ", "TREINTA ", "CUARENTA ", "CINCUENTA ", "SESENTA ", "SETENTA ", "OCHENTA ", "NOVENTA "};
        String[] especiales = {"DIEZ ", "ONCE ", "DOCE ", "TRECE ", "CATORCE ", "QUINCE ", "DIECISEIS ", "DIECISIETE ", "DIECIOCHO ", "DIECINUEVE "};
        String[] veintes = {"VEINTE ", "VEINTIUN ", "VEINTIDOS ", "VEINTITRES ", "VEINTICUATRO ", "VEINTICINCO ", "VEINTISEIS ", "VEINTISIETE ", "VEINTIOCHO ", "VEINTINUEVE "};
        String[] centenas = {"", "CIENTO ", "DOSCIENTOS ", "TRESCIENTOS ", "CUATROCIENTOS ", "QUINIENTOS ", "SEISCIENTOS ", "SETECIENTOS ", "OCHOCIENTOS ", "NOVECIENTOS "};
        if (n < 0) return "MENOS " + ConvertirAlgoritmoRec(-n);
        StringBuilder res = new StringBuilder();
        if (n >= 1000000) {
            long millones = n / 1000000;
            if (millones == 1) res.append("UN MILLON ");
            else res.append(ConvertirAlgoritmoRec(millones)).append("MILLONES ");
            n %= 1000000;
        }
        if (n >= 1000) {
            long miles = n / 1000;
            if (miles == 1) res.append("MIL ");
            else res.append(ConvertirAlgoritmoRec(miles)).append("MIL ");
            n %= 1000;
        }
        if (n >= 100) {
            if (n == 100) {
                res.append("CIEN ");
                n = 0;
            } else {
                res.append(centenas[(int)(n / 100)]);
                n %= 100;
            }
        }
        if (n >= 10) {
            int d = (int)(n / 10);
            int u = (int)(n % 10);
            if (d == 1) {
                res.append(especiales[u]);
                n = 0;
            } else if (d == 2) {
                res.append(veintes[u]);
                n = 0;
            } else {
                res.append(decenas[d]);
                if (u > 0) res.append("Y ");
                n %= 10;
            }
        }
        if (n > 0) res.append(unidades[(int)n]);
        return res.toString();
    }

    private static boolean esVentaPorAutoservicio(Venta venta) {
        if (venta.getCanalVenta() != null && !venta.getCanalVenta().isBlank()) {
            return venta.getCanalVenta().equalsIgnoreCase("AUTOSERVICIO");
        }
        return venta.getVendedor() == null || venta.getVendedor().isBlank();
    }

    private static String obtenerAtendidoPor(Venta venta) {
        if (esVentaPorAutoservicio(venta)) {
            return "AUTOSERVICIO (Kiosko)";
        }
        return (venta.getVendedor() != null && !venta.getVendedor().isBlank()) ? venta.getVendedor() : "Sin sesión";
    }

    private static String obtenerEtiquetaAtendidoPor(Venta venta) {
        if (esVentaPorAutoservicio(venta)) {
            return "Canal: AUTOSERVICIO (Kiosko)";
        }
        return "Cajero/a: " + obtenerAtendidoPor(venta);
    }

    private static String prepararDirectorio() {
        String rutaCarpeta = System.getProperty("user.dir") + File.separator + "Tickets";
        File carpetaTickets = new File(rutaCarpeta);
        if (!carpetaTickets.exists()) carpetaTickets.mkdirs();
        return rutaCarpeta;
    }

    private static void agregarCelda(PdfPTable tabla, String texto, Font fuente, boolean colspan2) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setBorder(Rectangle.NO_BORDER);
        if (colspan2) cell.setColspan(3);
        tabla.addCell(cell);
    }

    private static void agregarCeldaCentrada(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4);
        tabla.addCell(cell);
    }

    private static void agregarCeldaDerecha(PdfPTable tabla, String texto, Font fuente) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4);
        tabla.addCell(cell);
    }

    private static void agregarFilaTotalTicket(PdfPTable tabla, String etiqueta, double monto, Font fuente) {
        PdfPCell cEtiq = new PdfPCell(new Phrase(etiqueta, fuente));
        cEtiq.setBorder(Rectangle.NO_BORDER);
        cEtiq.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(cEtiq);
        PdfPCell cMonto = new PdfPCell(new Phrase(String.format("%.2f", monto), fuente));
        cMonto.setBorder(Rectangle.NO_BORDER);
        cMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(cMonto);
    }

    private static void agregarFilaTotalA4(PdfPTable tabla, String etiqueta, double monto, Font fuente) {
        PdfPCell cEtiq = new PdfPCell(new Phrase(etiqueta, fuente));
        cEtiq.setPadding(4);
        tabla.addCell(cEtiq);
        PdfPCell cMonto = new PdfPCell(new Phrase(String.format("%.2f", monto), fuente));
        cMonto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cMonto.setPadding(4);
        tabla.addCell(cMonto);
    }
}