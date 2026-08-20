package com.util.api.actualizacion;

public class ActualizacionDTO {

    public int versionActual;
    public String terminal;

    public ActualizacionDTO() {
    }

    public ActualizacionDTO(
            int versionActual,
            String terminal
    ) {
        this.versionActual = versionActual;
        this.terminal = terminal;
    }
}