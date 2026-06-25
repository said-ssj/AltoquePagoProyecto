package com.modelo;

public class Configuracion {
    // Datos de la empresa
    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;

    // Impresión
    private String impresora;
    private String tamañoPapel;
    private String mensajeTicket;

    // Kiosko
    private boolean sonidoEscaner;
    private boolean modoOscuro;
    private boolean impresionAuto;

    public Configuracion() {}

    // Getters y Setters
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getImpresora() { return impresora; }
    public void setImpresora(String impresora) { this.impresora = impresora; }

    public String getTamañoPapel() { return tamañoPapel; }
    public void setTamañoPapel(String tamañoPapel) { this.tamañoPapel = tamañoPapel; }

    public String getMensajeTicket() { return mensajeTicket; }
    public void setMensajeTicket(String mensajeTicket) { this.mensajeTicket = mensajeTicket; }

    public boolean isSonidoEscaner() { return sonidoEscaner; }
    public void setSonidoEscaner(boolean sonidoEscaner) { this.sonidoEscaner = sonidoEscaner; }

    public boolean isModoOscuro() { return modoOscuro; }
    public void setModoOscuro(boolean modoOscuro) { this.modoOscuro = modoOscuro; }

    public boolean isImpresionAuto() { return impresionAuto; }
    public void setImpresionAuto(boolean impresionAuto) { this.impresionAuto = impresionAuto; }
}