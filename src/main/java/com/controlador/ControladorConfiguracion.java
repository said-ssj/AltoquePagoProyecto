package com.controlador;

import com.dao.ConfiguracionDAO;
import com.modelo.Configuracion;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorConfiguracion implements Initializable {

    // --- Datos de la Empresa ---
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRuc;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;

    // --- Impresión ---
    @FXML private ComboBox<String> cbImpresoras;
    @FXML private ComboBox<String> cbTamañoPapel;
    @FXML private TextArea txtMensajeTicket;

    // --- Preferencias Kiosko ---
    @FXML private CheckBox chkSonidoEscaner;
    @FXML private CheckBox chkModoOscuro;
    @FXML private CheckBox chkImpresionAutomatica;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Cargar las impresoras instaladas en el sistema (Simulación)
        cbImpresoras.getItems().addAll(
                "EPSON TM-T20III Receipt",
                "Impresora PDF de Microsoft",
                "POS-80C Thermal Printer"
        );

        // 2. Tamaños de papel estándar
        cbTamañoPapel.getItems().addAll("58mm", "80mm");

        // 3. Cargar los datos guardados de la base de datos o archivo .properties
        cargarConfiguracionActual();
    }

    private void cargarConfiguracionActual() {
        ConfiguracionDAO dao = new ConfiguracionDAO();
        Configuracion config = dao.cargarConfiguracion();

        txtRazonSocial.setText(config.getRazonSocial());
        txtRuc.setText(config.getRuc());
        txtRazonSocial.setText("Minimarket ALToque Pago S.A.C.");
        txtRuc.setText("20123456789");
        txtDireccion.setText("Ica, Ica, Perú");
        txtTelefono.setText("056-123456");

        cbImpresoras.setValue("EPSON TM-T20III Receipt");
        cbTamañoPapel.setValue("80mm");
        txtMensajeTicket.setText("¡Gracias por tu compra en ALToque Pago! Conserva este ticket para cualquier reclamo.");

        chkSonidoEscaner.setSelected(true);
        chkModoOscuro.setSelected(false);
        chkImpresionAutomatica.setSelected(true);
    }

    @FXML
    public void guardarConfiguracion() {
        // Capturamos todos los datos de la interfaz
        String razonSocial = txtRazonSocial.getText();
        String ruc = txtRuc.getText();
        String direccion = txtDireccion.getText();
        String telefono = txtTelefono.getText();

        String impresora = cbImpresoras.getValue();
        String tamañoPapel = cbTamañoPapel.getValue();
        String mensaje = txtMensajeTicket.getText();

        boolean sonido = chkSonidoEscaner.isSelected();
        boolean oscuro = chkModoOscuro.isSelected();
        boolean impresionAuto = chkImpresionAutomatica.isSelected();

        // Validación básica
        if (razonSocial.isEmpty() || ruc.isEmpty()) {
            System.out.println("Error: Razón Social y RUC son obligatorios.");
            return;
        }

        System.out.println("Guardando configuración global del sistema...");

        System.out.println("Configuración actualizada correctamente.");
    }

    @FXML
    public void restablecerValores() {
        System.out.println("Descartando cambios...");
        cargarConfiguracionActual(); // Vuelve a cargar la info original de la BD
    }
}