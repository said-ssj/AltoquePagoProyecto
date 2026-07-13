package com.dao;

import com.modelo.Oferta;
import java.util.List;

public interface IOfertaDAO {
    List<Oferta> listarTodas();
    List<Oferta> buscar(String texto);
    Oferta buscarOferta(int idProducto);
    boolean insertar(Oferta o);
    boolean actualizar(Oferta o);
    boolean eliminar(int idOferta);
}