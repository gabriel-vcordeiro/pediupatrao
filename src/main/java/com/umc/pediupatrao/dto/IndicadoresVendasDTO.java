package com.umc.pediupatrao.dto;

public class IndicadoresVendasDTO {

    private double faturamentoTotal;
    private double faturamentoBruto;
    private double totalDescontos;
    private long totalPedidos;
    private long pedidosValidos;
    private long pedidosFinalizados;
    private long pedidosEmAndamento;
    private long pedidosCancelados;
    private double ticketMedio;
    private long totalItensVendidos;
    private double taxaCancelamento;

    public IndicadoresVendasDTO() {
    }

    public double getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(double faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
    }

    public double getFaturamentoBruto() {
        return faturamentoBruto;
    }

    public void setFaturamentoBruto(double faturamentoBruto) {
        this.faturamentoBruto = faturamentoBruto;
    }

    public double getTotalDescontos() {
        return totalDescontos;
    }

    public void setTotalDescontos(double totalDescontos) {
        this.totalDescontos = totalDescontos;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(long totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public long getPedidosValidos() {
        return pedidosValidos;
    }

    public void setPedidosValidos(long pedidosValidos) {
        this.pedidosValidos = pedidosValidos;
    }

    public long getPedidosFinalizados() {
        return pedidosFinalizados;
    }

    public void setPedidosFinalizados(long pedidosFinalizados) {
        this.pedidosFinalizados = pedidosFinalizados;
    }

    public long getPedidosEmAndamento() {
        return pedidosEmAndamento;
    }

    public void setPedidosEmAndamento(long pedidosEmAndamento) {
        this.pedidosEmAndamento = pedidosEmAndamento;
    }

    public long getPedidosCancelados() {
        return pedidosCancelados;
    }

    public void setPedidosCancelados(long pedidosCancelados) {
        this.pedidosCancelados = pedidosCancelados;
    }

    public double getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public long getTotalItensVendidos() {
        return totalItensVendidos;
    }

    public void setTotalItensVendidos(long totalItensVendidos) {
        this.totalItensVendidos = totalItensVendidos;
    }

    public double getTaxaCancelamento() {
        return taxaCancelamento;
    }

    public void setTaxaCancelamento(double taxaCancelamento) {
        this.taxaCancelamento = taxaCancelamento;
    }
}

