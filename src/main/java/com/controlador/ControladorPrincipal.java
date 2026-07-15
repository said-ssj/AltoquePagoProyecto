/*
 * En este controlador gestionamos la navegación principal del sistema y el menú lateral.
 * A diferencia de los otros módulos, aquí no necesitamos inyectar DAOs (Cumpliendo SRP),
 * ya que su única responsabilidad es orquestar las vistas y coordinar con el servicio
 * de SesionActual.
 *
 * Se han preservado intactas todas las directivas y documentaciones de seguridad.
 */
package com.controlador;

import com.dao.ProductoDAO;
import com.modelo.Notificacion;
import com.servicio.NotificacionServicio;
import com.servicio.SesionActual;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.kordamp.ikonli.javafx.FontIcon;

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

    // ============================================================
    // COMPONENTES DE LA INTERFAZ
    // ============================================================
    @FXML private BorderPane panelPrincipal;

    // Botones de Navegación
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

    // Información del Usuario
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblRolUsuario;

    // Notificaciones
    @FXML private Button btnNotificaciones;
    @FXML private Label lblBadgeNotificaciones;

    private final NotificacionServicio notificacionServicio = NotificacionServicio.getInstancia();
    private Popup popupNotificaciones;
    private VBox listaNotificacionesUI;

    // ============================================================
    // INICIALIZACIÓN
    // ============================================================
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

        inicializarNotificaciones();
    }

    // ============================================================
    // NOTIFICACIONES (Campanita)
    // ============================================================

    private void inicializarNotificaciones() {
        // Badge: se sincroniza con el contador de no leídas del servicio
        notificacionServicio.noLeidasProperty().addListener((obs, anterior, nuevo) -> actualizarBadge(nuevo.intValue()));
        actualizarBadge(notificacionServicio.getNoLeidas());

        construirPopupNotificaciones();

        // Verificación inicial de stock bajo al abrir el sistema
        notificacionServicio.revisarStockBajo(new ProductoDAO().obtenerTodos());
    }

    private void actualizarBadge(int cantidad) {
        lblBadgeNotificaciones.setText(cantidad > 99 ? "99+" : String.valueOf(cantidad));
        boolean hayNoLeidas = cantidad > 0;
        lblBadgeNotificaciones.setVisible(hayNoLeidas);
        lblBadgeNotificaciones.setManaged(hayNoLeidas);
    }

    private void construirPopupNotificaciones() {
        popupNotificaciones = new Popup();
        popupNotificaciones.setAutoHide(true);
        popupNotificaciones.setHideOnEscape(true);

        VBox contenedor = new VBox();
        contenedor.setPrefWidth(340);
        contenedor.setMaxHeight(420);
        contenedor.getStyleClass().add("panel-notificaciones");
        // Aseguramos fondo sólido y hoja de estilos, ya que un Popup no
        // hereda automáticamente los estilos de la escena principal.
        contenedor.getStylesheets().add(getClass().getResource("/com/stylesCSS/Style-Menu.css").toExternalForm());
        contenedor.setStyle("-fx-background-color: #ffffff;");

        Label titulo = new Label("Notificaciones");
        titulo.getStyleClass().add("titulo-panel");
        titulo.setStyle("-fx-text-fill: #0f172b; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox encabezado = new HBox(titulo);
        encabezado.setStyle("-fx-background-color: #ffffff;");
        encabezado.setPadding(new Insets(14, 16, 10, 16));

        listaNotificacionesUI = new VBox();
        listaNotificacionesUI.setSpacing(0);
        listaNotificacionesUI.setStyle("-fx-background-color: #ffffff;");

        ScrollPane scroll = new ScrollPane(listaNotificacionesUI);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(360);
        scroll.setStyle("-fx-background-color: #ffffff; -fx-background: #ffffff;");

        contenedor.getChildren().addAll(encabezado, scroll);
        popupNotificaciones.getContent().add(contenedor);

        // Redibujar la lista cada vez que cambie (nueva notificación agregada)
        notificacionServicio.getNotificaciones().addListener(
                (javafx.collections.ListChangeListener<Notificacion>) change -> refrescarListaNotificaciones());
        refrescarListaNotificaciones();
    }

    private void refrescarListaNotificaciones() {
        if (listaNotificacionesUI == null) return;
        listaNotificacionesUI.getChildren().clear();

        if (notificacionServicio.getNotificaciones().isEmpty()) {
            Label vacio = new Label("No hay notificaciones por ahora.");
            vacio.getStyleClass().add("sin-notificaciones");
            vacio.setPadding(new Insets(20, 16, 24, 16));
            listaNotificacionesUI.getChildren().add(vacio);
            return;
        }

        for (Notificacion n : notificacionServicio.getNotificaciones()) {
            listaNotificacionesUI.getChildren().add(crearItemNotificacion(n));
        }
    }

    private Node crearItemNotificacion(Notificacion n) {
        FontIcon icono = new FontIcon(n.getIconoLiteral());
        icono.setIconSize(18);
        icono.setIconColor(javafx.scene.paint.Color.web(n.getColor()));

        Label lblTitulo = new Label(n.getTitulo());
        lblTitulo.getStyleClass().add("item-titulo");
        lblTitulo.setStyle("-fx-text-fill: #0f172b; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblTitulo.setWrapText(true);

        Label lblMensaje = new Label(n.getMensaje());
        lblMensaje.getStyleClass().add("item-mensaje");
        lblMensaje.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
        lblMensaje.setWrapText(true);

        Label lblHora = new Label(n.getHoraFormateada());
        lblHora.getStyleClass().add("item-hora");
        lblHora.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        VBox textos = new VBox(2, lblTitulo, lblMensaje, lblHora);
        HBox.setHgrow(textos, Priority.ALWAYS);

        HBox fila = new HBox(10, icono, textos);
        fila.setAlignment(Pos.TOP_LEFT);
        fila.setPadding(new Insets(10, 16, 10, 16));
        fila.getStyleClass().add("item-notificacion");
        // Fondo blanco sólido + acento azul de la marca en el borde izquierdo
        // para que combine con el resto de la interfaz (menú lateral #144DEB).
        fila.setStyle(
                "-fx-background-color: " + (n.isLeida() ? "#ffffff" : "#eff6ff") + ";" +
                        "-fx-border-color: transparent transparent #f1f5f9 transparent;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        return fila;
    }

    @FXML
    public void alternarNotificaciones() {
        if (popupNotificaciones.isShowing()) {
            popupNotificaciones.hide();
            return;
        }
        javafx.geometry.Bounds bounds = btnNotificaciones.localToScreen(btnNotificaciones.getBoundsInLocal());
        double x = bounds.getMaxX() - 340;
        double y = bounds.getMaxY() + 6;
        popupNotificaciones.show(btnNotificaciones, x, y);

        // Al abrir la campanita se consideran todas las notificaciones como leídas
        notificacionServicio.marcarTodasComoLeidas();
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

    // ============================================================
    // NAVEGACIÓN CON CONTROL DE ACCESO POR ROL
    // ============================================================

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

    // ============================================================
    // HELPERS
    // ============================================================

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