package com.util.api.departamento;

public class DepartamentoDTO {
    public int codigo;
    public String nombre;
    public boolean habilitado;

    public DepartamentoDTO() {}

    public DepartamentoDTO(int codigo, String nombre, boolean habilitado) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.habilitado = habilitado;
    }
}
