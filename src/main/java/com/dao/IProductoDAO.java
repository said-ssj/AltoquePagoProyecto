package com.dao;

import com.modelo.Producto;
import java.util.List;

public interface IProductoDAO {
    Producto buscarPorCodigo(String codigo);
    Producto buscarPorId(int idProducto);
    List<Producto> buscarPorNombre(String nombreParcial);
    boolean actualizarStock(int idProducto, int cantidad);
    List<Producto> obtenerTodos();
    boolean actualizarProducto(Producto p);
    boolean eliminarProducto(int idProducto);
    boolean existeCodigo(String codigo);
    boolean guardarProducto(Producto producto);
}