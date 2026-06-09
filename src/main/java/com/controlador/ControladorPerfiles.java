package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class ControladorPerfiles {

    @FXML private Label lblTituloPermisos;

    // Los CheckBoxes de los permisos
    @FXML private CheckBox chkVentas;
    @FXML private CheckBox chkProductos;
    @FXML private CheckBox chkEmpleados;
    @FXML private CheckBox chkReportes;
    @FXML private CheckBox chkConfiguracion;

    @FXML
    public void initialize() {
        // Al cargar la vista, por defecto mostramos los permisos del Administrador
        seleccionarAdmin();
    }

    @FXML
    public void seleccionarAdmin() {
        lblTituloPermisos.setText("Permisos - Administrador");
        // El administrador tiene acceso a todo
        chkVentas.setSelected(true);
        chkProductos.setSelected(true);
        chkEmpleados.setSelected(true);
        chkReportes.setSelected(true);
        chkConfiguracion.setSelected(true);
    }

    @FXML
    public void seleccionarVendedor() {
        lblTituloPermisos.setText("Permisos - Vendedor");
        // El vendedor solo accede a Ventas y Productos (para ver precios)
        chkVentas.setSelected(true);
        chkProductos.setSelected(true);
        chkEmpleados.setSelected(false);
        chkReportes.setSelected(false);
        chkConfiguracion.setSelected(false);
    }

    @FXML
    public void seleccionarAlmacen() {
        lblTituloPermisos.setText("Permisos - Almacén");
        // El de almacén gestiona Productos y ve Reportes de inventario
        chkVentas.setSelected(false);
        chkProductos.setSelected(true);
        chkEmpleados.setSelected(false);
        chkReportes.setSelected(true);
        chkConfiguracion.setSelected(false);
    }
}