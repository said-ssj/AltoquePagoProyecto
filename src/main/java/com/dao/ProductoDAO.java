package com.dao;

import com.modelo.Producto;
import com.DB.ConexionDB;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductoDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductoDAO.class);

    public Producto buscarPorCodigo(String codigo){
        try{
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT * FROM producto WHERE codigo_barras=?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1,codigo);
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

        }catch(Exception e){
            logger.error("Error al consultar el producto con código de barras: {}", codigo, e);
        }
        return null;
    }

    public Producto buscarPorNombre(String nombre){
        try{
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT * FROM producto " +
                            "WHERE nombre LIKE ?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, "%" + nombre + "%");
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
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void actualizarStock(
            int idProducto,
            int cantidad
    ){
        try{Connection cn = ConexionDB.conectar();
            String sql = "UPDATE producto " + "SET stock = stock - ? " + "WHERE id_producto=?";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1,cantidad);
            ps.setInt(2,idProducto);
            ps.executeUpdate();
            System.out.println(
                    "Stock actualizado. Producto: "
                            + idProducto +
                            " Cantidad descontada: "
                            + cantidad
            );
        }catch(Exception e){
            logger.error("Error al actualizar el stock. ID Producto: {}, Cantidad a restar: {}", idProducto, cantidad, e);        }
    }
}