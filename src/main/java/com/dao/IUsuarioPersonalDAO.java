/*
 * Definimos el contrato formal para las operaciones de persistencia del personal y seguridad del sistema.
 * Al abstraer estos métodos en una interfaz, aseguramos que los controladores de acceso y gestión de empleados
 * no dependan de una base de datos específica, cumpliendo con el Principio de Inversión de Dependencias (DIP).
 */
package com.dao;

import com.modelo.UsuarioPersonal;
import java.util.List;

public interface IUsuarioPersonalDAO {
    List<UsuarioPersonal> obtenerTodos();

    UsuarioPersonal autenticarUsuario(String email, String passwordTextoPlano);
    boolean actualizar(UsuarioPersonal usuario);
    boolean eliminarUsuario(int idUsuario);
    boolean guardarUsuario(UsuarioPersonal usuario);
}