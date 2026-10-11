package com.umc.pediupatrao.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String username;
    private String password;
    private String role; // ADMIN, GERENTE ou ATENDENTE

    public Usuario() {
    }

    public Usuario(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        if (role != null && role.startsWith("ROLE_")) {
            return role.substring(5);
        }
        return role;
    }

    public void setRole(String role) {
        if (role != null && role.startsWith("ROLE_")) {
            this.role = role.substring(5).toUpperCase();
        } else if (role != null) {
            this.role = role.trim().toUpperCase();
        } else {
            this.role = null;
        }
    }

    public String getRoleFormatada() {
        String r = getRole();
        if (r == null) return "ATENDENTE";
        return r;
    }
}
