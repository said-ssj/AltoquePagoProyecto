package com.modelo;

public class ItemCarrito {
    private Producto producto;
    private int cantidad;

    public ItemCarrito(
            Producto producto,
            int cantidad
    ){
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void aumentarCantidad(){
        cantidad++;
    }

    public double getSubtotal(){
        return producto.getPrecio() * cantidad;
    }
}