package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        String role = usuario.getRole();
        if (role == null || role.isBlank()) {
            role = "ATENDENTE"; // Perfil padrão de menor privilégio
        }

        // Remove prefixo ROLE_ caso exista para o builder roles() adicionar corretamente
        role = role.replace("ROLE_", "").toUpperCase();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(role) // Mapeia para ROLE_ADMIN, ROLE_GERENTE ou ROLE_ATENDENTE
                .build();
    }
}
