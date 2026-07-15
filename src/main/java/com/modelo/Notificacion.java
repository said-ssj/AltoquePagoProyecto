package com.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una notificación mostrada en la campanita del menú principal.
 * No se persiste en base de datos: vive en memoria mientras la aplicación
 * está abierta (equivalente a un centro de notificaciones de sesión).
 */
public class Notificacion {

    public enum Tipo {
        STOCK_BAJO,
        EMPLEADO_NUEVO,
        CAJA_ABIERTA,
        CAJA_CERRADA
    }

    private final Tipo tipo;
    private final String titulo;
    private final String mensaje;
    private final LocalDateTime fecha;
    private boolean leida;

    public Notificacion(Tipo tipo, String titulo, String mensaje) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getHoraFormateada() {
        return fecha.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    /** Ícono FontAwesome (Ikonli) asociado al tipo de notificación. */
    public String getIconoLiteral() {
        return switch (tipo) {
            case STOCK_BAJO -> "fas-box-open";
            case EMPLEADO_NUEVO -> "fas-user-plus";
            case CAJA_ABIERTA -> "fas-cash-register";
            case CAJA_CERRADA -> "fas-lock";
        };
    }

    /** Color asociado al tipo, usado para el ícono en la lista de notificaciones. */
    public String getColor() {
        return switch (tipo) {
            case STOCK_BAJO -> "#f59e0b";
            case EMPLEADO_NUEVO -> "#2563eb";
            case CAJA_ABIERTA -> "#16a34a";
            case CAJA_CERRADA -> "#64748b";
        };
    }
}