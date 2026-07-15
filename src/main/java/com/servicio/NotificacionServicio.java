package com.servicio;

import com.modelo.Notificacion;
import com.modelo.Producto;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.Set;

/**
 * Centro de notificaciones de la aplicación (patrón Singleton, igual que
 * SesionActual). Mantiene en memoria la lista de notificaciones generadas
 * durante la sesión y expone un contador de "no leídas" observable para
 * poder pintar el badge de la campanita sin acoplar la UI a la lógica.
 *
 * Se generan notificaciones para:
 *  - Productos que caen en o por debajo del stock mínimo definido.
 *  - Alta de un nuevo empleado.
 *  - Apertura y cierre de caja.
 */
public class NotificacionServicio {

    private static NotificacionServicio instancia;

    /** Umbral por defecto usado para considerar "stock bajo" un producto. */
    public static final int STOCK_MINIMO_DEFECTO = 5;

    // Se muestran las más recientes primero
    private final ObservableList<Notificacion> notificaciones = FXCollections.observableArrayList();
    private final IntegerProperty noLeidas = new SimpleIntegerProperty(0);

    // Evita re-notificar el mismo producto en cada refresco mientras siga bajo de stock
    private final Set<Integer> productosYaNotificados = new HashSet<>();

    private NotificacionServicio() {
    }

    public static synchronized NotificacionServicio getInstancia() {
        if (instancia == null) {
            instancia = new NotificacionServicio();
        }
        return instancia;
    }

    public ObservableList<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public IntegerProperty noLeidasProperty() {
        return noLeidas;
    }

    public int getNoLeidas() {
        return noLeidas.get();
    }

    /** Agrega una notificación nueva y actualiza el contador de no leídas. */
    public void agregar(Notificacion.Tipo tipo, String titulo, String mensaje) {
        notificaciones.add(0, new Notificacion(tipo, titulo, mensaje));
        noLeidas.set(noLeidas.get() + 1);
    }

    public void notificarEmpleadoNuevo(String nombreEmpleado, String rol) {
        agregar(Notificacion.Tipo.EMPLEADO_NUEVO,
                "Nuevo empleado registrado",
                nombreEmpleado + " se agregó al sistema como " + rol + ".");
    }

    public void notificarCajaAbierta(double montoBase) {
        agregar(Notificacion.Tipo.CAJA_ABIERTA,
                "Caja abierta",
                String.format("Se abrió un nuevo turno con un monto base de S/ %.2f.", montoBase));
    }

    public void notificarCajaCerrada(double totalTurno, double diferencia) {
        String textoDiferencia = diferencia == 0 ? "cuadre perfecto"
                : (diferencia > 0 ? String.format("sobrante de S/ %.2f", diferencia)
                : String.format("faltante de S/ %.2f", Math.abs(diferencia)));
        agregar(Notificacion.Tipo.CAJA_CERRADA,
                "Caja cerrada",
                String.format("Turno cerrado con un total de S/ %.2f (%s).", totalTurno, textoDiferencia));
    }

    /**
     * Revisa una lista de productos y genera una notificación de stock bajo
     * por cada producto que esté en o por debajo del umbral y que todavía
     * no haya sido notificado. Si un producto vuelve a subir de stock por
     * encima del umbral, se le "olvida" para poder volver a notificarlo si
     * cae de nuevo más adelante.
     */
    public void revisarStockBajo(Iterable<Producto> productos) {
        revisarStockBajo(productos, STOCK_MINIMO_DEFECTO);
    }

    public void revisarStockBajo(Iterable<Producto> productos, int stockMinimo) {
        for (Producto p : productos) {
            if (p.getStock() <= stockMinimo) {
                if (!productosYaNotificados.contains(p.getId_producto())) {
                    productosYaNotificados.add(p.getId_producto());
                    String detalle = p.getStock() == 0
                            ? "sin stock disponible"
                            : "solo quedan " + p.getStock() + " unidades";
                    agregar(Notificacion.Tipo.STOCK_BAJO,
                            "Stock bajo: " + p.getNombre(),
                            "El producto \"" + p.getNombre() + "\" tiene " + detalle + " (mínimo: " + stockMinimo + ").");
                }
            } else {
                productosYaNotificados.remove(p.getId_producto());
            }
        }
    }

    /** Marca todas las notificaciones como leídas (se llama al abrir la campanita). */
    public void marcarTodasComoLeidas() {
        for (Notificacion n : notificaciones) {
            n.setLeida(true);
        }
        noLeidas.set(0);
    }

    public void limpiarTodas() {
        notificaciones.clear();
        noLeidas.set(0);
        productosYaNotificados.clear();
    }
}