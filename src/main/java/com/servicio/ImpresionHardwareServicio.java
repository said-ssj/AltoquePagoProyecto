/*
 * Administramos la comunicación directa con los controladores del sistema operativo y
 * los periféricos físicos del sistema. Al encapsular la lógica de PrintServiceLookup y
 * PrinterJob, garantizamos que un error de hardware o una desconexión no interrumpa el
 * flujo de renderizado lógico del sistema, aislando la infraestructura física según SRP.
 */
package com.servicio;

import java.io.File;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import javax.print.PrintServiceLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpresionHardwareServicio {

    private static final Logger log = LoggerFactory.getLogger(ImpresionHardwareServicio.class);

    public void enviarColaImpresoraFisica(String rutaArchivo) {
        try {
            File archivoImprimir = new File(rutaArchivo);
            if (archivoImprimir.exists() && PrintServiceLookup.lookupDefaultPrintService() != null) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintService(PrintServiceLookup.lookupDefaultPrintService());
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                    graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
                    return Printable.PAGE_EXISTS;
                });
                job.print();
            }
        } catch (Exception e) {
            log.error("Error crítico en la comunicación de hardware con la ticketera física.", e);
        }
    }
}