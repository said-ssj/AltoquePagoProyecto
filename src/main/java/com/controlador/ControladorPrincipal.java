package com.controlador;

import com.servicio.SesionActual;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del menú lateral principal.
 *
 * SEGURIDAD [SEC-03]: Cada método de navegación verifica el rol del
 * usuario antes de cargar la vista. Si no tiene permiso, se muestra
 * un mensaje de "Acceso Denegado" y no se carga la vista.
 *
 * SEGURIDAD [SEC-10]: El nombre y rol del usuario se toman de SesionActual
 * (no están hardcodeados en el FXML).
 *
 * SEGURIDAD [SEC-11]: Además de bloquear la navegación, el menú lateral
 * solo muestra los botones de los módulos a los que el rol logueado
 * tiene acceso. Los botones no permitidos se ocultan por completo
 * (setVisible(false) + setManaged(false)), no solo se deshabilitan.
 */
public class ControladorPrincipal implements Initializable {

    @FXML private BorderPane  panelPrincipal;
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
    @FXML private javafx.scene.control.Button btnPerfiles;
    @FXML private Label        lblNombreUsuario;
    @FXML private Label        lblRolUsuario;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnInicio.setSelected(true);

        // SEGURIDAD [SEC-10]: Mostrar nombre y rol real desde la sesión
        SesionActual sesion = SesionActual.getInstancia();
        if (sesion.getUsuario() != null) {
            lblNombreUsuario.setText(sesion.getNombreUsuario());
            lblRolUsuario.setText(nombreRol(sesion.getRolActual()));
        }

        aplicarVisibilidadPorRol(sesion);
        abrirInicio();
    }

    /**
     * SEGURIDAD [SEC-11]: Oculta del menú lateral los botones de los
     * módulos que el rol actual no puede usar, reutilizando exactamente
     * las mismas reglas de permisos que ya validan la navegación
     * (SesionActual). Así el usuario solo ve las vistas que le
     * corresponden, sin necesidad de hacer clic para descubrirlo.
     */
    private void aplicarVisibilidadPorRol(SesionActual sesion) {
        // Inicio, Productos, Consultas y Perfiles: disponibles para todos los roles.
        ocultarSiNoPermitido(btnVentas,        sesion.puedeVender());
        ocultarSiNoPermitido(btnEmpleados,     sesion.puedeGestionarEmpleados());
        ocultarSiNoPermitido(btnReportes,      sesion.puedeVerReportes());
        ocultarSiNoPermitido(btnOfertas,       sesion.esAdministrador());
        ocultarSiNoPermitido(btnInventario,    sesion.puedeGestionarInventario());
        ocultarSiNoPermitido(btnCaja,          sesion.puedeVender());
        ocultarSiNoPermitido(btnConfiguracion, sesion.esAdministrador());

        // SEGURIDAD [SEC-12]: Perfiles no se oculta (siempre visible), pero si el
        // rol no puede administrar perfiles, queda bloqueado: sin cursor de mano,
        // sin efecto hover y sin poder entrar. Visualmente es solo una imagen.
        boolean accesoPerfiles = sesion.esAdministrador();
        btnPerfiles.getStyleClass().remove("bloqueado");
        if (!accesoPerfiles) {
            btnPerfiles.getStyleClass().add("bloqueado");
        }
    }

    private void ocultarSiNoPermitido(Node nodo, boolean permitido) {
        nodo.setVisible(permitido);
        nodo.setManaged(permitido); // managed=false -> no ocupa espacio en el layout
    }

    private String nombreRol(int idRol) {
        return switch (idRol) {
            case SesionActual.ROL_ADMINISTRADOR -> "Administrador";
            case SesionActual.ROL_VENDEDOR      -> "Vendedor";
            case SesionActual.ROL_ALMACEN       -> "Almacén";
            default -> "Usuario";
        };
    }

    // ---------------------------------------------------------------
    // Navegación con control de acceso por rol
    // ---------------------------------------------------------------

    @FXML public void abrirInicio() {
        cargarVista("inicio-view.fxml");
    }

    @FXML public void abrirVentas() {
        if (!SesionActual.getInstancia().puedeVender()) {
            mostrarAccesoDenegado("Ventas");
            return;
        }
        cargarVista("ventas-view.fxml");
    }

    @FXML public void abrirProductos() {
        // Todos los roles pueden ver productos
        cargarVista("productos-view.fxml");
    }

    @FXML public void abrirEmpleados() {
        // SEGURIDAD [SEC-03]: Solo Administrador puede gestionar empleados
        if (!SesionActual.getInstancia().puedeGestionarEmpleados()) {
            mostrarAccesoDenegado("Empleados");
            return;
        }
        cargarVista("empleados-view.fxml");
    }

    @FXML public void abrirConsultas() {
        cargarVista("consultas-view.fxml");
    }

    @FXML public void abrirReportes() {
        // SEGURIDAD [SEC-03]: Solo Administrador puede ver reportes
        if (!SesionActual.getInstancia().puedeVerReportes()) {
            mostrarAccesoDenegado("Reportes");
            return;
        }
        cargarVista("reportes-view.fxml");
    }

    @FXML public void abrirPerfiles() {
        // SEGURIDAD [SEC-12]: El botón siempre se ve, pero solo Administrador
        // puede entrar. No se muestra alerta a propósito: el botón se comporta
        // como una imagen inerte para los demás roles, sin dar retroalimentación.
        if (!SesionActual.getInstancia().esAdministrador()) {
            return;
        }
        cargarVista("perfiles-view.fxml");
    }

    @FXML public void abrirOfertas() {
        if (!SesionActual.getInstancia().esAdministrador()) {
            mostrarAccesoDenegado("Ofertas");
            return;
        }
        cargarVista("ofertas-view.fxml");
    }

    @FXML public void abrirInventario() {
        if (!SesionActual.getInstancia().puedeGestionarInventario()) {
            mostrarAccesoDenegado("Inventario");
            return;
        }
        cargarVista("inventario-view.fxml");
    }

    @FXML public void abrirCaja() {
        // Ahora Administrador y Vendedor pueden acceder a Caja / Arqueo
        if (!SesionActual.getInstancia().puedeVender()) {
            mostrarAccesoDenegado("Caja / Arqueo");
            return;
        }
        cargarVista("caja-view.fxml");
    }

    @FXML public void abrirConfiguracion() {
        if (!SesionActual.getInstancia().esAdministrador()) {
            mostrarAccesoDenegado("Configuración");
            return;
        }
        cargarVista("configuracion-view.fxml");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void cargarVista(String nombreFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + nombreFxml));
            javafx.scene.Parent vista = loader.load();
            panelPrincipal.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + nombreFxml);
            e.printStackTrace();
        }
    }

    private void mostrarAccesoDenegado(String modulo) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Acceso Denegado");
        alerta.setHeaderText(null);
        alerta.setContentText("No tienes permiso para acceder al módulo \"" + modulo + "\".\n" +
                "Contacta al Administrador del sistema.");
        alerta.showAndWait();
    }
}
