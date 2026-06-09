package com.dao;

import com.modelo.Producto;
import com.DB.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductoDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProductoDAO.class);

    // ============================================================
    //  BUSCAR POR CÓDIGO DE BARRAS (exacto)
    // ============================================================
    public Producto buscarPorCodigo(String codigo) {
        try {
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT * FROM producto WHERE codigo_barras = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (Exception e) {
            logger.error("Error al buscar por código: {}", codigo, e);
        }
        return null;
    }

    // ============================================================
    //  BUSCAR POR NOMBRE (LIKE)
    // ============================================================
    public Producto buscarPorNombre(String nombre) {
        try {
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT * FROM producto WHERE nombre LIKE ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, "%" + nombre + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (Exception e) {
            logger.error("Error al buscar por nombre: {}", nombre, e);
        }
        return null;
    }

    // ============================================================
    //  VERIFICAR SI UN CÓDIGO YA EXISTE EN BD
    // ============================================================
    public boolean existePorCodigo(String codigo) {
        try {
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT COUNT(*) FROM producto WHERE codigo_barras = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            logger.error("Error al verificar existencia del código: {}", codigo, e);
        }
        return false;
    }

    // ============================================================
    //  GUARDAR NUEVO PRODUCTO
    // ============================================================
    public boolean guardarProducto(Producto p) {
        String sql = "INSERT INTO producto (codigo_barras, nombre, precio, stock) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo_barras());
            ps.setString(2, p.getNombre());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.executeUpdate();
            logger.info("Producto guardado: {} - {}", p.getCodigo_barras(), p.getNombre());
            return true;

        } catch (Exception e) {
            logger.error("Error al guardar producto: {}", p.getNombre(), e);
            return false;
        }
    }

    // ============================================================
    //  LISTAR TODOS LOS PRODUCTOS (para la tabla)
    // ============================================================
    public List<Producto> listarProductos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto ORDER BY id_producto DESC";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (Exception e) {
            logger.error("Error al listar productos", e);
        }
        return lista;
    }

    // ============================================================
    //  ACTUALIZAR STOCK (descontar al vender)
    // ============================================================
    public void actualizarStock(int idProducto, int cantidad) {
        try (Connection cn = ConexionDB.conectar()) {
            String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto = ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
            logger.info("Stock actualizado. Producto: {} - Cantidad descontada: {}", idProducto, cantidad);
        } catch (Exception e) {
            logger.error("Error al actualizar stock. ID: {}, Cantidad: {}", idProducto, cantidad, e);
        }
    }

    // ============================================================
    //  MAPEAR ResultSet → Producto (método privado de apoyo)
    // ============================================================
    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo_barras"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getInt("stock")
        );
    }
}