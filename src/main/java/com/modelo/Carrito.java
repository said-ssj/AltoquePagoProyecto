package com.modelo;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    private List<ItemCarrito> items =
            new ArrayList<>();

    public void agregarProducto(Producto producto){
        for(ItemCarrito item : items){
            if(item.getProducto().getId_producto() == producto.getId_producto()
            ){
                item.aumentarCantidad();
                return;
            }
        }

        items.add(
                new ItemCarrito(producto, 1)
        );
    }

    public double calcularTotal(){
        double total = 0;
        for(ItemCarrito item : items){
            total += item.getSubtotal();
        }
        return total;
    }

    public List<ItemCarrito> getItems(){
        return items;
    }
}
