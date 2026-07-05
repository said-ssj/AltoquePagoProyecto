package com.modelo;

import java.time.LocalDate;

public class Oferta {
    private int    id_oferta;
    private int    id_producto;
    private String nombreProducto;
    private String descripcion;
    private double descuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean estado;

    public Oferta() {}

    public Oferta(int id_oferta, int id_producto, String nombreProducto,
                  String descripcion, double descuento,
                  LocalDate fechaInicio, LocalDate fechaFin, boolean estado) {
        this.id_oferta       = id_oferta;
        this.id_producto     = id_producto;
        this.nombreProducto  = nombreProducto;
        this.descripcion     = descripcion;
        this.descuento       = descuento;
        this.fechaInicio     = fechaInicio;
        this.fechaFin        = fechaFin;
        this.estado          = estado;
    }

    // Getters
    public int       getId_oferta()      { return id_oferta; }
    public int       getId_producto()    { return id_producto; }
    public String    getNombreProducto() { return nombreProducto; }
    public String    getDescripcion()    { return descripcion; }
    public double    getDescuento()      { return descuento; }
    public LocalDate getFechaInicio()    { return fechaInicio; }
    public LocalDate getFechaFin()       { return fechaFin; }
    public boolean   isEstado()          { return estado; }
    public String    getEstadoTexto()    { return estado ? "Activa" : "Inactiva"; }

    // Setters
    public void setId_oferta(int v)          { this.id_oferta       = v; }
    public void setId_producto(int v)        { this.id_producto     = v; }
    public void setNombreProducto(String v)  { this.nombreProducto  = v; }
    public void setDescripcion(String v)     { this.descripcion     = v; }
    public void setDescuento(double v)       { this.descuento       = v; }
    public void setFechaInicio(LocalDate v)  { this.fechaInicio     = v; }
    public void setFechaFin(LocalDate v)     { this.fechaFin        = v; }
    public void setEstado(boolean v)         { this.estado          = v; }
}
