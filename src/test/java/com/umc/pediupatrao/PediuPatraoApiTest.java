package com.umc.pediupatrao;

import com.umc.pediupatrao.controller.AuditoriaController;
import com.umc.pediupatrao.controller.ClienteController;
import com.umc.pediupatrao.controller.PedidoController;
import com.umc.pediupatrao.controller.ProdutoController;
import com.umc.pediupatrao.controller.UsuarioController;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import com.umc.pediupatrao.service.UsuarioService;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PediuPatraoApiTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AuditoriaRepository auditoriaRepository;

    private AuditoriaService auditoriaService;
    private PedidoService pedidoService;
    private ClienteService clienteService;
    private ProdutoService produtoService;
    private UsuarioService usuarioService;
    private PasswordEncoder passwordEncoder;

    private PedidoController pedidoController;
    private ClienteController clienteController;
    private ProdutoController produtoController;
    private UsuarioController usuarioController;
    private AuditoriaController auditoriaController;

    @BeforeEach
    void setUp() throws Exception {
        passwordEncoder = new BCryptPasswordEncoder();
        auditoriaService = new AuditoriaService(auditoriaRepository);

        pedidoService = new PedidoService();
        injetarCampo(pedidoService, "pedidoRepository", pedidoRepository);
        injetarCampo(pedidoService, "produtoRepository", produtoRepository);
        injetarCampo(pedidoService, "auditoriaService", auditoriaService);

        clienteService = new ClienteService();
        injetarCampo(clienteService, "clienteRepository", clienteRepository);
        injetarCampo(clienteService, "auditoriaService", auditoriaService);

        produtoService = new ProdutoService();
        injetarCampo(produtoService, "produtoRepository", produtoRepository);

        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, auditoriaService);

        pedidoController = new PedidoController();
        injetarCampo(pedidoController, "pedidoService", pedidoService);

        clienteController = new ClienteController();
        injetarCampo(clienteController, "clienteService", clienteService);

        produtoController = new ProdutoController();
        injetarCampo(produtoController, "produtoService", produtoService);

        usuarioController = new UsuarioController(usuarioService);
        auditoriaController = new AuditoriaController(auditoriaService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComo(String username, String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void injetarCampo(Object alvo, String nomeCampo, Object valor) throws Exception {
        Field f = alvo.getClass().getDeclaredField(nomeCampo);
        f.setAccessible(true);
        f.set(alvo, valor);
    }

    // =========================================================================
    // TESTES DE PEDIDOS (/api/pedidos)
    // =========================================================================

    @Test
    @DisplayName("API Pedidos - Deve criar pedido calculando subtotal e total no servidor e registrar auditoria")
    void deveCriarPedidoCalculandoValoresNoServidor() {
        autenticarComo("atendente", "ATENDENTE");

        Produto pizza = new Produto();
        pizza.setId("prod-1");
        pizza.setNome("Pizza Calabresa");
        pizza.setTipo("Pizza");
        pizza.setPreco(55.0);

        when(produtoRepository.findById("prod-1")).thenReturn(Optional.of(pizza));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setId("ped-100");
            return p;
        });

        Pedido novo = new Pedido();
        novo.setClienteId("cli-1");
        novo.setNomeCliente("João Silva");
        novo.setItens(List.of(new ItemPedido("prod-1", "Qualquer", 2, 1.0))); // tenta manipular preço para R$ 1,00
        novo.setDescontoPercentual(15.0); // ATENDENTE não pode aplicar desconto -> deve ser zerado para 0%

        ResponseEntity<?> resp = pedidoController.criarPedido(novo);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());

        Pedido criado = (Pedido) resp.getBody();
        assertNotNull(criado);
        assertEquals("ped-100", criado.getId());
        assertEquals("RECEBIDO", criado.getStatus());
        assertEquals("atendente", criado.getResponsavelRegistro());
        assertEquals(110.0, criado.getValorSubtotal()); // Preço real do catálogo: 2 x 55.0
        assertEquals(0.0, criado.getDescontoPercentual()); // Desconto negado para ATENDENTE
        assertEquals(110.0, criado.getValorTotal());
        verify(auditoriaRepository, times(1)).save(any(Auditoria.class));
    }

    @Test
    @DisplayName("API Pedidos - Deve avançar status no ciclo de vida e preencher responsável/horário na saída")
    void deveAvancarStatusDoPedidoNoFluxoValido() {
        autenticarComo("atendente", "ATENDENTE");

        Pedido p = new Pedido();
        p.setId("ped-101");
        p.setStatusEnum(StatusPedido.PRONTO);
        p.setValorSubtotal(55.0);
        p.setValorTotal(55.0);

        when(pedidoRepository.findById("ped-101")).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> resp = pedidoController.atualizarStatus("ped-101", "SAIU_PARA_ENTREGA", null);
        assertEquals(HttpStatus.OK, resp.getStatusCode());

        Pedido atualizado = (Pedido) resp.getBody();
        assertNotNull(atualizado);
        assertEquals("SAIU_PARA_ENTREGA", atualizado.getStatus());
        assertNotNull(atualizado.getDataHoraSaida());
        assertEquals("atendente", atualizado.getResponsavelSaida());
    }

    @Test
    @DisplayName("API Pedidos - Deve bloquear transição inválida de status com HTTP 400")
    void deveBloquearTransicaoInvalidaDeStatus() {
        autenticarComo("atendente", "ATENDENTE");

        Pedido p = new Pedido();
        p.setId("ped-102");
        p.setStatusEnum(StatusPedido.RECEBIDO);

        when(pedidoRepository.findById("ped-102")).thenReturn(Optional.of(p));

        ResponseEntity<?> resp = pedidoController.atualizarStatus("ped-102", "FINALIZADO", null);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    @DisplayName("API Pedidos - Gerente deve aplicar desconto de até 20% e bloquear acima de 20%")
    void deveAplicarDescontoGerencialComLimiteDe20PorCento() {
        autenticarComo("gerente", "GERENTE");

        Pedido p = new Pedido();
        p.setId("ped-103");
        p.setStatusEnum(StatusPedido.EM_PREPARACAO);
        p.setItens(List.of(new ItemPedido("prod-1", "Pizza Calabresa", 2, 50.0)));
        p.recalcularValores();

        when(pedidoRepository.findById("ped-103")).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> respOk = pedidoController.aplicarDesconto("ped-103", 15.0, null);
        assertEquals(HttpStatus.OK, respOk.getStatusCode());
        Pedido comDesconto = (Pedido) respOk.getBody();
        assertNotNull(comDesconto);
        assertEquals(15.0, comDesconto.getDescontoPercentual());
        assertEquals(15.0, comDesconto.getValorDesconto());
        assertEquals(85.0, comDesconto.getValorTotal());

        // Tentativa acima de 20% deve retornar 400 Bad Request
        ResponseEntity<?> respErro = pedidoController.aplicarDesconto("ped-103", 25.0, null);
        assertEquals(HttpStatus.BAD_REQUEST, respErro.getStatusCode());
    }

    @Test
    @DisplayName("API Pedidos - Gerente deve cancelar pedido com justificativa obrigatória")
    void deveCancelarPedidoComJustificativaObrigatoria() {
        autenticarComo("gerente", "GERENTE");

        Pedido p = new Pedido();
        p.setId("ped-104");
        p.setStatusEnum(StatusPedido.RECEBIDO);

        when(pedidoRepository.findById("ped-104")).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        // Sem justificativa -> 400 Bad Request
        ResponseEntity<?> respSemJust = pedidoController.cancelarPedido("ped-104", "   ", null);
        assertEquals(HttpStatus.BAD_REQUEST, respSemJust.getStatusCode());

        // Com justificativa -> 200 OK
        ResponseEntity<?> respComJust = pedidoController.cancelarPedido("ped-104", null, Map.of("justificativa", "Cliente pediu cancelamento"));
        assertEquals(HttpStatus.OK, respComJust.getStatusCode());
        Pedido cancelado = (Pedido) respComJust.getBody();
        assertNotNull(cancelado);
        assertEquals("CANCELADO", cancelado.getStatus());
        assertEquals("Cliente pediu cancelamento", cancelado.getJustificativaCancelamento());
    }

    // =========================================================================
    // TESTES DE CLIENTES (/api/clientes)
    // =========================================================================

    @Test
    @DisplayName("API Clientes - Deve cadastrar e atualizar cliente auditando alterações de campos")
    void deveCadastrarEAtualizarClienteComAuditoria() {
        autenticarComo("atendente", "ATENDENTE");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            if (c.getId() == null) c.setId("cli-50");
            return c;
        });

        Cliente novo = new Cliente();
        novo.setNome("Maria Oliveira");
        novo.setTelefone("(11) 99999-1111");
        novo.setLogradouro("Rua das Flores");
        novo.setNumero("10");

        ResponseEntity<?> respCriacao = clienteController.salvarCliente(novo);
        assertEquals(HttpStatus.CREATED, respCriacao.getStatusCode());

        Cliente existente = new Cliente();
        existente.setId("cli-50");
        existente.setNome("Maria Oliveira");
        existente.setTelefone("(11) 99999-1111");
        existente.setLogradouro("Rua das Flores");
        existente.setNumero("10");

        when(clienteRepository.findById("cli-50")).thenReturn(Optional.of(existente));

        Cliente editado = new Cliente();
        editado.setNome("Maria Oliveira");
        editado.setTelefone("(11) 98888-2222");
        editado.setLogradouro("Av. Paulista");
        editado.setNumero("100");

        ResponseEntity<?> respAtualizacao = clienteController.atualizarCliente("cli-50", editado);
        assertEquals(HttpStatus.OK, respAtualizacao.getStatusCode());
        verify(auditoriaRepository, atLeast(3)).save(any(Auditoria.class));
    }

    // =========================================================================
    // TESTES DE PRODUTOS (/api/produtos)
    // =========================================================================

    @Test
    @DisplayName("API Produtos - Deve cadastrar, listar, atualizar e excluir produtos")
    void deveGerenciarProdutosViaApi() {
        autenticarComo("gerente", "GERENTE");

        Produto prod = new Produto();
        prod.setNome("Pizza Portuguesa");
        prod.setTipo("Pizza");
        prod.setPreco(60.0);

        when(produtoRepository.save(any(Produto.class))).thenAnswer(inv -> {
            Produto p = inv.getArgument(0);
            if (p.getId() == null) p.setId("prod-99");
            return p;
        });
        when(produtoRepository.existsById("prod-99")).thenReturn(true);

        ResponseEntity<Produto> salvoResp = produtoController.salvarProdutoJson(prod);
        assertEquals(HttpStatus.CREATED, salvoResp.getStatusCode());
        assertNotNull(salvoResp.getBody());
        assertTrue(salvoResp.getBody().getAtivo());

        prod.setPreco(64.0);
        ResponseEntity<Produto> atualizadoResp = produtoController.atualizarProduto("prod-99", prod);
        assertEquals(HttpStatus.OK, atualizadoResp.getStatusCode());
        assertEquals(64.0, atualizadoResp.getBody().getPreco());

        ResponseEntity<Void> deleteResp = produtoController.excluirProduto("prod-99");
        assertEquals(HttpStatus.NO_CONTENT, deleteResp.getStatusCode());
    }

    // =========================================================================
    // TESTES DE USUÁRIOS (/api/usuarios) E AUDITORIA (/api/auditoria)
    // =========================================================================

    @Test
    @DisplayName("API Usuários - Deve criar usuário com senha criptografada, sanitizar retorno e rejeitar perfil inválido")
    void deveCriarUsuarioSanitizandoSenhaERejeitarPerfilInvalido() {
        autenticarComo("admin", "ADMIN");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId("usr-10");
            return u;
        });

        Usuario novo = new Usuario("novo_gerente", "senhaForte123", "GERENTE");
        ResponseEntity<?> respOk = usuarioController.criarUsuario(novo);
        assertEquals(HttpStatus.CREATED, respOk.getStatusCode());

        Usuario body = (Usuario) respOk.getBody();
        assertNotNull(body);
        assertEquals("[PROTEGIDO]", body.getPassword());
        assertEquals("GERENTE", body.getRole());

        Usuario invalido = new Usuario("hacker", "123456", "SUPER_ROOT");
        ResponseEntity<?> respInvalido = usuarioController.criarUsuario(invalido);
        assertEquals(HttpStatus.BAD_REQUEST, respInvalido.getStatusCode());
    }

    @Test
    @DisplayName("API Auditoria - Deve consultar logs de auditoria por entidade e por usuário")
    void deveConsultarAuditoriaPorEntidadeEUsuario() {
        autenticarComo("admin", "ADMIN");

        Auditoria a1 = new Auditoria("gerente", "GERENTE", "CANCELAMENTO DE PEDIDO", "PEDIDO", "ped-03");
        when(auditoriaRepository.findByEntidadeOrderByDataHoraDesc("PEDIDO")).thenReturn(List.of(a1));
        when(auditoriaRepository.findByUsuarioOrderByDataHoraDesc("gerente")).thenReturn(List.of(a1));

        ResponseEntity<List<Auditoria>> respEntidade = auditoriaController.buscarPorEntidade("pedido");
        assertEquals(HttpStatus.OK, respEntidade.getStatusCode());
        assertEquals(1, respEntidade.getBody().size());

        ResponseEntity<List<Auditoria>> respUsuario = auditoriaController.buscarPorUsuario("gerente");
        assertEquals(HttpStatus.OK, respUsuario.getStatusCode());
        assertEquals(1, respUsuario.getBody().size());
    }
}
