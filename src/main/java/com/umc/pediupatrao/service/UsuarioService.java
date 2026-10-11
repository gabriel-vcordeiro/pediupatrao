package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.PerfilUsuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    // Busca um usuário por ID (apenas ADMIN pode consultar detalhes de usuários)
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    // Busca um usuário por username (uso interno/admin)
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Salva um novo usuário (restrito exclusivamente a ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario salvarUsuario(Usuario usuario) {
        if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username é obrigatório.");
        }

        // Validação rigorosa de perfil para impedir atribuição indevida de privilégios
        if (usuario.getRole() == null || !PerfilUsuario.isValid(usuario.getRole())) {
            throw new IllegalArgumentException("Perfil inválido. Perfis permitidos: ADMIN, GERENTE, ATENDENTE.");
        }

        usuario.setRole(usuario.getRole().toUpperCase());

        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("A senha é obrigatória para cadastro.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        Usuario salvo = usuarioRepository.save(usuario);

        // Auditoria persistente da criação de usuário (sem credenciais)
        auditoriaService.registrar("CADASTRO DE USUÁRIO", "USUARIO", salvo.getId(),
                "role", "—", salvo.getRole(), "Criação de usuário: " + salvo.getUsername());

        return salvo;
    }

    // Deleta um usuário pelo ID (restrito a ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletarUsuario(String id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuarioRepository.deleteById(id);
            auditoriaService.registrar("EXCLUSÃO DE USUÁRIO", "USUARIO", id,
                    "username", usuario.getUsername(), "REMOVIDO", "Exclusão do usuário: " + usuario.getUsername());
        });
    }

    // Lista todos os usuários (restrito a ADMIN)
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Atualiza o usuário (restrito a ADMIN, impede atribuição indevida)
    @PreAuthorize("hasRole('ADMIN')")
    public Usuario atualizarUsuario(String id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuarioExistente -> {
            List<AlteracaoCampo> alteracoes = new ArrayList<>();

            if (!usuarioExistente.getUsername().equals(usuarioAtualizado.getUsername())) {
                alteracoes.add(new AlteracaoCampo("username", usuarioExistente.getUsername(), usuarioAtualizado.getUsername()));
                usuarioExistente.setUsername(usuarioAtualizado.getUsername());
            }

            // Validação de perfil
            if (usuarioAtualizado.getRole() != null) {
                if (!PerfilUsuario.isValid(usuarioAtualizado.getRole())) {
                    throw new IllegalArgumentException("Perfil inválido. Perfis permitidos: ADMIN, GERENTE, ATENDENTE.");
                }
                String novaRole = usuarioAtualizado.getRole().toUpperCase();
                if (!novaRole.equals(usuarioExistente.getRole())) {
                    alteracoes.add(new AlteracaoCampo("role", usuarioExistente.getRole(), novaRole));
                    usuarioExistente.setRole(novaRole);
                }
            }

            // Atualiza senha se informada (não registrar o hash em auditoria)
            if (usuarioAtualizado.getPassword() != null && !usuarioAtualizado.getPassword().isBlank()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioAtualizado.getPassword()));
                alteracoes.add(new AlteracaoCampo("senha", "[REDACTED]", "[REDACTED]"));
            }

            Usuario salvo = usuarioRepository.save(usuarioExistente);

            if (!alteracoes.isEmpty()) {
                auditoriaService.registrarComAlteracoes("ALTERAÇÃO DE USUÁRIO", "USUARIO", id, alteracoes,
                        "Atualização cadastral do usuário " + salvo.getUsername());
            }

            return salvo;
        }).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado para atualização."));
    }
}
