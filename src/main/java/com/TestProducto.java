package com;

import com.dao.ProductoDAO;
import com.modelo.Producto;

public class TestProducto {
    public static void main(String[] args) {

        ProductoDAO dao = new ProductoDAO();

        Producto producto = dao.buscarPorCodigo("789456");

        if(producto != null){
            System.out.println("Producto encontrado");
            System.out.println(producto.getNombre());
            System.out.println(producto.getPrecio());
        }else{
            System.out.println("Producto no encontrado");
        }
    }
}