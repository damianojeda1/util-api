package com.util.api.condicioniva;

public class CondicionIVADTO {

    public int codigo;
    public String descripcion;

    public CondicionIVADTO() {
    }

    public CondicionIVADTO(
            int codigo,
            String descripcion
    ) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}