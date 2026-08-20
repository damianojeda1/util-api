package com.util.api.reportes;

import com.util.api.reportes.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteComprobanteDTO {

    private ReporteEmpresaDTO empresa;
    private ReporteClienteDTO cliente;
    private ReporteCabeceraDTO comprobante;
    private ReporteTotalesDTO totales;

    private List<ReporteItemDTO> items = new ArrayList<>();
    private List<ReportePagoDTO> pagos = new ArrayList<>();

    private Map<String, Object> parametros = new HashMap<>();

    public ReporteComprobanteDTO() {
    }

    public ReporteEmpresaDTO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(ReporteEmpresaDTO empresa) {
        this.empresa = empresa;
    }

    public ReporteClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ReporteClienteDTO cliente) {
        this.cliente = cliente;
    }

    public ReporteCabeceraDTO getComprobante() {
        return comprobante;
    }

    public void setComprobante(ReporteCabeceraDTO comprobante) {
        this.comprobante = comprobante;
    }

    public ReporteTotalesDTO getTotales() {
        return totales;
    }

    public void setTotales(ReporteTotalesDTO totales) {
        this.totales = totales;
    }

    public List<ReporteItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReporteItemDTO> items) {
        this.items = items;
    }

    public List<ReportePagoDTO> getPagos() {
        return pagos;
    }

    public void setPagos(List<ReportePagoDTO> pagos) {
        this.pagos = pagos;
    }

    public Map<String, Object> getParametros() {
        return parametros;
    }

    public void setParametros(Map<String, Object> parametros) {
        this.parametros = parametros;
    }
}