package com.controlador;

import com.dao.PagoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorCaja implements Initializable {

    // --- Etiquetas de Resumen del Sistema ---
    @FXML private Label lblEstadoCaja;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotalDigital;
    @FXML private Label lblTotalTarjeta;
    @FXML private Label lblTotalTurno;

    // --- Datos del turno ---
    @FXML private Label lblCajero;
    @FXML private Label lblFecha;
    @FXML private Label lblHoraApertura;

    // --- Elementos del Arqueo ---
    @FXML private TextField txtEfectivoReal;
    @FXML private Label lblDiferencia;
    @FXML private Label lblMensajeDiferencia;

    // --- Botones ---
    @FXML private Button btnAbrirCaja;
    @FXML private Button btnCerrarCaja;

    private final PagoDAO pagoDAO = new PagoDAO();

    // Estado del turno. Se mantiene estático para que sobreviva mientras la app siga abierta,
    // aunque el usuario navegue a otro módulo y vuelva a entrar a Caja.
    private static boolean cajaAbierta = true;
    private static double montoBaseApertura = 0.0;
    private static String horaApertura = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

    // Totales calculados en la última carga (para el cálculo de la diferencia del arqueo)
    private double totalEfectivoSistema = 0.0;
    private double totalDigitalSistema = 0.0;
    private double totalTarjetaSistema = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        cargarDatosTurno();
        actualizarEstadoVisual();
    }

    /**
     * Trae los totales reales del día desde la tabla `pago` (unida a `venta`).
     * NOTA: el ENUM de pago.metodo solo admite YAPE, PLIN y TARJETA (no existe un
     * método EFECTIVO en la base de datos), por lo que el efectivo del sistema se
     * calcula como el monto base declarado al abrir la caja. Si en el futuro se
     * agrega un método de pago en efectivo a la tabla `pago`, esta suma debería
     * incluirse aquí también.
     */
    private void cargarDatosTurno() {
        totalDigitalSistema = pagoDAO.obtenerTotalBilleterasDigitalesHoy();
        totalTarjetaSistema = pagoDAO.obtenerTotalPorMetodoHoy("TARJETA");
        totalEfectivoSistema = montoBaseApertura;

        double totalTurno = totalEfectivoSistema + totalDigitalSistema + totalTarjetaSistema;

        lblTotalEfectivo.setText(String.format("S/ %.2f", totalEfectivoSistema));
        lblTotalDigital.setText(String.format("S/ %.2f", totalDigitalSistema));
        lblTotalTarjeta.setText(String.format("S/ %.2f", totalTarjetaSistema));
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
            double diferencia = efectivoReal - totalEfectivoSistema;

            lblDiferencia.setText(String.format("S/ %.2f", Math.abs(diferencia)));

            lblDiferencia.getStyleClass().removeAll("texto-sobrante", "texto-faltante");
            lblMensajeDiferencia.getStyleClass().removeAll("texto-sobrante", "texto-faltante");
            lblDiferencia.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

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
                lblDiferencia.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            }

        } catch (NumberFormatException e) {
            lblDiferencia.setText("S/ 0.00");
            lblMensajeDiferencia.setText("Ingrese un valor numérico válido.");
        }
    }

    @FXML
    public void cerrarCaja() {
        if (!cajaAbierta) {
            return;
        }

        String textoReal = txtEfectivoReal.getText() == null ? "" : txtEfectivoReal.getText().trim();
        if (textoReal.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta el arqueo", "Debe ingresar el monto real contado en efectivo antes de cerrar la caja.");
            return;
        }

        double efectivoReal;
        try {
            efectivoReal = Double.parseDouble(textoReal);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Monto inválido", "El monto de efectivo contado no es un número válido.");
            return;
        }

        double diferencia = efectivoReal - totalEfectivoSistema;
        double totalTurno = totalEfectivoSistema + totalDigitalSistema + totalTarjetaSistema;

        String resumen = String.format(
                "Efectivo (sistema): S/ %.2f%nEfectivo (contado): S/ %.2f%nDiferencia: S/ %.2f (%s)%n" +
                        "Digital (Yape/Plin): S/ %.2f%nTarjeta: S/ %.2f%nTotal turno: S/ %.2f",
                totalEfectivoSistema, efectivoReal, Math.abs(diferencia),
                diferencia == 0 ? "Cuadre perfecto" : (diferencia > 0 ? "Sobrante" : "Faltante"),
                totalDigitalSistema, totalTarjetaSistema, totalTurno
        );

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cierre de caja");
        confirmacion.setHeaderText("¿Desea cerrar la caja y guardar este arqueo?");
        confirmacion.setContentText(resumen);

        Optional<javafx.scene.control.ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        cajaAbierta = false;
        actualizarEstadoVisual();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Caja cerrada", "El arqueo se registró correctamente:\n\n" + resumen);
    }

    @FXML
    public void abrirCaja() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Abrir nueva caja");
        dialog.setHeaderText("Apertura de turno");
        dialog.setContentText("Ingrese el monto base en efectivo (S/):");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        double montoBase;
        try {
            montoBase = Double.parseDouble(resultado.get().trim());
            if (montoBase < 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Monto inválido", "El monto base no puede ser negativo.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Monto inválido", "Debe ingresar un valor numérico válido.");
            return;
        }

        montoBaseApertura = montoBase;
        cajaAbierta = true;
        horaApertura = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        txtEfectivoReal.clear();
        lblDiferencia.setText("S/ 0.00");
        lblMensajeDiferencia.setText("");

        cargarDatosTurno();
        actualizarEstadoVisual();
    }

    /** Sincroniza todos los controles visuales con el estado interno (cajaAbierta / horaApertura). */
    private void actualizarEstadoVisual() {
        lblHoraApertura.setText("Hora Apertura: " + horaApertura);

        if (cajaAbierta) {
            lblEstadoCaja.setText("CAJA ABIERTA");
            lblEstadoCaja.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15;");

            txtEfectivoReal.setDisable(false);
            btnCerrarCaja.setVisible(true);
            btnCerrarCaja.setManaged(true);
            btnAbrirCaja.setVisible(false);
            btnAbrirCaja.setManaged(false);
        } else {
            lblEstadoCaja.setText("CAJA CERRADA");
            lblEstadoCaja.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 15;");

            txtEfectivoReal.setDisable(true);
            btnCerrarCaja.setVisible(false);
            btnCerrarCaja.setManaged(false);
            btnAbrirCaja.setVisible(true);
            btnAbrirCaja.setManaged(true);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
