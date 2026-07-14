package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Node;

import com.servicio.ProductoServicio;
import com.dao.ProductoDAO;
import com.dao.IProductoDAO;
import com.modelo.Producto;
import com.modelo.Oferta;
import com.dao.OfertaDAO;
import com.dao.CarritoDAO;
import com.servicio.ColorConfig.ColorOfertaMap; // Importación correcta

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ControladorAutoservicioEscaner {

    @FXML private TextField txtLectorBarras;
    @FXML private VBox panelCarrito;
    @FXML private StackPane panelCarrusel;
    @FXML private TextField txtCodigoBarra;

    private List<Oferta> ofertasActivas = new ArrayList<>();
    private int indiceOfertaActual = 0;
    private Timeline timelineCarrusel;
    private static final int SEGUNDOS_ROTACION = 5;

    private ControladorAutoservicioCheckoutDividida contenedorPadre;
    private final ProductoServicio productoServicio;

    public ControladorAutoservicioEscaner() {
        IProductoDAO productoDAOReal = new ProductoDAO();
        this.productoServicio = new ProductoServicio(productoDAOReal);
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }

    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final CarritoDAO carritoDAO = new CarritoDAO();
    private int idCarritoActivo = -1;
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
            cargarPromocionesDisponibles();
        });

        txtLectorBarras.setOnAction(event -> procesarEscaneo());
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
        double precioProducto = obtenerPrecioConOferta(producto);

        if (idCarritoActivo == -1) {
            idCarritoActivo = carritoDAO.obtenerOCrearCarritoActivo(1);
        }

        if (contenedorPadre != null) {
            contenedorPadre.actualizarUnidadesContador(1);
        }

        if (mapaCarrito.containsKey(codigo)) {
            ControladorCarrito cardExistente = mapaCarrito.get(codigo);
            if (cardExistente.getCantidad() < producto.getStock()) {
                cardExistente.incrementarCantidad();
                carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioProducto);
                if (contenedorPadre != null) {
                    contenedorPadre.agregarProductoAlTotal(precioProducto);
                }
            } else {
                System.out.println("-> Límite de stock alcanzado para este ítem.");
            }
        } else {
            ControladorCarrito nuevaCard = new ControladorCarrito(producto, precioProducto);
            mapaCarrito.put(codigo, nuevaCard);
            panelCarrito.getChildren().add(nuevaCard.getNodoVisual());

            carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioProducto);

            if (contenedorPadre != null) {
                contenedorPadre.agregarProductoAlTotal(precioProducto);
            }
        }
    }

    private double obtenerPrecioConOferta(Producto producto) {
        double precioBase = producto.getPrecio();
        Oferta oferta = ofertaDAO.buscarOferta(producto.getId_producto());
        if (oferta != null) {
            double precioConDescuento = precioBase - oferta.getDescuento();
            return Math.max(precioConDescuento, 0);
        }
        return precioBase;
    }

    public void cargarPromocionesDisponibles() {
        if (panelCarrusel == null) return;

        if (timelineCarrusel != null) {
            timelineCarrusel.stop();
        }

        ofertasActivas = ofertaDAO.listarTodas().stream()
                .filter(Oferta::isEstado)
                .collect(Collectors.toList());

        panelCarrusel.getChildren().clear();
        indiceOfertaActual = 0;

        if (ofertasActivas.isEmpty()) {
            Label lblVacio = new Label("No hay promociones disponibles por el momento.");
            lblVacio.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:13px;");
            panelCarrusel.getChildren().add(lblVacio);
            return;
        }

        mostrarOfertaEnCarrusel(indiceOfertaActual, false);

        if (ofertasActivas.size() > 1) {
            timelineCarrusel = new Timeline(
                    new KeyFrame(Duration.seconds(SEGUNDOS_ROTACION), e -> avanzarCarrusel())
            );
            timelineCarrusel.setCycleCount(Timeline.INDEFINITE);
            timelineCarrusel.play();
        }
    }

    private void avanzarCarrusel() {
        if (ofertasActivas.isEmpty()) return;
        indiceOfertaActual = (indiceOfertaActual + 1) % ofertasActivas.size();
        mostrarOfertaEnCarrusel(indiceOfertaActual, true);
    }

    private void mostrarOfertaEnCarrusel(int indice, boolean animado) {
        Oferta oferta = ofertasActivas.get(indice);

        Node nuevoTicket = construirTicketOferta(oferta);

        Node ticketAnterior = panelCarrusel.getChildren().isEmpty()
                ? null : panelCarrusel.getChildren().get(0);

        if (!animado) {
            panelCarrusel.getChildren().setAll(nuevoTicket);
            return;
        }

        nuevoTicket.setOpacity(0.0);
        panelCarrusel.getChildren().add(nuevoTicket);

        FadeTransition entrada = new FadeTransition(Duration.millis(450), nuevoTicket);
        entrada.setFromValue(0.0);
        entrada.setToValue(1.0);

        if (ticketAnterior != null) {
            FadeTransition salida = new FadeTransition(Duration.millis(450), ticketAnterior);
            salida.setFromValue(1.0);
            salida.setToValue(0.0);
            salida.setOnFinished(e -> panelCarrusel.getChildren().remove(ticketAnterior));
            salida.play();
        }

        entrada.play();
    }

    private Node construirTicketOferta(Oferta oferta) {
        // LEER COLOR: Se obtiene directamente el color guardado localmente usando la clase importada
        String colorHex = ColorOfertaMap.obtenerColor(oferta.getId_oferta());

        HBox ticket = new HBox();
        ticket.setPrefSize(360, 120);
        ticket.setMaxSize(360, 120);
        ticket.setMinSize(360, 120);
        ticket.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-background-radius: 14px; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 12, 0, 0, 4);"
        );

        Region franja = new Region();
        franja.setPrefWidth(16);
        franja.setMinWidth(16);
        franja.setMaxWidth(16);
        franja.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 14 0 0 14;");

        Region divisor = new Region();
        divisor.setPrefWidth(2);
        divisor.setMaxWidth(2);
        divisor.setStyle(
                "-fx-border-color: transparent transparent transparent #cbd5e1; " +
                        "-fx-border-width: 0 0 0 2; " +
                        "-fx-border-style: segments(6,5) line-cap round;"
        );
        HBox.setMargin(divisor, new Insets(14, 0, 14, 0));

        VBox contenido = new VBox(6);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(14, 18, 14, 14));
        HBox.setHgrow(contenido, javafx.scene.layout.Priority.ALWAYS);

        Label lblProducto = new Label(oferta.getNombreProducto());
        lblProducto.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblProducto.setWrapText(true);

        Label lblDesc = new Label(oferta.getDescripcion());
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorHex + "; -fx-font-weight: 600;");
        lblDesc.setWrapText(true);

        Label lblDescuento = new Label("- S/ " + String.format("%.2f", oferta.getDescuento()));
        lblDescuento.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #16a34a;");

        contenido.getChildren().addAll(lblProducto, lblDesc, lblDescuento);
        ticket.getChildren().addAll(franja, divisor, contenido);

        ticket.setOnMouseClicked(e -> {
            Producto prodCompleto = productoServicio.buscarProductoPorId(oferta.getId_producto());
            if (prodCompleto != null) {
                if (prodCompleto.getStock() > 0) {
                    añadirAlCarritoVisual(prodCompleto);
                } else {
                    System.out.println("-> Sin stock en almacén para: " + prodCompleto.getNombre());
                }
            }
        });

        Circle muescaIzq = new Circle(9, Color.web("#fafafa"));
        Circle muescaDer = new Circle(9, Color.web("#fafafa"));

        StackPane contenedorTicket = new StackPane();
        contenedorTicket.getChildren().addAll(ticket, muescaIzq, muescaDer);
        StackPane.setAlignment(muescaIzq, Pos.CENTER_LEFT);
        StackPane.setAlignment(muescaDer, Pos.CENTER_RIGHT);
        muescaIzq.setTranslateX(-9);
        muescaDer.setTranslateX(9);

        return contenedorTicket;
    }
}