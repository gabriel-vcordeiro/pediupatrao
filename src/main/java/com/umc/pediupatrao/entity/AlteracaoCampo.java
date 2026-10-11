package com.umc.pediupatrao.entity;

public class AlteracaoCampo {

    private String campo;
    private String valorAnterior;
    private String novoValor;

    public AlteracaoCampo() {
    }

    public AlteracaoCampo(String campo, String valorAnterior, String novoValor) {
        this.campo = campo;
        this.valorAnterior = valorAnterior != null ? valorAnterior : "—";
        this.novoValor = novoValor != null ? novoValor : "—";
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }

    public String getNovoValor() {
        return novoValor;
    }

    public void setNovoValor(String novoValor) {
        this.novoValor = novoValor;
    }
}
