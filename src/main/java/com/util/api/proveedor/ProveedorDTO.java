package com.util.api.proveedor;

public class ProveedorDTO {

    public int codigo;
    public Integer codigoProveedorExterno;

    public String razonSocial;

    public int tipoIdentificacionCodigo;
    public String tipoIdentificacionNombre;

    public String codigoIdentificacion;

    public int condicionIvaCodigo;
    public String condicionIvaNombre;

    public String ingresosBrutos;

    public String telefonoFijo;
    public String telefonoMovil;

    public String emailPersonal;
    public String emailFacturacion;

    public String domicilio;

    public int localidadId;
    public String localidadNombre;

    public Integer provinciaId;
    public String provinciaNombre;

    public double bonificacion;
    public String observacion;

    public boolean habilitado;
    public boolean habilitadoUsuario;

    public ProveedorDTO() {
    }

    public static class ComboDTO {
        public int codigo;
        public String razonSocial;

        public ComboDTO() {
        }

        public ComboDTO(int codigo, String razonSocial) {
            this.codigo = codigo;
            this.razonSocial = razonSocial;
        }
    }

    public static class GuardarRequest {
        public ProveedorDTO proveedor;
        public int codigoUsuario;

        public GuardarRequest() {
        }
    }
}