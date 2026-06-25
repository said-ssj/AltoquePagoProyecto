package com.modelo;

import java.time.LocalDateTime;

public class MovimientoInventario {
    private int idMovimiento;
    private int idProducto;
    private String tipoMovimiento;
    private int cantidad;
    private LocalDateTime fecha;
    private String descripcion;

    public MovimientoInventario() {}

    public MovimientoInventario(int idMovimiento, int idProducto, String tipoMovimiento, int cantidad, LocalDateTime fecha, String descripcion) {
        this.idMovimiento = idMovimiento;
        this.idProducto = idProducto;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(int idMovimiento) { this.idMovimiento = idMovimiento; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}