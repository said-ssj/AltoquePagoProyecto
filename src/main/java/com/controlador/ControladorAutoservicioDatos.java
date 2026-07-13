/*
 * Gestionamos la captura y validación de los datos de facturación del cliente en el kiosko.
 * Hemos aplicado el Principio de Inversión de Dependencias (DIP) utilizando la abstracción IClienteDAO
 * inyectada en el constructor para eliminar los acoplamientos rígidos con la base de datos, manteniendo
 * la delegación de hilos en cumplimiento con el diseño arquitectónico estructurado.
 */
package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Button;
import com.servicio.ApiSunatServicio;
import com.dao.ClienteDAO;
import com.dao.IClienteDAO;
import com.modelo.Cliente;
import com.google.gson.JsonObject;
import javafx.application.Platform;

public class ControladorAutoservicioDatos {

    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtCorreoCliente;
    @FXML private TextField txtDireccionCliente;
    @FXML private ToggleButton btnBoleta;
    @FXML private ToggleButton btnFactura;
    @FXML private Button btnBuscarDoc;

    private ControladorAutoservicioCheckoutDividida contenedorPadre;
    private final IClienteDAO clienteDAO;

    public ControladorAutoservicioDatos() {
        this.clienteDAO = new ClienteDAO();
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (txtDocumento == null) return;
            txtDocumento.setText("00000000");
            txtNombreCliente.setText("CLIENTES VARIOS");
            btnBuscarDoc.setOnAction(event -> ejecutarBusquedaDocumento());

            txtDocumento.textProperty().addListener((observable, oldValue, newValue) -> {
                if (contenedorPadre == null) return;
                String docLimpio = (newValue == null) ? "" : newValue.trim();
                if (docLimpio.isEmpty() || docLimpio.equals("00000000")) {
                    txtNombreCliente.setText("CLIENTES VARIOS");
                    txtDireccionCliente.clear();
                    txtCorreoCliente.clear();
                    contenedorPadre.modificarBotonDatos(false);
                    contenedorPadre.setDatosCliente("00000000", "CLIENTES VARIOS", "", "");
                } else {
                    contenedorPadre.modificarBotonDatos(true);
                }
            });

            txtCorreoCliente.textProperty().addListener((obs, viejo, nuevo) -> sincronizarDatosConPadre());
            txtDireccionCliente.textProperty().addListener((obs, viejo, nuevo) -> sincronizarDatosConPadre());
        });
    }

    private void sincronizarDatosConPadre() {
        if (contenedorPadre == null) return;
        String documento = txtDocumento.getText() != null ? txtDocumento.getText().trim() : "00000000";
        if (documento.isEmpty() || documento.equals("00000000")) return;
        contenedorPadre.setDatosCliente(
                documento,
                txtNombreCliente.getText() != null ? txtNombreCliente.getText() : "",
                txtCorreoCliente.getText() != null ? txtCorreoCliente.getText() : "",
                txtDireccionCliente.getText() != null ? txtDireccionCliente.getText() : ""
        );
    }

    private void ejecutarBusquedaDocumento() {
        String documento = txtDocumento.getText().trim();
        if (documento.length() != 8 && documento.length() != 11) return;
        btnBuscarDoc.setDisable(true);

        new Thread(() -> {
            Cliente clienteGuardado = clienteDAO.buscarPorDocumento(documento);
            if (clienteGuardado != null) {
                Platform.runLater(() -> {
                    String nombreMostrar = (documento.length() == 11 && clienteGuardado.getRazonSocial() != null
                            && !clienteGuardado.getRazonSocial().trim().isEmpty())
                            ? clienteGuardado.getRazonSocial()
                            : clienteGuardado.getNombre();
                    txtNombreCliente.setText(nombreMostrar != null ? nombreMostrar : "");
                    txtDireccionCliente.setText(clienteGuardado.getDireccion() != null ? clienteGuardado.getDireccion() : "");
                    txtCorreoCliente.setText(clienteGuardado.getCorreo() != null ? clienteGuardado.getCorreo() : "");
                    if (contenedorPadre != null) {
                        contenedorPadre.setDatosCliente(
                                documento,
                                nombreMostrar != null ? nombreMostrar : "",
                                clienteGuardado.getCorreo() != null ? clienteGuardado.getCorreo() : "",
                                clienteGuardado.getDireccion() != null ? clienteGuardado.getDireccion() : ""
                        );
                    }
                    btnBuscarDoc.setDisable(false);
                });
                return;
            }

            try {
                JsonObject data = ApiSunatServicio.consultarDocumento(documento);
                Platform.runLater(() -> {
                    if (data != null) {
                        String nombreExtraido = "";
                        String direccionExtraida = "";
                        if (documento.length() == 11) {
                            if (data.has("nombre_o_razon_social") && !data.get("nombre_o_razon_social").isJsonNull()) {
                                nombreExtraido = data.get("nombre_o_razon_social").getAsString();
                            }
                            if (data.has("direccion_completa") && !data.get("direccion_completa").isJsonNull()) {
                                direccionExtraida = data.get("direccion_completa").getAsString();
                            } else if (data.has("direccion") && !data.get("direccion").isJsonNull()) {
                                direccionExtraida = data.get("direccion").getAsString();
                            }
                        } else {
                            if (data.has("nombre_completo") && !data.get("nombre_completo").isJsonNull()) {
                                nombreExtraido = data.get("nombre_completo").getAsString();
                            } else if (data.has("nombres") && !data.get("nombres").isJsonNull()) {
                                nombreExtraido = data.get("nombres").getAsString();
                            }
                        }
                        txtNombreCliente.setText(nombreExtraido);
                        txtDireccionCliente.setText(direccionExtraida);
                        if (contenedorPadre != null) {
                            contenedorPadre.setDatosCliente(
                                    documento,
                                    nombreExtraido,
                                    txtCorreoCliente.getText() != null ? txtCorreoCliente.getText() : "",
                                    direccionExtraida
                            );
                        }
                    }
                    btnBuscarDoc.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> btnBuscarDoc.setDisable(false));
            }
        }).start();
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }
}