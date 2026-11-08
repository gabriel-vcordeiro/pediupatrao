package com.umc.pediupatrao.dto;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponseDTO {

    private IndicadoresVendasDTO indicadores;
    private List<VendaPeriodoDTO> vendasPorPeriodo = new ArrayList<>();
    private List<SituacaoPedidoDTO> situacaoPedidos = new ArrayList<>();
    private List<ProdutoMaisVendidoDTO> produtosMaisVendidos = new ArrayList<>();
    private List<VendaCategoriaDTO> vendasPorCategoria = new ArrayList<>();
    private List<PedidoResumoDashboardDTO> pedidosRecentes = new ArrayList<>();
    private List<String> categoriasDisponiveis = new ArrayList<>();
    private String atualizadoEm;

    public DashboardResponseDTO() {
    }

    public IndicadoresVendasDTO getIndicadores() {
        return indicadores;
    }

    public void setIndicadores(IndicadoresVendasDTO indicadores) {
        this.indicadores = indicadores;
    }

    public List<VendaPeriodoDTO> getVendasPorPeriodo() {
        return vendasPorPeriodo;
    }

    public void setVendasPorPeriodo(List<VendaPeriodoDTO> vendasPorPeriodo) {
        this.vendasPorPeriodo = vendasPorPeriodo;
    }

    public List<SituacaoPedidoDTO> getSituacaoPedidos() {
        return situacaoPedidos;
    }

    public void setSituacaoPedidos(List<SituacaoPedidoDTO> situacaoPedidos) {
        this.situacaoPedidos = situacaoPedidos;
    }

    public List<ProdutoMaisVendidoDTO> getProdutosMaisVendidos() {
        return produtosMaisVendidos;
    }

    public void setProdutosMaisVendidos(List<ProdutoMaisVendidoDTO> produtosMaisVendidos) {
        this.produtosMaisVendidos = produtosMaisVendidos;
    }

    public List<VendaCategoriaDTO> getVendasPorCategoria() {
        return vendasPorCategoria;
    }

    public void setVendasPorCategoria(List<VendaCategoriaDTO> vendasPorCategoria) {
        this.vendasPorCategoria = vendasPorCategoria;
    }

    public List<PedidoResumoDashboardDTO> getPedidosRecentes() {
        return pedidosRecentes;
    }

    public void setPedidosRecentes(List<PedidoResumoDashboardDTO> pedidosRecentes) {
        this.pedidosRecentes = pedidosRecentes;
    }

    public List<String> getCategoriasDisponiveis() {
        return categoriasDisponiveis;
    }

    public void setCategoriasDisponiveis(List<String> categoriasDisponiveis) {
        this.categoriasDisponiveis = categoriasDisponiveis;
    }

    public String getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(String atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}

