package com.umc.pediupatrao.dto;

public class VendaPeriodoDTO {

    private String data;
    private String dataFormatada;
    private long quantidadePedidos;
    private double faturamento;
    private long itensVendidos;

    public VendaPeriodoDTO() {
    }

    public VendaPeriodoDTO(String data, String dataFormatada, long quantidadePedidos, double faturamento, long itensVendidos) {
        this.data = data;
        this.dataFormatada = dataFormatada;
        this.quantidadePedidos = quantidadePedidos;
        this.faturamento = faturamento;
        this.itensVendidos = itensVendidos;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDataFormatada() {
        return dataFormatada;
    }

    public void setDataFormatada(String dataFormatada) {
        this.dataFormatada = dataFormatada;
    }

    public long getQuantidadePedidos() {
        return quantidadePedidos;
    }

    public void setQuantidadePedidos(long quantidadePedidos) {
        this.quantidadePedidos = quantidadePedidos;
    }

    public double getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(double faturamento) {
        this.faturamento = faturamento;
    }

    public long getItensVendidos() {
        return itensVendidos;
    }

    public void setItensVendidos(long itensVendidos) {
        this.itensVendidos = itensVendidos;
    }
}

