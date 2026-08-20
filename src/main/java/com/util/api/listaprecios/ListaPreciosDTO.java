package com.util.api.listaprecios;

public final class ListaPreciosDTO {

    private ListaPreciosDTO() {
    }

    public static class Response {

        private int codigo;
        private String descripcion;
        private double porcentaje;

        public Response() {
        }

        public Response(
                int codigo,
                String descripcion,
                double porcentaje
        ) {
            this.codigo = codigo;
            this.descripcion = descripcion;
            this.porcentaje = porcentaje;
        }

        public int getCodigo() {
            return codigo;
        }

        public void setCodigo(int codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(double porcentaje) {
            this.porcentaje = porcentaje;
        }
    }

    public static class CrearRequest {

        private String descripcion;
        private double porcentaje;

        public CrearRequest() {
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(double porcentaje) {
            this.porcentaje = porcentaje;
        }
    }

    public static class ActualizarRequest {

        private int codigo;
        private String descripcion;
        private double porcentaje;

        public ActualizarRequest() {
        }

        public int getCodigo() {
            return codigo;
        }

        public void setCodigo(int codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public double getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(double porcentaje) {
            this.porcentaje = porcentaje;
        }
    }
}