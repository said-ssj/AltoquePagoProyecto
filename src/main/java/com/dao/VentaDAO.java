package com.dao;

import com.DB.ConexionDB;
import java.sql.*;

public class VentaDAO {
    public void guardarVenta(
            double total
    ){
        try{
            Connection cn = ConexionDB.conectar();
            String sql = "INSERT INTO venta(total) " + "VALUES(?)";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setDouble(1,total);
            ps.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}