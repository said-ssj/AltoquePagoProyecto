package com.servicio;

import com.dao.ProductoDAO;
import com.modelo.Producto;

public class EscaneoService {

    ProductoDAO dao = new ProductoDAO();

    public Producto escanear(String codigo_barras){
        return dao.buscarPorCodigo(codigo_barras);
    }
}
