package com.util.api.mediopago;

import java.math.BigDecimal;

public class MedioPagoDTO {

    public int id;
    public String codigo;
    public String descripcion;
    public String codigoEnum;
    public boolean activo;
    public int orden;
    public boolean esFisico;
    public boolean permiteRef;
    public BigDecimal porcentajeComision = BigDecimal.ZERO;

    public MedioPagoDTO() {
    }
}