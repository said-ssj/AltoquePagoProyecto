package com.modelo;

public class ItemVenta {
    private String producto;
    private int cantidad;
    private double precio;
    private double subtotal;

    public ItemVenta(
            String producto,
            int cantidad,
            double precio,
            double subtotal
    ){
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = subtotal;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio() {
        return precio;
    }

    public double getSubtotal() {
        return subtotal;
    }
}