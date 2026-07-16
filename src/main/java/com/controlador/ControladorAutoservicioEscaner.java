package com.controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
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
import com.servicio.ColorConfig.ColorOfertaMap;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ControladorAutoservicioEscaner {

    // ── FXML ──────────────────────────────────────────────────────
    @FXML private TextField  txtLectorBarras;
    @FXML private VBox       panelCarrito;
    @FXML private StackPane  panelCarrusel;      // tarjeta única visible
    @FXML private HBox       panelIndicadores;   // puntos ●○○
    @FXML private Button     btnAnterior;
    @FXML private Button     btnSiguiente;
    @FXML private TextField  txtCodigoBarra;

    // ── Estado del carrusel ───────────────────────────────────────
    private List<Oferta> ofertasActivas  = new ArrayList<>();
    private int          indiceActual    = 0;
    private Timeline     timelineAuto;
    private static final int SEGUNDOS_ROTACION = 5;

    // ── Dependencias ──────────────────────────────────────────────
    private ControladorAutoservicioCheckoutDividida contenedorPadre;
    private final ProductoServicio productoServicio;
    private final OfertaDAO   ofertaDAO  = new OfertaDAO();
    private final CarritoDAO  carritoDAO = new CarritoDAO();

    private int idCarritoActivo = -1;
    private final Map<String, ControladorCarrito> mapaCarrito = new HashMap<>();

    public ControladorAutoservicioEscaner() {
        IProductoDAO productoDAOReal = new ProductoDAO();
        this.productoServicio = new ProductoServicio(productoDAOReal);
    }

    public void setContenedorPadre(ControladorAutoservicioCheckoutDividida padre) {
        this.contenedorPadre = padre;
    }

    // ============================================================
    //  INIT
    // ============================================================
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if (txtLectorBarras != null && txtLectorBarras.getScene() != null) {
                txtLectorBarras.getScene().setOnKeyPressed(event -> {
                    if (!txtLectorBarras.isFocused() && event.getText() != null && !event.getText().isEmpty()) {
                        txtLectorBarras.requestFocus();
                        txtLectorBarras.appendText(event.getText());
                    }
                });
            }
            if (txtLectorBarras != null) txtLectorBarras.requestFocus();
            cargarPromocionesDisponibles();
        });

        if (txtLectorBarras != null) txtLectorBarras.setOnAction(event -> procesarEscaneo());
    }

    // ============================================================
    //  CARGA DEL CARRUSEL
    // ============================================================
    public void cargarPromocionesDisponibles() {
        if (panelCarrusel == null) return;

        detenerTimeline();

        ofertasActivas = ofertaDAO.listarTodas().stream()
                .filter(Oferta::isEstado)
                .collect(Collectors.toList());

        panelCarrusel.getChildren().clear();
        indiceActual = 0;

        // Ocultar/mostrar flechas según cantidad
        boolean hayMasDeUna = ofertasActivas.size() > 1;
        if (btnAnterior  != null) { btnAnterior .setVisible(hayMasDeUna); btnAnterior .setManaged(hayMasDeUna); }
        if (btnSiguiente != null) { btnSiguiente.setVisible(hayMasDeUna); btnSiguiente.setManaged(hayMasDeUna); }

        if (ofertasActivas.isEmpty()) {
            Label lblVacio = new Label("No hay promociones disponibles.");
            lblVacio.setStyle("-fx-text-fill:#94a3b8; -fx-font-size:14px;");
            panelCarrusel.getChildren().add(lblVacio);
            actualizarIndicadores();
            return;
        }

        mostrarOferta(indiceActual, false);
        actualizarIndicadores();

        if (hayMasDeUna) {
            timelineAuto = new Timeline(
                    new KeyFrame(Duration.seconds(SEGUNDOS_ROTACION), e -> avanzarCarrusel())
            );
            timelineAuto.setCycleCount(Timeline.INDEFINITE);
            timelineAuto.play();
        }
    }

    // ============================================================
    //  NAVEGACIÓN MANUAL
    // ============================================================
    @FXML
    public void avanzarCarruselManual() {
        reiniciarTimer();
        avanzarCarrusel();
    }

    @FXML
    public void retrocederCarrusel() {
        if (ofertasActivas.isEmpty()) return;
        reiniciarTimer();
        indiceActual = (indiceActual - 1 + ofertasActivas.size()) % ofertasActivas.size();
        mostrarOferta(indiceActual, true);
        actualizarIndicadores();
    }

    private void avanzarCarrusel() {
        if (ofertasActivas.isEmpty()) return;
        indiceActual = (indiceActual + 1) % ofertasActivas.size();
        mostrarOferta(indiceActual, true);
        actualizarIndicadores();
    }

    private void reiniciarTimer() {
        detenerTimeline();
        if (ofertasActivas.size() > 1) {
            timelineAuto = new Timeline(
                    new KeyFrame(Duration.seconds(SEGUNDOS_ROTACION), e -> avanzarCarrusel())
            );
            timelineAuto.setCycleCount(Timeline.INDEFINITE);
            timelineAuto.play();
        }
    }

    private void detenerTimeline() {
        if (timelineAuto != null) { timelineAuto.stop(); timelineAuto = null; }
    }

    // ============================================================
    //  MOSTRAR TARJETA CON FADE
    // ============================================================
    private void mostrarOferta(int indice, boolean animado) {
        if (ofertasActivas.isEmpty() || panelCarrusel == null) return;
        Oferta oferta   = ofertasActivas.get(indice);
        Node   nuevo    = construirTicketOferta(oferta);
        Node   anterior = panelCarrusel.getChildren().isEmpty() ? null
                : panelCarrusel.getChildren().get(0);

        if (!animado) {
            panelCarrusel.getChildren().setAll(nuevo);
            return;
        }

        nuevo.setOpacity(0.0);
        panelCarrusel.getChildren().add(nuevo);

        FadeTransition entrada = new FadeTransition(Duration.millis(400), nuevo);
        entrada.setFromValue(0.0);
        entrada.setToValue(1.0);

        if (anterior != null) {
            FadeTransition salida = new FadeTransition(Duration.millis(400), anterior);
            salida.setFromValue(1.0);
            salida.setToValue(0.0);
            salida.setOnFinished(e -> panelCarrusel.getChildren().remove(anterior));
            salida.play();
        }
        entrada.play();
    }

    // ============================================================
    //  PUNTOS INDICADORES (●○○○)
    // ============================================================
    private void actualizarIndicadores() {
        if (panelIndicadores == null) return;
        panelIndicadores.getChildren().clear();
        for (int i = 0; i < ofertasActivas.size(); i++) {
            final int idx = i;
            Circle punto = new Circle(i == indiceActual ? 6 : 4);
            punto.setFill(i == indiceActual ? Color.web("#1d4ed8") : Color.web("#cbd5e1"));
            punto.setStyle("-fx-cursor: hand;");
            punto.setOnMouseClicked(e -> {
                reiniciarTimer();
                indiceActual = idx;
                mostrarOferta(indiceActual, true);
                actualizarIndicadores();
            });
            panelIndicadores.getChildren().add(punto);
        }
    }

    // ============================================================
    //  CONSTRUIR TARJETA DE OFERTA (ticket con franja de color)
    // ============================================================
    private Node construirTicketOferta(Oferta oferta) {
        String colorHex = ColorOfertaMap.obtenerColor(oferta.getId_oferta());

        // ── Contenedor exterior (sombra + bordes redondeados) ──
        HBox ticket = new HBox();
        ticket.setPrefSize(370, 130);
        ticket.setMaxSize(370, 130);
        ticket.setMinSize(300, 110);
        ticket.setStyle(
                "-fx-background-color:#ffffff;" +
                        "-fx-background-radius:14px;" +
                        "-fx-cursor:hand;" +
                        "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.15),12,0,0,4);"
        );

        // Franja de color lateral
        Region franja = new Region();
        franja.setPrefWidth(16);
        franja.setMinWidth(16);
        franja.setMaxWidth(16);
        franja.setStyle("-fx-background-color:" + colorHex + ";" +
                "-fx-background-radius:14 0 0 14;");

        // Divisor punteado
        Region divisor = new Region();
        divisor.setPrefWidth(2);
        divisor.setMaxWidth(2);
        divisor.setStyle("-fx-border-color:transparent transparent transparent #cbd5e1;" +
                "-fx-border-width:0 0 0 2;");
        HBox.setMargin(divisor, new Insets(14, 0, 14, 0));

        // Contenido de texto
        VBox contenido = new VBox(7);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(16, 18, 16, 16));
        HBox.setHgrow(contenido, Priority.ALWAYS);

        Label lblProducto = new Label(oferta.getNombreProducto());
        lblProducto.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#1e293b;");
        lblProducto.setWrapText(true);

        Label lblDesc = new Label(oferta.getDescripcion());
        lblDesc.setStyle("-fx-font-size:12px;-fx-text-fill:" + colorHex + ";-fx-font-weight:600;");
        lblDesc.setWrapText(true);

        Label lblDescuento = new Label("- S/ " + String.format("%.2f", oferta.getDescuento()));
        lblDescuento.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#16a34a;");

        contenido.getChildren().addAll(lblProducto, lblDesc, lblDescuento);
        ticket.getChildren().addAll(franja, divisor, contenido);

        // Muescas circulares decorativas (efecto ticket)
        Circle muescaIzq = new Circle(9, Color.web("#fafafa"));
        Circle muescaDer = new Circle(9, Color.web("#fafafa"));
        StackPane contenedorTicket = new StackPane();
        contenedorTicket.getChildren().addAll(ticket, muescaIzq, muescaDer);
        StackPane.setAlignment(muescaIzq, Pos.CENTER_LEFT);
        StackPane.setAlignment(muescaDer, Pos.CENTER_RIGHT);
        muescaIzq.setTranslateX(-9);
        muescaDer.setTranslateX(9);

        // Clic en la tarjeta → añadir al carrito
        contenedorTicket.setOnMouseClicked(e -> {
            Producto prodCompleto = productoServicio.buscarProductoPorId(oferta.getId_producto());
            if (prodCompleto != null && prodCompleto.getStock() > 0) {
                añadirAlCarritoVisual(prodCompleto);
            }
        });

        return contenedorTicket;
    }

    // ============================================================
    //  ESCANEO Y CARRITO (sin cambios respecto al original)
    // ============================================================
    private void procesarEscaneo() {
        String codigo = "";
        if (txtLectorBarras != null && !txtLectorBarras.getText().trim().isEmpty()) {
            codigo = txtLectorBarras.getText().trim();
            txtLectorBarras.clear();
        } else if (txtCodigoBarra != null && !txtCodigoBarra.getText().trim().isEmpty()) {
            codigo = txtCodigoBarra.getText().trim();
            txtCodigoBarra.clear();
        }

        if (codigo.isEmpty()) return;
        System.out.println("-> Lectura: [" + codigo + "]");

        Producto producto = productoServicio.buscarProducto(codigo);
        if (producto != null) {
            if (producto.getStock() <= 0) { System.out.println("-> Sin stock."); return; }
            Platform.runLater(() -> añadirAlCarritoVisual(producto));
        } else {
            System.out.println("-> Código no encontrado: " + codigo);
        }

        if (txtLectorBarras  != null) txtLectorBarras.requestFocus();
        else if (txtCodigoBarra != null) txtCodigoBarra.requestFocus();
    }

    private void añadirAlCarritoVisual(Producto producto) {
        String codigo        = producto.getCodigo_barras();
        double precioFinal   = obtenerPrecioConOferta(producto);

        if (idCarritoActivo == -1)
            idCarritoActivo = carritoDAO.obtenerOCrearCarritoActivo(1);

        if (contenedorPadre != null)
            contenedorPadre.actualizarUnidadesContador(1);

        if (mapaCarrito.containsKey(codigo)) {
            ControladorCarrito card = mapaCarrito.get(codigo);
            if (card.getCantidad() < producto.getStock()) {
                card.incrementarCantidad();
                carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioFinal);
                if (contenedorPadre != null) contenedorPadre.agregarProductoAlTotal(precioFinal);
            } else {
                System.out.println("-> Stock máximo alcanzado.");
            }
        } else {
            ControladorCarrito nueva = new ControladorCarrito(producto, precioFinal);
            mapaCarrito.put(codigo, nueva);
            panelCarrito.getChildren().add(nueva.getNodoVisual());
            carritoDAO.agregarProductoAlBD(idCarritoActivo, producto.getId_producto(), precioFinal);
            if (contenedorPadre != null) contenedorPadre.agregarProductoAlTotal(precioFinal);
        }
    }

    private double obtenerPrecioConOferta(Producto producto) {
        double base  = producto.getPrecio();
        Oferta oferta = ofertaDAO.buscarOferta(producto.getId_producto());
        if (oferta != null) return Math.max(base - oferta.getDescuento(), 0);
        return base;
    }
}
