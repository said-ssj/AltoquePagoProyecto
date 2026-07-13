package com.dao;

import java.util.List;
import java.util.Map;

public interface IInicioDAO {
    double obtenerVentasDelMes();
    int obtenerTotalProductos();
    double obtenerCrecimientoVentas();
    List<Map<String, Object>> obtenerVentasRecientes(int limite);
    List<Map<String, Object>> obtenerProductosMasVendidos(int limite);
}