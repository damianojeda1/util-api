package com.util.api.unidadventa;

public class UnidadVentaDTO {

    public int codigo;
    public String nombre;

    public UnidadVentaDTO() {
    }

    public UnidadVentaDTO(
            int codigo,
            String nombre
    ) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public static class GuardarRequest {

        public String nombre;

        public GuardarRequest() {
        }
    }
}