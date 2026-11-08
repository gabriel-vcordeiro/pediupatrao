package com.umc.pediupatrao;

import com.umc.pediupatrao.controller.DashboardController;
import com.umc.pediupatrao.dto.DashboardResponseDTO;
import com.umc.pediupatrao.dto.IndicadoresVendasDTO;
import com.umc.pediupatrao.dto.ProdutoMaisVendidoDTO;
import com.umc.pediupatrao.dto.SituacaoPedidoDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.service.DashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardApiTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private DashboardController dashboardController;

    private Produto prodCalabresa;
    private Produto prodQuatroQueijos;
    private Produto prodRefrigerante;

    @BeforeEach
    void setUp() throws Exception {
        dashboardController = new DashboardController();
        var field = DashboardController.class.getDeclaredField("dashboardService");
        field.setAccessible(true);
        field.set(dashboardController, dashboardService);

        prodCalabresa = new Produto();
        prodCalabresa.setId("prod-1");
        prodCalabresa.setNome("Pizza Calabresa");
        prodCalabresa.setTipo("Pizza");
        prodCalabresa.setPreco(55.0);
        prodCalabresa.setAtivo(true);

        prodQuatroQueijos = new Produto();
        prodQuatroQueijos.setId("prod-2");
        prodQuatroQueijos.setNome("Pizza Quatro Queijos");
        prodQuatroQueijos.setTipo("Pizza");
        prodQuatroQueijos.setPreco(62.0);
        prodQuatroQueijos.setAtivo(true);

        prodRefrigerante = new Produto();
        prodRefrigerante.setId("prod-3");
        prodRefrigerante.setNome("Refrigerante 2L");
        prodRefrigerante.setTipo("Bebida");
        prodRefrigerante.setPreco(14.0);
        prodRefrigerante.setAtivo(true);
    }

    private List<Pedido> criarMassaDePedidosPadrao() {
        // Pedido 1: FINALIZADO (2x Calabresa + 1x Refrigerante = 110 + 14 = 124.00, sem desconto)
        Pedido p1 = new Pedido();
        p1.setId("ped-01");
        p1.setNomeCliente("Carlos Eduardo");
        p1.setItens(List.of(
                new ItemPedido("prod-1", "Pizza Calabresa", 2, 55.0),
                new ItemPedido("prod-3", "Refrigerante 2L", 1, 14.0)
        ));
        p1.setDescontoPercentual(0.0);
        p1.recalcularValores();
        p1.setStatusEnum(StatusPedido.FINALIZADO);
        p1.setDataHoraEntrada(LocalDateTime.of(2026, 11, 1, 19, 30));
        p1.setDataHoraSaida(LocalDateTime.of(2026, 11, 1, 20, 15));

        // Pedido 2: EM_PREPARACAO (1x Quatro Queijos = 62.00, com 10% desconto -> R$ 55.80)
        Pedido p2 = new Pedido();
        p2.setId("ped-02");
        p2.setNomeCliente("Ana Paula");
        p2.setItens(List.of(
                new ItemPedido("prod-2", "Pizza Quatro Queijos", 1, 62.0)
        ));
        p2.setDescontoPercentual(10.0);
        p2.recalcularValores();
        p2.setStatusEnum(StatusPedido.EM_PREPARACAO);
        p2.setDataHoraEntrada(LocalDateTime.of(2026, 11, 2, 20, 0));

        // Pedido 3: CANCELADO (1x Calabresa = 55.00 - não deve somar no faturamento líquido)
        Pedido p3 = new Pedido();
        p3.setId("ped-03");
        p3.setNomeCliente("Marcos Vinicius");
        p3.setItens(List.of(
                new ItemPedido("prod-1", "Pizza Calabresa", 1, 55.0)
        ));
        p3.setDescontoPercentual(0.0);
        p3.recalcularValores();
        p3.setStatusEnum(StatusPedido.CANCELADO);
        p3.setJustificativaCancelamento("Cliente desistiu do pedido");
        p3.setDataHoraEntrada(LocalDateTime.of(2026, 11, 2, 21, 0));

        return List.of(p1, p2, p3);
    }

    @Test
    @DisplayName("CT01 (RF01) - Deve calcular corretamente os indicadores gerais de vendas")
    void deveCalcularIndicadoresGeraisDeVendas() {
        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa, prodQuatroQueijos, prodRefrigerante));
        when(pedidoRepository.findAll()).thenReturn(criarMassaDePedidosPadrao());

        ResponseEntity<?> response = dashboardController.obterIndicadores(null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        IndicadoresVendasDTO ind = (IndicadoresVendasDTO) response.getBody();
        assertNotNull(ind);
        assertEquals(3, ind.getTotalPedidos());
        assertEquals(2, ind.getPedidosValidos());
        assertEquals(1, ind.getPedidosFinalizados());
        assertEquals(1, ind.getPedidosEmAndamento());
        assertEquals(1, ind.getPedidosCancelados());
        assertEquals(186.0, ind.getFaturamentoBruto());
        assertEquals(6.2, ind.getTotalDescontos());
        assertEquals(179.8, ind.getFaturamentoTotal());
        assertEquals(89.9, ind.getTicketMedio());
        assertEquals(4, ind.getTotalItensVendidos());
        assertEquals(33.33, ind.getTaxaCancelamento());
    }

    @Test
    @DisplayName("CT02 (RF02) - Deve retornar a evolução de vendas por período para os gráficos")
    void deveRetornarEvolucaoVendasPorPeriodo() {
        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa, prodQuatroQueijos, prodRefrigerante));
        when(pedidoRepository.findAll()).thenReturn(criarMassaDePedidosPadrao());

        ResponseEntity<?> response = dashboardController.obterVendasPorPeriodo(null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<VendaPeriodoDTO> serie = (List<VendaPeriodoDTO>) response.getBody();
        assertNotNull(serie);
        assertEquals(2, serie.size());
        assertEquals("2026-11-01", serie.get(0).getData());
        assertEquals(124.0, serie.get(0).getFaturamento());
        assertEquals("2026-11-02", serie.get(1).getData());
        assertEquals(55.8, serie.get(1).getFaturamento());
    }

    @Test
    @DisplayName("CT03 (RF03) - Deve retornar a situação dos pedidos em todos os status do ciclo de vida")
    void deveRetornarSituacaoDosPedidos() {
        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa, prodQuatroQueijos, prodRefrigerante));
        when(pedidoRepository.findAll()).thenReturn(criarMassaDePedidosPadrao());

        ResponseEntity<?> response = dashboardController.obterSituacaoPedidos(null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<SituacaoPedidoDTO> situacoes = (List<SituacaoPedidoDTO>) response.getBody();
        assertNotNull(situacoes);
        assertEquals(StatusPedido.values().length, situacoes.size());

        SituacaoPedidoDTO finalizados = situacoes.stream()
                .filter(s -> "FINALIZADO".equals(s.getStatus())).findFirst().orElseThrow();
        assertEquals(1, finalizados.getQuantidade());
        assertEquals(124.0, finalizados.getValorTotal());
    }

    @Test
    @DisplayName("CT04 (RF04) - Deve ordenar corretamente o ranking de produtos mais vendidos")
    void deveRetornarRankingProdutosMaisVendidos() {
        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa, prodQuatroQueijos, prodRefrigerante));
        when(pedidoRepository.findAll()).thenReturn(criarMassaDePedidosPadrao());

        ResponseEntity<?> response = dashboardController.obterProdutosMaisVendidos(null, null, null, null, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        List<ProdutoMaisVendidoDTO> ranking = (List<ProdutoMaisVendidoDTO>) response.getBody();
        assertNotNull(ranking);
        assertEquals(3, ranking.size());
        assertEquals(1, ranking.get(0).getPosicao());
        assertEquals("Pizza Calabresa", ranking.get(0).getNomeProduto());
        assertEquals(2, ranking.get(0).getQuantidadeVendida());
        assertEquals(110.0, ranking.get(0).getFaturamentoTotal());
    }

    @Test
    @DisplayName("CT05 (RF05) - Deve aplicar filtros combinados de período, status e categoria")
    void deveAplicarFiltrosCombinados() {
        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa, prodQuatroQueijos, prodRefrigerante));
        when(pedidoRepository.findAll()).thenReturn(criarMassaDePedidosPadrao());

        ResponseEntity<?> response = dashboardController.obterResumoDashboard(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                "FINALIZADO",
                "Bebida",
                null
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());

        DashboardResponseDTO resumo = (DashboardResponseDTO) response.getBody();
        assertNotNull(resumo);
        assertEquals(1, resumo.getIndicadores().getTotalPedidos());
        assertEquals(14.0, resumo.getIndicadores().getFaturamentoTotal());
        assertEquals(1, resumo.getProdutosMaisVendidos().size());
        assertEquals("Refrigerante 2L", resumo.getProdutosMaisVendidos().get(0).getNomeProduto());
    }

    @Test
    @DisplayName("CT06 (RF07) - Deve rejeitar filtro com dataInicio posterior a dataFim (HTTP 400)")
    void deveRejeitarIntervaloDeDatasInvalido() {
        ResponseEntity<?> response = dashboardController.obterResumoDashboard(
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 1),
                null, null, null
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("CT07 (RF07) - Deve tratar pedidos legados sem lista de itens sem lançar exceção")
    void deveTratarPedidosLegadosSemListaDeItens() {
        Pedido legado = new Pedido();
        legado.setId("ped-legado");
        legado.setNomeCliente("Cliente Antigo");
        legado.setTipoPizza("Pizza");
        legado.setSabor("Pizza Calabresa");
        legado.setQuantidade(2);
        legado.setValorTotal(110.0);
        legado.setStatusEnum(StatusPedido.FINALIZADO);

        when(produtoRepository.findAll()).thenReturn(List.of(prodCalabresa));
        when(pedidoRepository.findAll()).thenReturn(List.of(legado));

        DashboardResponseDTO dados = dashboardService.obterDadosDashboard(null, null, null, null, null);
        assertNotNull(dados);
        assertEquals(1, dados.getIndicadores().getTotalPedidos());
        assertEquals(110.0, dados.getIndicadores().getFaturamentoTotal());
        assertEquals(1, dados.getProdutosMaisVendidos().size());
        assertEquals("Pizza Calabresa", dados.getProdutosMaisVendidos().get(0).getNomeProduto());
    }
}

