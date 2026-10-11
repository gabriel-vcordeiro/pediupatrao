package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuditoriaService auditoriaService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(Model model, @RequestParam(required = false) Boolean erroAcesso) {
        if (Boolean.TRUE.equals(erroAcesso)) {
            model.addAttribute("erro", "Acesso negado: seu perfil não possui autorização para a ação solicitada.");
        }
        model.addAttribute("content", "home :: content");
        return "layout";
    }

    // ==========================================
    // USUÁRIOS (EXCLUSIVO ADMIN)
    // ==========================================
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = (List<Usuario>) usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("content", "usuarios/lista :: content");
        return "layout";
    }

    @GetMapping("/usuarios/novo")
    @PreAuthorize("hasRole('ADMIN')")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @GetMapping("/usuarios/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarUsuarioForm(@PathVariable String id, Model model,
                                    RedirectAttributes redirectAttributes) {
        boolean encontrado = usuarioService.buscarPorId(id).map(u -> {
            model.addAttribute("usuario", u);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/usuarios";
        }

        model.addAttribute("content", "usuarios/form :: content");
        return "layout";
    }

    @PostMapping("/usuarios/salvar")
    @PreAuthorize("hasRole('ADMIN')")
    public String salvarUsuario(@ModelAttribute Usuario usuario,
                                RedirectAttributes redirectAttributes) {
        try {
            usuarioService.salvarUsuario(usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String atualizarUsuario(@PathVariable String id,
                                   @ModelAttribute Usuario usuario,
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.atualizarUsuario(id, usuario);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/deletar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletarUsuario(@PathVariable String id,
                                 RedirectAttributes redirectAttributes) {
        try {
            usuarioService.deletarUsuario(id);
            redirectAttributes.addFlashAttribute("sucesso", "Usuário removido com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao remover usuário: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }

    // ==========================================
    // PEDIDOS (ADMIN, GERENTE e ATENDENTE)
    // ==========================================
    @GetMapping("/pedidos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public String pedidos(Model model) {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        List<Cliente> clientes = clienteService.listarClientes();
        List<Produto> produtos = produtoService.listarProdutos();

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("clientes", clientes);
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "pedidos :: content");
        return "layout";
    }

    @PostMapping("/pedidos/salvar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String criarPedidoForm(@RequestParam String clienteId,
                                  @RequestParam String produtoId,
                                  @RequestParam(defaultValue = "1") int quantidade,
                                  @RequestParam(required = false, defaultValue = "0") Double descontoPercentual,
                                  RedirectAttributes redirectAttributes) {
        try {
            Pedido pedido = new Pedido();
            pedido.setClienteId(clienteId);

            // Vincula o nome do cliente
            clienteService.buscarPorId(clienteId).ifPresent(c -> pedido.setNomeCliente(c.getNome()));

            // Adiciona item selecionado
            ItemPedido item = new ItemPedido();
            item.setProdutoId(produtoId);
            item.setQuantidade(quantidade);
            produtoService.listarProdutos().stream()
                    .filter(p -> p.getId().equals(produtoId))
                    .findFirst()
                    .ifPresent(p -> {
                        item.setNomeProduto(p.getNome());
                        item.setPrecoUnitario(p.getPreco());
                    });

            pedido.getItens().add(item);
            pedido.setDescontoPercentual(descontoPercentual);

            Pedido criado = pedidoService.criarPedido(pedido);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido #" + criado.getId() + " registrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar pedido: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    @PostMapping("/pedidos/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String atualizarStatusWeb(@PathVariable String id,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        try {
            Pedido atualizado = pedidoService.atualizarStatus(id, status);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido #" + id + " avançou para o status: " + atualizado.getStatusEnum().getDescricao());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível avançar status: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    @PostMapping("/pedidos/{id}/cancelar")
    @PreAuthorize("hasRole('GERENTE')")
    public String cancelarPedidoWeb(@PathVariable String id,
                                    @RequestParam String justificativa,
                                    RedirectAttributes redirectAttributes) {
        try {
            pedidoService.cancelarPedido(id, justificativa);
            redirectAttributes.addFlashAttribute("sucesso", "Pedido #" + id + " foi cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar pedido: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    @PostMapping("/pedidos/{id}/desconto")
    @PreAuthorize("hasRole('GERENTE')")
    public String aplicarDescontoWeb(@PathVariable String id,
                                     @RequestParam Double percentual,
                                     RedirectAttributes redirectAttributes) {
        try {
            Pedido atualizado = pedidoService.aplicarDesconto(id, percentual);
            redirectAttributes.addFlashAttribute("sucesso", "Desconto de " + percentual + "% aplicado com sucesso! Novo Total: R$ " + atualizado.getValorTotal());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao aplicar desconto: " + e.getMessage());
        }
        return "redirect:/pedidos";
    }

    // ==========================================
    // AUDITORIA (EXCLUSIVO ADMIN E GERENTE)
    // ==========================================
    @GetMapping("/auditoria")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String listarAuditoria(@RequestParam(required = false) String entidade,
                                  @RequestParam(required = false) String usuario,
                                  Model model) {
        List<Auditoria> auditorias;
        if (entidade != null && !entidade.isBlank()) {
            auditorias = auditoriaService.buscarPorEntidade(entidade.toUpperCase());
        } else if (usuario != null && !usuario.isBlank()) {
            auditorias = auditoriaService.buscarPorUsuario(usuario);
        } else {
            auditorias = auditoriaService.listarTodas();
        }

        model.addAttribute("auditorias", auditorias);
        model.addAttribute("filtroEntidade", entidade);
        model.addAttribute("filtroUsuario", usuario);
        model.addAttribute("content", "auditoria/lista :: content");
        return "layout";
    }

    // ==========================================
    // CLIENTES
    // ==========================================
    @GetMapping("/clientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        model.addAttribute("content", "clientes/lista :: content");
        return "layout";
    }

    @GetMapping("/clientes/novo")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String novoClienteForm(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/salvar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String salvarCliente(@ModelAttribute Cliente cliente,
                                RedirectAttributes redirectAttributes) {
        try {
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String editarClienteForm(@PathVariable String id, Model model,
                                    RedirectAttributes redirectAttributes) {
        boolean encontrado = clienteService.buscarPorId(id).map(c -> {
            model.addAttribute("cliente", c);
            return true;
        }).orElse(false);

        if (!encontrado) {
            redirectAttributes.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }

        model.addAttribute("content", "clientes/form :: content");
        return "layout";
    }

    @PostMapping("/clientes/editar/{id}")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public String atualizarCliente(@PathVariable String id,
                                   @ModelAttribute Cliente cliente,
                                   RedirectAttributes redirectAttributes) {
        try {
            cliente.setId(id);
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao atualizar cliente: " + e.getMessage());
        }
        return "redirect:/clientes";
    }

    // ==========================================
    // PRODUTOS
    // ==========================================
    @GetMapping("/produtos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String produtos(Model model) {
        List<Produto> produtos = produtoService.listarProdutos();
        model.addAttribute("produtos", produtos);
        model.addAttribute("content", "produtos/lista :: content");
        return "layout";
    }

    @PostMapping("/produtos/salvar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public String salvarProdutoWeb(@ModelAttribute Produto produto,
                                   RedirectAttributes redirectAttributes) {
        try {
            produtoService.salvar(produto);
            redirectAttributes.addFlashAttribute("sucesso", "Produto cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar produto: " + e.getMessage());
        }
        return "redirect:/produtos";
    }

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        model.addAttribute("content", "configuracoes :: content");
        return "layout";
    }
}
