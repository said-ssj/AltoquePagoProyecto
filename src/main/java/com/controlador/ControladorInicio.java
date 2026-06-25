package com.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

public class ControladorInicio {
    @FXML private Label lblVentasDelMes;

    @FXML private Label lblTotalDeProductos;

    @FXML private Label lblCrecimiento;

    //  PRODUCTOS MÁS VENDIDOS

    @FXML private Label lblTop1;
    @FXML private ProgressBar barTop1;

    @FXML private Label lblTop2;
    @FXML private ProgressBar barTop2;

    @FXML private Label lblTop3;
    @FXML private ProgressBar barTop3;

    @FXML private Label lblTop4;
    @FXML private ProgressBar barTop4;

    @FXML private javafx.scene.control.Button btnIrPuntoVenta;
    @FXML private javafx.scene.control.Button btnEscanearProductos;

    @FXML
    public void abrirPuntoVenta(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevaventa-view.fxml");
    }

    @FXML
    public void abrirVistaProductos(javafx.event.ActionEvent event) {
        cambiarVistaCentro(event, "nuevoproducto-view.fxml");
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

    public void cargarProductosMasVendidos() {

        // 1er Producto
        lblTop1.setText("Laptop Dell XPS 15");
        barTop1.setProgress(0.85);

        // 2do Producto
        lblTop2.setText("Mouse Logitech MX");
        barTop2.setProgress(0.60);

        // 3er Producto
        lblTop3.setText("Teclado Mecánico RGB");
        barTop3.setProgress(0.40);

        // 4to Producto
        lblTop4.setText("Monitor LG 27\"");
        barTop4.setProgress(0.15);
    }

    // =========================================================
    // LÓGICA DE PERMISOS PARA ACCESOS RÁPIDOS
    // =========================================================
    public void configurarAccesosRapidos(int idRol) {
        // Asegurarnos de que los botones ya están cargados
        if (btnIrPuntoVenta == null || btnEscanearProductos == null) return;

        // idRol 1 = Administrador (Ve ambos, no hacemos nada)

        if (idRol == 2) {
            // VENDEDOR: Ocultamos el botón de Escanear Productos
            btnEscanearProductos.setVisible(false);
            btnEscanearProductos.setManaged(false);
        } else if (idRol == 3) {
            // ALMACÉN: Ocultamos el botón de Punto de Venta
            btnIrPuntoVenta.setVisible(false);
            btnIrPuntoVenta.setManaged(false);
        }
    }

}
