package com.dao;

import com.modelo.Configuracion;

/**
 * Contrato para la persistencia de las preferencias y configuraciones del sistema.
 */
public interface IConfiguracionDAO {
    Configuracion cargarConfiguracion();
    boolean guardarConfiguracion(Configuracion config);
}