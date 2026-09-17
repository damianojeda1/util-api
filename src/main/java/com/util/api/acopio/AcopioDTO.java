package com.util.api.acopio;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class AcopioDTO {

    private AcopioDTO() {
    }

    public static class ResumenDTO {

        public int id;
        public int ticket;

        public int cliente;
        public int usuario;

        public Date fecha;

        public String observaciones;
        public String estado;

        public double cantidadOriginal;
        public double cantidadRetirada;
        public double cantidadPendiente;

        public ResumenDTO() {
        }
    }

    public static class DetalleDTO {

        public int id;
        public int acopio;

        public String articulo;
        public String descripcion;

        public double cantidad;
        public double precio;

        public double cantidadRetirada;
        public double cantidadPendiente;

        public DetalleDTO() {
        }
    }

    public static class GuardarRetiroRequest {

        public int acopio;
        public int usuario;
        public String observaciones;

        public List<RetiroDetalleRequest> detalles =
                new ArrayList<>();

        public GuardarRetiroRequest() {
        }
    }

    public static class RetiroDetalleRequest {

        public int acopioDetalle;
        public double cantidad;

        public RetiroDetalleRequest() {
        }
    }

    public static class GuardarRetiroResponse {

        public boolean ok;
        public int idRetiro;
        public String mensaje;

        public GuardarRetiroResponse() {
        }

        public static GuardarRetiroResponse ok(
                int idRetiro
        ) {

            GuardarRetiroResponse response =
                    new GuardarRetiroResponse();

            response.ok = true;
            response.idRetiro = idRetiro;

            return response;
        }

        public static GuardarRetiroResponse error(
                String mensaje
        ) {

            GuardarRetiroResponse response =
                    new GuardarRetiroResponse();

            response.ok = false;
            response.mensaje = mensaje;

            return response;
        }
    }

}