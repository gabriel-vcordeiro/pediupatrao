package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    // Permitido ao ATENDENTE e GERENTE registrar novos pedidos
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido criarPedido(Pedido pedido) {
        String usuarioLogado = auditoriaService.obterUsuarioAutenticado();
        String perfilLogado = auditoriaService.obterPerfilAutenticado();

        // RF02 & RF03: Responsável e data/hora de entrada
        pedido.setResponsavelRegistro(usuarioLogado);
        pedido.setDataHoraEntrada(LocalDateTime.now());
        pedido.setStatusEnum(StatusPedido.RECEBIDO);

        // Validação e sincronização dos itens com preços oficiais do catálogo
        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            for (ItemPedido item : pedido.getItens()) {
                if (item.getQuantidade() <= 0) {
                    item.setQuantidade(1);
                }
                // Proteção: Preço unitário validado a partir do banco de dados quando informado produtoId
                if (item.getProdutoId() != null && !item.getProdutoId().isBlank()) {
                    Optional<Produto> produtoOpt = produtoRepository.findById(item.getProdutoId());
                    if (produtoOpt.isPresent()) {
                        Produto prod = produtoOpt.get();
                        item.setNomeProduto(prod.getNome());
                        item.setPrecoUnitario(prod.getPreco());
                    }
                }
                if (item.getPrecoUnitario() == null || item.getPrecoUnitario() < 0) {
                    item.setPrecoUnitario(0.0);
                }
                item.setSubtotal(item.calcularSubtotal());
            }
        } else if (pedido.getTipoPizza() != null || pedido.getSabor() != null) {
            // Retrocompatibilidade para criação via atributos legados
            String nome = (pedido.getTipoPizza() != null ? pedido.getTipoPizza() : "Pizza") +
                    (pedido.getSabor() != null ? " - " + pedido.getSabor() : "");
            int qtd = pedido.getQuantidade() > 0 ? pedido.getQuantidade() : 1;
            Double preco = pedido.getValorTotal() != null && pedido.getValorTotal() > 0
                    ? pedido.getValorTotal() / qtd : 50.0;

            ItemPedido itemLegado = new ItemPedido(null, nome, qtd, preco);
            pedido.getItens().add(itemLegado);
        }

        // RF01 & RF04: Apenas GERENTE pode aplicar descontos
        if (pedido.getDescontoPercentual() != null && pedido.getDescontoPercentual() > 0) {
            if (!"GERENTE".equalsIgnoreCase(perfilLogado)) {
                // Se não for gerente, o desconto não é autorizado
                pedido.setDescontoPercentual(0.0);
            } else if (pedido.getDescontoPercentual() > 20.0) {
                pedido.setDescontoPercentual(20.0);
            }
        } else {
            pedido.setDescontoPercentual(0.0);
        }

        // RF02: Cálculo do valor final estritamente no servidor
        pedido.recalcularValores();

        Pedido salvo = pedidoRepository.save(pedido);

        // RF05: Auditoria persistente da inclusão de pedido
        auditoriaService.registrar("CRIAÇÃO DE PEDIDO", "PEDIDO", salvo.getId(),
                "status", "—", StatusPedido.RECEBIDO.name(),
                "Pedido criado com " + salvo.getQuantidade() + " item(ns). Valor Total: R$ " + salvo.getValorTotal());

        return salvo;
    }

    // Consulta de pedidos disponível para ADMIN, GERENTE e ATENDENTE
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public Optional<Pedido> buscarPorId(String id) {
        return pedidoRepository.findById(id);
    }

    // RF03: Controle de fluxo e transições de pedidos (ATENDENTE e GERENTE)
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Pedido atualizarStatus(String id, String novoStatusStr) {
        StatusPedido novoStatus = StatusPedido.fromString(novoStatusStr);

        // Cancelamento deve ser feito exclusivamente pela operação dedicada de cancelamento
        if (novoStatus == StatusPedido.CANCELADO) {
            throw new IllegalArgumentException("O cancelamento deve ser realizado pela função de cancelamento com justificativa obrigatória.");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        StatusPedido statusAtual = pedido.getStatusEnum();

        // RF03: Impedir qualquer alteração após cancelamento ou finalização
        if (statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Operação bloqueada: o pedido está CANCELADO e não permite novas alterações.");
        }
        if (statusAtual == StatusPedido.FINALIZADO) {
            throw new IllegalStateException("Operação bloqueada: o pedido já está FINALIZADO.");
        }

        // RF03: Validar transição permitida
        if (!statusAtual.podeTransitarPara(novoStatus)) {
            throw new IllegalStateException("Transição inválida! Não é permitido transitar de ["
                    + statusAtual.getDescricao() + "] para [" + novoStatus.getDescricao() + "].");
        }

        String usuarioLogado = auditoriaService.obterUsuarioAutenticado();

        // RF03: Registrar data/hora e responsável pela saída ao passar para SAIU_PARA_ENTREGA ou RETIRADO
        if (novoStatus.isStatusSaida()) {
            pedido.setDataHoraSaida(LocalDateTime.now());
            pedido.setResponsavelSaida(usuarioLogado);
        }

        pedido.setStatusEnum(novoStatus);
        Pedido atualizado = pedidoRepository.save(pedido);

        // RF05: Auditoria persistente da mudança de status
        auditoriaService.registrar("MUDANÇA DE STATUS", "PEDIDO", atualizado.getId(),
                "status", statusAtual.name(), novoStatus.name(),
                "Transição de status de " + statusAtual.getDescricao() + " para " + novoStatus.getDescricao());

        return atualizado;
    }

    // RF04: Cancelamento exclusivo do GERENTE com justificativa
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelarPedido(String id, String justificativa) {
        if (justificativa == null || justificativa.trim().isBlank()) {
            throw new IllegalArgumentException("A justificativa é obrigatória para o cancelamento do pedido.");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        StatusPedido statusAtual = pedido.getStatusEnum();

        if (statusAtual == StatusPedido.CANCELADO) {
            throw new IllegalStateException("O pedido já se encontra CANCELADO.");
        }

        // RF03 & RF04: Cancelamento somente antes da saída para entrega ou retirada
        if (!statusAtual.permiteCancelamento()) {
            throw new IllegalStateException("Cancelamento não permitido: o pedido já saiu para entrega/retirada ou foi concluído. Status atual: "
                    + statusAtual.getDescricao());
        }

        pedido.setStatusEnum(StatusPedido.CANCELADO);
        pedido.setJustificativaCancelamento(justificativa.trim());
        Pedido cancelado = pedidoRepository.save(pedido);

        // RF05: Auditoria persistente do cancelamento
        auditoriaService.registrar("CANCELAMENTO DE PEDIDO", "PEDIDO", cancelado.getId(),
                "status", statusAtual.name(), StatusPedido.CANCELADO.name(),
                justificativa.trim());

        return cancelado;
    }

    // RF04: Desconto exclusivo do GERENTE (máximo 20% e antes da saída)
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(String id, Double percentual) {
        if (percentual == null || percentual <= 0.0 || percentual > 20.0) {
            throw new IllegalArgumentException("O desconto deve ser um percentual positivo e limitado a no máximo 20%.");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + id));

        StatusPedido statusAtual = pedido.getStatusEnum();

        if (statusAtual == StatusPedido.CANCELADO || statusAtual == StatusPedido.FINALIZADO) {
            throw new IllegalStateException("Não é possível aplicar desconto em pedido cancelado ou finalizado.");
        }

        // RF04: Somente antes da saída para entrega ou retirada
        if (!statusAtual.permiteDesconto()) {
            throw new IllegalStateException("Desconto só pode ser aplicado antes da saída para entrega ou retirada. Status atual: "
                    + statusAtual.getDescricao());
        }

        Double valorTotalAnterior = pedido.getValorTotal();
        Double descontoAnterior = pedido.getDescontoPercentual();

        pedido.setDescontoPercentual(percentual);
        pedido.recalcularValores();

        Pedido atualizado = pedidoRepository.save(pedido);

        // RF05: Auditoria persistente da aplicação de desconto
        auditoriaService.registrar("APLICAÇÃO DE DESCONTO", "PEDIDO", atualizado.getId(),
                "descontoPercentual",
                descontoAnterior + "% (R$ " + valorTotalAnterior + ")",
                percentual + "% (R$ " + atualizado.getValorTotal() + ")",
                "Desconto comercial de " + percentual + "% aplicado pelo gerente.");

        return atualizado;
    }
}
