package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Button;

public class ControladorPrincipal implements Initializable {
    @FXML private BorderPane panelPrincipal;

    // TODOS los botones del menú inyectados
    @FXML private ToggleButton btnInicio;
    @FXML private ToggleButton btnVentas;
    @FXML private ToggleButton btnProductos;
    @FXML private ToggleButton btnEmpleados;
    @FXML private ToggleButton btnConsultas;
    @FXML private ToggleButton btnReportes;
    @FXML private ToggleButton btnOfertas;
    @FXML private ToggleButton btnInventario;
    @FXML private ToggleButton btnCaja;
    @FXML private ToggleButton btnConfiguracion;
    @FXML private Button btnPerfiles;
    private int idRolActual = 1; // Por defecto empezamos como Admin


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnInicio.setSelected(true);
        abrirInicio();
    }

    // =========================================================
    // LÓGICA DE PERFILES Y PERMISOS
    // =========================================================

    public void configurarAccesos(int idRol) {
        this.idRolActual = idRol;
        System.out.println("Configurando menú para el Rol ID: " + idRol);

        // --- RESTRICCIÓN ESTRICTA DE SEGURIDAD ---
        // Si no es el Administrador (id 1), ocultamos el botón de Perfiles de Acceso
        if (idRol != 1) {
            ocultarBoton(btnPerfiles);
        }
        // -----------------------------------------

        if (idRol == 2) {
            // VENDEDOR / CAJERO
            ocultarBoton(btnEmpleados);
            ocultarBoton(btnReportes);
            ocultarBoton(btnInventario);
            ocultarBoton(btnConfiguracion);

        } else if (idRol == 3) {
            // ALMACÉN
            ocultarBoton(btnVentas);
            ocultarBoton(btnEmpleados);
            ocultarBoton(btnOfertas);
            ocultarBoton(btnCaja);
            ocultarBoton(btnConfiguracion);
        }

        abrirInicio();
    }

    /**
     * Oculta el botón visualmente y le dice a JavaFX que ignore su espacio
     * en el diseño, haciendo que los demás botones suban y ocupen su lugar.
     */
    private void ocultarBoton(javafx.scene.Node elemento) {
        if (elemento != null) {
            elemento.setVisible(false);
            elemento.setManaged(false);
        }
    }

    // =========================================================
    // MÉTODOS DE NAVEGACIÓN
    // =========================================================

    @FXML public void abrirInicio() { cargarVista("inicio-view.fxml"); }
    @FXML public void abrirVentas() { cargarVista("ventas-view.fxml"); }
    @FXML public void abrirProductos() { cargarVista("productos-view.fxml"); }
    @FXML public void abrirEmpleados() { cargarVista("empleados-view.fxml"); }
    @FXML public void abrirConsultas() { cargarVista("consultas-view.fxml"); }
    @FXML public void abrirReportes() { cargarVista("reportes-view.fxml"); }
    @FXML public void abrirOfertas() { cargarVista("ofertas-view.fxml"); }
    @FXML public void abrirInventario() { cargarVista("inventario-view.fxml"); }
    @FXML public void abrirCaja() { cargarVista("caja-view.fxml"); }
    @FXML public void abrirConfiguracion() { cargarVista("configuracion-view.fxml"); }
    @FXML public void abrirPerfiles() { cargarVista("perfiles-view.fxml"); }

    private void cargarVista(String nombreFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + nombreFxml));
            javafx.scene.Parent vista = loader.load();

            if (nombreFxml.equals("inicio-view.fxml")) {
                ControladorInicio controladorInicio = loader.getController();
                controladorInicio.configurarAccesosRapidos(this.idRolActual);
            }

            panelPrincipal.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + nombreFxml);
            e.printStackTrace();
        }
    }
}