package com.controlador;

import com.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ControladorVentas {

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> colId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, Integer> colProductos;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;

    // Lista dinámica donde se guardan las ventas
    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Vincular columnas del FXML con las propiedades de la clase Venta
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colProductos.setCellValueFactory(new PropertyValueFactory<>("productos"));

        // Formatear la columna Total para que muestre el signo
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("S/%,.0f", item));
            }
        });

        // Estilo de Badges de colores (Verde/Amarillo) para la columna Estado
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");

                    if (item.equalsIgnoreCase("Completada")) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #166534;");
                    } else if (item.equalsIgnoreCase("Pendiente")) {
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Asignar la lista a la tabla
        tablaVentas.setItems(listaVentas);
        listaVentas.addAll(
                new Venta("#1234", "27/05/2026", "Juan Pérez", 3, 1234, "Completada"),
                new Venta("#1235", "27/05/2026", "María García", 2, 856, "Completada"),
                new Venta("#1236", "26/05/2026", "Carlos López", 5, 2100, "Pendiente")
        );
    }

    // MÉTODO QUE SE EJECUTA AL DAR CLICK EN "+ NUEVA VENTA"
    @FXML
    private void abrirFormularioNuevaVenta() {
        // Crear la ventana de diálogo flotante
        Dialog<Venta> dialog = new Dialog<>();
        dialog.setTitle("Registrar Nueva Venta");
        dialog.setHeaderText("Complete los datos del cliente y de la transacción:");

        // Configurar botones de Confirmar y Cancelar
        ButtonType btnGuardarType = new ButtonType("Guardar Venta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        // Crear el contenedor tipo formulario (Grid)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Componentes de entrada de datos
        TextField txtCliente = new TextField();
        txtCliente.setPromptText("Nombre completo");

        TextField txtProductos = new TextField();
        txtProductos.setPromptText("Ej. 3");

        TextField txtTotal = new TextField();
        txtTotal.setPromptText("Ej. 1500");

        ComboBox<String> cbEstado = new ComboBox<>();
        cbEstado.getItems().addAll("Completada", "Pendiente");
        cbEstado.setValue("Completada"); // Opción por defecto

        // Organizar en la cuadrícula (Columna, Fila)
        grid.add(new Label("Cliente:"), 0, 0);
        grid.add(txtCliente, 1, 0);
        grid.add(new Label("Cantidad Productos:"), 0, 1);
        grid.add(txtProductos, 1, 1);
        grid.add(new Label("Total ($):"), 0, 2);
        grid.add(txtTotal, 1, 2);
        grid.add(new Label("Estado:"), 0, 3);
        grid.add(cbEstado, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convertir la respuesta cuando el usuario presione "Guardar Venta"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) {
                try {
                    // 1. AUTO-GENERAR ID (Siguiente número secuencial)
                    int baseId = 1234 + listaVentas.size();
                    String idAutomatico = "#" + baseId;

                    // 2. AUTO-GENERAR FECHA ACTUAL DE LA PC
                    String fechaAutomatica = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    // 3. OBTENER DATOS INGRESADOS
                    String cliente = txtCliente.getText().trim();
                    int productos = Integer.parseInt(txtProductos.getText().trim());
                    double total = Double.parseDouble(txtTotal.getText().trim());
                    String estado = cbEstado.getValue();

                    if(cliente.isEmpty()) return null; // Validación básica

                    return new Venta(idAutomatico, fechaAutomatica, cliente, productos, total, estado);
                } catch (NumberFormatException e) {
                    // Si meten letras en total o productos, muestra error
                    System.out.println("Error de formato numérico.");
                }
            }
            return null;
        });

        // Mostrar el diálogo y capturar el resultado
        Optional<Venta> resultado = dialog.showAndWait();

        // Si todo salió bien, agregamos el objeto Venta a la lista de la tabla
        resultado.ifPresent(listaVentas::add);
    }
}