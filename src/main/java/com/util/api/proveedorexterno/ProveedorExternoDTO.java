package com.util.api.proveedorexterno;

import java.util.ArrayList;
import java.util.List;

public final class ProveedorExternoDTO {

    private ProveedorExternoDTO() {
    }

    public static class ProveedorResponse {

        private int codigoProveedor;
        private int idProveedorLocal;
        private int codigoCliente;
        private String razonSocial;
        private String ultimaActualizacion;

        public ProveedorResponse() {
        }

        public int getCodigoProveedor() {
            return codigoProveedor;
        }

        public void setCodigoProveedor(int codigoProveedor) {
            this.codigoProveedor = codigoProveedor;
        }

        public int getIdProveedorLocal() {
            return idProveedorLocal;
        }

        public void setIdProveedorLocal(int idProveedorLocal) {
            this.idProveedorLocal = idProveedorLocal;
        }

        public int getCodigoCliente() {
            return codigoCliente;
        }

        public void setCodigoCliente(int codigoCliente) {
            this.codigoCliente = codigoCliente;
        }

        public String getRazonSocial() {
            return razonSocial;
        }

        public void setRazonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
        }

        public String getUltimaActualizacion() {
            return ultimaActualizacion;
        }

        public void setUltimaActualizacion(String ultimaActualizacion) {
            this.ultimaActualizacion = ultimaActualizacion;
        }
    }

    public static class SincronizarRequest {

        private boolean cargarArticulos;

        public SincronizarRequest() {
        }

        public boolean isCargarArticulos() {
            return cargarArticulos;
        }

        public void setCargarArticulos(boolean cargarArticulos) {
            this.cargarArticulos = cargarArticulos;
        }
    }

    public static class SincronizarResponse {

        private boolean exitoso;
        private String mensaje;
        private int totalArticulos;
        private int articulosInsertados;
        private int articulosActualizados;
        private int empaquesInsertados;
        private int articulosUtilInsertados;
        private int articulosUtilActualizados;
        private String fechaActualizacion;
        private List<String> advertencias = new ArrayList<>();

        public SincronizarResponse() {
        }

        public boolean isExitoso() {
            return exitoso;
        }

        public void setExitoso(boolean exitoso) {
            this.exitoso = exitoso;
        }

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }

        public int getTotalArticulos() {
            return totalArticulos;
        }

        public void setTotalArticulos(int totalArticulos) {
            this.totalArticulos = totalArticulos;
        }

        public int getArticulosInsertados() {
            return articulosInsertados;
        }

        public void setArticulosInsertados(int articulosInsertados) {
            this.articulosInsertados = articulosInsertados;
        }

        public int getArticulosActualizados() {
            return articulosActualizados;
        }

        public void setArticulosActualizados(int articulosActualizados) {
            this.articulosActualizados = articulosActualizados;
        }

        public int getEmpaquesInsertados() {
            return empaquesInsertados;
        }

        public void setEmpaquesInsertados(int empaquesInsertados) {
            this.empaquesInsertados = empaquesInsertados;
        }

        public int getArticulosUtilInsertados() {
            return articulosUtilInsertados;
        }

        public void setArticulosUtilInsertados(int articulosUtilInsertados) {
            this.articulosUtilInsertados = articulosUtilInsertados;
        }

        public int getArticulosUtilActualizados() {
            return articulosUtilActualizados;
        }

        public void setArticulosUtilActualizados(int articulosUtilActualizados) {
            this.articulosUtilActualizados = articulosUtilActualizados;
        }

        public String getFechaActualizacion() {
            return fechaActualizacion;
        }

        public void setFechaActualizacion(String fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
        }

        public List<String> getAdvertencias() {
            return advertencias;
        }

        public void setAdvertencias(List<String> advertencias) {
            this.advertencias = advertencias;
        }
    }
}