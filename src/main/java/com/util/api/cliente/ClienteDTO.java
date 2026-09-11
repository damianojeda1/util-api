package com.util.api.cliente;

public final class ClienteDTO {

    private ClienteDTO() {
    }

    public static class Response {

        public int codigo;
        public String razonSocial;

        public int tipoIdentificacionId;
        public String tipoIdentificacionNombre;

        public String codigoIdentificacion;

        public int condicionIvaId;
        public String condicionIvaNombre;

        public String ingresosBrutos;
        public String telefonoFijo;
        public String telefonoMovil;
        public String emailPersonal;
        public String emailFacturacion;
        public String domicilio;

        public int vendedorId;
        public String vendedorNombre;

        public int localidadId;
        public String localidadCodigoPostal;
        public String localidadNombre;
        public int provinciaId;
        public String provinciaNombre;
        public int paisId;
        public String paisNombre;

        public String observacion;
        public double limiteCuentaCorriente;
        public boolean cuentaCorriente;
        public boolean habilitado;
        public boolean mayorista;

        public Response() {
        }

        public int getCodigo() {
            return codigo;
        }

        public void setCodigo(int codigo) {
            this.codigo = codigo;
        }

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public int getTipoIdentificacionId() {
            return tipoIdentificacionId;
        }

        public void setTipoIdentificacionId(int tipoIdentificacionId) {
            this.tipoIdentificacionId = tipoIdentificacionId;
        }

        public String getTipoIdentificacionNombre() {
            return tipoIdentificacionNombre;
        }

        public void setTipoIdentificacionNombre(String tipoIdentificacionNombre) {
            this.tipoIdentificacionNombre = tipoIdentificacionNombre;
        }

        public String getCodigoIdentificacion() {
            return codigoIdentificacion;
        }

        public void setCodigoIdentificacion(String codigoIdentificacion) {
            this.codigoIdentificacion = codigoIdentificacion;
        }

        public int getCondicionIvaId() {
            return condicionIvaId;
        }

        public void setCondicionIvaId(int condicionIvaId) {
            this.condicionIvaId = condicionIvaId;
        }

        public String getCondicionIvaNombre() {
            return condicionIvaNombre;
        }

        public void setCondicionIvaNombre(String condicionIvaNombre) {
            this.condicionIvaNombre = condicionIvaNombre;
        }

        public String getIngresosBrutos() {
            return ingresosBrutos;
        }

        public void setIngresosBrutos(String ingresosBrutos) {
            this.ingresosBrutos = ingresosBrutos;
        }

        public String getTelefonoFijo() {
            return telefonoFijo;
        }

        public void setTelefonoFijo(String telefonoFijo) {
            this.telefonoFijo = telefonoFijo;
        }

        public String getTelefonoMovil() {
            return telefonoMovil;
        }

        public void setTelefonoMovil(String telefonoMovil) {
            this.telefonoMovil = telefonoMovil;
        }

        public String getEmailPersonal() {
            return emailPersonal;
        }

        public void setEmailPersonal(String emailPersonal) {
            this.emailPersonal = emailPersonal;
        }

        public String getEmailFacturacion() {
            return emailFacturacion;
        }

        public void setEmailFacturacion(String emailFacturacion) {
            this.emailFacturacion = emailFacturacion;
        }

        public String getDomicilio() {
            return domicilio;
        }

        public void setDomicilio(String domicilio) {
            this.domicilio = domicilio;
        }

        public int getVendedorId() {
            return vendedorId;
        }

        public void setVendedorId(int vendedorId) {
            this.vendedorId = vendedorId;
        }

        public String getVendedorNombre() {
            return vendedorNombre;
        }

        public void setVendedorNombre(String vendedorNombre) {
            this.vendedorNombre = vendedorNombre;
        }

        public int getLocalidadId() {
            return localidadId;
        }

        public void setLocalidadId(int localidadId) {
            this.localidadId = localidadId;
        }

        public String getLocalidadCodigoPostal() {
            return localidadCodigoPostal;
        }

