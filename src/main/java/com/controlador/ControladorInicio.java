/*
 * En este controlador gestionamos el Dashboard o panel principal del sistema.
 * Hemos aplicado el Principio de Inversión de Dependencias (SOLID / DIP),
 * extrayendo toda la lógica de acceso a datos y sentencias SQL hacia la
 * abstracción IInicioDAO. De esta forma, el controlador se vuelve 100%
 * responsable de la UI y la navegación, manteniéndose limpio y fácil de escalar.
 */
package com.controlador;

import com.dao.IInicioDAO;
import com.dao.InicioDAO;
import com.servicio.SesionActual;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ControladorInicio implements Initializable {

    @FXML private Button btnIrPuntoVenta;
    @FXML private Button btnEscanearProductos;

    @FXML private Label lblVentasDelMes;
    @FXML private Label lblTotalDeProductos;
    @FXML private Label lblCrecimiento;

    @FXML private Label lblVR1Id;     @FXML private Label lblVR1Nombre;
    @FXML private Label lblVR1Monto;  @FXML private Label lblVR1Fecha;

    @FXML private Label lblVR2Id;     @FXML private Label lblVR2Nombre;
    @FXML private Label lblVR2Monto;  @FXML private Label lblVR2Fecha;

    @FXML private Label lblVR3Id;     @FXML private Label lblVR3Nombre;
    @FXML private Label lblVR3Monto;  @FXML private Label lblVR3Fecha;

    @FXML private Label lblVR4Id;     @FXML private Label lblVR4Nombre;
    @FXML private Label lblVR4Monto;  @FXML private Label lblVR4Fecha;

    @FXML private Label       lblTop1;  @FXML private Label       lblCant1;
    @FXML private Label       lblTop2;  @FXML private Label       lblCant2;
    @FXML private Label       lblTop3;  @FXML private Label       lblCant3;
    @FXML private Label       lblTop4;  @FXML private Label       lblCant4;

    @FXML private ProgressBar barTop1;
    @FXML private ProgressBar barTop2;
    @FXML private ProgressBar barTop3;
    @FXML private ProgressBar barTop4;

    private final IInicioDAO inicioDAO;

    public ControladorInicio() {
        this.inicioDAO = new InicioDAO();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        aplicarVisibilidadPorRol();
        cargarVentasDelMes();
        cargarTotalProductos();
        cargarCrecimiento();
        cargarVentasRecientes();
        cargarProductosMasVendidos();
    }

    /**
     * SEGURIDAD [SEC-11]: Los accesos rápidos de Inicio también respetan
     * el rol de la sesión actual:
     *   - Administrador: ve "Ir al Punto de Venta" y "Escanear Productos".
     *   - Vendedor: ve solo "Ir al Punto de Venta".
     *   - Almacén: ve solo "Escanear Productos".
     */
    private void aplicarVisibilidadPorRol() {
        SesionActual sesion = SesionActual.getInstancia();
        boolean vePuntoVenta = sesion.puedeVender();
        boolean veEscaner    = sesion.puedeGestionarInventario();

        btnIrPuntoVenta.setVisible(vePuntoVenta);
        btnIrPuntoVenta.setManaged(vePuntoVenta);

        btnEscanearProductos.setVisible(veEscaner);
        btnEscanearProductos.setManaged(veEscaner);
    }

    private void cargarVentasDelMes() {
        double total = inicioDAO.obtenerVentasDelMes();
        lblVentasDelMes.setText("S/ " + String.format("%,.2f", total));
    }

    private void cargarTotalProductos() {
        int total = inicioDAO.obtenerTotalProductos();
        lblTotalDeProductos.setText(String.valueOf(total));
    }

    private void cargarCrecimiento() {
        double crec = inicioDAO.obtenerCrecimientoVentas();
        String signo = crec >= 0 ? "+" : "";
        lblCrecimiento.setText(signo + String.format("%.1f%%", crec));
    }

    private void cargarVentasRecientes() {
        List<Map<String, Object>> recientes = inicioDAO.obtenerVentasRecientes(4);

        Label[] idsLbl    = {lblVR1Id,    lblVR2Id,    lblVR3Id,    lblVR4Id};
        Label[] nomLbl    = {lblVR1Nombre,lblVR2Nombre,lblVR3Nombre,lblVR4Nombre};
        Label[] montoLbl  = {lblVR1Monto, lblVR2Monto, lblVR3Monto, lblVR4Monto};
        Label[] fechaLbl  = {lblVR1Fecha, lblVR2Fecha, lblVR3Fecha, lblVR4Fecha};

        for (int i = 0; i < 4; i++) {
            if (i < recientes.size()) {
                Map<String, Object> venta = recientes.get(i);

                String id     = String.format("#%04d", (Integer) venta.get("id"));
                String nombre = (String) venta.get("nombre");
                double monto  = (Double) venta.get("monto");
                String fecha  = (String) venta.get("fecha");

                idsLbl[i].setText(id);
                nomLbl[i].setText(nombre != null ? nombre : "---");
                montoLbl[i].setText("S/ " + String.format("%,.2f", monto));
                fechaLbl[i].setText(fecha != null ? fecha : "--/--/----");
            } else {
                idsLbl[i].setText("#----");
                nomLbl[i].setText("Sin ventas");
                montoLbl[i].setText("S/ 0.00");
                fechaLbl[i].setText("--/--/----");
            }
        }
    }

    private void cargarProductosMasVendidos() {
        List<Map<String, Object>> topProductos = inicioDAO.obtenerProductosMasVendidos(4);

        Label[]       labels = {lblTop1, lblTop2, lblTop3, lblTop4};
        Label[]       cants  = {lblCant1,lblCant2,lblCant3,lblCant4};
        ProgressBar[] bars   = {barTop1, barTop2, barTop3, barTop4};

        if (topProductos.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                labels[i].setText("Sin ventas");
                cants[i].setText("0");
                bars[i].setProgress(0);
            }
            return;
        }

        int maxVentas = (Integer) topProductos.get(0).get("vendidos");

        for (int i = 0; i < 4; i++) {
            if (i < topProductos.size()) {
                Map<String, Object> prod = topProductos.get(i);
                int vendidos = (Integer) prod.get("vendidos");

                labels[i].setText((String) prod.get("nombre"));
                cants[i].setText(String.valueOf(vendidos));
                bars[i].setProgress((double) vendidos / maxVentas);
            } else {
                labels[i].setText("---");
                cants[i].setText("0");
                bars[i].setProgress(0);
            }
        }
    }

    // ============================================================
    //  NAVEGACIÓN
    // ============================================================
    @FXML
    public void abrirPuntoVenta(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevaventa-view.fxml");
    }

    @FXML
    public void abrirVistaProductos(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevoproducto-view.fxml");
    }

    private void cambiarVistaCentro(javafx.event.ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + fxml));
            javafx.scene.Parent vista = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panel =
                    (javafx.scene.layout.BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista desde Inicio: " + fxml);
            e.printStackTrace();
        }
    }
}