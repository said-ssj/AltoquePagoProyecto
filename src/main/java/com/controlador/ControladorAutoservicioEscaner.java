package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import com.servicio.ProductoServicio;
import com.modelo.Producto;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

public class ControladorAutoservicioEscaner {

    @FXML
    private TextField txtLectorBarras;

    @FXML
    private VBox panelCarrito;

    @FXML
    private VBox panelProductos;

    @FXML
    private TextField txtCodigoBarra;

    private ControladorAutoservicioCheckoutDividida contenedorPadre;

    private final ProductoServicio productoServicio = new ProductoServicio();
    private final Map<String, CardCarrito> mapaCarrito = new HashMap<>();

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (txtLectorBarras.getScene() != null) {
                txtLectorBarras.getScene().setOnKeyPressed(event -> {
                    if (!txtLectorBarras.isFocused() && event.getText() != null && !event.getText().isEmpty()) {
                        txtLectorBarras.requestFocus();
                        txtLectorBarras.appendText(event.getText());
                    }
                });
            }
            txtLectorBarras.requestFocus();
        });

        txtLectorBarras.setOnAction(event -> {
            procesarEscaneo();
        });
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }

    private void procesarEscaneo() {
        // Buscamos el texto de forma segura intentando de ambos campos por si acaso
        String codigo = "";
        if (txtLectorBarras != null && !txtLectorBarras.getText().trim().isEmpty()) {
            codigo = txtLectorBarras.getText().trim();
            txtLectorBarras.clear();
        } else if (txtCodigoBarra != null && !txtCodigoBarra.getText().trim().isEmpty()) {
            codigo = txtCodigoBarra.getText().trim();
            txtCodigoBarra.clear();
        }

        System.out.println("-> ¡Intento de lectura detectado!: [" + codigo + "]");

        if (codigo.isEmpty()) {
            return;
        }

        // Buscar en la base de datos usando tu servicio existente
        Producto producto = productoServicio.buscarProducto(codigo);

        if (producto != null) {
            System.out.println("-> Producto encontrado: " + producto.getNombre() + " | Stock: " + producto.getStock());
            if (producto.getStock() <= 0) {
                System.out.println("-> Error: El producto no cuenta con stock.");
                return;
            }

            // Forzar que la actualización corra en el hilo gráfico de JavaFX
            Platform.runLater(() -> {
                añadirAlCarritoVisual(producto);
            });

        } else {
            System.out.println("-> Código '" + codigo + "' no registrado en la base de datos.");
        }

        // Devolver siempre el foco al lector listo para el siguiente artículo
        if (txtLectorBarras != null) txtLectorBarras.requestFocus();
        else if (txtCodigoBarra != null) txtCodigoBarra.requestFocus();
    }

    private void añadirAlCarritoVisual(Producto producto) {
        String codigo = producto.getCodigo_barras();
        double precioProducto = producto.getPrecio(); // O getPrecioVenta() según tu modelo

        if (mapaCarrito.containsKey(codigo)) {
            CardCarrito cardExistente = mapaCarrito.get(codigo);
            if (cardExistente.getCantidad() < producto.getStock()) {
                cardExistente.incrementarCantidad();
                System.out.println("-> Cantidad incrementada para: " + producto.getNombre());

                // ACOPLAMIENTO: Notificamos al padre para que sume este incremento al total
                if (contenedorPadre != null) {
                    contenedorPadre.agregarProductoAlTotal(precioProducto);
                }
            } else {
                System.out.println("-> Límite de stock alcanzado para este ítem.");
            }
        } else {
            CardCarrito nuevaCard = new CardCarrito(producto);
            mapaCarrito.put(codigo, nuevaCard);
            panelCarrito.getChildren().add(nuevaCard.getNodoVisual());
            System.out.println("-> Agregado nueva tarjeta al carrito para: " + producto.getNombre());

            // ACOPLAMIENTO: Notificamos al padre para sumar la primera unidad del nuevo ítem
            if (contenedorPadre != null) {
                contenedorPadre.agregarProductoAlTotal(precioProducto);
            }
        }
    }

    private static class CardCarrito {
        private final Producto producto;
        private int cantidad;
        private final HBox contenedorVisual;
        private final Label lblCantidad;

        public CardCarrito(Producto producto) {
            this.producto = producto;
            this.cantidad = 1;

            this.contenedorVisual = new HBox();
            this.contenedorVisual.setAlignment(Pos.CENTER_LEFT);
            this.contenedorVisual.getStyleClass().add("item-carrito-tarjeta");

            VBox infoProducto = new VBox();
            infoProducto.getStyleClass().add("item-carrito-info");
            HBox.setHgrow(infoProducto, javafx.scene.layout.Priority.ALWAYS);

            Label lblNombre = new Label(producto.getNombre());
            lblNombre.getStyleClass().add("item-carrito-nombre");
            lblNombre.setWrapText(true);

            Label lblPrecio = new Label("S/ " + String.format("%.2f", producto.getPrecio()));
            lblPrecio.getStyleClass().add("item-carrito-precio");

            infoProducto.getChildren().addAll(lblNombre, lblPrecio);

            this.lblCantidad = new Label("x" + cantidad);
            this.lblCantidad.getStyleClass().add("item-carrito-cantidad");
            this.lblCantidad.setAlignment(Pos.CENTER_RIGHT);

            this.contenedorVisual.getChildren().addAll(infoProducto, this.lblCantidad);
        }

        public void incrementarCantidad() {
            this.cantidad++;
            this.lblCantidad.setText("x" + this.cantidad);
        }

        public int getCantidad() {
            return cantidad;
        }

        public HBox getNodoVisual() {
            return contenedorVisual;
        }
    }
}