        public void setLocalidadCodigoPostal(String localidadCodigoPostal) {
            this.localidadCodigoPostal = localidadCodigoPostal;
        }

        public String getLocalidadNombre() {
            return localidadNombre;
        }

        public void setLocalidadNombre(String localidadNombre) {
            this.localidadNombre = localidadNombre;
        }

        public int getProvinciaId() {
            return provinciaId;
        }

        public void setProvinciaId(int provinciaId) {
            this.provinciaId = provinciaId;
        }

        public String getProvinciaNombre() {
            return provinciaNombre;
        }

        public void setProvinciaNombre(String provinciaNombre) {
            this.provinciaNombre = provinciaNombre;
        }

        public int getPaisId() {
            return paisId;
        }

        public void setPaisId(int paisId) {
            this.paisId = paisId;
        }

        public String getPaisNombre() {
            return paisNombre;
        }

        public void setPaisNombre(String paisNombre) {
            this.paisNombre = paisNombre;
        }

        public String getObservacion() {
            return observacion;
        }

        public void setObservacion(String observacion) {
            this.observacion = observacion;
        }

        public double getLimiteCuentaCorriente() {
            return limiteCuentaCorriente;
        }

        public void setLimiteCuentaCorriente(double limiteCuentaCorriente) {
            this.limiteCuentaCorriente = limiteCuentaCorriente;
        }

        public boolean isCuentaCorriente() {
            return cuentaCorriente;
        }

        public void setCuentaCorriente(boolean cuentaCorriente) {
            this.cuentaCorriente = cuentaCorriente;
        }

        public boolean isHabilitado() {
            return habilitado;
        }

        public void setHabilitado(boolean habilitado) {
            this.habilitado = habilitado;
        }

        public boolean isMayorista() {
            return mayorista;
        }

        public void setMayorista(boolean mayorista) {
            this.mayorista = mayorista;
        }
    }

    public static class CrearRequest {

        public String razonSocial;

        public int tipoIdentificacionId;
        public String codigoIdentificacion;

        public int condicionIvaId;

        public String ingresosBrutos;
        public String telefonoFijo;
        public String telefonoMovil;
        public String emailPersonal;
        public String emailFacturacion;
        public String domicilio;

        public int vendedorId;
        public int localidadId;

        public String observacion;

        public boolean cuentaCorriente;
        public double limiteCuentaCorriente;
        public double montoInicialCuentaCorriente;

        public boolean habilitado;
        public boolean mayorista;

        /*
         * Usuario logueado que realiza la operación.
         * Se utiliza únicamente para registrar el ticket
         * correspondiente al saldo inicial.
         */
        public int usuarioActualId;
        public String usuarioActualNombre;

        public CrearRequest() {
        }
    }

    public static class ActualizarRequest {

        public String razonSocial;

        public int tipoIdentificacionId;
        public String codigoIdentificacion;

        public int condicionIvaId;

        public String ingresosBrutos;
        public String telefonoFijo;
        public String telefonoMovil;
        public String emailPersonal;
        public String emailFacturacion;
        public String domicilio;

        public int vendedorId;
        public int localidadId;

        public String observacion;

        public boolean cuentaCorriente;
        public double limiteCuentaCorriente;

        public boolean habilitado;
        public boolean mayorista;

        public ActualizarRequest() {
        }
    }

    public static class OperacionResponse {

        public boolean exitoso;
        public String codigo;

        public OperacionResponse() {
        }

        public OperacionResponse(
                boolean exitoso,
                String codigo
        ) {
            this.exitoso = exitoso;
            this.codigo = codigo;
        }
    }

    public static class CodigoResponse {

        public int codigo;

        public CodigoResponse() {
        }

        public CodigoResponse(int codigo) {
            this.codigo = codigo;
        }
    }

    public static class SaldoResponse {

        public int clienteId;
        public double saldo;

        public SaldoResponse() {
        }

        public SaldoResponse(
                int clienteId,
                double saldo
        ) {
            this.clienteId = clienteId;
            this.saldo = saldo;
        }
    }
}