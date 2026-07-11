package com.modelo;

import java.util.List;

public class Venta {
    private String id;
    private String fecha;
    private String cliente;
    private int    idCliente;
    private int    productos;
    private double total;
    private String estado;
    private String metodoPago;

    private List<DetalleVenta> detalles;

    public Venta() {} // Constructor vacío obligatorio

    public Venta(String id, String fecha, String cliente, int idCliente,
                 int productos, double total, String estado, String metodoPago) {
        this.id         = id;
        this.fecha      = fecha;
        this.cliente    = cliente;
        this.idCliente  = idCliente;
        this.productos  = productos;
        this.total      = total;
        this.estado     = estado;
        this.metodoPago = metodoPago;
    }

    // Getters
    public String getId()          { return id; }
    public String getFecha()       { return fecha; }
    public String getCliente()     { return cliente; }
    public int    getIdCliente()   { return idCliente; }
    public int    getProductos()   { return productos; }
    public double getTotal()       { return total; }
    public String getEstado()      { return estado; }
    public String getMetodoPago()  { return metodoPago; }
    public List<DetalleVenta> getDetalles() { return detalles; } // <-- NUEVO

    // Setters
    public void setId(int id)                  { this.id = String.valueOf(id); }
    public void setId(String id)               { this.id = id; }
    public void setFecha(String fecha)         { this.fecha = fecha; }
    public void setTotal(double total)         { this.total = total; }
    public void setCliente(String cliente)     { this.cliente    = cliente; }
    public void setIdCliente(int idCliente)    { this.idCliente  = idCliente; }
    public void setEstado(String estado)       { this.estado     = estado; }
    public void setMetodoPago(String metodo)   { this.metodoPago = metodo; }
    public void setProductos(int productos)    { this.productos  = productos; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; } // <-- NUEVO
}