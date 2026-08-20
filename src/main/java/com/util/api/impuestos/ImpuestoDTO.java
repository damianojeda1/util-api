package com.util.api.impuestos;

public class ImpuestoDTO {

    public int codigo;
    public double alicuota;
    public String nombre;

    public ImpuestoDTO() {
    }

    public ImpuestoDTO(
            int codigo,
            double alicuota,
            String nombre
    ) {
        this.codigo = codigo;
        this.alicuota = alicuota;
        this.nombre = nombre;
    }
}