package com.servicio;

import com.dao.ProductoDAO;
import com.modelo.Producto;

public class ProductoServicio {
    ProductoDAO dao = new ProductoDAO();

    // Búsqueda para el escáner
    public Producto buscarProducto(String codigo){
        return dao.buscarPorCodigo(codigo);
    }

    // Búsqueda para los botones de ofertas
    public Producto buscarProductoPorId(int idProducto) {
        return dao.buscarPorId(idProducto);
    }
}