package com.servicio;

import com.modelo.UsuarioPersonal;

/**
 * Singleton que guarda el usuario autenticado durante toda la sesión.
 *
 * SEGURIDAD [SEC-03]: Antes, ningún controlador verificaba el rol del usuario
 * después del login. Esta clase permite que cada controlador compruebe si el
 * usuario logueado tiene permiso para realizar una acción.
 *
 * Constantes de rol (deben coincidir con los valores de id_rol en MySQL):
 *   ROL_ADMINISTRADOR = 1
 *   ROL_VENDEDOR      = 2
 *   ROL_ALMACEN       = 3
 */
public class SesionActual {

    public static final int ROL_ADMINISTRADOR = 1;
    public static final int ROL_VENDEDOR      = 2;
    public static final int ROL_ALMACEN       = 3;

    private static SesionActual instancia;
    private UsuarioPersonal usuarioActual;

    private SesionActual() {}

    public static SesionActual getInstancia() {
        if (instancia == null) {
            instancia = new SesionActual();
        }
        return instancia;
    }

    /** Guarda el usuario autenticado al iniciar sesión. */
    public void iniciarSesion(UsuarioPersonal usuario) {
        this.usuarioActual = usuario;
    }

    /** Limpia la sesión al cerrar sesión o salir del sistema. */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public UsuarioPersonal getUsuario() {
        return usuarioActual;
    }

    /** Devuelve el id_rol del usuario logueado, o -1 si no hay sesión activa. */
    public int getRolActual() {
        return usuarioActual != null ? usuarioActual.getIdRol() : -1;
    }

    public String getNombreUsuario() {
        return usuarioActual != null ? usuarioActual.getNombre() : "Sin sesión";
    }

    /** true si el usuario tiene rol Administrador. */
    public boolean esAdministrador() {
        return getRolActual() == ROL_ADMINISTRADOR;
    }

    /** true si el usuario puede gestionar empleados (solo Administrador). */
    public boolean puedeGestionarEmpleados() {
        return esAdministrador();
    }

    /** true si el usuario puede ver y generar reportes (Administrador). */
    public boolean puedeVerReportes() {
        return esAdministrador();
    }

    /** true si el usuario puede hacer ventas (Administrador o Vendedor). */
    public boolean puedeVender() {
        int rol = getRolActual();
        return rol == ROL_ADMINISTRADOR || rol == ROL_VENDEDOR;
    }

    /** true si el usuario puede gestionar inventario (Administrador o Almacén). */
    public boolean puedeGestionarInventario() {
        int rol = getRolActual();
        return rol == ROL_ADMINISTRADOR || rol == ROL_ALMACEN;
    }
}
