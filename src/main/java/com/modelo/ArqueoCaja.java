package com.modelo;

import java.time.LocalDateTime;

public class ArqueoCaja {
    private int idArqueo;
    private LocalDateTime fechaApertura;
    private double montoInicial;
    private String estado; // 'ABIERTA' o 'CERRADA'

    public ArqueoCaja(int idArqueo, LocalDateTime fechaApertura, double montoInicial, String estado) {
        this.idArqueo = idArqueo;
        this.fechaApertura = fechaApertura;
        this.montoInicial = montoInicial;
        this.estado = estado;
    }

    public int getIdArqueo() { return idArqueo; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public double getMontoInicial() { return montoInicial; }
    public String getEstado() { return estado; }
}
