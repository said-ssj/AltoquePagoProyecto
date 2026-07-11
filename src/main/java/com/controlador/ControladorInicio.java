package com.controlador;

import com.DB.ConexionDB;
import com.servicio.SesionActual;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * Ambos botones tienen HBox.hgrow="ALWAYS" en el FXML, así que al
     * ocultar uno (setManaged(false)) el otro se expande automáticamente
     * y ocupa el espacio disponible.
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

    //  VENTAS DEL MES  (columna: fecha, no fecha_venta)
    private void cargarVentasDelMes() {
        String sql = """
            SELECT IFNULL(SUM(total), 0) AS total_mes
            FROM venta
            WHERE MONTH(fecha) = MONTH(CURDATE())
              AND YEAR(fecha)  = YEAR(CURDATE())
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble("total_mes");
                lblVentasDelMes.setText("S/ " + String.format("%,.2f", total));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblVentasDelMes.setText("S/ 0.00");
        }
    }

    //  TOTAL DE PRODUCTOS
    private void cargarTotalProductos() {
        String sql = "SELECT COUNT(*) AS total FROM producto";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) lblTotalDeProductos.setText(String.valueOf(rs.getInt("total")));
        } catch (Exception e) {
            e.printStackTrace();
            lblTotalDeProductos.setText("0");
        }
    }

    //  CRECIMIENTO  = ((mes_actual - mes_anterior) / mes_anterior) * 100
    private void cargarCrecimiento() {
        String sql = """
            SELECT
              (SELECT IFNULL(SUM(total),0) FROM venta
               WHERE MONTH(fecha)=MONTH(CURDATE()) AND YEAR(fecha)=YEAR(CURDATE()))
              AS actual,
              (SELECT IFNULL(SUM(total),0) FROM venta
               WHERE MONTH(fecha)=MONTH(CURDATE() - INTERVAL 1 MONTH)
                 AND YEAR(fecha)=YEAR(CURDATE() - INTERVAL 1 MONTH))
              AS anterior
        """;
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double actual   = rs.getDouble("actual");
                double anterior = rs.getDouble("anterior");
                double crec     = (anterior > 0) ? ((actual - anterior) / anterior) * 100 : 0;
                String signo    = crec >= 0 ? "+" : "";
                lblCrecimiento.setText(signo + String.format("%.1f%%", crec));
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblCrecimiento.setText("+0%");
        }
    }

    //  VENTAS RECIENTES (últimas 4, con nombre del cliente)
    private void cargarVentasRecientes() {
        String sql = """
            SELECT v.id_venta,
                   CONCAT(c.nombre, ' ', IFNULL(c.apellido,'')) AS nombre_cliente,
                   v.total,
                   DATE_FORMAT(v.fecha, '%d/%m/%Y') AS fecha_fmt
            FROM venta v
            INNER JOIN cliente c ON v.id_cliente = c.id_cliente
            ORDER BY v.fecha DESC, v.id_venta DESC
            LIMIT 4
        """;

        Label[] idsLbl    = {lblVR1Id,    lblVR2Id,    lblVR3Id,    lblVR4Id};
        Label[] nomLbl    = {lblVR1Nombre,lblVR2Nombre,lblVR3Nombre,lblVR4Nombre};
        Label[] montoLbl  = {lblVR1Monto, lblVR2Monto, lblVR3Monto, lblVR4Monto};
        Label[] fechaLbl  = {lblVR1Fecha, lblVR2Fecha, lblVR3Fecha, lblVR4Fecha};

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int i = 0;
            while (rs.next() && i < 4) {
                String id     = String.format("#%04d", rs.getInt("id_venta"));
                String nombre = rs.getString("nombre_cliente");
                double monto  = rs.getDouble("total");
                String fecha  = rs.getString("fecha_fmt");

                idsLbl  [i].setText(id);
                nomLbl  [i].setText(nombre != null ? nombre : "---");
                montoLbl[i].setText("S/ " + String.format("%,.2f", monto));
                fechaLbl[i].setText(fecha != null ? fecha : "--/--/----");
                i++;
            }
            for (; i < 4; i++) {
                idsLbl  [i].setText("#----");
                nomLbl  [i].setText("Sin ventas");
                montoLbl[i].setText("S/ 0.00");
                fechaLbl[i].setText("--/--/----");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  PRODUCTOS MÁS VENDIDOS (cantidad REAL de unidades vendidas)
    private void cargarProductosMasVendidos() {
        String sql = """
            SELECT p.nombre,
                   SUM(dv.cantidad) AS vendidos
            FROM detalle_venta dv
            INNER JOIN producto p ON dv.id_producto = p.id_producto
            GROUP BY p.id_producto, p.nombre
            ORDER BY vendidos DESC
            LIMIT 4
        """;

        Label[]       labels = {lblTop1, lblTop2, lblTop3, lblTop4};
        Label[]       cants  = {lblCant1,lblCant2,lblCant3,lblCant4};
        ProgressBar[] bars   = {barTop1, barTop2, barTop3, barTop4};

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int maxVentas = 1;
            int index     = 0;

            if (rs.next()) {
                maxVentas = rs.getInt("vendidos");
                labels[0].setText(rs.getString("nombre"));
                cants [0].setText(String.valueOf(maxVentas));
                bars  [0].setProgress(1.0);
                index = 1;
            }

            if (index == 0) {
                for (int i = 0; i < 4; i++) {
                    labels[i].setText("Sin ventas");
                    cants [i].setText("0");
                    bars  [i].setProgress(0);
                }
                return;
            }

            while (rs.next() && index < 4) {
                int vendidos = rs.getInt("vendidos");
                labels[index].setText(rs.getString("nombre"));
                cants [index].setText(String.valueOf(vendidos));
                bars  [index].setProgress((double) vendidos / maxVentas);
                index++;
            }

            for (; index < 4; index++) {
                labels[index].setText("---");
                cants [index].setText("0");
                bars  [index].setProgress(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  NAVEGACIÓN
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