package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.dto.DashboardResponseDTO;
import com.umc.pediupatrao.dto.IndicadoresVendasDTO;
import com.umc.pediupatrao.dto.ProdutoMaisVendidoDTO;
import com.umc.pediupatrao.dto.SituacaoPedidoDTO;
import com.umc.pediupatrao.dto.VendaCategoriaDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Endpoint consolidado do Dashboard Administrativo (RF01 a RF07).
     * Retorna indicadores gerais, séries temporais para gráficos, situação dos pedidos e ranking de produtos.
     */
    @GetMapping({"", "/resumo"})
    public ResponseEntity<?> obterResumoDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        DashboardResponseDTO dados = dashboardService.obterDadosDashboard(
                dataInicio, dataFim, status, categoria, produtoId
        );
        return ResponseEntity.ok(dados);
    }

    /**
     * RF01 — Endpoint de Indicadores Gerais de Vendas.
     */
    @GetMapping("/indicadores")
    public ResponseEntity<?> obterIndicadores(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        IndicadoresVendasDTO indicadores = dashboardService.obterIndicadores(
                dataInicio, dataFim, status, categoria, produtoId
        );
        return ResponseEntity.ok(indicadores);
    }

    /**
     * RF02 — Endpoint de Análise de Vendas por Período (evolução diária de faturamento e pedidos).
     */
    @GetMapping("/vendas-periodo")
    public ResponseEntity<?> obterVendasPorPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        List<VendaPeriodoDTO> vendas = dashboardService.obterVendasPorPeriodo(
                dataInicio, dataFim, status, categoria, produtoId
        );
        return ResponseEntity.ok(vendas);
    }

    /**
     * RF02 — Endpoint de Análise de Vendas por Categoria de Produto.
     */
    @GetMapping("/vendas-categoria")
    public ResponseEntity<?> obterVendasPorCategoria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        List<VendaCategoriaDTO> categorias = dashboardService.obterDadosDashboard(
                dataInicio, dataFim, status, categoria, produtoId
        ).getVendasPorCategoria();
        return ResponseEntity.ok(categorias);
    }

    /**
     * RF03 — Endpoint de Situação dos Pedidos (distribuição por status do ciclo de vida).
     */
    @GetMapping("/situacao-pedidos")
    public ResponseEntity<?> obterSituacaoPedidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        List<SituacaoPedidoDTO> situacao = dashboardService.obterSituacaoPedidos(
                dataInicio, dataFim, status, categoria, produtoId
        );
        return ResponseEntity.ok(situacao);
    }

    /**
     * RF04 — Endpoint de Ranking de Produtos Mais Vendidos.
     */
    @GetMapping("/produtos-mais-vendidos")
    public ResponseEntity<?> obterProdutosMaisVendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String produtoId) {

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "A data inicial não pode ser posterior à data final."));
        }

        List<ProdutoMaisVendidoDTO> ranking = dashboardService.obterProdutosMaisVendidos(
                dataInicio, dataFim, status, categoria, produtoId
        );
        return ResponseEntity.ok(ranking);
    }
}

