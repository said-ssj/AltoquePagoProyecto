package com.controlador;

import com.DB.ConexionDB;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class ControladorInicio implements Initializable{
    @FXML private Label lblVentasDelMes;

    @FXML private Label lblTotalDeProductos;

    @FXML private Label lblCrecimiento;

    //  PRODUCTOS MÁS VENDIDOS

    @FXML private Label lblTop1;
    @FXML private Label lblTop2;
    @FXML private Label lblTop3;
    @FXML private Label lblTop4;

    @FXML private ProgressBar barTop1;
    @FXML private ProgressBar barTop2;
    @FXML private ProgressBar barTop3;
    @FXML private ProgressBar barTop4;

    @FXML
    public void abrirPuntoVenta(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevaventa-view.fxml");
    }

    @FXML
    public void abrirVistaProductos(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevoproducto-view.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        cargarVentasDelMes();
        cargarTotalProductos();
        cargarProductosMasVendidos();
        cargarCrecimiento();
    }
    // Método reutilizable para inyectar vistas en el BorderPane principal
    private void cambiarVistaCentro(javafx.event.ActionEvent event, String fxml) {
        try {
            // AQUÍ AGREGAMOS LA RUTA: /com/vista/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/" + fxml));
            javafx.scene.Parent vista = loader.load();

            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            panelPrincipal.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista desde Inicio: " + fxml);
            e.printStackTrace();
        }
    }

    private void cargarProductosMasVendidos() {

        String sql = """
        SELECT p.nombre,
               SUM(dv.cantidad) AS vendidos
        FROM detalle_venta dv
        INNER JOIN producto p
            ON dv.id_producto = p.id_producto
        GROUP BY p.nombre
        ORDER BY vendidos DESC
        LIMIT 4
    """;

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Label[] labels = {lblTop1, lblTop2, lblTop3, lblTop4};
            ProgressBar[] bars = {barTop1, barTop2, barTop3, barTop4};

            int index = 0;
            int maxVentas = 1;

            // Obtener máximo para porcentaje
            if (rs.next()) {

                maxVentas = rs.getInt("vendidos");

                labels[0].setText(
                        rs.getString("nombre") + " (" + maxVentas + ")"
                );

                bars[0].setProgress(1.0);

                index = 1;
            }
            if (index == 0) {
                lblTop1.setText("Sin ventas");
                lblTop2.setText("Sin ventas");
                lblTop3.setText("Sin ventas");
                lblTop4.setText("Sin ventas");

                barTop1.setProgress(0);
                barTop2.setProgress(0);
                barTop3.setProgress(0);
                barTop4.setProgress(0);

                return;
            }

            while (rs.next() && index < 4) {

                int vendidos = rs.getInt("vendidos");

                labels[index].setText(
                        rs.getString("nombre") + " (" + vendidos + ")"
                );

                bars[index].setProgress(
                        (double) vendidos / maxVentas
                );

                index++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cargarVentasDelMes() {

        String sql = """
        SELECT IFNULL(SUM(total),0) AS total_mes
        FROM venta
        WHERE MONTH(fecha_venta)=MONTH(CURDATE())
        AND YEAR(fecha_venta)=YEAR(CURDATE())
    """;

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double total = rs.getDouble("total_mes");
                lblVentasDelMes.setText("S/ " + String.format("%.2f", total));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void cargarTotalProductos() {

        String sql = "SELECT COUNT(*) AS total FROM producto";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                lblTotalDeProductos.setText(
                        String.valueOf(rs.getInt("total"))
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void cargarCrecimiento() {

        String sql = """
        SELECT
        (
            SELECT IFNULL(SUM(total),0)
            FROM venta
            WHERE MONTH(fecha_venta)=MONTH(CURDATE())
            AND YEAR(fecha_venta)=YEAR(CURDATE())
        ) AS actual,

        (
            SELECT IFNULL(SUM(total),0)
            FROM venta
            WHERE MONTH(fecha_venta)=MONTH(CURDATE()-INTERVAL 1 MONTH)
            AND YEAR(fecha_venta)=YEAR(CURDATE()-INTERVAL 1 MONTH)
        ) AS anterior
    """;

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                double actual = rs.getDouble("actual");
                double anterior = rs.getDouble("anterior");

                double crecimiento = 0;

                if (anterior > 0) {
                    crecimiento = ((actual - anterior) / anterior) * 100;
                }

                lblCrecimiento.setText(
                        String.format("%.1f%%", crecimiento)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}