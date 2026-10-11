package com.umc.pediupatrao.entity.enums;

public enum PerfilUsuario {
    ADMIN("ADMIN"),
    GERENTE("GERENTE"),
    ATENDENTE("ATENDENTE");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static boolean isValid(String role) {
        if (role == null) return false;
        String cleanRole = role.replace("ROLE_", "").toUpperCase();
        for (PerfilUsuario p : values()) {
            if (p.name().equalsIgnoreCase(cleanRole)) {
                return true;
            }
        }
        return false;
    }
}
