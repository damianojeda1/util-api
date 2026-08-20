package com.util.api.version;

import java.math.BigDecimal;

public class VersionDTO {

    public int idApp;
    public String terminal;

    public BigDecimal versionActual;

    public String fecha;
    public String observacion;
    public String fechaActualizacion;
    public String fechaRevision;

    public VersionDTO() {
    }

    public static class ActualizacionRequest {

        public int idApp;
        public String terminal;
        public BigDecimal versionActual;
        public String observacion;

        public ActualizacionRequest() {
        }

        public ActualizacionRequest(
                int idApp,
                String terminal,
                BigDecimal versionActual,
                String observacion
        ) {
            this.idApp = idApp;
            this.terminal = terminal;
            this.versionActual = versionActual;
            this.observacion = observacion;
        }
    }

    public static class IdentificacionRequest {

        public int idApp;
        public String terminal;

        public IdentificacionRequest() {
        }

        public IdentificacionRequest(
                int idApp,
                String terminal
        ) {
            this.idApp = idApp;
            this.terminal = terminal;
        }
    }

    public static class ExisteResponse {

        private boolean existe;

        public ExisteResponse() {
        }

        public ExisteResponse(
                boolean existe
        ) {
            this.existe = existe;
        }

        public boolean isExiste() {
            return existe;
        }

        public void setExiste(
                boolean existe
        ) {
            this.existe = existe;
        }
    }
}