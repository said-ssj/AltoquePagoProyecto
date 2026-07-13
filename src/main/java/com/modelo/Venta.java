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

    // NUEVO: trazabilidad de quién / por qué canal se realizó la venta.
    // "vendedor"   -> nombre del usuario logueado (Administrador o Vendedor) que atendió la venta.
    //                 Queda en null/"" cuando la venta se originó en el kiosko de autoservicio.
    // "canalVenta" -> "BACKOFFICE" (venta hecha por un cajero desde el sistema de gestión)
    //                 o "AUTOSERVICIO" (venta hecha por el cliente en el kiosko, sin cajero).
    private String vendedor;
    private String canalVenta;

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
    public String getVendedor()    { return vendedor; }   // <-- NUEVO
    public String getCanalVenta()  { return canalVenta; } // <-- NUEVO

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
    public void setVendedor(String vendedor)     { this.vendedor   = vendedor; }   // <-- NUEVO
    public void setCanalVenta(String canalVenta) { this.canalVenta = canalVenta; } // <-- NUEVO
}