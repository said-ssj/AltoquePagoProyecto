package com.dao;

import java.util.List;
import java.util.Map;

public interface IConsultaDAO {
    List<String> obtenerDepartamentos();
    List<Map<String, Object>> consultarVentas(String fechaInicio, String fechaFin);
    List<Map<String, Object>> consultarProductos(int stockMinimo, String categoria);
    List<Map<String, Object>> consultarEmpleados(String departamento, String estado);
    List<Map<String, Object>> consultarGeneral(String tipo, String parametro);
}