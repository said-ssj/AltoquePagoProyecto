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
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

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

        } catch(Exception e){
            logger.error("Error al consultar el producto con código de barras: {}", codigo, e);
        }
        return null;
    }

    // ============================================================
    //  BUSCAR POR NOMBRE (LIKE)
    // ============================================================
    public Producto buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM producto WHERE nombre LIKE ?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, "%" + nombre + "%");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Producto(
                        rs.getInt("id_producto"),
                        rs.getString("codigo_barras"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("stock")
                );
            }
        } catch (Exception e) {
            logger.error("Error al buscar por nombre: {}", nombre, e);
        }
        return null;
    }

    // ============================================================
    //  VERIFICAR SI EXISTE UN CÓDIGO
    // ============================================================
    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM producto WHERE codigo_barras=?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

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
    //  ACTUALIZAR STOCK AL VENDER
    // ============================================================
    public void actualizarStock(int idProducto, int cantidad) {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto=?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.executeUpdate();

            System.out.println("Stock actualizado. Producto ID: " + idProducto + " | Cantidad descontada: " + cantidad);

        } catch(Exception e){
            logger.error("Error al actualizar el stock. ID Producto: {}, Cantidad a restar: {}", idProducto, cantidad, e);
        }
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

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;

        } catch(Exception e){
            logger.error("Error al guardar el producto: {}", p.getNombre(), e);
            return false;
        }
    }

    public List<Producto> listarProductos() {

        List<Producto> lista = new ArrayList<>();

        String sql = "SELECT * FROM producto";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}