package com.util.api.ticket;

public class ResumenPeriodoDTO {

    private double neto;
    private double iva;
    private double total;

    public ResumenPeriodoDTO() {
    }

    public double getNeto() {
        return neto;
    }

    public void setNeto(
            double neto
    ) {
        this.neto = neto;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(
            double iva
    ) {
        this.iva = iva;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(
            double total
    ) {
        this.total = total;
    }
}