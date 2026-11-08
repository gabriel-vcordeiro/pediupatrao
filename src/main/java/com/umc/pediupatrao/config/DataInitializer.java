package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.entity.enums.StatusPedido;
import com.umc.pediupatrao.repository.ClienteRepository;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepo,
                                   ProdutoRepository produtoRepo,
                                   ClienteRepository clienteRepo,
                                   PedidoRepository pedidoRepo,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Inicializa ou atualiza usuários padrão (ADMIN, GERENTE, ATENDENTE)
            usuarioRepo.findByUsername("admin").ifPresentOrElse(
                    user -> {
                        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
                            user.setRole("ADMIN");
                            usuarioRepo.save(user);
                        }
                    },
                    () -> {
                        Usuario admin = new Usuario("admin", passwordEncoder.encode("teste123"), "ADMIN");
                        usuarioRepo.save(admin);
                    }
            );

            if (usuarioRepo.findByUsername("gerente").isEmpty()) {
                Usuario gerente = new Usuario("gerente", passwordEncoder.encode("teste123"), "GERENTE");
                usuarioRepo.save(gerente);
            }

            if (usuarioRepo.findByUsername("atendente").isEmpty()) {
                Usuario atendente = new Usuario("atendente", passwordEncoder.encode("teste123"), "ATENDENTE");
                usuarioRepo.save(atendente);
            }

            // 2. Seeding do Catálogo de Produtos (garante produtos nas 4 categorias para o Dashboard)
            Map<String, Produto> catalogo = new HashMap<>();
            for (Produto existente : produtoRepo.findAll()) {
                if (existente.getNome() != null) {
                    catalogo.put(existente.getNome().trim().toLowerCase(), existente);
                }
            }

            Produto pCalabresa = garantirProduto(produtoRepo, catalogo, "Pizza Calabresa", "Pizza", 55.0);
            Produto pQuatroQueijos = garantirProduto(produtoRepo, catalogo, "Pizza Quatro Queijos", "Pizza", 62.0);
            Produto pFrangoCatupiry = garantirProduto(produtoRepo, catalogo, "Pizza Frango com Catupiry", "Pizza", 58.0);
            Produto pPortuguesa = garantirProduto(produtoRepo, catalogo, "Pizza Portuguesa", "Pizza", 60.0);
            Produto pEsfihaCarne = garantirProduto(produtoRepo, catalogo, "Esfiha de Carne", "Esfiha", 8.50);
            Produto pEsfihaQueijo = garantirProduto(produtoRepo, catalogo, "Esfiha de Queijo", "Esfiha", 9.00);
            Produto pXBacon = garantirProduto(produtoRepo, catalogo, "X-Bacon Artesanal", "Lanche", 34.0);
            Produto pXTudo = garantirProduto(produtoRepo, catalogo, "X-Tudo Patrão", "Lanche", 38.0);
            Produto pRefri2L = garantirProduto(produtoRepo, catalogo, "Refrigerante 2L", "Bebida", 14.0);
            Produto pSucoLaranja = garantirProduto(produtoRepo, catalogo, "Suco Natural 500ml", "Bebida", 12.0);

            // 3. Seeding de Clientes para demonstração
            Cliente c1 = garantirCliente(clienteRepo, "cli-seed-01", "Carlos Eduardo Silva", "(11) 98765-4321", "Av. Brasil", "150", "Centro", "Mogi das Cruzes");
            Cliente c2 = garantirCliente(clienteRepo, "cli-seed-02", "Ana Paula Ferreira", "(11) 97654-3210", "Rua das Flores", "85", "Jardim Universo", "Mogi das Cruzes");
            Cliente c3 = garantirCliente(clienteRepo, "cli-seed-03", "Marcos Vinicius Souza", "(11) 96543-2109", "Rua Coronel Souza Franco", "420", "Centro", "Mogi das Cruzes");
            Cliente c4 = garantirCliente(clienteRepo, "cli-seed-04", "Fernanda Lima Costa", "(11) 95432-1098", "Av. Voluntário Fernando Pinheiro Franco", "910", "Vila Oliveira", "Mogi das Cruzes");
            Cliente c5 = garantirCliente(clienteRepo, "cli-seed-05", "Roberto Almeida Santos", "(11) 94321-0987", "Rua Barão de Jaceguai", "312", "Centro", "Mogi das Cruzes");

            // 4. Seeding de Pedidos para o Dashboard Administrativo (sempre com datas relativas ao dia atual)
            LocalDateTime agora = LocalDateTime.now();

            // Há 6 dias
            salvarPedidoSeed(pedidoRepo, "seed-ped-01", c1, List.of(
                    criarItem(pCalabresa, 2),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusDays(6).withHour(19).withMinute(20), agora.minusDays(6).withHour(20).withMinute(5), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-02", c2, List.of(
                    criarItem(pXTudo, 2),
                    criarItem(pSucoLaranja, 2)
            ), StatusPedido.FINALIZADO, 5.0, agora.minusDays(6).withHour(21).withMinute(10), agora.minusDays(6).withHour(21).withMinute(50), null);

            // Há 5 dias
            salvarPedidoSeed(pedidoRepo, "seed-ped-03", c3, List.of(
                    criarItem(pQuatroQueijos, 1),
                    criarItem(pFrangoCatupiry, 1),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.FINALIZADO, 10.0, agora.minusDays(5).withHour(20).withMinute(0), agora.minusDays(5).withHour(20).withMinute(45), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-04", c4, List.of(
                    criarItem(pEsfihaCarne, 6),
                    criarItem(pEsfihaQueijo, 4),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusDays(5).withHour(21).withMinute(30), agora.minusDays(5).withHour(22).withMinute(10), null);

            // Há 4 dias
            salvarPedidoSeed(pedidoRepo, "seed-ped-05", c5, List.of(
                    criarItem(pPortuguesa, 2),
                    criarItem(pRefri2L, 2)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusDays(4).withHour(19).withMinute(45), agora.minusDays(4).withHour(20).withMinute(30), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-06", c1, List.of(
                    criarItem(pCalabresa, 1)
            ), StatusPedido.CANCELADO, 0.0, agora.minusDays(4).withHour(20).withMinute(15), null, "Cliente solicitou cancelamento por erro no pedido");

            // Há 3 dias
            salvarPedidoSeed(pedidoRepo, "seed-ped-07", c2, List.of(
                    criarItem(pCalabresa, 2),
                    criarItem(pQuatroQueijos, 1),
                    criarItem(pRefri2L, 2)
            ), StatusPedido.FINALIZADO, 15.0, agora.minusDays(3).withHour(20).withMinute(10), agora.minusDays(3).withHour(20).withMinute(55), null);

            // Há 2 dias
            salvarPedidoSeed(pedidoRepo, "seed-ped-08", c3, List.of(
                    criarItem(pXBacon, 3),
                    criarItem(pSucoLaranja, 3)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusDays(2).withHour(19).withMinute(30), agora.minusDays(2).withHour(20).withMinute(10), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-09", c4, List.of(
                    criarItem(pFrangoCatupiry, 2),
                    criarItem(pEsfihaCarne, 4)
            ), StatusPedido.FINALIZADO, 5.0, agora.minusDays(2).withHour(21).withMinute(0), agora.minusDays(2).withHour(21).withMinute(45), null);

            // Ontem (Há 1 dia)
            salvarPedidoSeed(pedidoRepo, "seed-ped-10", c5, List.of(
                    criarItem(pCalabresa, 1),
                    criarItem(pPortuguesa, 1),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusDays(1).withHour(19).withMinute(15), agora.minusDays(1).withHour(20).withMinute(0), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-11", c1, List.of(
                    criarItem(pQuatroQueijos, 2),
                    criarItem(pSucoLaranja, 2)
            ), StatusPedido.FINALIZADO, 10.0, agora.minusDays(1).withHour(20).withMinute(40), agora.minusDays(1).withHour(21).withMinute(25), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-12", c2, List.of(
                    criarItem(pXTudo, 1)
            ), StatusPedido.CANCELADO, 0.0, agora.minusDays(1).withHour(21).withMinute(10), null, "Endereço fora da área de cobertura da entrega");

            // Hoje (Pedidos em todos os estágios operacionais para demonstração ao vivo no Dashboard)
            salvarPedidoSeed(pedidoRepo, "seed-ped-13", c3, List.of(
                    criarItem(pCalabresa, 2),
                    criarItem(pEsfihaCarne, 5),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.FINALIZADO, 0.0, agora.minusHours(3), agora.minusHours(2).minusMinutes(15), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-14", c4, List.of(
                    criarItem(pFrangoCatupiry, 1),
                    criarItem(pPortuguesa, 1),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.SAIU_PARA_ENTREGA, 10.0, agora.minusHours(1).minusMinutes(20), agora.minusMinutes(35), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-15", c5, List.of(
                    criarItem(pXBacon, 2),
                    criarItem(pSucoLaranja, 2)
            ), StatusPedido.RETIRADO, 0.0, agora.minusHours(1), agora.minusMinutes(20), null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-16", c1, List.of(
                    criarItem(pQuatroQueijos, 1),
                    criarItem(pEsfihaQueijo, 4)
            ), StatusPedido.PRONTO, 0.0, agora.minusMinutes(45), null, null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-17", c2, List.of(
                    criarItem(pCalabresa, 1),
                    criarItem(pFrangoCatupiry, 1)
            ), StatusPedido.EM_PREPARACAO, 5.0, agora.minusMinutes(25), null, null);

            salvarPedidoSeed(pedidoRepo, "seed-ped-18", c3, List.of(
                    criarItem(pPortuguesa, 1),
                    criarItem(pRefri2L, 1)
            ), StatusPedido.RECEBIDO, 0.0, agora.minusMinutes(10), null, null);

            System.out.println("Seeding do Dashboard Administrativo sincronizado com sucesso (18 pedidos de demonstração)!");
        };
    }

    private Produto garantirProduto(ProdutoRepository produtoRepo,
                                    Map<String, Produto> catalogo,
                                    String nome,
                                    String tipo,
                                    Double preco) {
        String chave = nome.trim().toLowerCase();
        if (catalogo.containsKey(chave)) {
            Produto existente = catalogo.get(chave);
            if (existente.getTipo() == null || existente.getTipo().isBlank()) {
                existente.setTipo(tipo);
                existente = produtoRepo.save(existente);
            }
            return existente;
        }
        Produto novo = new Produto();
        novo.setNome(nome);
        novo.setTipo(tipo);
        novo.setPreco(preco);
        novo.setAtivo(true);
        novo = produtoRepo.save(novo);
        catalogo.put(chave, novo);
        return novo;
    }

    private Cliente garantirCliente(ClienteRepository clienteRepo,
                                    String id,
                                    String nome,
                                    String telefone,
                                    String logradouro,
                                    String numero,
                                    String bairro,
                                    String cidade) {
        return clienteRepo.findById(id).orElseGet(() -> {
            Cliente c = new Cliente();
            c.setId(id);
            c.setNome(nome);
            c.setTelefone(telefone);
            c.setCep("08780-000");
            c.setLogradouro(logradouro);
            c.setNumero(numero);
            c.setBairro(bairro);
            c.setCidade(cidade);
            c.setEstado("SP");
            return clienteRepo.save(c);
        });
    }

    private ItemPedido criarItem(Produto p, int quantidade) {
        return new ItemPedido(p.getId(), p.getNome(), quantidade, p.getPreco());
    }

    private void salvarPedidoSeed(PedidoRepository pedidoRepo,
                                  String idSeed,
                                  Cliente cliente,
                                  List<ItemPedido> itens,
                                  StatusPedido status,
                                  Double descontoPercentual,
                                  LocalDateTime entrada,
                                  LocalDateTime saida,
                                  String justificativa) {
        Pedido p = pedidoRepo.findById(idSeed).orElseGet(Pedido::new);
        p.setId(idSeed);
        p.setClienteId(cliente.getId());
        p.setNomeCliente(cliente.getNome());
        p.setItens(itens);
        p.setDescontoPercentual(descontoPercentual);
        p.recalcularValores();
        p.setStatusEnum(status);
        p.setResponsavelRegistro("atendente");
        p.setDataHoraEntrada(entrada);
        p.setDataHoraSaida(saida);
        p.setResponsavelSaida(saida != null ? "atendente" : null);
        p.setJustificativaCancelamento(justificativa);
        pedidoRepo.save(p);
    }
}
