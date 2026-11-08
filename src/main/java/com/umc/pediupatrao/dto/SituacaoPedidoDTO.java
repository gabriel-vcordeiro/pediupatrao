package com.umc.pediupatrao.dto;

public class SituacaoPedidoDTO {

    private String status;
    private String descricao;
    private long quantidade;
    private double percentual;
    private double valorTotal;

    public SituacaoPedidoDTO() {
    }

    public SituacaoPedidoDTO(String status, String descricao, long quantidade, double percentual, double valorTotal) {
        this.status = status;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.percentual = percentual;
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }

    public double getPercentual() {
        return percentual;
    }

    public void setPercentual(double percentual) {
        this.percentual = percentual;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}

