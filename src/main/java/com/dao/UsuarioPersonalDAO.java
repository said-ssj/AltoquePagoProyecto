package com.dao;

import com.DB.ConexionDB;
import com.modelo.UsuarioPersonal;
import com.servicio.Seguridad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioPersonalDAO implements IUsuarioPersonalDAO{

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
    //  OBTENER TODOS (Para la tabla de empleados)
    // ============================================================
    public List<UsuarioPersonal> obtenerTodos() {
        List<UsuarioPersonal> lista = new ArrayList<>();
        // SEGURIDAD [SEC-03]: No se selecciona la columna contraseña para evitar
        // exponer hashes innecesariamente en la capa de presentación.
        String sql = "SELECT id_usuario, nombre, email, telefono, numero_documento, id_rol FROM usuario_personal";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioPersonal u = new UsuarioPersonal();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setTelefono(rs.getString("telefono"));
                u.setNumeroDocumento(rs.getString("numero_documento"));
                u.setIdRol(rs.getInt("id_rol"));
                lista.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public boolean actualizar(UsuarioPersonal usuario) {
        String sql = "UPDATE usuario_personal SET nombre=?, email=?, id_rol=?, area=?, tipo_contrato=?, telefono=?, salario_base=? WHERE id_usuario=?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setInt   (3, usuario.getIdRol());
            ps.setString(4, usuario.getArea());
            ps.setString(5, usuario.getTipoContrato());
            ps.setString(6, usuario.getTelefono());
            ps.setDouble(7, usuario.getSalarioBase());
            ps.setInt   (8, usuario.getIdUsuario()); // O el campo que use tu modelo para el ID

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    //  AUTENTICAR USUARIO
    //
    //  SEGURIDAD [SEC-04] + [SEC-03]:
    //  - Se busca al usuario SOLO por email (no por contraseña en SQL).
    //  - La verificación de contraseña se hace en Java con Seguridad.verificarPassword(),
    //    que soporta tanto PBKDF2 (nuevo) como SHA-256 (legacy).
    //  - Si la contraseña era legacy SHA-256 y es correcta, se re-hashea con PBKDF2
    //    y se actualiza en la base de datos de forma transparente.
    //  - Al autenticar con éxito, no se expone la contraseña hasheada al controlador.
    // ============================================================
    public UsuarioPersonal autenticarUsuario(String email, String passwordTextoPlano) {
        // Buscar usuario solo por email
        String sql = "SELECT id_usuario, nombre, email, contraseña, id_rol FROM usuario_personal WHERE email = ?";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return null;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String hashAlmacenado = rs.getString("contraseña");

                        // Verificar en Java (no en SQL)
                        if (!Seguridad.verificarPassword(passwordTextoPlano, hashAlmacenado)) {
                            return null; // Contraseña incorrecta
                        }

                        UsuarioPersonal u = new UsuarioPersonal();
                        u.setIdUsuario(rs.getInt("id_usuario"));
                        u.setNombre(rs.getString("nombre"));
                        u.setEmail(rs.getString("email"));
                        u.setIdRol(rs.getInt("id_rol"));
                        // No se devuelve la contraseña hasheada al controlador

                        // Migración silenciosa: si era SHA-256 legacy, re-hashear con PBKDF2
                        if (hashAlmacenado.matches("[0-9a-f]{64}")) {
                            migrarPasswordAPBKDF2(cn, u.getIdUsuario(), passwordTextoPlano);
                        }

                        return u;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al autenticar al usuario.");
            e.printStackTrace();
        }
        return null;
    }

    /** Re-hashea la contraseña de un usuario con PBKDF2 (migración desde SHA-256). */
    private void migrarPasswordAPBKDF2(Connection cn, int idUsuario, String passwordTextoPlano) {
        String nuevoHash = Seguridad.encriptarPassword(passwordTextoPlano);
        String sql = "UPDATE usuario_personal SET contraseña = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
            System.out.println("[Seguridad] Contraseña migrada a PBKDF2 para usuario ID: " + idUsuario);
        } catch (Exception e) {
            System.err.println("[Seguridad] Error al migrar contraseña: " + e.getMessage());
        }
    }

    @Override
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuario_personal WHERE id_usuario = ?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
