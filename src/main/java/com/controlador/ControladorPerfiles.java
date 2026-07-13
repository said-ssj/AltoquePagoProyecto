/*
 * En este controlador gestionamos la matriz visual de perfiles y permisos del sistema.
 * Hemos aplicado el Principio de Responsabilidad Única (SRP) manteniendo las acciones
 * de selección de la UI limpias y centralizadas, preparadas estructuralmente para reflejar
 * de manera reactiva la carga y asignación estricta de accesos por cada rol.
 */
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
        configurarPermisosVisuales("Permisos - Administrador", true, true, true, true, true);
    }

    @FXML
    public void seleccionarVendedor() {
        configurarPermisosVisuales("Permisos - Vendedor", true, true, false, false, false);
    }

    @FXML
    public void seleccionarAlmacen() {
        configurarPermisosVisuales("Permisos - Almacén", false, true, false, true, false);
    }



    // ============================================================
    //  MÉTODOS UTILITARIOS (DRY)
    // ============================================================

    /**
     * Helper centralizado para asignar de un solo golpe los estados lógicos de la UI,
     * evitando la repetición redundante de código en los métodos de selección.
     */
    private void configurarPermisosVisuales (String titulo, boolean ventas, boolean productos,
                                           boolean empleados, boolean reportes, boolean configuracion) {
        lblTituloPermisos.setText(titulo);
        chkVentas.setSelected(ventas);
        chkProductos.setSelected(productos);
        chkEmpleados.setSelected(empleados);
        chkReportes.setSelected(reportes);
        chkConfiguracion.setSelected(configuracion);
    }
}