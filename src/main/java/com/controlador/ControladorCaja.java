package com.controlador;

import com.dao.PagoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorCaja implements Initializable {

    // --- Etiquetas de Resumen del Sistema ---
    @FXML private Label lblEstadoCaja;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotalDigital;
    @FXML private Label lblTotalTarjeta;
    @FXML private Label lblTotalTurno;

    // --- Elementos del Arqueo ---
    @FXML private TextField txtEfectivoReal;
    @FXML private Label lblDiferencia;
    @FXML private Label lblMensajeDiferencia;

    // --- Botones ---
    @FXML private Button btnAbrirCaja;
    @FXML private Button btnCerrarCaja;

    // Variables internas (Estos valores vendrán de tu base de datos mediante PagoDAO)
    private double sistemaEfectivo = 450.50;
    private boolean cajaAbierta = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Al cargar la vista, consultaríamos la Base de Datos para traer las sumatorias
        // del turno actual buscando los registros de la tabla 'pago' y 'venta'.
        cargarDatosTurno();
    }

    private void cargarDatosTurno() {
        PagoDAO pagoDAO = new PagoDAO();

        // Obtener los datos reales desde MySQL
        sistemaEfectivo = pagoDAO.obtenerTotalPorMetodoHoy("EFECTIVO");
        double totalDigital = pagoDAO.obtenerTotalBilleterasDigitalesHoy();
        double totalTarjeta = pagoDAO.obtenerTotalPorMetodoHoy("TARJETA");
        double totalTurno = pagoDAO.obtenerTotalIngresosHoy();

        // Actualizar las etiquetas en la pantalla
        lblTotalEfectivo.setText(String.format("S/ %.2f", sistemaEfectivo));
        lblTotalDigital.setText(String.format("S/ %.2f", totalDigital));
        lblTotalTarjeta.setText(String.format("S/ %.2f", totalTarjeta));
        lblTotalTurno.setText(String.format("S/ %.2f", totalTurno));
    }

    @FXML
    public void calcularDiferencia() {
        try {
            String textoReal = txtEfectivoReal.getText().trim();
            if (textoReal.isEmpty()) {
                lblDiferencia.setText("S/ 0.00");
                lblMensajeDiferencia.setText("");
                return;
            }

            double efectivoReal = Double.parseDouble(textoReal);
            double diferencia = efectivoReal - sistemaEfectivo;

            // Formatear el texto de salida
            lblDiferencia.setText(String.format("S/ %.2f", Math.abs(diferencia)));

            // Aplicar clases CSS según el resultado (Sobrante, Faltante o Cuadre)
            lblDiferencia.getStyleClass().removeAll("texto-sobrante", "texto-faltante");
            lblMensajeDiferencia.getStyleClass().removeAll("texto-sobrante", "texto-faltante");

            if (diferencia > 0) {
                lblMensajeDiferencia.setText("(Sobrante en caja)");
                lblDiferencia.getStyleClass().add("texto-sobrante");
                lblMensajeDiferencia.getStyleClass().add("texto-sobrante");
            } else if (diferencia < 0) {
                lblMensajeDiferencia.setText("(Faltante en caja)");
                lblDiferencia.getStyleClass().add("texto-faltante");
                lblMensajeDiferencia.getStyleClass().add("texto-faltante");
            } else {
                lblMensajeDiferencia.setText("(Cuadre Perfecto)");
                lblDiferencia.setStyle("-fx-text-fill: #2c3e50;"); // Color neutro
            }

        } catch (NumberFormatException e) {
            lblMensajeDiferencia.setText("Ingrese un valor numérico válido.");
        }
    }

    @FXML
    public void cerrarCaja() {
        if (txtEfectivoReal.getText().isEmpty()) {
            System.out.println("Debe ingresar el monto real en efectivo.");
            return;
        }

        System.out.println("Cerrando caja... Guardando historial de arqueo.");
        // Aquí harías el INSERT a una tabla 'turno' o 'arqueo_caja' en tu DB.

        cajaAbierta = false;
        lblEstadoCaja.setText("CAJA CERRADA");
        lblEstadoCaja.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15;");

        // Alternar botones
        btnCerrarCaja.setVisible(false);
        btnCerrarCaja.setManaged(false);
        btnAbrirCaja.setVisible(true);
        btnAbrirCaja.setManaged(true);

        txtEfectivoReal.setDisable(true);
    }

    @FXML
    public void abrirCaja() {
        System.out.println("Abriendo nueva caja...");
        cajaAbierta = true;

        // Restablecer interfaz
        lblEstadoCaja.setText("CAJA ABIERTA");
        lblEstadoCaja.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15;");

        txtEfectivoReal.setDisable(false);
        txtEfectivoReal.clear();
        calcularDiferencia();

        btnAbrirCaja.setVisible(false);
        btnAbrirCaja.setManaged(false);
        btnCerrarCaja.setVisible(true);
        btnCerrarCaja.setManaged(true);
    }
}