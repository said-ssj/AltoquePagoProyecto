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
    //  BUSCAR POR CÓDIGO DE BARRAS
    // ============================================================
    public Producto buscarPorCodigo(String codigo){
        String sql = "SELECT * FROM producto WHERE codigo_barras=?";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                logger.error("No se pudo conectar a la base de datos.");
                return null;
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, codigo);
                ResultSet rs = ps.executeQuery();

                if(rs.next()){
                    return new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo_barras"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    );
                }
            }
        } catch(Exception e){
            logger.error("Error al consultar el producto con código de barras: {}", codigo, e);
        }
        return null;
    }

    // ============================================================
    //  BUSCAR POR NOMBRE (LIKE) - CORREGIDO (Sin columna 'estado')
    // ============================================================
    public List<Producto> buscarPorNombre(String nombreParcial) {
        List<Producto> lista = new ArrayList<>();
        // Se quitó "AND estado = 'Activo'" porque no existe en MySQL actual
        String sql = "SELECT * FROM producto WHERE nombre LIKE ?";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return lista;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, "%" + nombreParcial + "%"); // Añadimos los % para que funcione el LIKE
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    lista.add(new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo_barras"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    ));
                }
            }
        } catch (Exception e) {
            logger.error("Error al buscar por nombre: {}", nombreParcial, e);
        }
        return lista;
    }

    // ============================================================
    //  VERIFICAR SI EXISTE UN CÓDIGO
    // ============================================================
    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM producto WHERE codigo_barras=?";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                logger.error("Conexión nula al verificar código.");
                return false;
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, codigo);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            logger.error("Error al verificar existencia del código: {}", codigo, e);
        }
        return false;
    }

    // ============================================================
    //  ACTUALIZAR STOCK AL VENDER
    // ============================================================
    public boolean actualizarStock(int idProducto, int cantidad) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto=? AND stock >= ?";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return false;

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idProducto);
                ps.setInt(3,cantidad);

                int filasAfectadas = ps.executeUpdate();

                // Si filasAfectadas es 0, significa que no se actualizó porque el stock era menor a la cantidad
                if (filasAfectadas == 0) {
                    System.out.println("No hay stock suficiente para el producto ID: " + idProducto);
                    return false;
                }
                return true;
            }
        } catch(Exception e){
            logger.error("Error al actualizar el stock. ID Producto: {}, Cantidad a restar: {}", idProducto, cantidad, e);
            return false;
        }
    }

    // ============================================================
    //  GUARDAR NUEVO PRODUCTO
    // ============================================================
    public boolean guardarProducto(Producto p) {
        String sql = "INSERT INTO producto (codigo_barras, nombre, precio, stock) VALUES (?, ?, ?, ?)";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) {
                logger.error("No se pudo conectar a la base de datos para guardar el producto.");
                return false;
            }

            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, p.getCodigo_barras());
                ps.setString(2, p.getNombre());
                ps.setDouble(3, p.getPrecio());
                ps.setInt(4, p.getStock());

                int filasAfectadas = ps.executeUpdate();
                return filasAfectadas > 0;
            }
        } catch(Exception e){
            logger.error("Error al guardar el producto: {}", p.getNombre(), e);
            return false;
        }
    }

    // ============================================================
    //  OBTENER TODOS LOS PRODUCTOS (Renombrado para tu Controlador)
    // ============================================================
    public List<Producto> obtenerTodos() {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM producto";

        try (Connection cn = ConexionDB.conectar()) {
            if (cn == null) return lista;

            try (PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Producto p = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo_barras"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    );
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            logger.error("Error al listar los productos", e);
        }
        return lista;
    }
}