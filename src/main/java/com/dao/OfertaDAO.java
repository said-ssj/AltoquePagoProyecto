package com.dao;

import com.DB.ConexionDB;
import com.modelo.Oferta;
import java.sql.*;

public class OfertaDAO {
    public Oferta buscarOferta(
            int idProducto
    ){
        try{
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT * FROM oferta " + "WHERE id_producto=? " + "AND estado=1";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setInt(1,idProducto);
            ResultSet rs =
                    ps.executeQuery();

            if(rs.next()){
                return new Oferta(
                        rs.getInt("id_oferta"),
                        rs.getInt("id_producto"),
                        rs.getString("descripcion"),
                        rs.getDouble("descuento")
                );
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
