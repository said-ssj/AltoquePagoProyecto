package com.modelo;

public class DetalleVenta {

    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // ============================================================
    // CONSTRUCTOR NUEVO (5 parámetros - Usado por el Kiosko/Ticket)
    // ============================================================
    public DetalleVenta(
            int idProducto,
            String nombreProducto,
            int cantidad,
            double precioUnitario,
            double subtotal
    ) {
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // ============================================================
    // CONSTRUCTOR ANTIGUO SOBRECARGADO (4 parámetros - Soluciona el error)
    // ============================================================
    public DetalleVenta(
            int idProducto,
            int cantidad,
            double precioUnitario,
            double subtotal
    ) {
        this.idProducto = idProducto;
        this.nombreProducto = "Producto sin nombre"; // Valor por defecto para compatibilidad
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Getters
    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return subtotal;
    }
}