package com.controlador;

import com.modelo.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ControladorVentas {

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> colId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, Integer> colProductos;
    @FXML private TableColumn<Venta, Double> colTotal;
    @FXML private TableColumn<Venta, String> colEstado;
    @FXML private BorderPane panelPrincipal;
    @FXML private ToggleButton btnInicializadorVentas;

    // Declaración del ComboBox
    @FXML private ComboBox<String> cbFiltrosVentas;
    // Lista dinámica donde se guardan las ventas
    private final ObservableList<Venta> listaVentas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // CONFIGURACION DEL COMBOBOX
        cbFiltrosVentas.getItems().addAll("Hoy", "Últimos 7 días", "Ultimos 30 dias", "Todos");
        cbFiltrosVentas.setOnAction(e -> {
            String seleccion = cbFiltrosVentas.getValue();
            System.out.println("Filtrando por: " + seleccion);
        });

        // CONFIGURACION DE LA TABLA
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
    @FXML
    public void abrirNuevaVenta(javafx.event.ActionEvent event) {
        try {
            // 1. Cargamos el diseño de la Nueva Venta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("nuevaventa-view.fxml"));
            javafx.scene.Parent vistaNuevaVenta = loader.load();

            // 2. Usamos el botón clickeado para rastrear y encontrar el BorderPane principal
            javafx.scene.Node boton = (javafx.scene.Node) event.getSource();
            javafx.scene.layout.BorderPane panelPrincipal = (javafx.scene.layout.BorderPane) boton.getScene().getRoot();

            // 3. Reemplazamos el centro del programa con tu nueva vista
            panelPrincipal.setCenter(vistaNuevaVenta);

        } catch (IOException e) {
            System.err.println("Error al cargar la vista de nueva venta");
            e.printStackTrace();
        }
    }


}