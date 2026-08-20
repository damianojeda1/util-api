package com.util.api.reportes.dto;

public class ReporteEmpresaDTO {

    private String razonSocial;
    private String titulo;

    private String cuit;
    private String categoriaIVA;

    private String ingresosBrutos;
    private String inicioActividad;

    private String direccion;
    private String telefono;
    private String email;

    public ReporteEmpresaDTO() {
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getCategoriaIVA() {
        return categoriaIVA;
    }

    public void setCategoriaIVA(String categoriaIVA) {
        this.categoriaIVA = categoriaIVA;
    }

    public String getIngresosBrutos() {
        return ingresosBrutos;
    }

    public void setIngresosBrutos(String ingresosBrutos) {
        this.ingresosBrutos = ingresosBrutos;
    }

    public String getInicioActividad() {
        return inicioActividad;
    }

    public void setInicioActividad(String inicioActividad) {
        this.inicioActividad = inicioActividad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}