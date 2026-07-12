package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Button;
import com.servicio.ApiSunatServicio;
import com.dao.ClienteDAO;
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

            // NEGOCIO [CLIENTES]: si el cliente escribe/corrige manualmente el
            // correo o la dirección (no vinieron de la API ni de la BD), esos
            // valores también se guardan al finalizar la compra.
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
            // NEGOCIO [CLIENTES]: primero se busca en la BASE DE DATOS LOCAL. Si el
            // cliente ya compró antes, se usan sus datos reales guardados (incluido
            // el correo, que la API nunca provee) en vez de ir a la API.
            Cliente clienteGuardado = new ClienteDAO().buscarPorDocumento(documento);

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
                return; // Ya tenemos los datos reales: no hace falta la API.
            }

            // No está guardado localmente: se consulta la API externa (comportamiento original).
            try {
                JsonObject data = ApiSunatServicio.consultarDocumento(documento);
                Platform.runLater(() -> {
                    if (data != null) {
                        String nombreExtraido = "";
                        String direccionExtraida = "";

                        if (documento.length() == 11) {

                            // La API usa la clave "nombre_o_razon_social"
                            if (data.has("nombre_o_razon_social") && !data.get("nombre_o_razon_social").isJsonNull()) {
                                nombreExtraido = data.get("nombre_o_razon_social").getAsString();
                            }

                            // Validamos tanto "direccion_completa" como "direccion" por seguridad
                            if (data.has("direccion_completa") && !data.get("direccion_completa").isJsonNull()) {
                                direccionExtraida = data.get("direccion_completa").getAsString();
                            } else if (data.has("direccion") && !data.get("direccion").isJsonNull()) {
                                direccionExtraida = data.get("direccion").getAsString();
                            }

                        } else { // LÓGICA PARA DNI (BOLETA)
                            if (data.has("nombre_completo") && !data.get("nombre_completo").isJsonNull()) {
                                nombreExtraido = data.get("nombre_completo").getAsString();
                            } else if (data.has("nombres") && !data.get("nombres").isJsonNull()) {
                                nombreExtraido = data.get("nombres").getAsString();
                            }
                        }

                        // Setear los datos en los TextFields de la interfaz gráfica
                        txtNombreCliente.setText(nombreExtraido);
                        txtDireccionCliente.setText(direccionExtraida);
                        // El correo nunca lo trae la API: queda tal como lo haya escrito el cliente.

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