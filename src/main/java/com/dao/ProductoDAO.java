package com.dao;

import com.modelo.Producto;
import com.DB.ConexionDB;
import java.sql.*;

public class ProductoDAO {
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
                        rs.getDouble("precio")
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
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}