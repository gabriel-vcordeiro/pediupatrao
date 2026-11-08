package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.DashboardResponseDTO;
import com.umc.pediupatrao.dto.IndicadoresVendasDTO;
import com.umc.pediupatrao.dto.PedidoResumoDashboardDTO;
import com.umc.pediupatrao.dto.ProdutoMaisVendidoDTO;
import com.umc.pediupatrao.dto.SituacaoPedidoDTO;
import com.umc.pediupatrao.dto.VendaCategoriaDTO;
import com.umc.pediupatrao.dto.VendaPeriodoDTO;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final DateTimeFormatter DATE_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public DashboardResponseDTO obterDadosDashboard(LocalDate dataInicio,
                                                    LocalDate dataFim,
                                                    String statusFiltro,
                                                    String categoriaFiltro,
                                                    String produtoIdFiltro) {
        List<Produto> produtos = produtoRepository.findAll();
        Map<String, Produto> mapaPorId = new HashMap<>();
        Map<String, Produto> mapaPorNome = new HashMap<>();
        Set<String> categorias = new LinkedHashSet<>();

        for (Produto p : produtos) {
            if (p.getId() != null) {
                mapaPorId.put(p.getId(), p);
            }
            if (p.getNome() != null && !p.getNome().isBlank()) {
                mapaPorNome.put(p.getNome().trim().toLowerCase(), p);
            }
            if (p.getTipo() != null && !p.getTipo().isBlank()) {
                categorias.add(p.getTipo().trim());
            }
        }
        if (categorias.isEmpty()) {
            categorias.add("Pizza");
            categorias.add("Bebida");
            categorias.add("Esfiha");
            categorias.add("Lanche");
        }

        List<Pedido> todosPedidos = pedidoRepository.findAll();
        List<Pedido> pedidosFiltrados = filtrarETratarPedidos(
                todosPedidos, dataInicio, dataFim, statusFiltro, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome
        );

        DashboardResponseDTO response = new DashboardResponseDTO();
        response.setIndicadores(calcularIndicadores(pedidosFiltrados, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome));
        response.setVendasPorPeriodo(calcularVendasPorPeriodo(pedidosFiltrados, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome));
        response.setSituacaoPedidos(calcularSituacaoPedidos(pedidosFiltrados));
        response.setProdutosMaisVendidos(calcularProdutosMaisVendidos(pedidosFiltrados, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome));
        response.setVendasPorCategoria(calcularVendasPorCategoria(pedidosFiltrados, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome));
        response.setPedidosRecentes(converterPedidosRecentes(pedidosFiltrados, mapaPorId, mapaPorNome));
        response.setCategoriasDisponiveis(new ArrayList<>(categorias));
        response.setAtualizadoEm(LocalDateTime.now().format(DATETIME_BR));

        return response;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public IndicadoresVendasDTO obterIndicadores(LocalDate dataInicio, LocalDate dataFim,
                                                 String status, String categoria, String produtoId) {
        return obterDadosDashboard(dataInicio, dataFim, status, categoria, produtoId).getIndicadores();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<VendaPeriodoDTO> obterVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim,
                                                       String status, String categoria, String produtoId) {
        return obterDadosDashboard(dataInicio, dataFim, status, categoria, produtoId).getVendasPorPeriodo();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<SituacaoPedidoDTO> obterSituacaoPedidos(LocalDate dataInicio, LocalDate dataFim,
                                                        String status, String categoria, String produtoId) {
        return obterDadosDashboard(dataInicio, dataFim, status, categoria, produtoId).getSituacaoPedidos();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<ProdutoMaisVendidoDTO> obterProdutosMaisVendidos(LocalDate dataInicio, LocalDate dataFim,
                                                                 String status, String categoria, String produtoId) {
        return obterDadosDashboard(dataInicio, dataFim, status, categoria, produtoId).getProdutosMaisVendidos();
    }

    /**
     * RF05 & RF07: Aplica filtros de consulta e realiza o tratamento e normalização segura dos dados.
     */
    private List<Pedido> filtrarETratarPedidos(List<Pedido> pedidos,
                                               LocalDate dataInicio,
                                               LocalDate dataFim,
                                               String statusFiltro,
                                               String categoriaFiltro,
                                               String produtoIdFiltro,
                                               Map<String, Produto> mapaPorId,
                                               Map<String, Produto> mapaPorNome) {
        List<Pedido> resultado = new ArrayList<>();
        if (pedidos == null) {
            return resultado;
        }

        String statusNormalizado = (statusFiltro != null && !statusFiltro.isBlank() && !"TODOS".equalsIgnoreCase(statusFiltro.trim()))
                ? statusFiltro.trim().toUpperCase() : null;
        String categoriaNormalizada = (categoriaFiltro != null && !categoriaFiltro.isBlank() && !"TODAS".equalsIgnoreCase(categoriaFiltro.trim()))
                ? categoriaFiltro.trim() : null;
        String produtoIdNormalizado = (produtoIdFiltro != null && !produtoIdFiltro.isBlank() && !"TODOS".equalsIgnoreCase(produtoIdFiltro.trim()))
                ? produtoIdFiltro.trim() : null;

        for (Pedido p : pedidos) {
            if (p == null) {
                continue;
            }

            // Tratamento de valores e itens legados em memória
            normalizarPedidoEmMemoria(p, mapaPorId, mapaPorNome);

            // Filtro por data de entrada
            LocalDate dataPedido = p.getDataHoraEntrada() != null ? p.getDataHoraEntrada().toLocalDate() : null;
            if (dataInicio != null) {
                if (dataPedido == null || dataPedido.isBefore(dataInicio)) {
                    continue;
                }
            }
            if (dataFim != null) {
                if (dataPedido == null || dataPedido.isAfter(dataFim)) {
                    continue;
                }
            }

            // Filtro por situação/status
            if (statusNormalizado != null) {
                StatusPedido st = p.getStatusEnum();
                if (!st.name().equalsIgnoreCase(statusNormalizado)) {
                    continue;
                }
            }

            // Filtro por categoria ou produto específico
            if (categoriaNormalizada != null || produtoIdNormalizado != null) {
                boolean possuiItemCompativel = false;
                for (ItemPedido item : p.getItens()) {
                    if (itemCorrespondeAoFiltro(item, categoriaNormalizada, produtoIdNormalizado, mapaPorId, mapaPorNome)) {
                        possuiItemCompativel = true;
                        break;
                    }
                }
                if (!possuiItemCompativel) {
                    continue;
                }
            }

            resultado.add(p);
        }

        return resultado;
    }

    private void normalizarPedidoEmMemoria(Pedido p, Map<String, Produto> mapaPorId, Map<String, Produto> mapaPorNome) {
        if ((p.getItens() == null || p.getItens().isEmpty()) && (p.getTipoPizza() != null || p.getSabor() != null)) {
            String nomeLegado = (p.getSabor() != null && !p.getSabor().isBlank())
                    ? p.getSabor().trim()
                    : (p.getTipoPizza() != null ? p.getTipoPizza().trim() : "Produto");
            int qtd = p.getQuantidade() > 0 ? p.getQuantidade() : 1;
            Produto prodCatalogo = mapaPorNome.get(nomeLegado.toLowerCase());
            double precoUnit = prodCatalogo != null && prodCatalogo.getPreco() != null
                    ? prodCatalogo.getPreco()
                    : (p.getValorTotal() != null && p.getValorTotal() > 0 ? p.getValorTotal() / qtd : 50.0);
            String prodId = prodCatalogo != null ? prodCatalogo.getId() : null;
            ItemPedido itemSintetico = new ItemPedido(prodId, nomeLegado, qtd, precoUnit);
            p.getItens().add(itemSintetico);
        }

        if (p.getItens() != null) {
            for (ItemPedido item : p.getItens()) {
                if (item == null) continue;
                if (item.getQuantidade() <= 0) {
                    item.setQuantidade(1);
                }
                Produto prod = resolverProduto(item, mapaPorId, mapaPorNome);
                if ((item.getNomeProduto() == null || item.getNomeProduto().isBlank()) && prod != null) {
                    item.setNomeProduto(prod.getNome());
                }
                if ((item.getPrecoUnitario() == null || item.getPrecoUnitario() <= 0) && prod != null && prod.getPreco() != null) {
                    item.setPrecoUnitario(prod.getPreco());
                }
                if (item.getPrecoUnitario() == null) {
                    item.setPrecoUnitario(0.0);
                }
                item.setSubtotal(item.calcularSubtotal());
            }
        }

        if (p.getValorTotal() == null || p.getValorSubtotal() == null || (p.getValorTotal() == 0.0 && !p.getItens().isEmpty())) {
            p.recalcularValores();
        }
    }

    private Produto resolverProduto(ItemPedido item, Map<String, Produto> mapaPorId, Map<String, Produto> mapaPorNome) {
        if (item == null) {
            return null;
        }
        if (item.getProdutoId() != null && mapaPorId.containsKey(item.getProdutoId())) {
            return mapaPorId.get(item.getProdutoId());
        }
        if (item.getNomeProduto() != null && !item.getNomeProduto().isBlank()) {
            return mapaPorNome.get(item.getNomeProduto().trim().toLowerCase());
        }
        return null;
    }

    private String resolverCategoriaItem(ItemPedido item, Map<String, Produto> mapaPorId, Map<String, Produto> mapaPorNome) {
        Produto prod = resolverProduto(item, mapaPorId, mapaPorNome);
        if (prod != null && prod.getTipo() != null && !prod.getTipo().isBlank()) {
            return prod.getTipo().trim();
        }
        String nome = item != null && item.getNomeProduto() != null ? item.getNomeProduto().toLowerCase() : "";
        if (nome.contains("refrig") || nome.contains("suco") || nome.contains("água") || nome.contains("agua") || nome.contains("coca") || nome.contains("guaran")) {
            return "Bebida";
        }
        if (nome.contains("esfiha")) {
            return "Esfiha";
        }
        if (nome.contains("lanche") || nome.contains("burger") || nome.contains("hamb")) {
            return "Lanche";
        }
        return "Pizza";
    }

    private boolean itemCorrespondeAoFiltro(ItemPedido item,
                                            String categoriaFiltro,
                                            String produtoIdFiltro,
                                            Map<String, Produto> mapaPorId,
                                            Map<String, Produto> mapaPorNome) {
        if (item == null) {
            return false;
        }
        if (produtoIdFiltro != null && !produtoIdFiltro.isBlank() && !"TODOS".equalsIgnoreCase(produtoIdFiltro)) {
            Produto prod = resolverProduto(item, mapaPorId, mapaPorNome);
            String idReal = item.getProdutoId() != null ? item.getProdutoId() : (prod != null ? prod.getId() : null);
            if (idReal == null || !idReal.equals(produtoIdFiltro)) {
                return false;
            }
        }
        if (categoriaFiltro != null && !categoriaFiltro.isBlank() && !"TODAS".equalsIgnoreCase(categoriaFiltro)) {
            String cat = resolverCategoriaItem(item, mapaPorId, mapaPorNome);
            if (!cat.equalsIgnoreCase(categoriaFiltro.trim())) {
                return false;
            }
        }
        return true;
    }

    private double calcularFatorDescontoPedido(Pedido p) {
        double descPerc = p.getDescontoPercentual() != null ? p.getDescontoPercentual() : 0.0;
        if (descPerc < 0.0) descPerc = 0.0;
        if (descPerc > 20.0) descPerc = 20.0;
        return 1.0 - (descPerc / 100.0);
    }

    /**
     * RF01: Calcula os indicadores gerais de vendas a partir dos pedidos filtrados.
     */
    private IndicadoresVendasDTO calcularIndicadores(List<Pedido> pedidos,
                                                     String categoriaFiltro,
                                                     String produtoIdFiltro,
                                                     Map<String, Produto> mapaPorId,
                                                     Map<String, Produto> mapaPorNome) {
        IndicadoresVendasDTO dto = new IndicadoresVendasDTO();
        long totalPedidos = pedidos.size();
        long pedidosValidos = 0;
        long pedidosFinalizados = 0;
        long pedidosEmAndamento = 0;
        long pedidosCancelados = 0;
        double faturamentoTotal = 0.0;
        double faturamentoBruto = 0.0;
        double totalDescontos = 0.0;
        long totalItensVendidos = 0;

        boolean temFiltroItem = (categoriaFiltro != null && !categoriaFiltro.isBlank() && !"TODAS".equalsIgnoreCase(categoriaFiltro))
                || (produtoIdFiltro != null && !produtoIdFiltro.isBlank() && !"TODOS".equalsIgnoreCase(produtoIdFiltro));

        for (Pedido p : pedidos) {
            StatusPedido st = p.getStatusEnum();
            if (st == StatusPedido.CANCELADO) {
                pedidosCancelados++;
                continue;
            }

            pedidosValidos++;
            if (st == StatusPedido.FINALIZADO) {
                pedidosFinalizados++;
            } else {
                pedidosEmAndamento++;
            }

            if (!temFiltroItem) {
                double sub = p.getValorSubtotal() != null ? p.getValorSubtotal() : 0.0;
                double desc = p.getValorDesconto() != null ? p.getValorDesconto() : 0.0;
                double tot = p.getValorTotal() != null ? p.getValorTotal() : 0.0;

                faturamentoBruto += sub;
                totalDescontos += desc;
                faturamentoTotal += tot;

                int qtdItensPedido = 0;
                for (ItemPedido item : p.getItens()) {
                    if (item != null) {
                        qtdItensPedido += Math.max(0, item.getQuantidade());
                    }
                }
                if (qtdItensPedido == 0 && p.getQuantidade() > 0) {
                    qtdItensPedido = p.getQuantidade();
                }
                totalItensVendidos += qtdItensPedido;
            } else {
                double fator = calcularFatorDescontoPedido(p);
                for (ItemPedido item : p.getItens()) {
                    if (itemCorrespondeAoFiltro(item, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome)) {
                        double subItem = item.getSubtotal() != null ? item.getSubtotal() : item.calcularSubtotal();
                        double totItem = subItem * fator;
                        faturamentoBruto += subItem;
                        faturamentoTotal += totItem;
                        totalDescontos += (subItem - totItem);
                        totalItensVendidos += Math.max(0, item.getQuantidade());
                    }
                }
            }
        }

        double ticketMedio = pedidosValidos > 0 ? arredondar(faturamentoTotal / pedidosValidos) : 0.0;
        double taxaCancelamento = totalPedidos > 0 ? arredondar((pedidosCancelados * 100.0) / totalPedidos) : 0.0;

        dto.setTotalPedidos(totalPedidos);
        dto.setPedidosValidos(pedidosValidos);
        dto.setPedidosFinalizados(pedidosFinalizados);
        dto.setPedidosEmAndamento(pedidosEmAndamento);
        dto.setPedidosCancelados(pedidosCancelados);
        dto.setFaturamentoBruto(arredondar(faturamentoBruto));
        dto.setTotalDescontos(arredondar(totalDescontos));
        dto.setFaturamentoTotal(arredondar(faturamentoTotal));
        dto.setTicketMedio(ticketMedio);
        dto.setTotalItensVendidos(totalItensVendidos);
        dto.setTaxaCancelamento(taxaCancelamento);

        return dto;
    }

    /**
     * RF02: Agrupa as vendas por data para construção dos gráficos de evolução temporal.
     */
    private List<VendaPeriodoDTO> calcularVendasPorPeriodo(List<Pedido> pedidos,
                                                           String categoriaFiltro,
                                                           String produtoIdFiltro,
                                                           Map<String, Produto> mapaPorId,
                                                           Map<String, Produto> mapaPorNome) {
        Map<LocalDate, VendaPeriodoDTO> agrupado = new TreeMap<>();
        boolean temFiltroItem = (categoriaFiltro != null && !categoriaFiltro.isBlank() && !"TODAS".equalsIgnoreCase(categoriaFiltro))
                || (produtoIdFiltro != null && !produtoIdFiltro.isBlank() && !"TODOS".equalsIgnoreCase(produtoIdFiltro));

        for (Pedido p : pedidos) {
            if (p.getStatusEnum() == StatusPedido.CANCELADO) {
                continue;
            }
            LocalDate data = p.getDataHoraEntrada() != null ? p.getDataHoraEntrada().toLocalDate() : LocalDate.now();
            VendaPeriodoDTO vendaDia = agrupado.computeIfAbsent(data,
                    d -> new VendaPeriodoDTO(d.format(DATE_ISO), d.format(DATE_BR), 0, 0.0, 0));

            vendaDia.setQuantidadePedidos(vendaDia.getQuantidadePedidos() + 1);

            if (!temFiltroItem) {
                double tot = p.getValorTotal() != null ? p.getValorTotal() : 0.0;
                int qtd = 0;
                for (ItemPedido item : p.getItens()) {
                    if (item != null) qtd += Math.max(0, item.getQuantidade());
                }
                if (qtd == 0 && p.getQuantidade() > 0) qtd = p.getQuantidade();

                vendaDia.setFaturamento(arredondar(vendaDia.getFaturamento() + tot));
                vendaDia.setItensVendidos(vendaDia.getItensVendidos() + qtd);
            } else {
                double fator = calcularFatorDescontoPedido(p);
                for (ItemPedido item : p.getItens()) {
                    if (itemCorrespondeAoFiltro(item, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome)) {
                        double subItem = item.getSubtotal() != null ? item.getSubtotal() : item.calcularSubtotal();
                        vendaDia.setFaturamento(arredondar(vendaDia.getFaturamento() + (subItem * fator)));
                        vendaDia.setItensVendidos(vendaDia.getItensVendidos() + Math.max(0, item.getQuantidade()));
                    }
                }
            }
        }

        return new ArrayList<>(agrupado.values());
    }

    /**
     * RF03: Calcula a distribuição de pedidos por cada status do ciclo de vida.
     */
    private List<SituacaoPedidoDTO> calcularSituacaoPedidos(List<Pedido> pedidos) {
        Map<StatusPedido, Long> contagem = new EnumMap<>(StatusPedido.class);
        Map<StatusPedido, Double> valores = new EnumMap<>(StatusPedido.class);

        for (StatusPedido st : StatusPedido.values()) {
            contagem.put(st, 0L);
            valores.put(st, 0.0);
        }

        long total = pedidos.size();
        for (Pedido p : pedidos) {
            StatusPedido st = p.getStatusEnum();
            contagem.put(st, contagem.getOrDefault(st, 0L) + 1);
            double val = p.getValorTotal() != null ? p.getValorTotal() : 0.0;
            valores.put(st, arredondar(valores.getOrDefault(st, 0.0) + val));
        }

        List<SituacaoPedidoDTO> lista = new ArrayList<>();
        for (StatusPedido st : StatusPedido.values()) {
            long qtd = contagem.getOrDefault(st, 0L);
            double perc = total > 0 ? arredondar((qtd * 100.0) / total) : 0.0;
            double valorTotal = valores.getOrDefault(st, 0.0);
            lista.add(new SituacaoPedidoDTO(st.name(), st.getDescricao(), qtd, perc, valorTotal));
        }

        return lista;
    }

    /**
     * RF04: Gera o ranking de produtos mais vendidos (por quantidade e faturamento).
     */
    private List<ProdutoMaisVendidoDTO> calcularProdutosMaisVendidos(List<Pedido> pedidos,
                                                                     String categoriaFiltro,
                                                                     String produtoIdFiltro,
                                                                     Map<String, Produto> mapaPorId,
                                                                     Map<String, Produto> mapaPorNome) {
        Map<String, ProdutoMaisVendidoDTO> acumulado = new HashMap<>();
        double faturamentoGeralItens = 0.0;

        for (Pedido p : pedidos) {
            if (p.getStatusEnum() == StatusPedido.CANCELADO) {
                continue;
            }
            double fator = calcularFatorDescontoPedido(p);

            for (ItemPedido item : p.getItens()) {
                if (!itemCorrespondeAoFiltro(item, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome)) {
                    continue;
                }

                Produto prod = resolverProduto(item, mapaPorId, mapaPorNome);
                String nome = item.getNomeProduto() != null && !item.getNomeProduto().isBlank()
                        ? item.getNomeProduto().trim()
                        : (prod != null ? prod.getNome() : "Produto sem nome");
                String chave = nome.toLowerCase();
                String prodId = item.getProdutoId() != null ? item.getProdutoId() : (prod != null ? prod.getId() : "");
                String categoria = resolverCategoriaItem(item, mapaPorId, mapaPorNome);

                int qtd = Math.max(0, item.getQuantidade());
                double sub = item.getSubtotal() != null ? item.getSubtotal() : item.calcularSubtotal();
                double valorLiquidoItem = sub * fator;

                faturamentoGeralItens += valorLiquidoItem;

                ProdutoMaisVendidoDTO dto = acumulado.computeIfAbsent(chave,
                        k -> new ProdutoMaisVendidoDTO(0, prodId, nome, categoria, 0L, 0.0, 0.0));
                dto.setQuantidadeVendida(dto.getQuantidadeVendida() + qtd);
                dto.setFaturamentoTotal(arredondar(dto.getFaturamentoTotal() + valorLiquidoItem));
            }
        }

        List<ProdutoMaisVendidoDTO> ranking = new ArrayList<>(acumulado.values());
        ranking.sort(Comparator.comparingLong(ProdutoMaisVendidoDTO::getQuantidadeVendida).reversed()
                .thenComparing(Comparator.comparingDouble(ProdutoMaisVendidoDTO::getFaturamentoTotal).reversed()));

        for (int i = 0; i < ranking.size(); i++) {
            ProdutoMaisVendidoDTO item = ranking.get(i);
            item.setPosicao(i + 1);
            double perc = faturamentoGeralItens > 0
                    ? arredondar((item.getFaturamentoTotal() * 100.0) / faturamentoGeralItens)
                    : 0.0;
            item.setPercentualVendas(perc);
        }

        return ranking;
    }

    /**
     * RF02: Agrupa vendas por categoria/tipo de produto para análise gráfica.
     */
    private List<VendaCategoriaDTO> calcularVendasPorCategoria(List<Pedido> pedidos,
                                                               String categoriaFiltro,
                                                               String produtoIdFiltro,
                                                               Map<String, Produto> mapaPorId,
                                                               Map<String, Produto> mapaPorNome) {
        Map<String, VendaCategoriaDTO> mapa = new HashMap<>();
        double somaFaturamento = 0.0;

        for (Pedido p : pedidos) {
            if (p.getStatusEnum() == StatusPedido.CANCELADO) {
                continue;
            }
            double fator = calcularFatorDescontoPedido(p);

            for (ItemPedido item : p.getItens()) {
                if (!itemCorrespondeAoFiltro(item, categoriaFiltro, produtoIdFiltro, mapaPorId, mapaPorNome)) {
                    continue;
                }
                String categoria = resolverCategoriaItem(item, mapaPorId, mapaPorNome);
                int qtd = Math.max(0, item.getQuantidade());
                double sub = item.getSubtotal() != null ? item.getSubtotal() : item.calcularSubtotal();
                double valorLiquido = sub * fator;

                somaFaturamento += valorLiquido;

                VendaCategoriaDTO dto = mapa.computeIfAbsent(categoria,
                        c -> new VendaCategoriaDTO(c, 0L, 0.0, 0.0));
                dto.setQuantidadeVendida(dto.getQuantidadeVendida() + qtd);
                dto.setFaturamentoTotal(arredondar(dto.getFaturamentoTotal() + valorLiquido));
            }
        }

        List<VendaCategoriaDTO> resultado = new ArrayList<>(mapa.values());
        resultado.sort(Comparator.comparingDouble(VendaCategoriaDTO::getFaturamentoTotal).reversed());

        for (VendaCategoriaDTO dto : resultado) {
            double perc = somaFaturamento > 0 ? arredondar((dto.getFaturamentoTotal() * 100.0) / somaFaturamento) : 0.0;
            dto.setPercentual(perc);
        }

        return resultado;
    }

    /**
     * RF03: Converte pedidos filtrados em DTOs resumidos ordenados do mais recente ao mais antigo.
     */
    private List<PedidoResumoDashboardDTO> converterPedidosRecentes(List<Pedido> pedidos,
                                                                    Map<String, Produto> mapaPorId,
                                                                    Map<String, Produto> mapaPorNome) {
        return pedidos.stream()
                .sorted(Comparator.comparing(
                        (Pedido p) -> p.getDataHoraEntrada() != null ? p.getDataHoraEntrada() : LocalDateTime.MIN
                ).reversed())
                .limit(25)
                .map(p -> {
                    PedidoResumoDashboardDTO dto = new PedidoResumoDashboardDTO();
                    dto.setId(p.getId());
                    dto.setNomeCliente(p.getNomeCliente() != null && !p.getNomeCliente().isBlank()
                            ? p.getNomeCliente()
                            : (p.getClienteId() != null && !p.getClienteId().isBlank() ? p.getClienteId() : "Cliente Balcão"));

                    StringBuilder itensSb = new StringBuilder();
                    int qtdTotal = 0;
                    for (ItemPedido item : p.getItens()) {
                        if (item == null) continue;
                        if (itensSb.length() > 0) {
                            itensSb.append(", ");
                        }
                        Produto prod = resolverProduto(item, mapaPorId, mapaPorNome);
                        String nomeItem = item.getNomeProduto() != null ? item.getNomeProduto()
                                : (prod != null ? prod.getNome() : "Item");
                        itensSb.append(item.getQuantidade()).append("x ").append(nomeItem);
                        qtdTotal += Math.max(0, item.getQuantidade());
                    }
                    dto.setResumoItens(itensSb.length() > 0 ? itensSb.toString() : "—");
                    dto.setQuantidadeItens(qtdTotal > 0 ? qtdTotal : p.getQuantidade());

                    StatusPedido st = p.getStatusEnum();
                    dto.setStatus(st.name());
                    dto.setDescricaoStatus(st.getDescricao());
                    dto.setValorSubtotal(p.getValorSubtotal() != null ? arredondar(p.getValorSubtotal()) : 0.0);
                    dto.setValorDesconto(p.getValorDesconto() != null ? arredondar(p.getValorDesconto()) : 0.0);
                    dto.setValorTotal(p.getValorTotal() != null ? arredondar(p.getValorTotal()) : 0.0);
                    dto.setDataHoraEntrada(p.getDataHoraEntradaFormatada());
                    dto.setDataHoraSaida(p.getDataHoraSaidaFormatada());
                    dto.setResponsavelRegistro(p.getResponsavelRegistro() != null ? p.getResponsavelRegistro() : "—");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}

