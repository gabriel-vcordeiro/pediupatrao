package com.umc.pediupatrao.entity;

import com.umc.pediupatrao.entity.enums.StatusPedido;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")
@Entity
public class Pedido {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    private String id;
    private String clienteId;
    private String nomeCliente;

    // Campos legados mantidos para compatibilidade retroativa
    private String tipoPizza;
    private String sabor;
    private int quantidade;

    // Itens detalhados do pedido
    private List<ItemPedido> itens = new ArrayList<>();

    // Status do pedido com fluxo de transições controlado
    private StatusPedido status = StatusPedido.RECEBIDO;

    // Valores calculados exclusivamente no servidor
    private Double valorSubtotal = 0.0;
    private Double descontoPercentual = 0.0; // limitado a 20%
    private Double valorDesconto = 0.0;
    private Double valorTotal = 0.0;

    // Controle de entrada
    private String responsavelRegistro;
    private LocalDateTime dataHoraEntrada;

    // Controle de saída
    private String responsavelSaida;
    private LocalDateTime dataHoraSaida;

    // Cancelamento (exclusivo do GERENTE com justificativa)
    private String justificativaCancelamento;

    public Pedido() {
        this.dataHoraEntrada = LocalDateTime.now();
        this.status = StatusPedido.RECEBIDO;
    }

    /**
     * Recalcula subtotal, desconto e valor total com base nos itens cadastrados.
     * Operação executada estritamente no servidor para impedir manipulação por requisições HTTP.
     */
    public void recalcularValores() {
        double subtotalCalculado = 0.0;
        int qtdCalculada = 0;

        if (itens != null && !itens.isEmpty()) {
            for (ItemPedido item : itens) {
                if (item != null) {
                    subtotalCalculado += item.getSubtotal() != null ? item.getSubtotal() : item.calcularSubtotal();
                    qtdCalculada += item.getQuantidade();
                }
            }
        } else if (this.valorSubtotal != null && this.valorSubtotal > 0) {
            subtotalCalculado = this.valorSubtotal;
            qtdCalculada = this.quantidade > 0 ? this.quantidade : 1;
        } else if (tipoPizza != null || sabor != null) {
            // Retrocompatibilidade se só houver campos legados
            if (this.quantidade <= 0) {
                this.quantidade = 1;
            }
            qtdCalculada = this.quantidade;
            if (this.valorTotal != null && this.valorTotal > 0) {
                subtotalCalculado = this.valorTotal;
            }
        }

        this.valorSubtotal = Math.round(subtotalCalculado * 100.0) / 100.0;
        if (qtdCalculada > 0) {
            this.quantidade = qtdCalculada;
        }

        if (this.descontoPercentual == null || this.descontoPercentual < 0.0) {
            this.descontoPercentual = 0.0;
        }
        if (this.descontoPercentual > 20.0) {
            this.descontoPercentual = 20.0;
        }

        this.valorDesconto = Math.round((this.valorSubtotal * (this.descontoPercentual / 100.0)) * 100.0) / 100.0;
        this.valorTotal = Math.round((this.valorSubtotal - this.valorDesconto) * 100.0) / 100.0;
    }

    public String getDataHoraEntradaFormatada() {
        return dataHoraEntrada != null ? dataHoraEntrada.format(FORMATTER) : "—";
    }

    public String getDataHoraSaidaFormatada() {
        return dataHoraSaida != null ? dataHoraSaida.format(FORMATTER) : "—";
    }

    // --- Getters e Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTipoPizza() {
        return tipoPizza;
    }

    public void setTipoPizza(String tipoPizza) {
        this.tipoPizza = tipoPizza;
    }

    public String getStatus() {
        return status != null ? status.name() : StatusPedido.RECEBIDO.name();
    }

    public StatusPedido getStatusEnum() {
        return status != null ? status : StatusPedido.RECEBIDO;
    }

    public void setStatus(String status) {
        this.status = StatusPedido.fromString(status);
    }

    public void setStatusEnum(StatusPedido status) {
        this.status = status;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getSabor() {
        return sabor;
    }

    public void setSabor(String sabor) {
        this.sabor = sabor;
    }

    public List<ItemPedido> getItens() {
        if (itens == null) {
            itens = new ArrayList<>();
        }
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
        recalcularValores();
    }

    public Double getValorSubtotal() {
        return valorSubtotal;
    }

    public void setValorSubtotal(Double valorSubtotal) {
        this.valorSubtotal = valorSubtotal;
    }

    public Double getDescontoPercentual() {
        return descontoPercentual;
    }

    public void setDescontoPercentual(Double descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }

    public Double getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(Double valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getResponsavelRegistro() {
        return responsavelRegistro;
    }

    public void setResponsavelRegistro(String responsavelRegistro) {
        this.responsavelRegistro = responsavelRegistro;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public String getResponsavelSaida() {
        return responsavelSaida;
    }

    public void setResponsavelSaida(String responsavelSaida) {
        this.responsavelSaida = responsavelSaida;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public String getJustificativaCancelamento() {
        return justificativaCancelamento;
    }

    public void setJustificativaCancelamento(String justificativaCancelamento) {
        this.justificativaCancelamento = justificativaCancelamento;
    }
}
