package com.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IReporteDAO {
    List<Map<String, Object>> obtenerReporteVentas(LocalDate inicio, LocalDate fin);
    List<Map<String, Object>> obtenerReporteInventario(LocalDate inicio, LocalDate fin);
    List<Map<String, Object>> obtenerReporteEmpleados();
    void guardarRegistroReporte(int idUsuario, String tipoReporte);
    List<Map<String, Object>> obtenerHistorialReportes(int limite);
}