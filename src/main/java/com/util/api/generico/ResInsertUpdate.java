package com.util.api.generico;

public class ResInsertUpdate {
    private String codError;
    private boolean correcto;

    public ResInsertUpdate(String codError, boolean correcto) {
        this.codError = codError;
        this.correcto = correcto;
    }

    public String getCodError() {
        return codError;
    }

    public String getMensajeError() {
        switch (codError) {
            case "00000":
                // successful_completion
                return "Insertado correctamente";
            case "23505":
                // unique_violation
                return "Error, ya estaba cargado con anterioridad";
            case "40000":
                // transaction_rollback
                return "Error interno";
            case "42804":
                // Error de tipos en la base de datos
                return "Ocurrio un problema con los datos ingresados";
            default:
                // Si tiene mas de
                System.out.println("CodError: "+ codError);
                return (codError.length() < 6 ? "Error inesperado" : codError);
        }
    }

    public void setCodError(String codError) {
        this.codError = codError;
    }

    public boolean isCorrecto() {
        return correcto;
    }

    public void setCorrecto(boolean correcto) {
        this.correcto = correcto;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResInsertUpdate) {
            return this.correcto == ((ResInsertUpdate) obj).isCorrecto();
        } else if (obj instanceof Boolean) {
            return this.correcto = (Boolean) obj;
        }
        return false;
    }
}