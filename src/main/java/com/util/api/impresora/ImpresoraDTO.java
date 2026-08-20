package com.util.api.impresora;

public final class ImpresoraDTO {

    private ImpresoraDTO() {
    }

    public static class Response {

        private String reporte;
        private String nombreImpresora;
        private String terminal;
        private boolean fiscal;

        public Response() {
        }

        public String getReporte() {
            return reporte;
        }

        public void setReporte(String reporte) {
            this.reporte = reporte;
        }

        public String getNombreImpresora() {
            return nombreImpresora;
        }

        public void setNombreImpresora(String nombreImpresora) {
            this.nombreImpresora = nombreImpresora;
        }

        public String getTerminal() {
            return terminal;
        }

        public void setTerminal(String terminal) {
            this.terminal = terminal;
        }

        public boolean isFiscal() {
            return fiscal;
        }

        public void setFiscal(boolean fiscal) {
            this.fiscal = fiscal;
        }
    }

    public static class CrearRequest {

        private String reporte;
        private String nombreImpresora;
        private String terminal;
        private boolean fiscal;

        public CrearRequest() {
        }

        public String getReporte() {
            return reporte;
        }

        public void setReporte(String reporte) {
            this.reporte = reporte;
        }

        public String getNombreImpresora() {
            return nombreImpresora;
        }

        public void setNombreImpresora(String nombreImpresora) {
            this.nombreImpresora = nombreImpresora;
        }

        public String getTerminal() {
            return terminal;
        }

        public void setTerminal(String terminal) {
            this.terminal = terminal;
        }

        public boolean isFiscal() {
            return fiscal;
        }

        public void setFiscal(boolean fiscal) {
            this.fiscal = fiscal;
        }
    }

    public static class EliminarRequest {

        private String reporte;
        private String nombreImpresora;
        private String terminal;

        public EliminarRequest() {
        }

        public String getReporte() {
            return reporte;
        }

        public void setReporte(String reporte) {
            this.reporte = reporte;
        }

        public String getNombreImpresora() {
            return nombreImpresora;
        }

        public void setNombreImpresora(String nombreImpresora) {
            this.nombreImpresora = nombreImpresora;
        }

        public String getTerminal() {
            return terminal;
        }

        public void setTerminal(String terminal) {
            this.terminal = terminal;
        }
    }

    public static class OperacionResponse {

        private boolean exitoso;
        private String mensaje;

        public OperacionResponse() {
        }

        public OperacionResponse(
                boolean exitoso,
                String mensaje
        ) {
            this.exitoso = exitoso;
            this.mensaje = mensaje;
        }

        public boolean isExitoso() {
            return exitoso;
        }

        public void setExitoso(boolean exitoso) {
            this.exitoso = exitoso;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}