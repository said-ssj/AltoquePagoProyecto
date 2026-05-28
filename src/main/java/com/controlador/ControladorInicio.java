package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ControladorInicio {
    @FXML
    private Label lblVentasDelMes;

    @FXML
    private Label lblTotalDeProductos;

    @FXML
    private Label lblCrecimiento;

    // --- PRODUCTOS MÁS VENDIDOS ---

    @FXML
    private Label lblTop1;
    @FXML
    private ProgressBar barTop1;

    @FXML
    private Label lblTop2;
    @FXML
    private ProgressBar barTop2;

    @FXML
    private Label lblTop3;
    @FXML
    private ProgressBar barTop3;

    @FXML
    private Label lblTop4;
    @FXML
    private ProgressBar barTop4;

    public void cargarProductosMasVendidos() {
        // 1er Producto
        lblTop1.setText("Laptop Dell XPS 15");
        barTop1.setProgress(0.85); // 85% de la barra llena

        // 2do Producto
        lblTop2.setText("Mouse Logitech MX");
        barTop2.setProgress(0.60); // 60% de la barra llena

        // 3er Producto
        lblTop3.setText("Teclado Mecánico RGB");
        barTop3.setProgress(0.40); // 40% de la barra llena

        // 4to Producto
        lblTop4.setText("Monitor LG 27\"");
        barTop4.setProgress(0.15); // 15% de la barra llena
    }

}
