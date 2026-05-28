package com.modelo;

public class Oferta {
    private int id_oferta;
    private int id_producto;
    private String descripcion;
    private double descuento;

    public Oferta(){}

    public Oferta(
            int id_oferta,
            int id_producto,
            String descripcion,
            double descuento){
        this.id_oferta = id_oferta;
        this.id_producto = id_producto;
        this.descripcion = descripcion;
        this.descuento = descuento;
    }

    public int getId_oferta() {
        return id_oferta;
    }

    public int getId_producto() {
        return id_producto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getDescuento() {
        return descuento;
    }
}