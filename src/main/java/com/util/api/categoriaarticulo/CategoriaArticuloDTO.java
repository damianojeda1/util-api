package com.util.api.categoriaarticulo;

public class CategoriaArticuloDTO {

    public int codigo;
    public String nombre;

    public CategoriaArticuloDTO() {
    }

    public CategoriaArticuloDTO(
            int codigo,
            String nombre
    ) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}