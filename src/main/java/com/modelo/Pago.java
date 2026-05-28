package com.modelo;

public class Pago {
    private int id_pago;
    private String metodo;
    private double monto;
    private String estado;

    public Pago(){}

    public Pago(
            int id_pago,
            String metodo,
            double monto,
            String estado
    ){
        this.id_pago = id_pago;
        this.metodo = metodo;
        this.monto = monto;
        this.estado = estado;
    }

    public String getMetodo() {
        return metodo;
    }

    public double getMonto() {
        return monto;
    }

    public String getEstado() {
        return estado;
    }
}
