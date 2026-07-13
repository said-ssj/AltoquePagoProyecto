/*
 * Definimos el contrato formal para el control de flujos de efectivo y turnos de caja.
 * Al aislar estas firmas en una interfaz, garantizamos que las operaciones financieras de arqueo
 * queden completamente desvinculadas de la persistencia física en MySQL, permitiendo un diseño
 * mantenible y alineado al Principio de Inversión de Dependencias (DIP).
 */
package com.dao;

import com.modelo.ArqueoCaja;

public interface IArqueoCajaDAO {
    ArqueoCaja obtenerArqueoAbierto();
    int abrirArqueo(double montoInicial, Integer idUsuario);
    boolean cerrarArqueo(int idArqueo, double efectivoSistema, double efectivoContado, double diferencia,
                         double totalYape, double totalPlin, double totalTarjeta, double totalVentas);
}