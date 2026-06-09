package com.modelo;

public class DetalleVenta {

    private int idProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleVenta(
            int idProducto,
            int cantidad,
            double precioUnitario,
            double subtotal
    ) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getIdProducto() {
        return idProducto;
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
