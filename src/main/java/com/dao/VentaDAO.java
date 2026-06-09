package com.dao;

import com.DB.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.modelo.Venta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VentaDAO {
    private static final Logger logger = LoggerFactory.getLogger(VentaDAO.class);
    private String cliente;

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public int guardarVenta(int idCliente, double total){
        int idVentaGenerado = 0;
        try{
            Connection cn = ConexionDB.conectar();
            String sql = "INSERT INTO venta(id_cliente,total) VALUES(?,?)";
            PreparedStatement ps = cn.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, idCliente);
            ps.setDouble(2, total);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                idVentaGenerado = rs.getInt(1);
            }
            System.out.println("Venta guardada con ID: " + idVentaGenerado);
        }catch(Exception e){
            e.printStackTrace();
        }
        return idVentaGenerado;
    }

    public List<Venta> listarVentas() {
        List<Venta> lista = new ArrayList<>();
        try {
            Connection cn = ConexionDB.conectar();
            String sql = "SELECT v.id_venta, v.fecha, v.total, c.nombre AS cliente, " +
                            "COUNT(dv.id_producto) AS productos, " +
                            "p.estado AS estado " +
                            "FROM venta v " +
                            "INNER JOIN cliente c ON v.id_cliente = c.id_cliente " +
                            "LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta " +
                            "LEFT JOIN pago p ON v.id_venta = p.id_venta " +
                            "GROUP BY v.id_venta, v.fecha, v.total, c.nombre, p.estado " +
                            "ORDER BY v.id_venta DESC";
            PreparedStatement ps = cn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String estado = rs.getString("estado");
                if (estado == null) {
                    estado = "PENDIENTE";
                }
                Venta venta = new Venta(String.valueOf(rs.getInt("id_venta")),
                        rs.getString("fecha"),
                        rs.getString("cliente"),
                        rs.getInt("productos"),
                        rs.getDouble("total"),
                        estado
                );
                lista.add(venta);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}