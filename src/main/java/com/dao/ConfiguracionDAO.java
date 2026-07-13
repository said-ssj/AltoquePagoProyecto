package com.dao;

import com.modelo.Configuracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class ConfiguracionDAO implements IConfiguracionDAO{

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionDAO.class);
    // Ruta del archivo donde se guardarán las preferencias locales
    private static final String RUTA_ARCHIVO = "configuracion.properties";

    // ============================================================
    // CARGAR CONFIGURACIÓN DESDE EL ARCHIVO
    // ============================================================
    public Configuracion cargarConfiguracion() {
        Configuracion config = new Configuracion();
        Properties prop = new Properties();

        try (InputStream input = new FileInputStream(RUTA_ARCHIVO)) {
            prop.load(input);

            // Leer propiedades y asignarlas al modelo
            config.setRazonSocial(prop.getProperty("empresa.razonSocial", "ALToquePago S.A.C."));
            config.setRuc(prop.getProperty("empresa.ruc", "20000000000"));
            config.setDireccion(prop.getProperty("empresa.direccion", "Ica, Ica, Perú"));
            config.setTelefono(prop.getProperty("empresa.telefono", "000-000000"));

            config.setImpresora(prop.getProperty("impresion.impresora", "Ninguna"));
            config.setTamañoPapel(prop.getProperty("impresion.tamañoPapel", "80mm"));
            config.setMensajeTicket(prop.getProperty("impresion.mensajeTicket", "¡Gracias por su compra en ALToque Pago!"));

            // Booleanos
            config.setSonidoEscaner(Boolean.parseBoolean(prop.getProperty("kiosko.sonidoEscaner", "true")));
            config.setModoOscuro(Boolean.parseBoolean(prop.getProperty("kiosko.modoOscuro", "false")));
            config.setImpresionAuto(Boolean.parseBoolean(prop.getProperty("kiosko.impresionAuto", "true")));

        } catch (FileNotFoundException e) {
            logger.warn("El archivo configuracion.properties no existe aún. Se crearán valores por defecto.");

            // SOLUCIÓN 1: Llenar el objeto con valores por defecto antes de guardar
            config.setRazonSocial("ALToquePago S.A.C.");
            config.setRuc("20000000000");
            config.setDireccion("Ica, Ica, Perú");
            config.setTelefono("000-000000");
            config.setImpresora("Ninguna");
            config.setTamañoPapel("80mm");
            config.setMensajeTicket("¡Gracias por su compra en ALToque Pago!");
            config.setSonidoEscaner(true);
            config.setModoOscuro(false);
            config.setImpresionAuto(true);

            guardarConfiguracion(config);
        } catch (IOException e) {
            logger.error("Error al leer la configuración local", e);
        }

        return config;
    }

    // ============================================================
    // GUARDAR O ACTUALIZAR CONFIGURACIÓN EN EL ARCHIVO
    // ============================================================
    public boolean guardarConfiguracion(Configuracion config) {
        Properties prop = new Properties();

        // SOLUCIÓN Operadores ternarios para evitar que Properties reciba un null
        prop.setProperty("empresa.razonSocial", config.getRazonSocial() != null ? config.getRazonSocial() : "");
        prop.setProperty("empresa.ruc", config.getRuc() != null ? config.getRuc() : "");
        prop.setProperty("empresa.direccion", config.getDireccion() != null ? config.getDireccion() : "");
        prop.setProperty("empresa.telefono", config.getTelefono() != null ? config.getTelefono() : "");

        prop.setProperty("impresion.impresora", config.getImpresora() != null ? config.getImpresora() : "Ninguna");
        prop.setProperty("impresion.tamañoPapel", config.getTamañoPapel() != null ? config.getTamañoPapel() : "80mm");
        prop.setProperty("impresion.mensajeTicket", config.getMensajeTicket() != null ? config.getMensajeTicket() : "");

        prop.setProperty("kiosko.sonidoEscaner", String.valueOf(config.isSonidoEscaner()));
        prop.setProperty("kiosko.modoOscuro", String.valueOf(config.isModoOscuro()));
        prop.setProperty("kiosko.impresionAuto", String.valueOf(config.isImpresionAuto()));

        try (OutputStream output = new FileOutputStream(RUTA_ARCHIVO)) {
            prop.store(output, "Configuraciones Locales de ALToque Pago");
            return true;
        } catch (IOException e) {
            logger.error("Error al guardar la configuración local", e);
            return false;
        }
    }
}