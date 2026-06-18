package com.dao;

import com.DB.ConexionDB;
import com.modelo.UsuarioPersonal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioPersonalDAO {

    // ============================================================
    //  GUARDAR NUEVO EMPLEADO EN MYSQL
    // ============================================================
    public boolean guardarUsuario(UsuarioPersonal u) {
        String sql = "INSERT INTO usuario_personal (nombre, email, contraseña, id_rol, fecha_nacimiento, " +
                "tipo_documento, numero_documento, nacionalidad, direccion, telefono, telefono_emergencia, " +
                "area, tipo_contrato, fecha_inicio, salario_base, metodo_pago, datos_bancarios, antecedentes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return false;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getEmail());
                ps.setString(3, u.getContraseña());
                ps.setInt(4, u.getIdRol());
                ps.setString(5, u.getFechaNacimiento());
                ps.setString(6, u.getTipoDocumento());
                ps.setString(7, u.getNumeroDocumento());
                ps.setString(8, u.getNacionalidad());
                ps.setString(9, u.getDireccion());
                ps.setString(10, u.getTelefono());
                ps.setString(11, u.getTelefonoEmergencia());
                ps.setString(12, u.getArea());
                ps.setString(13, u.getTipoContrato());
                ps.setString(14, u.getFechaInicio());
                ps.setDouble(15, u.getSalarioBase());
                ps.setString(16, u.getMetodoPago());
                ps.setString(17, u.getDatosBancarios());
                ps.setString(18, u.getAntecedentes());

                int filas = ps.executeUpdate();
                return filas > 0;
            }
        } catch (Exception e) {
            System.err.println("Error al guardar el empleado en la base de datos.");
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  OBTENER TODOS (Para tu tabla de empleados)
    // ============================================================
    public List<UsuarioPersonal> obtenerTodos() {
        List<UsuarioPersonal> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario_personal";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioPersonal u = new UsuarioPersonal();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setIdRol(rs.getInt("id_rol"));
                // Puedes extraer los demás campos aquí si planeas mostrarlos en la tabla visualmente

                lista.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}