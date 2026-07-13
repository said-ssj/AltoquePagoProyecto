/*
 * Implementamos las reglas de negocio para la localización e identificación de mercancía.
 * Hemos aplicado el Principio de Inversión de Dependencias (DIP) inyectando la interfaz abstracta
 * IProductoDAO en el constructor, eliminando de forma definitiva cualquier instanciación manual
 * y asegurando un acoplamiento laxo idóneo para pruebas unitarias.
 */
package com.servicio;

import com.dao.IProductoDAO;
import com.modelo.Producto;

public class ProductoServicio implements IProductoServicio {

    private final IProductoDAO dao;

    public ProductoServicio(IProductoDAO dao) {
        this.dao = dao;
    }

    @Override
    public Producto buscarProducto(String codigo) {
        return dao.buscarPorCodigo(codigo);
    }

    @Override
    public Producto buscarProductoPorId(int idProducto) {
        return dao.buscarPorId(idProducto);
    }
}