package com.controlador;

import com.modelo.Producto;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Componente visual independiente para representar y controlar un ítem
 * dentro del carrito de compras del Kiosco de Autoservicio.
 */
public class ControladorCarrito {
    private final Producto producto;
    private final double precioUnitario;
    private int cantidad;
    private final HBox contenedorVisual;
    private final Label lblCantidad;

    /** Mantiene compatibilidad con código existente: usa el precio de lista sin descuento. */
    public ControladorCarrito(Producto producto) {
        this(producto, producto.getPrecio());
    }

    /**
     * @param precioUnitario precio real a cobrar por unidad (ya con el descuento de
     *                       la oferta activa aplicado, si corresponde). Es el precio
     *                       que se muestra en la tarjeta, para que coincida con el
     *                       total real del kiosko.
     */
    public ControladorCarrito(Producto producto, double precioUnitario) {
        this.producto = producto;
        this.precioUnitario = precioUnitario;
        this.cantidad = 1;

        // Crear el contenedor horizontal de la tarjeta
        this.contenedorVisual = new HBox();
        this.contenedorVisual.setAlignment(Pos.CENTER_LEFT);
        this.contenedorVisual.getStyleClass().add("item-carrito-tarjeta");

        // Contenedor vertical para el texto informativo
        VBox infoProducto = new VBox();
        infoProducto.getStyleClass().add("item-carrito-info");
        HBox.setHgrow(infoProducto, javafx.scene.layout.Priority.ALWAYS);

        // Nombre del producto
        Label lblNombre = new Label(producto.getNombre());
        lblNombre.getStyleClass().add("item-carrito-nombre");
        lblNombre.setWrapText(true);

        // Precio unitario formateado (ya con descuento de oferta si aplica)
        Label lblPrecio = new Label("S/ " + String.format("%.2f", precioUnitario));
        lblPrecio.getStyleClass().add("item-carrito-precio");

        infoProducto.getChildren().addAll(lblNombre, lblPrecio);

        // Indicador de cantidad (ej: x1, x2)
        this.lblCantidad = new Label("x" + cantidad);
        this.lblCantidad.getStyleClass().add("item-carrito-cantidad");
        this.lblCantidad.setAlignment(Pos.CENTER_RIGHT);

        // Ensamblar la tarjeta completa
        this.contenedorVisual.getChildren().addAll(infoProducto, this.lblCantidad);
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void incrementarCantidad() {
        this.cantidad++;
        this.lblCantidad.setText("x" + this.cantidad);
    }

    public int getCantidad() {
        return cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public HBox getNodoVisual() {
        return contenedorVisual;
    }
}