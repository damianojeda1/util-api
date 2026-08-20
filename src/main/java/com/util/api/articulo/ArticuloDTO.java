package com.util.api.articulo;

import com.util.api.generico.ParDTO;
import com.util.api.generico.TriplaDTO;

import java.util.List;

public class ArticuloDTO {

    public String codigo;
    public String descripcion;
    public String descripcionPropia;

    public ParDTO unidadVenta;
    public String unidadCompra;

    public Double multiplicadorCompra;
    public Double costo;
    public Double margenExtra;

    public ParDTO categoria;
    public TriplaDTO impuesto;
    public Double alicuota;

    public ParDTO proveedor;

    public Boolean llevaStock;
    public Double stock;
    public Double stockMinimo;

    public String artPadre;
    public Integer artPadreProveedor;
    public Integer hijos;

    public String observacion;
    public String fechaAlta;

    public Integer origen;
    public Boolean habilitado;
    public Integer estadoProveedor;


    public class CambiarProveedorRequest {
        public String codigoArticulo;
        public int proveedorActual;
        public int proveedorNuevo;
    }

    public class EstablecerMargenRequest {
        public int codigoProveedor;
        public double margen;
    }

    public class ActualizarCostosRequest {
        public List<ArticuloDTO> articulos;
        public double porcentaje;
    }

    public static class CambiarCodigoRequest {
        public String codigoActual;
        public String codigoNuevo;
        public int proveedor;
    }

    public static class ActualizarCostosProveedorRequest {

        public int codigoProveedor;
        public double porcentaje;

        public ActualizarCostosProveedorRequest() {
        }

    }

    public static class EstablecerMargenCategoriaRequest {

        public int codigoCategoria;
        public double margen;

        public EstablecerMargenCategoriaRequest() {
        }
    }

}