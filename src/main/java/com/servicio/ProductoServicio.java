package com.servicio;

import com.dao.ProductoDAO;
import com.modelo.Producto;

public class ProductoServicio {
    ProductoDAO dao = new ProductoDAO();

    public Producto buscarProducto(
            String codigo
    ){
        return dao.buscarPorCodigo(
                codigo
        );
    }
}