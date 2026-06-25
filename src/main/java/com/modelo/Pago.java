package com.modelo;

public class Pago {
    private int idPago;
    private int idVenta;
    private String metodo; // 'EFECTIVO', 'YAPE', 'PLIN', 'TARJETA'
    private double monto;
    private String estado; // 'PENDIENTE', 'PAGADO', 'RECHAZADO'

    public Pago() {}

    public Pago(int idPago, int idVenta, String metodo, double monto, String estado) {
        this.idPago = idPago;
        this.idVenta = idVenta;
        this.metodo = metodo;
        this.monto = monto;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}