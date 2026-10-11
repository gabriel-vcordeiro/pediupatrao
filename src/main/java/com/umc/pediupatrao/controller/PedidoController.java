package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Criar pedido (ATENDENTE e GERENTE)
    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public ResponseEntity<?> criarPedido(@RequestBody Pedido pedido) {
        try {
            Pedido criado = pedidoService.criarPedido(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Listar pedidos (ADMIN, GERENTE e ATENDENTE)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public ResponseEntity<List<Pedido>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarPedidos());
    }

    // Buscar pedido por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        return pedidoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar status do pedido (transições do ciclo de vida)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public ResponseEntity<?> atualizarStatus(@PathVariable String id, @RequestParam(required = false) String status,
                                            @RequestBody(required = false) Map<String, String> body) {
        try {
            String statusFinal = status;
            if (statusFinal == null && body != null) {
                statusFinal = body.get("status");
            }
            if (statusFinal == null) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Parâmetro 'status' é obrigatório."));
            }
            Pedido atualizado = pedidoService.atualizarStatus(id, statusFinal);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Cancelar pedido com justificativa (EXCLUSIVO GERENTE)
    @RequestMapping(value = "/{id}/cancelar", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> cancelarPedido(@PathVariable String id,
                                            @RequestParam(required = false) String justificativa,
                                            @RequestBody(required = false) Map<String, String> body) {
        try {
            String justFinal = justificativa;
            if (justFinal == null && body != null) {
                justFinal = body.get("justificativa");
            }
            Pedido cancelado = pedidoService.cancelarPedido(id, justFinal);
            return ResponseEntity.ok(cancelado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Aplicar desconto percentual (EXCLUSIVO GERENTE, até 20%)
    @RequestMapping(value = "/{id}/desconto", method = {RequestMethod.POST, RequestMethod.PUT})
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> aplicarDesconto(@PathVariable String id,
                                             @RequestParam(required = false) Double percentual,
                                             @RequestBody(required = false) Map<String, Object> body) {
        try {
            Double percFinal = percentual;
            if (percFinal == null && body != null && body.containsKey("percentual")) {
                percFinal = Double.valueOf(body.get("percentual").toString());
            }
            Pedido atualizado = pedidoService.aplicarDesconto(id, percFinal);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
