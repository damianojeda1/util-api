package com.util.api.localidad;

public final class LocalidadDTO {

    private LocalidadDTO() {
    }

    public static class Response {

        private int id;
        private String codigoPostal;
        private String nombre;
        private int provinciaId;
        private String provinciaNombre;
        private int paisId;
        private String paisNombre;

        public Response() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public void setCodigoPostal(String codigoPostal) {
            this.codigoPostal = codigoPostal;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getProvinciaId() {
            return provinciaId;
        }

        public void setProvinciaId(int provinciaId) {
            this.provinciaId = provinciaId;
        }

        public String getProvinciaNombre() {
            return provinciaNombre;
        }

        public void setProvinciaNombre(String provinciaNombre) {
            this.provinciaNombre = provinciaNombre;
        }

        public int getPaisId() {
            return paisId;
        }

        public void setPaisId(int paisId) {
            this.paisId = paisId;
        }

        public String getPaisNombre() {
            return paisNombre;
        }

        public void setPaisNombre(String paisNombre) {
            this.paisNombre = paisNombre;
        }
    }

    public static class CrearRequest {

        private String nombre;
        private String codigoPostal;
        private int provinciaId;

        public CrearRequest() {
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public void setCodigoPostal(String codigoPostal) {
            this.codigoPostal = codigoPostal;
        }

        public int getProvinciaId() {
            return provinciaId;
        }

        public void setProvinciaId(int provinciaId) {
            this.provinciaId = provinciaId;
        }
    }

    public static class OperacionResponse {

        private boolean exitoso;
        private int id;
        private String mensaje;

        public OperacionResponse() {
        }

        public OperacionResponse(
                boolean exitoso,
                int id,
                String mensaje
        ) {
            this.exitoso = exitoso;
            this.id = id;
            this.mensaje = mensaje;
        }

        public boolean isExitoso() {
            return exitoso;
        }

        public void setExitoso(boolean exitoso) {
            this.exitoso = exitoso;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}