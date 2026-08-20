package com.util.api.configuracion;

public class ConfiguracionDTO {

    public String clave;
    public String valor;

    public ConfiguracionDTO() {
    }

    public ConfiguracionDTO(
            String clave,
            String valor
    ) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}