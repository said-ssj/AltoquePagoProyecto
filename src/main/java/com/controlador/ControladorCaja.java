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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import com.dao.ArqueoCajaDAO;
import com.modelo.ArqueoCaja;

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
    private final ArqueoCajaDAO arqueoCajaDAO = new ArqueoCajaDAO();

    // Estado del turno, cargado/persistido en la tabla arqueo_caja
    private boolean cajaAbierta = false;
    private int idArqueoActual = -1;
    private double montoBaseApertura = 0.0;
    private LocalDateTime fechaApertura;

    // Totales calculados en la última carga (para el cálculo de la diferencia del arqueo)
    private double totalEfectivoSistema = 0.0;
    private double totalYapeSistema = 0.0;
    private double totalPlinSistema = 0.0;
    private double totalTarjetaSistema = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblFecha.setText("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Recupera de la BD si ya hay un turno abierto (por ejemplo si se cerró la app sin cerrar caja)
        ArqueoCaja abierto = arqueoCajaDAO.obtenerArqueoAbierto();
        if (abierto != null) {
            cajaAbierta = true;
            idArqueoActual = abierto.getIdArqueo();
            montoBaseApertura = abierto.getMontoInicial();
            fechaApertura = abierto.getFechaApertura();
        } else {
            cajaAbierta = false;
            idArqueoActual = -1;
            montoBaseApertura = 0.0;
            fechaApertura = null;
        }

        cargarDatosTurno();
        actualizarEstadoVisual();
    }

    /**
     * Trae los totales reales del día desde la tabla `pago` (unida a `venta`).
     * NOTA: el ENUM de pago.metodo solo admite YAPE, PLIN y TARJETA (no existe un
     * método EFECTIVO en la base de datos), por lo que el efectivo del sistema se
     * calcula como el monto base declarado al abrir la caja (columna monto_inicial
     * de arqueo_caja).
     */
    private void cargarDatosTurno() {
        totalYapeSistema = pagoDAO.obtenerTotalPorMetodoHoy("YAPE");
        totalPlinSistema = pagoDAO.obtenerTotalPorMetodoHoy("PLIN");
        totalTarjetaSistema = pagoDAO.obtenerTotalPorMetodoHoy("TARJETA");
        totalEfectivoSistema = montoBaseApertura;

        double totalDigital = totalYapeSistema + totalPlinSistema;
        double totalTurno = totalEfectivoSistema + totalDigital + totalTarjetaSistema;

        lblTotalEfectivo.setText(String.format("S/ %.2f", totalEfectivoSistema));
        lblTotalDigital.setText(String.format("S/ %.2f", totalDigital));
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
        if (!cajaAbierta || idArqueoActual == -1) {
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
        double totalDigital = totalYapeSistema + totalPlinSistema;
        double totalVentas = totalDigital + totalTarjetaSistema;
        double totalTurno = totalEfectivoSistema + totalVentas;

        String resumen = String.format(
                "Efectivo (sistema): S/ %.2f%nEfectivo (contado): S/ %.2f%nDiferencia: S/ %.2f (%s)%n" +
                        "Yape: S/ %.2f%nPlin: S/ %.2f%nTarjeta: S/ %.2f%nTotal turno: S/ %.2f",
                totalEfectivoSistema, efectivoReal, Math.abs(diferencia),
                diferencia == 0 ? "Cuadre perfecto" : (diferencia > 0 ? "Sobrante" : "Faltante"),
                totalYapeSistema, totalPlinSistema, totalTarjetaSistema, totalTurno
        );

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cierre de caja");
        confirmacion.setHeaderText("¿Desea cerrar la caja y guardar este arqueo?");
        confirmacion.setContentText(resumen);

        Optional<javafx.scene.control.ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        boolean guardado = arqueoCajaDAO.cerrarArqueo(
                idArqueoActual, totalEfectivoSistema, efectivoReal, diferencia,
                totalYapeSistema, totalPlinSistema, totalTarjetaSistema, totalVentas
        );

        if (!guardado) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar",
                    "No se pudo guardar el arqueo en la base de datos. Verifique la conexión e intente nuevamente.");
            return;
        }

        cajaAbierta = false;
        idArqueoActual = -1;
        actualizarEstadoVisual();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Caja cerrada", "El arqueo se registró correctamente:\n\n" + resumen);
    }

    @FXML
    public void abrirCaja() {
        if (cajaAbierta) {
            mostrarAlerta(Alert.AlertType.WARNING, "Caja ya abierta", "Ya existe un turno de caja abierto.");
            return;
        }

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

        // id_usuario queda en NULL: el proyecto todavía no guarda una sesión global del empleado logueado.
        int idGenerado = arqueoCajaDAO.abrirArqueo(montoBase, null);
        if (idGenerado == -1) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al abrir caja",
                    "No se pudo registrar la apertura en la base de datos. Verifique la conexión e intente nuevamente.");
            return;
        }

        montoBaseApertura = montoBase;
        cajaAbierta = true;
        idArqueoActual = idGenerado;
        fechaApertura = LocalDateTime.now();

        txtEfectivoReal.clear();
        lblDiferencia.setText("S/ 0.00");
        lblMensajeDiferencia.setText("");

        cargarDatosTurno();
        actualizarEstadoVisual();
    }

    /** Sincroniza todos los controles visuales con el estado interno. */
    private void actualizarEstadoVisual() {
        lblHoraApertura.setText("Hora Apertura: " +
                (fechaApertura != null ? fechaApertura.format(DateTimeFormatter.ofPattern("hh:mm a")) : "--:--"));

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
