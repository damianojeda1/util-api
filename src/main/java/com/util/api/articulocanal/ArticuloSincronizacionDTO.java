package com.util.api.articulocanal;

public class ArticuloSincronizacionDTO {

    public String codigoArticulo;
    public int codigoProveedor;

    public String descripcion;

    public double precio;
    public double stock;

    public boolean sincronizarImagen;
    public String imagenBase64;
    public String nombreImagen;

    public ArticuloSincronizacionDTO() {
    }
}