package com.dao;

import com.DB.ConexionDB;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VentaDAO {
    private static final Logger logger = LoggerFactory.getLogger(VentaDAO.class);

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
            logger.error("Error al guardar la venta en la base de datos. Total intentado: {}", total, e);
        }
    }
}