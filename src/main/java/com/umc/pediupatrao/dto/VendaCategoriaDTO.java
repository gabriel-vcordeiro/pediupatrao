package com.umc.pediupatrao.dto;

public class VendaCategoriaDTO {

    private String categoria;
    private long quantidadeVendida;
    private double faturamentoTotal;
    private double percentual;

    public VendaCategoriaDTO() {
    }

    public VendaCategoriaDTO(String categoria, long quantidadeVendida, double faturamentoTotal, double percentual) {
        this.categoria = categoria;
        this.quantidadeVendida = quantidadeVendida;
        this.faturamentoTotal = faturamentoTotal;
        this.percentual = percentual;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public void setQuantidadeVendida(long quantidadeVendida) {
        this.quantidadeVendida = quantidadeVendida;
    }

    public double getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(double faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
    }

    public double getPercentual() {
        return percentual;
    }

    public void setPercentual(double percentual) {
        this.percentual = percentual;
    }
}

