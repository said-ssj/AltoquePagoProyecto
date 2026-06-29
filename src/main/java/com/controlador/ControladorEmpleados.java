package com.controlador;

import com.DB.ConexionDB;
import com.dao.UsuarioPersonalDAO;
import com.modelo.UsuarioPersonal;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ControladorEmpleados implements Initializable {

    @FXML private Button btnNuevoEmpleado;
    @FXML private TextField txtBuscarEmpleado;
    @FXML private ComboBox<String> cbFiltrosEmpleados;

    @FXML private TableView<UsuarioPersonal> tablaEmpleados;
    @FXML private TableColumn<UsuarioPersonal, String> colNombre;
    @FXML private TableColumn<UsuarioPersonal, String> colEmail;
    @FXML private TableColumn<UsuarioPersonal, String> colTelefono;
    @FXML private TableColumn<UsuarioPersonal, String> colNumeroDocumento;
    @FXML private TableColumn<UsuarioPersonal, String> colRol;
    @FXML private TableColumn<UsuarioPersonal, Void>   colAcciones;

    private ObservableList<UsuarioPersonal> listaEmpleados;
    private final UsuarioPersonalDAO usuarioDAO = new UsuarioPersonalDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cbFiltrosEmpleados.getItems().addAll("A - Z ⬆", "Z - A ⬇", "Administrador - Rol", "Empleado - Rol");

        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colEmail  != null) colEmail .setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colTelefono != null)
            colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        if (colNumeroDocumento != null)
            colNumeroDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        if (colRol    != null) {
            colRol.setCellValueFactory(cellData -> {
                int rol = cellData.getValue().getIdRol();
                String nombreRol = rol == 1 ? "Administrador" : (rol == 2 ? "Vendedor" : "Almacén");
                return new SimpleStringProperty(nombreRol);
            });
        }

        configurarColumnaAcciones();
        cargarDatosTabla();

        // Búsqueda en tiempo real
        txtBuscarEmpleado.textProperty().addListener((obs, old, nuevo) -> filtrarEmpleados(nuevo));
    }

    // ============================================================
    //  COLUMNA DE ACCIONES (Editar / Eliminar)
    // ============================================================
    private void configurarColumnaAcciones() {
        if (colAcciones == null) return;
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏ Editar");
            private final Button btnEliminar = new Button("🗑 Eliminar");
            private final HBox   contenedor  = new HBox(8, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnEditar  .getStyleClass().add("boton-editar");
                btnEliminar.getStyleClass().add("boton-eliminar");

                btnEditar.setOnAction(e -> {
                    UsuarioPersonal emp = getTableView().getItems().get(getIndex());
                    abrirDialogoEdicion(emp);
                });

                btnEliminar.setOnAction(e -> {
                    UsuarioPersonal emp = getTableView().getItems().get(getIndex());
                    confirmarYEliminar(emp);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    // ============================================================
    //  DIÁLOGO DE EDICIÓN
    // ============================================================
    private void abrirDialogoEdicion(UsuarioPersonal emp) {
        Dialog<UsuarioPersonal> dialog = new Dialog<>();
        dialog.setTitle("Editar Empleado");
        dialog.setHeaderText("Modificar datos de: " + emp.getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        // Campos del formulario
        TextField fNombre = new TextField(emp.getNombre());
        TextField fEmail  = new TextField(emp.getEmail());
        ComboBox<String> fRol = new ComboBox<>();
        fRol.getItems().addAll("Administrador", "Vendedor", "Almacén");
        fRol.setValue(emp.getIdRol() == 1 ? "Administrador" : (emp.getIdRol() == 2 ? "Vendedor" : "Almacén"));

        TextField fNumeroDocumento = new TextField(emp.getNumeroDocumento() != null ? emp.getNumeroDocumento() : "");
        TextField fTelefono = new TextField(emp.getTelefono() != null ? emp.getTelefono() : "");
        TextField fArea     = new TextField(emp.getArea()     != null ? emp.getArea()     : "");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Nombre:"),   fNombre);
        grid.addRow(1, new Label("Email:"),    fEmail);
        grid.addRow(2, new Label("Rol:"),      fRol);
        grid.addRow(3, new Label("N° Documento:"), fNumeroDocumento);
        grid.addRow(4, new Label("Teléfono:"),     fTelefono);
        grid.addRow(5, new Label("Área:"),         fArea);
        fNombre.setPrefWidth(250); fEmail.setPrefWidth(250);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                emp.setNombre(fNombre.getText().trim());
                emp.setEmail(fEmail.getText().trim());
                emp.setIdRol(fRol.getValue().equals("Administrador") ? 1 : fRol.getValue().equals("Vendedor") ? 2 : 3);
                emp.setNumeroDocumento(fNumeroDocumento.getText().trim());
                emp.setTelefono(fTelefono.getText().trim());
                emp.setArea(fArea.getText().trim());
                return emp;
            }
            return null;
        });

        Optional<UsuarioPersonal> resultado = dialog.showAndWait();
        resultado.ifPresent(e -> {
            if (actualizarEmpleadoBD(e)) {
                cargarDatosTabla();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Empleado actualizado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el empleado.");
            }
        });
    }

    // ============================================================
    //  CONFIRMAR Y ELIMINAR
    // ============================================================
    private void confirmarYEliminar(UsuarioPersonal emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("¿Eliminar empleado?");
        confirm.setContentText("Se eliminará a: " + emp.getNombre() + "\nEsta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (eliminarEmpleadoBD(emp.getIdUsuario())) {
                    listaEmpleados.remove(emp);
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado", "Empleado eliminado correctamente.");
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el empleado.");
                }
            }
        });
    }

    // ============================================================
    //  BASE DE DATOS
    // ============================================================
    private boolean actualizarEmpleadoBD(UsuarioPersonal emp) {
        String sql = "UPDATE usuario_personal SET nombre=?, email=?, id_rol=?, numero_documento=?, telefono=?, area=? WHERE id_usuario=?";
        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getEmail());
            ps.setInt   (3, emp.getIdRol());
            ps.setString(4, emp.getNumeroDocumento());
            ps.setString(5, emp.getTelefono());
            ps.setString(6, emp.getArea());
            ps.setInt   (7, emp.getIdUsuario());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean eliminarEmpleadoBD(int id) {
        String sql = "DELETE FROM usuario_personal WHERE id_usuario=?";
        try (Connection cn = ConexionDB.conectar(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ============================================================
    //  CARGA Y FILTRO
    // ============================================================
    private void cargarDatosTabla() {
        if (tablaEmpleados == null) return;
        List<UsuarioPersonal> lista = usuarioDAO.obtenerTodos();
        listaEmpleados = FXCollections.observableArrayList(lista);
        tablaEmpleados.setItems(listaEmpleados);
    }

    private void filtrarEmpleados(String texto) {
        if (texto == null || texto.isBlank()) {
            tablaEmpleados.setItems(listaEmpleados);
            return;
        }
        String lower = texto.toLowerCase();
        ObservableList<UsuarioPersonal> filtrado = listaEmpleados.filtered(
                e -> e.getNombre().toLowerCase().contains(lower) || e.getEmail().toLowerCase().contains(lower)
        );
        tablaEmpleados.setItems(filtrado);
    }

    @FXML
    public void abrirNuevoEmpleado(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/vista/nuevoempleado-view.fxml"));
            javafx.scene.Parent vista = loader.load();
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panel = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();
            panel.setCenter(vista);
        } catch (java.io.IOException e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}