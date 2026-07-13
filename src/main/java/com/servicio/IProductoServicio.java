/*
 * Definimos el contrato para las operaciones de negocio relacionadas con el catálogo de productos.
 * Esta abstracción blinda a los controladores de stock e inventario contra cambios directos
 * en la forma en que se extrae o procesa la información de los artículos.
 */
package com.servicio;

import com.modelo.Producto;

public interface IProductoServicio {
    Producto buscarProducto(String codigo);
    Producto buscarProductoPorId(int idProducto);
}