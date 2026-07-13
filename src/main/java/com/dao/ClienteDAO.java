package com.dao;
import com.DB.ConexionDB;
import com.modelo.Cliente;
import java.sql.*;
public class ClienteDAO implements IClienteDAO{

    public int guardarCliente(Cliente c) {
        int idGenerado = -1;

        // Ajustado para coincidir exactamente con tu nueva tabla MySQL
        String sql = "INSERT INTO cliente(nombre, apellido, razon_social, correo, numero_documento, " +
                "numero_ruc, telefono, direccion, ubigeo, tipo_documento, observacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getRazonSocial());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getNumeroDocumento());
            ps.setString(6, c.getNumeroRuc());
            ps.setString(7, c.getTelefono());
            ps.setString(8, c.getDireccion());
            ps.setString(9, c.getUbigeo());
            ps.setString(10, c.getTipoDocumento());
            ps.setString(11, c.getObservacion());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idGenerado = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return idGenerado;
    }

    /**
     * NEGOCIO [CLIENTES]: Busca un cliente ya guardado por su número de
     * documento (DNI) o RUC. Se usa para no depender solo de la API externa
     * (SUNAT/RENIEC) al ingresar un DNI: si el cliente ya compró antes,
     * se traen sus datos reales guardados (correo, dirección, teléfono),
     * que la API nunca provee.
     */
    public Cliente buscarPorDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) return null;
        String doc = documento.trim();

        String sql = "SELECT * FROM cliente WHERE numero_documento = ? OR numero_ruc = ? " +
                "ORDER BY id_cliente DESC LIMIT 1";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, doc);
            ps.setString(2, doc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * NEGOCIO [CLIENTES]: Inserta un cliente nuevo, o si ya existe uno con el
     * mismo documento/RUC, actualiza sus datos en vez de duplicarlo.
     * Los campos que llegan vacíos en {@code c} NO borran lo que el cliente
     * ya tenía guardado de una venta anterior (p.ej. si esta vez no escribió
     * el correo, se conserva el correo que ya se había guardado antes).
     * Devuelve el id del cliente (nuevo o existente).
     */
    public int guardarOActualizarCliente(Cliente c) {
        String clave = (c.getNumeroRuc() != null && !c.getNumeroRuc().trim().isEmpty())
                ? c.getNumeroRuc() : c.getNumeroDocumento();

        Cliente existente = buscarPorDocumento(clave);
        if (existente == null) {
            return guardarCliente(c);
        }

        String sql = "UPDATE cliente SET nombre=?, apellido=?, razon_social=?, correo=?, telefono=?, " +
                "direccion=?, ubigeo=?, tipo_documento=?, observacion=? WHERE id_cliente=?";

        try (Connection con = ConexionDB.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, valorOAnterior(c.getNombre(), existente.getNombre()));
            ps.setString(2, valorOAnterior(c.getApellido(), existente.getApellido()));
            ps.setString(3, valorOAnterior(c.getRazonSocial(), existente.getRazonSocial()));
            ps.setString(4, valorOAnterior(c.getCorreo(), existente.getCorreo()));
            ps.setString(5, valorOAnterior(c.getTelefono(), existente.getTelefono()));
            ps.setString(6, valorOAnterior(c.getDireccion(), existente.getDireccion()));
            ps.setString(7, valorOAnterior(c.getUbigeo(), existente.getUbigeo()));
            ps.setString(8, valorOAnterior(c.getTipoDocumento(), existente.getTipoDocumento()));
            ps.setString(9, valorOAnterior(c.getObservacion(), existente.getObservacion()));
            ps.setInt(10, existente.getIdCliente());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return existente.getIdCliente();
    }

    private String valorOAnterior(String nuevo, String anterior) {
        return (nuevo != null && !nuevo.trim().isEmpty()) ? nuevo : anterior;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setRazonSocial(rs.getString("razon_social"));
        c.setCorreo(rs.getString("correo"));
        c.setNumeroDocumento(rs.getString("numero_documento"));
        c.setNumeroRuc(rs.getString("numero_ruc"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setUbigeo(rs.getString("ubigeo"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setObservacion(rs.getString("observacion"));
        return c;
    }
}