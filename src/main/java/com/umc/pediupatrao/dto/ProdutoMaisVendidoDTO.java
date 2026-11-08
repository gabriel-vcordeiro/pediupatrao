package com.umc.pediupatrao.dto;

public class ProdutoMaisVendidoDTO {

    private int posicao;
    private String produtoId;
    private String nomeProduto;
    private String categoria;
    private long quantidadeVendida;
    private double faturamentoTotal;
    private double percentualVendas;

    public ProdutoMaisVendidoDTO() {
    }

    public ProdutoMaisVendidoDTO(int posicao, String produtoId, String nomeProduto, String categoria,
                                 long quantidadeVendida, double faturamentoTotal, double percentualVendas) {
        this.posicao = posicao;
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.quantidadeVendida = quantidadeVendida;
        this.faturamentoTotal = faturamentoTotal;
        this.percentualVendas = percentualVendas;
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
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

    public double getPercentualVendas() {
        return percentualVendas;
    }

    public void setPercentualVendas(double percentualVendas) {
        this.percentualVendas = percentualVendas;
    }
}

