package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.application.Platform;

// Imports de tus servicios, DAOs y modelos
import com.servicio.ProductoServicio;
import com.modelo.Producto;
import com.modelo.Oferta;
import com.dao.OfertaDAO;
import com.dao.CarritoDAO;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class ControladorAutoservicioEscaner {

    @FXML private TextField txtLectorBarras;
    @FXML private VBox panelCarrito;
    @FXML private VBox panelProductos;
    @FXML private TextField txtCodigoBarra;

    private ControladorAutoservicioCheckoutDividida contenedorPadre;

    // ==========================================
    // VARIABLES DE CLASE (Declaradas una sola vez)
    // ==========================================
    private final ProductoServicio productoServicio = new ProductoServicio();
    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final CarritoDAO carritoDAO = new CarritoDAO();

    // Rastreo del carrito en la base de datos
    private int idCarritoActivo = -1;

    // Mapa usando tu clase visual separada ControladorCarrito
    private final Map<String, ControladorCarrito> mapaCarrito = new HashMap<>();

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

            // Carga los botones de las ofertas al inicio
            cargarPromocionesDisponibles();
        });

        txtLectorBarras.setOnAction(event -> {
            procesarEscaneo();
        });
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }

    private void procesarEscaneo() {
        String codigo = "";
        if (txtLectorBarras != null && !txtLectorBarras.getText().trim().isEmpty()) {
            codigo = txtLectorBarras.getText().trim();
            txtLectorBarras.clear();
        } else if (txtCodigoBarra != null && !txtCodigoBarra.getText().trim().isEmpty()) {
            codigo = txtCodigoBarra.getText().trim();
            txtCodigoBarra.clear();
        }

        System.out.println("-> ¡Intento de lectura detectado!: [" + codigo + "]");

        if (codigo.isEmpty()) return;

        Producto producto = productoServicio.buscarProducto(codigo);

        if (producto != null) {
            if (producto.getStock() <= 0) {
                System.out.println("-> Error: El producto no cuenta con stock.");
                return;
            }
            Platform.runLater(() -> añadirAlCarritoVisual(producto));
        } else {
            System.out.println("-> Código '" + codigo + "' no registrado en la base de datos.");
        }

        if (txtLectorBarras != null) txtLectorBarras.requestFocus();
        else if (txtCodigoBarra != null) txtCodigoBarra.requestFocus();
    }

    private void añadirAlCarritoVisual(Producto producto) {
        String codigo = producto.getCodigo_barras();
        double precioProducto = producto.getPrecio();

        // 1. CREA O RECUPERA EL CARRITO EN MySQL (Asignado al cliente ID 1)
        if (idCarritoActivo == -1) {
            idCarritoActivo = carritoDAO.obtenerOCrearCarritoActivo(1);
        }

        if (contenedorPadre != null) {
            contenedorPadre.actualizarUnidadesContador(1);
        }

        if (mapaCarrito.containsKey(codigo)) {
            ControladorCarrito cardExistente = mapaCarrito.get(codigo);
            if (cardExistente.getCantidad() < producto.getStock()) {

                // Actualiza visualmente el "x2, x3"
                cardExistente.incrementarCantidad();

                // 2. ACTUALIZA EN MySQL (suma 1 cantidad al detalle)
                carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioProducto);

                if (contenedorPadre != null) {
                    contenedorPadre.agregarProductoAlTotal(precioProducto);
                }
            } else {
                System.out.println("-> Límite de stock alcanzado para este ítem.");
            }
        } else {
            // Crea la tarjeta visual por primera vez
            ControladorCarrito nuevaCard = new ControladorCarrito(producto);
            mapaCarrito.put(codigo, nuevaCard);
            panelCarrito.getChildren().add(nuevaCard.getNodoVisual());

            // 3. INSERTA EN MySQL (primer producto de este tipo)
            carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioProducto);

            if (contenedorPadre != null) {
                contenedorPadre.agregarProductoAlTotal(precioProducto);
            }
        }
    }

    public void cargarPromocionesDisponibles() {
        if (panelProductos == null) return;

        panelProductos.getChildren().clear();

        List<Oferta> listaOfertas = ofertaDAO.listarTodas();

        for (Oferta oferta : listaOfertas) {
            if (oferta.isEstado()) {

                HBox botonPromocion = new HBox();
                botonPromocion.setAlignment(Pos.CENTER_LEFT);
                botonPromocion.setSpacing(12.0);
                botonPromocion.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));

                botonPromocion.setStyle(
                        "-fx-background-color: #ffffff; " +
                                "-fx-border-color: #e2e8f0; " +
                                "-fx-border-width: 1px; " +
                                "-fx-border-radius: 10px; " +
                                "-fx-background-radius: 10px; " +
                                "-fx-cursor: hand;"
                );

                botonPromocion.setOnMouseEntered(e -> botonPromocion.setStyle(
                        "-fx-background-color: #f8fafc; -fx-border-color: #3b82f6; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;"
                ));
                botonPromocion.setOnMouseExited(e -> botonPromocion.setStyle(
                        "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;"
                ));

                VBox infoTexto = new VBox();
                infoTexto.setSpacing(4.0);
                HBox.setHgrow(infoTexto, javafx.scene.layout.Priority.ALWAYS);

                Label lblProducto = new Label(oferta.getNombreProducto());
                lblProducto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                lblProducto.setWrapText(true);

                Label lblDesc = new Label(oferta.getDescripcion());
                lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444; -fx-font-weight: 500;");

                infoTexto.getChildren().addAll(lblProducto, lblDesc);

                Label lblPrecioDscto = new Label("- S/ " + String.format("%.2f", oferta.getDescuento()));
                lblPrecioDscto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");

                botonPromocion.getChildren().addAll(infoTexto, lblPrecioDscto);

                botonPromocion.setOnMouseClicked(e -> {
                    Producto prodCompleto = productoServicio.buscarProductoPorId(oferta.getId_producto());
                    if (prodCompleto != null) {
                        if (prodCompleto.getStock() > 0) {
                            añadirAlCarritoVisual(prodCompleto);
                        } else {
                            System.out.println("-> Sin stock en almacén para: " + prodCompleto.getNombre());
                        }
                    }
                });

                panelProductos.getChildren().add(botonPromocion);
            }
        }
    }
}