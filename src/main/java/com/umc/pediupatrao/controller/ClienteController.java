package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.service.ClienteService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Listar clientes (ADMIN, GERENTE e ATENDENTE)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<Cliente> listarClientes() {
        return clienteService.listarClientes();
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Excluir cliente (ADMIN e GERENTE)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluirCliente(@PathVariable String id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // Criar cliente (ATENDENTE e GERENTE)
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public ResponseEntity<?> salvarCliente(@RequestBody Cliente cliente) {
        try {
            Cliente salvo = clienteService.salvar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Atualizar cliente (ATENDENTE e GERENTE)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public ResponseEntity<?> atualizarCliente(@PathVariable String id, @RequestBody Cliente cliente) {
        try {
            cliente.setId(id);
            Cliente atualizado = clienteService.salvar(cliente);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
