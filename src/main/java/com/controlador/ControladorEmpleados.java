/*
 * Gestionamos la interfaz gráfica para el mantenimiento (CRUD) y asignación de roles del personal.
 * Hemos adaptado el controlador para que trabaje directamente con 'idRol' como un entero (1=ADMIN, 2=CAJERO, 3=ALMACENERO),
 * alineándonos al modelo original del sistema y cumpliendo el Principio de Inversión de Dependencias (DIP).
 */
package com.controlador;

import com.dao.IUsuarioPersonalDAO;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorEmpleados implements Initializable {

    @FXML private TextField txtBuscarEmpleado;
    @FXML private TableView<UsuarioPersonal> tablaEmpleados;
    @FXML private TableColumn<UsuarioPersonal, Integer> colId;
    @FXML private TableColumn<UsuarioPersonal, String> colNombre;
    @FXML private TableColumn<UsuarioPersonal, String> colEmail;
    @FXML private TableColumn<UsuarioPersonal, Integer> colRol; // Cambiado a Integer para mapear idRol
    @FXML private TableColumn<UsuarioPersonal, String> colEstado;
    @FXML private TableColumn<UsuarioPersonal, String> colTelefono;
    @FXML private TableColumn<UsuarioPersonal, String> colNumeroDocumento;
    @FXML private TableColumn<UsuarioPersonal, Void> colAcciones;

    private final IUsuarioPersonalDAO usuarioPersonalDAO;
    private final ObservableList<UsuarioPersonal> listaEmpleados = FXCollections.observableArrayList();

    public ControladorEmpleados() {
        this.usuarioPersonalDAO = new UsuarioPersonalDAO();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("area")); // Usamos area o estado según tu FXML
        if (colTelefono != null) colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        if (colNumeroDocumento != null) colNumeroDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));

        // Traducimos el idRol numérico a texto legible en la tabla de forma dinámica
        if (colRol != null) {
            colRol.setCellValueFactory(new PropertyValueFactory<>("idRol"));
            colRol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Integer idRol, boolean empty) {
                    super.updateItem(idRol, empty);
                    if (empty || idRol == null) {
                        setText(null);
                    } else {
                        setText(convertirIdRolATexto(idRol));
                    }
                }
            });
        }

        configurarColumnaAcciones();

        if (txtBuscarEmpleado != null) {
            txtBuscarEmpleado.textProperty().addListener((obs, oldVal, newVal) -> filtrarEmpleados(newVal));
        }

        cargarDatosTabla();
    }

    private void cargarDatosTabla() {
        if (tablaEmpleados == null) return;
        listaEmpleados.setAll(usuarioPersonalDAO.obtenerTodos());
        tablaEmpleados.setItems(listaEmpleados);
    }

    private void configurarColumnaAcciones() {
        if (colAcciones == null) return;
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏ Editar");
            private final Button btnEliminar = new Button("🗑 Dar de Baja");
            private final HBox contenedor = new HBox(8, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnEditar.getStyleClass().add("boton-editar");
                btnEliminar.getStyleClass().add("boton-eliminar");

                btnEditar.setOnAction(e -> {
                    UsuarioPersonal emp = getTableView().getItems().get(getIndex());
                    abrirDialogoEdicion(emp);
                });
                btnEliminar.setOnAction(e -> {
                    UsuarioPersonal emp = getTableView().getItems().get(getIndex());
                    confirmarBajaEmpleado(emp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void abrirDialogoEdicion(UsuarioPersonal emp) {
        Dialog<UsuarioPersonal> dialog = new Dialog<>();
        dialog.setTitle("Editar Personal");
        dialog.setHeaderText("Modificar ficha de: " + emp.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField fNombre = new TextField(emp.getNombre());
        TextField fEmail = new TextField(emp.getEmail());

        ComboBox<String> fRol = new ComboBox<>();
        fRol.getItems().addAll("ADMINISTRADOR", "CAJERO", "ALMACENERO");
        fRol.setValue(convertirIdRolATexto(emp.getIdRol()));

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nombre completo:"), fNombre);
        grid.addRow(1, new Label("Correo Electrónico:"), fEmail);
        grid.addRow(2, new Label("Rol / Permisos:"), fRol);
        fNombre.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                if (fNombre.getText().trim().isEmpty() || fEmail.getText().trim().isEmpty()) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "El nombre y el correo son obligatorios.");
                    return null;
                }
                if (!com.servicio.ValidacionFormatos.validarCorreo(fEmail.getText().trim())) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Correo Inválido", "Ingresa un correo con formato válido (debe contener \"@\"). Ej: usuario@correo.com");
                    return null;
                }
                emp.setNombre(fNombre.getText().trim());
                emp.setEmail(fEmail.getText().trim());

                // Mapeamos de vuelta el String del combo al int requerido por tu setter original
                emp.setIdRol(convertirTextoAIdRol(fRol.getValue()));
                return emp;
            }
            return null;
        });

        Optional<UsuarioPersonal> resultado = dialog.showAndWait();
        resultado.ifPresent(u -> {
            if (u != null && usuarioPersonalDAO.actualizar(u)) {
                cargarDatosTabla();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Actualizado", "Datos del empleado modificados con éxito.");
            } else if (u != null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron salvar los cambios.");
            }
        });
    }

    private void confirmarBajaEmpleado(UsuarioPersonal emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Salida");
        confirm.setHeaderText("¿Remover empleado?");
        confirm.setContentText("Se eliminará a: " + emp.getNombre());

        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (usuarioPersonalDAO.eliminarUsuario(emp.getIdUsuario())) {
                    listaEmpleados.remove(emp);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "El empleado ha sido removido.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar al usuario.");
                }
            }
        });
    }

    private void filtrarEmpleados(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaEmpleados.setItems(listaEmpleados);
            return;
        }

        String lower = texto.toLowerCase();
        ObservableList<UsuarioPersonal> filtrado = listaEmpleados.filtered(emp ->
                emp.getNombre().toLowerCase().contains(lower)
                        || emp.getEmail().toLowerCase().contains(lower)
        );
        tablaEmpleados.setItems(filtrado);
    }

    // Métodos utilitarios de conversión enteros <-> textos
    private String convertirIdRolATexto(int idRol) {
        return switch (idRol) {
            case 1 -> "ADMINISTRADOR";
            case 2 -> "CAJERO";
            case 3 -> "ALMACENERO";
            default -> "SIN ROL";
        };
    }

    private int convertirTextoAIdRol(String rol) {
        if (rol == null) return 2; // Por defecto Cajero
        return switch (rol) {
            case "ADMINISTRADOR" -> 1;
            case "CAJERO" -> 2;
            case "ALMACENERO" -> 3;
            default -> 2;
        };
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    // ============================================================
    //  NAVEGACIÓN
    // ============================================================
    @FXML
    public void abrirNuevoEmpleado(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoempleado-view.fxml"));
            Parent vista = loader.load();
            Node boton = (Node) event.getSource();
            BorderPane panel = (BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de nuevo empleado");
            e.printStackTrace();
        }
    }
}