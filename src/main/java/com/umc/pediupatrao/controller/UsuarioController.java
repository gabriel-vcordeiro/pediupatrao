package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.UsuarioService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para listar todos os usuários (apenas ADMIN)
    @GetMapping
    public ResponseEntity<Iterable<Usuario>> listarTodos() {
        Iterable<Usuario> usuarios = usuarioService.listarTodos();
        List<Usuario> sanitizados = new ArrayList<>();
        for (Usuario u : usuarios) {
            Usuario copy = new Usuario();
            copy.setId(u.getId());
            copy.setUsername(u.getUsername());
            copy.setRole(u.getRole());
            copy.setPassword("[PROTEGIDO]");
            sanitizados.add(copy);
        }
        return ResponseEntity.ok(sanitizados);
    }

    // Endpoint para buscar um usuário por ID (apenas ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        if (usuario.isPresent()) {
            Usuario u = usuario.get();
            Usuario copy = new Usuario();
            copy.setId(u.getId());
            copy.setUsername(u.getUsername());
            copy.setRole(u.getRole());
            copy.setPassword("[PROTEGIDO]");
            return ResponseEntity.ok(copy);
        }
        return ResponseEntity.notFound().build();
    }

    // Endpoint para criar um novo usuário (apenas ADMIN)
    @PostMapping
    public ResponseEntity<?> criarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario salvo = usuarioService.salvarUsuario(usuario);
            Usuario copy = new Usuario();
            copy.setId(salvo.getId());
            copy.setUsername(salvo.getUsername());
            copy.setRole(salvo.getRole());
            copy.setPassword("[PROTEGIDO]");
            return ResponseEntity.status(HttpStatus.CREATED).body(copy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Endpoint para atualizar um usuário (apenas ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable String id, @RequestBody Usuario usuarioAtualizado) {
        try {
            Usuario atualizado = usuarioService.atualizarUsuario(id, usuarioAtualizado);
            Usuario copy = new Usuario();
            copy.setId(atualizado.getId());
            copy.setUsername(atualizado.getUsername());
            copy.setRole(atualizado.getRole());
            copy.setPassword("[PROTEGIDO]");
            return ResponseEntity.ok(copy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Endpoint para deletar um usuário (apenas ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable String id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
