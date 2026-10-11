package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Listar todos os registros de auditoria (ADMIN e GERENTE)
    @GetMapping
    public ResponseEntity<List<Auditoria>> listarTodas() {
        return ResponseEntity.ok(auditoriaService.listarTodas());
    }

    // Filtrar auditoria por tipo de entidade (PEDIDO, CLIENTE, USUARIO)
    @GetMapping("/entidade/{entidade}")
    public ResponseEntity<List<Auditoria>> buscarPorEntidade(@PathVariable String entidade) {
        return ResponseEntity.ok(auditoriaService.buscarPorEntidade(entidade.toUpperCase()));
    }

    // Filtrar auditoria por identificador da entidade afetada
    @GetMapping("/entidade-id/{id}")
    public ResponseEntity<List<Auditoria>> buscarPorEntidadeId(@PathVariable String id) {
        return ResponseEntity.ok(auditoriaService.buscarPorEntidadeId(id));
    }

    // Filtrar auditoria por usuário que executou a operação
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<Auditoria>> buscarPorUsuario(@PathVariable String usuario) {
        return ResponseEntity.ok(auditoriaService.buscarPorUsuario(usuario));
    }
}
