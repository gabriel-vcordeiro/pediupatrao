package com.umc.pediupatrao.entity;

public class ItemPedido {

    private String produtoId;
    private String nomeProduto;
    private int quantidade;
    private Double precoUnitario;
    private Double subtotal;

    public ItemPedido() {
    }

    public ItemPedido(String produtoId, String nomeProduto, int quantidade, Double precoUnitario) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = calcularSubtotal();
    }

    public Double calcularSubtotal() {
        if (precoUnitario == null || quantidade <= 0) {
            return 0.0;
        }
        return Math.round(precoUnitario * quantidade * 100.0) / 100.0;
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

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        this.subtotal = calcularSubtotal();
    }

    public Double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Double precoUnitario) {
        this.precoUnitario = precoUnitario;
        this.subtotal = calcularSubtotal();
    }

    public Double getSubtotal() {
        if (subtotal == null) {
            subtotal = calcularSubtotal();
        }
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
