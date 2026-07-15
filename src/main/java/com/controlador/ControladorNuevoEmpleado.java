/*
 * En este controlador gestionamos el flujo estructurado para registrar un nuevo
 * empleado mediante paneles tipo acordeón y validaciones por sección. Hemos
 * aplicado el Principio de Inversión de Dependencias (SOLID / DIP), extrayendo
 * la persistencia hacia la abstracción IUsuarioPersonalDAO. De esta manera,
 * mantenemos la compleja lógica visual y de estados totalmente separada
 * de las consultas y transacciones hacia la base de datos.
 */
package com.controlador;

import com.dao.IUsuarioPersonalDAO;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import com.servicio.NotificacionServicio;
import com.servicio.Seguridad;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ControladorNuevoEmpleado {

    // ================== SECCIÓN 1: INFO PERSONAL ==================
    @FXML private Button btnMostrarInfoPersonal, btnCerrarInfoPersonal, btnGuardarInfoPersonal;
    @FXML private VBox panelInfoPersonal;
    @FXML private TextField txtNombre, txtNumeroDocumento, txtNacionalidad;
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private DatePicker dpFechaNacimiento;

    // ================== SECCIÓN 2: CONTACTO Y ACCESO ==================
    @FXML private Button btnMostrarContacto, btnCerrarContacto, btnGuardarContacto;
    @FXML private VBox panelContacto;
    @FXML private TextField txtEmail, txtTelefono, txtDireccion, txtTelefonoEmergencia;
    @FXML private PasswordField txtContraseña;
    @FXML private ComboBox<String> cbRol;

    // ================== SECCIÓN 3: LABORAL ==================
    @FXML private Button btnMostrarLaboral, btnCerrarLaboral, btnGuardarLaboral;
    @FXML private VBox panelLaboral;
    @FXML private ComboBox<String> cbArea, cbTipoContrato;
    @FXML private DatePicker dpFechaInicio;

    // ================== SECCIÓN 4: COMPENSACIÓN ==================
    @FXML private Button btnMostrarCompensacion, btnCerrarCompensacion, btnGuardarCompensacion;
    @FXML private VBox panelCompensacion;
    @FXML private TextField txtSalario, txtDatosBancarios;
    @FXML private ComboBox<String> cbMetodoPago;

    // ================== SECCIÓN 5: DOCUMENTACIÓN ==================
    @FXML private Button btnMostrarDocumentacion, btnCerrarDocumentacion, btnGuardarDocumentacion;
    @FXML private VBox panelDocumentacion;
    @FXML private ComboBox<String> cbAntecedentes;

    // ================== GENERALES ==================
    @FXML private Button btnGuardarGeneral, btnCancelar;

    // Abstracción inyectada (SOLID)
    private final IUsuarioPersonalDAO usuarioDAO;

    // Variables para controlar si las secciones obligatorias están listas
    private boolean secInfoLista = false;
    private boolean secContactoLista = false;
    private boolean secLaboralLista = false;
    private boolean secCompensacionLista = false;

    public ControladorNuevoEmpleado() {
        this.usuarioDAO = new UsuarioPersonalDAO();
    }

    @FXML
    public void initialize() {
        // 1. Inicializar listas de los ComboBox
        cbTipoDocumento.getItems().addAll("DNI", "Carnet de Extranjería", "Pasaporte");
        cbTipoDocumento.setValue("DNI");

        cbRol.getItems().addAll("Administrador", "Vendedor", "Almacén");
        cbArea.getItems().addAll("Administración", "Ventas", "Almacén");
        cbTipoContrato.getItems().addAll("Tiempo Completo", "Medio Tiempo", "Por Recibos");
        cbMetodoPago.getItems().addAll("Efectivo", "Transferencia BCP", "Transferencia BBVA", "Interbancario");
        cbAntecedentes.getItems().addAll("Pendiente", "Entregado y Limpio", "Con Observaciones");
        cbAntecedentes.setValue("Pendiente");

        // 2. Configurar eventos para Abrir Paneles (Botones Azules Principales)
        btnMostrarInfoPersonal.setOnAction(e -> abrirPanel(panelInfoPersonal));
        btnMostrarContacto.setOnAction(e -> abrirPanel(panelContacto));
        btnMostrarLaboral.setOnAction(e -> abrirPanel(panelLaboral));
        btnMostrarCompensacion.setOnAction(e -> abrirPanel(panelCompensacion));
        btnMostrarDocumentacion.setOnAction(e -> abrirPanel(panelDocumentacion));

        // 3. Configurar eventos para Cerrar Paneles (La 'X' en la esquina)
        btnCerrarInfoPersonal.setOnAction(e -> cerrarPanel(panelInfoPersonal));
        btnCerrarContacto.setOnAction(e -> cerrarPanel(panelContacto));
        btnCerrarLaboral.setOnAction(e -> cerrarPanel(panelLaboral));
        btnCerrarCompensacion.setOnAction(e -> cerrarPanel(panelCompensacion));
        btnCerrarDocumentacion.setOnAction(e -> cerrarPanel(panelDocumentacion));

        // 4. Configurar Validaciones de cada sección ("Confirmar Sección")
        btnGuardarInfoPersonal.setOnAction(e -> validarInfoPersonal());
        btnGuardarContacto.setOnAction(e -> validarContacto());
        btnGuardarLaboral.setOnAction(e -> validarLaboral());
        btnGuardarCompensacion.setOnAction(e -> validarCompensacion());

        // La documentación es opcional, solo se guarda y se pinta azul
        btnGuardarDocumentacion.setOnAction(e -> {
            cerrarPanel(panelDocumentacion);
            marcarBotonCompleto(btnMostrarDocumentacion);
        });
    }

    // ============================================================
    // MÉTODOS DE ANIMACIÓN DE PANELES
    // ============================================================
    private void abrirPanel(VBox panel) {
        // Cierra todos los demás paneles para efecto acordeón
        cerrarPanel(panelInfoPersonal);
        cerrarPanel(panelContacto);
        cerrarPanel(panelLaboral);
        cerrarPanel(panelCompensacion);
        cerrarPanel(panelDocumentacion);

        panel.setVisible(true);
        panel.setManaged(true);
    }

    private void cerrarPanel(VBox panel) {
        panel.setVisible(false);
        panel.setManaged(false);
    }

    private void marcarBotonCompleto(Button btn) {
        btn.getStyleClass().remove("btn-seccion-incompleta");
        if (!btn.getStyleClass().contains("btn-seccion-completa")) {
            btn.getStyleClass().add("btn-seccion-completa");
        }
    }

    private void revisarBotonGuardarGeneral() {
        // Si las 4 secciones obligatorias están listas, se activa el botón grande
        btnGuardarGeneral.setDisable(!(secInfoLista && secContactoLista && secLaboralLista && secCompensacionLista));
    }

    // ============================================================
    // VALIDACIONES POR SECCIÓN
    // ============================================================

    private void validarInfoPersonal() {
        if (txtNombre.getText().trim().isEmpty() || txtNumeroDocumento.getText().trim().isEmpty() || dpFechaNacimiento.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan Datos", "Llena Nombre, Fecha de Nacimiento y Documento.");
            secInfoLista = false;
        } else {
            secInfoLista = true;
            cerrarPanel(panelInfoPersonal);
            marcarBotonCompleto(btnMostrarInfoPersonal);
        }
        revisarBotonGuardarGeneral();
    }

    private void validarContacto() {
        if (txtEmail.getText().trim().isEmpty() || txtContraseña.getText().isEmpty() || cbRol.getValue() == null || txtTelefono.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan Datos", "Llena Correo, Contraseña, Rol y Teléfono.");
            secContactoLista = false;
        } else if (!com.servicio.ValidacionFormatos.validarCorreo(txtEmail.getText().trim())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo Inválido", "Ingresa un correo con formato válido (debe contener \"@\"). Ej: usuario@correo.com");
            secContactoLista = false;
        } else {
            secContactoLista = true;
            cerrarPanel(panelContacto);
            marcarBotonCompleto(btnMostrarContacto);
        }
        revisarBotonGuardarGeneral();
    }

    private void validarLaboral() {
        if (cbArea.getValue() == null || cbTipoContrato.getValue() == null || dpFechaInicio.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan Datos", "Selecciona Área, Contrato y Fecha de Inicio.");
            secLaboralLista = false;
        } else {
            secLaboralLista = true;
            cerrarPanel(panelLaboral);
            marcarBotonCompleto(btnMostrarLaboral);
        }
        revisarBotonGuardarGeneral();
    }

    private void validarCompensacion() {
        if (txtSalario.getText().trim().isEmpty() || cbMetodoPago.getValue() == null || txtDatosBancarios.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan Datos", "Ingresa Salario, Método de Pago y Datos Bancarios.");
            secCompensacionLista = false;
        } else {
            // Validar que el salario sea un número
            try {
                Double.parseDouble(txtSalario.getText().trim());
                secCompensacionLista = true;
                cerrarPanel(panelCompensacion);
                marcarBotonCompleto(btnMostrarCompensacion);
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Salario Inválido", "El salario debe ser un número (Ej. 1200.50).");
                secCompensacionLista = false;
            }
        }
        revisarBotonGuardarGeneral();
    }

    // ============================================================
    // GUARDADO GENERAL (Base de Datos)
    // ============================================================
    @FXML
    public void guardarEmpleadoGeneral() {
        try {
            // 1. Mapear el Rol a ID numérico
            int idRol = cbRol.getValue().equals("Administrador") ? 1 : (cbRol.getValue().equals("Almacén") ? 3 : 2);

            // 2. Encriptar Contraseña
            String passEncriptado = Seguridad.encriptarPassword(txtContraseña.getText());

            // 3. Crear el objeto vacío e ir llenándolo con lo que digitó el usuario
            UsuarioPersonal nuevo = new UsuarioPersonal();

            // Sección 1 & 2
            nuevo.setNombre(txtNombre.getText().trim());
            nuevo.setEmail(txtEmail.getText().trim());
            nuevo.setContraseña(passEncriptado);
            nuevo.setIdRol(idRol);
            nuevo.setFechaNacimiento(dpFechaNacimiento.getValue().toString());
            nuevo.setTipoDocumento(cbTipoDocumento.getValue());
            nuevo.setNumeroDocumento(txtNumeroDocumento.getText().trim());
            nuevo.setNacionalidad(txtNacionalidad.getText().trim());
            nuevo.setDireccion(txtDireccion.getText() != null ? txtDireccion.getText().trim() : "");
            nuevo.setTelefono(txtTelefono.getText().trim());
            nuevo.setTelefonoEmergencia(txtTelefonoEmergencia.getText() != null ? txtTelefonoEmergencia.getText().trim() : "");

            // Sección 3
            nuevo.setArea(cbArea.getValue());
            nuevo.setTipoContrato(cbTipoContrato.getValue());
            nuevo.setFechaInicio(dpFechaInicio.getValue().toString());

            // Sección 4 & 5
            nuevo.setSalarioBase(Double.parseDouble(txtSalario.getText().trim()));
            nuevo.setMetodoPago(cbMetodoPago.getValue());
            nuevo.setDatosBancarios(txtDatosBancarios.getText().trim());
            nuevo.setAntecedentes(cbAntecedentes.getValue());

            // 4. Mandar todo a MySQL a través del DAO inyectado
            boolean exito = usuarioDAO.guardarUsuario(nuevo);

            if (exito) {
                NotificacionServicio.getInstancia().notificarEmpleadoNuevo(nuevo.getNombre(), cbRol.getValue());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El empleado ha sido registrado correctamente y sus credenciales han sido encriptadas.");
                abrirEmpleados(new javafx.event.ActionEvent(btnGuardarGeneral, null));
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo registrar el empleado. Revisa si el correo ya existe.");
            }

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error del Sistema", "Ocurrió un problema inesperado al guardar el empleado.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ============================================================
    // NAVEGACIÓN
    // ============================================================
    @FXML
    public void abrirEmpleados(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/empleados-view.fxml"));
            javafx.scene.Parent vistaEmpleados = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            BorderPane panelPrincipal = (BorderPane) boton.getScene().getRoot();
            panelPrincipal.setCenter(vistaEmpleados);
        } catch (IOException e) {
            System.err.println("Error al volver a la vista de empleados");
            e.printStackTrace();
        }
    }
}