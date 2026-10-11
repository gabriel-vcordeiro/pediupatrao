package com.umc.pediupatrao.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum StatusPedido {
    RECEBIDO("Recebido"),
    EM_PREPARACAO("Em Preparação"),
    PRONTO("Pronto"),
    SAIU_PARA_ENTREGA("Saiu para Entrega"),
    RETIRADO("Retirado"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna os status para os quais o pedido pode transitar legalmente no fluxo padrão.
     */
    public Set<StatusPedido> transicoesValidas() {
        return switch (this) {
            case RECEBIDO -> EnumSet.of(EM_PREPARACAO, CANCELADO);
            case EM_PREPARACAO -> EnumSet.of(PRONTO, CANCELADO);
            case PRONTO -> EnumSet.of(SAIU_PARA_ENTREGA, RETIRADO, CANCELADO);
            case SAIU_PARA_ENTREGA -> EnumSet.of(FINALIZADO);
            case RETIRADO -> EnumSet.of(FINALIZADO);
            case FINALIZADO, CANCELADO -> Collections.emptySet();
        };
    }

    /**
     * Indica se a transição para o novo status é permitida.
     */
    public boolean podeTransitarPara(StatusPedido novoStatus) {
        return transicoesValidas().contains(novoStatus);
    }

    /**
     * O cancelamento só é permitido antes da saída para entrega ou retirada.
     */
    public boolean permiteCancelamento() {
        return this == RECEBIDO || this == EM_PREPARACAO || this == PRONTO;
    }

    /**
     * Descontos só podem ser aplicados antes da saída para entrega ou retirada.
     */
    public boolean permiteDesconto() {
        return this == RECEBIDO || this == EM_PREPARACAO || this == PRONTO;
    }

    /**
     * Indica se é um status de saída (que exige registro de data/hora e responsável da saída).
     */
    public boolean isStatusSaida() {
        return this == SAIU_PARA_ENTREGA || this == RETIRADO;
    }

    /**
     * Converte de String para StatusPedido com tolerância a formatos antigos ou nulos.
     */
    public static StatusPedido fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            return RECEBIDO;
        }
        String clean = valor.trim().toUpperCase()
                .replace(" ", "_")
                .replace("PREPARO", "PREPARACAO")
                .replace("EM_PREPARACAO", "EM_PREPARACAO");
        for (StatusPedido s : values()) {
            if (s.name().equalsIgnoreCase(clean)) {
                return s;
            }
        }
        try {
            return StatusPedido.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return RECEBIDO;
        }
    }
}
