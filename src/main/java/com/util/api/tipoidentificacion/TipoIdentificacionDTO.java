package com.util.api.tipoidentificacion;

public class TipoIdentificacionDTO {

    public int codigo;
    public String descripcion;

    public TipoIdentificacionDTO() {
    }

    public TipoIdentificacionDTO(
            int codigo,
            String descripcion
    ) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
}