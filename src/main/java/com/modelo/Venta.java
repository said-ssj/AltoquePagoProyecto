package com.modelo;

public class Venta {
    private String id;
    private String fecha;
    private String cliente;
    private int productos;
    private double total;
    private String estado;

    public Venta(String id, String fecha, String cliente, int productos, double total, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.cliente = cliente;
        this.productos = productos;
        this.total = total;
        this.estado = estado;
    }


    public String getId() { return id; }
    public String getFecha() { return fecha; }
    public String getCliente() { return cliente; }
    public int getProductos() { return productos; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }
    public void setProductos(int productos) {
        this.productos = productos;
    }
}