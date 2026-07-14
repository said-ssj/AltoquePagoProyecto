package com.servicio.ColorConfig;

import java.io.*;
import java.util.Properties;

public class ColorOfertaMap {
    private static final String ARCHIVO_COLORES = "colores_ofertas.properties";
    private static final String COLOR_POR_DEFECTO = "#3b82f6";

    // Guarda el color de una oferta usando su ID como clave
    public static void guardarColor(int idOferta, String colorHex) {
        Properties props = new Properties();
        File archivo = new File(ARCHIVO_COLORES);

        if (archivo.exists()) {
            try (InputStream in = new FileInputStream(archivo)) {
                props.load(in);
            } catch (IOException e) { e.printStackTrace(); }
        }

        props.setProperty("oferta." + idOferta, colorHex);

        try (OutputStream out = new FileOutputStream(archivo)) {
            props.store(out, "Mapeo de colores de ofertas (Sin alterar Base de Datos)");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Lee el color guardado para el ID de la oferta, si no existe devuelve el de defecto
    public static String obtenerColor(int idOferta) {
        Properties props = new Properties();
        File archivo = new File(ARCHIVO_COLORES);

        if (!archivo.exists()) return COLOR_POR_DEFECTO;

        try (InputStream in = new FileInputStream(archivo)) {
            props.load(in);
            return props.getProperty("oferta." + idOferta, COLOR_POR_DEFECTO);
        } catch (IOException e) {
            e.printStackTrace();
            return COLOR_POR_DEFECTO;
        }
    }
}