package com.dao;

import com.DB.ConexionDB;
import com.modelo.Carrito;

import java.sql.*;

public class CarritoDAO implements ICarritoDAO {

    // 1. Busca si el cliente tiene un carrito pendiente, si no, le crea uno nuevo
    public int obtenerOCrearCarritoActivo(int idCliente) {
        String sqlBusqueda = "SELECT id_carrito FROM carrito WHERE id_cliente = ? AND estado = 'ACTIVO'";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement psBusqueda = cn.prepareStatement(sqlBusqueda)) {

            psBusqueda.setInt(1, idCliente);
            ResultSet rs = psBusqueda.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_carrito");
            } else {
                String sqlInsert = "INSERT INTO carrito (id_cliente, total, estado) VALUES (?, 0, 'ACTIVO')";
                try (PreparedStatement psInsert = cn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    psInsert.setInt(1, idCliente);
                    psInsert.executeUpdate();
                    ResultSet rsKeys = psInsert.getGeneratedKeys();
                    if (rsKeys.next()) {
                        return rsKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 2. Inserta el producto al carrito en la BD (o suma 1 si ya existía)
    public void agregarProductoAlBD(int idCarrito, int idProducto, double precioUnitario) {
        String sqlCheck = "SELECT cantidad FROM detalle_carrito WHERE id_carrito = ? AND id_producto = ?";

        try (Connection cn = ConexionDB.conectar();
             PreparedStatement psCheck = cn.prepareStatement(sqlCheck)) {

            psCheck.setInt(1, idCarrito);
            psCheck.setInt(2, idProducto);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // Si el producto ya está, solo sumamos 1 a la cantidad
                String sqlUpd = "UPDATE detalle_carrito SET cantidad = cantidad + 1 WHERE id_carrito = ? AND id_producto = ?";
                try (PreparedStatement psUpd = cn.prepareStatement(sqlUpd)) {
                    psUpd.setInt(1, idCarrito);
                    psUpd.setInt(2, idProducto);
                    psUpd.executeUpdate();
                }
            } else {
                // Si es un producto nuevo en este carrito, lo insertamos con los campos de tu esquema
                String sqlIns = "INSERT INTO detalle_carrito (id_carrito, id_producto, cantidad, precio_unitario) VALUES (?, ?, 1, ?)";
                try (PreparedStatement psIns = cn.prepareStatement(sqlIns)) {
                    psIns.setInt(1, idCarrito);
                    psIns.setInt(2, idProducto);
                    psIns.setDouble(3, precioUnitario);
                    psIns.executeUpdate();
                }
            }

            // Recalculamos la suma total del carrito principal
            actualizarSumaTotal(cn, idCarrito);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. Recalcula el campo 'total' de la tabla 'carrito' multiplicando cantidad por precio unitario
    private void actualizarSumaTotal(Connection cn, int idCarrito) throws SQLException {
        String sql = "UPDATE carrito SET total = (SELECT IFNULL(SUM(cantidad * precio_unitario), 0) FROM detalle_carrito WHERE id_carrito = ?) WHERE id_carrito = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.setInt(2, idCarrito);
            ps.executeUpdate();
        }
    }

    public java.util.List<com.modelo.DetalleVenta> obtenerDetallesDelCarrito(int idCarrito) {
        java.util.List<com.modelo.DetalleVenta> lista = new java.util.ArrayList<>();
        String sql = "SELECT d.id_producto, p.nombre, d.cantidad, d.precio_unitario " +
                "FROM detalle_carrito d " +
                "INNER JOIN producto p ON d.id_producto = p.id_producto " +
                "WHERE d.id_carrito = ?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id_producto");
                String nombre = rs.getString("nombre");
                int cant = rs.getInt("cantidad");
                double precio = rs.getDouble("precio_unitario");
                double subtotal = cant * precio;

                lista.add(new com.modelo.DetalleVenta(id, nombre, cant, precio, subtotal));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // Cambia el estado para que el siguiente cliente tenga un carrito limpio
    public void marcarCarritoComoPagado(int idCarrito) {
        String sql = "UPDATE carrito SET estado = 'PAGADO' WHERE id_carrito = ?";
        try (Connection cn = ConexionDB.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}