package com.umc.pediupatrao.dto;

public class PedidoResumoDashboardDTO {

    private String id;
    private String nomeCliente;
    private String resumoItens;
    private int quantidadeItens;
    private String status;
    private String descricaoStatus;
    private double valorSubtotal;
    private double valorDesconto;
    private double valorTotal;
    private String dataHoraEntrada;
    private String dataHoraSaida;
    private String responsavelRegistro;

    public PedidoResumoDashboardDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getResumoItens() {
        return resumoItens;
    }

    public void setResumoItens(String resumoItens) {
        this.resumoItens = resumoItens;
    }

    public int getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(int quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescricaoStatus() {
        return descricaoStatus;
    }

    public void setDescricaoStatus(String descricaoStatus) {
        this.descricaoStatus = descricaoStatus;
    }

    public double getValorSubtotal() {
        return valorSubtotal;
    }

    public void setValorSubtotal(double valorSubtotal) {
        this.valorSubtotal = valorSubtotal;
    }

    public double getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(double valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(String dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public String getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(String dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public String getResponsavelRegistro() {
        return responsavelRegistro;
    }

    public void setResponsavelRegistro(String responsavelRegistro) {
        this.responsavelRegistro = responsavelRegistro;
    }
}

