package com.modelo;

public class Producto {

    private int id_producto;
    private String codigo_barras;
    private String nombre;
    private double precio;
    private int stock;

    public Producto() {
    }

    public Producto(int id_producto, String codigo_barras, String nombre, double precio, int stock) {
        this.id_producto = id_producto;
        this.codigo_barras = codigo_barras;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public int getId_producto() {
        return id_producto;
    }

    public void setId_producto(int id_producto) {
        this.id_producto = id_producto;
    }

    public String getCodigo_barras() {
        return codigo_barras;
    }

    public void setCodigo_barras(
            String codigo_barras
    ) {
        this.codigo_barras = codigo_barras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(
            String nombre
    ) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(
            double precio
    ) {
        this.precio = precio;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
    public int getStock() {
        return stock;
    }
}