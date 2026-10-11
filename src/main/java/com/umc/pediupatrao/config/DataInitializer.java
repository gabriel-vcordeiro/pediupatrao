package com.umc.pediupatrao.config;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.ProdutoRepository;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepo,
                                  ProdutoRepository produtoRepo,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            // Inicializa ou atualiza usuário ADMIN
            usuarioRepo.findByUsername("admin").ifPresentOrElse(
                    user -> {
                        if (user.getRole() == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
                            user.setRole("ADMIN");
                            usuarioRepo.save(user);
                            System.out.println("Usuário admin atualizado para perfil ADMIN!");
                        }
                    },
                    () -> {
                        Usuario admin = new Usuario();
                        admin.setUsername("admin");
                        admin.setPassword(passwordEncoder.encode("teste123"));
                        admin.setRole("ADMIN");
                        usuarioRepo.save(admin);
                        System.out.println("Usuário padrão criado: admin (ADMIN)");
                    }
            );

            // Inicializa usuário GERENTE
            if (usuarioRepo.findByUsername("gerente").isEmpty()) {
                Usuario gerente = new Usuario();
                gerente.setUsername("gerente");
                gerente.setPassword(passwordEncoder.encode("teste123"));
                gerente.setRole("GERENTE");
                usuarioRepo.save(gerente);
                System.out.println("Usuário padrão criado: gerente (GERENTE)");
            }

            // Inicializa usuário ATENDENTE
            if (usuarioRepo.findByUsername("atendente").isEmpty()) {
                Usuario atendente = new Usuario();
                atendente.setUsername("atendente");
                atendente.setPassword(passwordEncoder.encode("teste123"));
                atendente.setRole("ATENDENTE");
                usuarioRepo.save(atendente);
                System.out.println("Usuário padrão criado: atendente (ATENDENTE)");
            }

            // Se não houver produtos, cria produtos de exemplo para testar os pedidos
            if (produtoRepo.count() == 0) {
                Produto p1 = new Produto();
                p1.setNome("Pizza Calabresa");
                p1.setTipo("Pizza");
                p1.setPreco(55.0);
                p1.setAtivo(true);
                produtoRepo.save(p1);

                Produto p2 = new Produto();
                p2.setNome("Pizza Quatro Queijos");
                p2.setTipo("Pizza");
                p2.setPreco(62.0);
                p2.setAtivo(true);
                produtoRepo.save(p2);

                Produto p3 = new Produto();
                p3.setNome("Pizza Frango com Catupiry");
                p3.setTipo("Pizza");
                p3.setPreco(58.0);
                p3.setAtivo(true);
                produtoRepo.save(p3);

                Produto p4 = new Produto();
                p4.setNome("Refrigerante 2L");
                p4.setTipo("Bebida");
                p4.setPreco(14.0);
                p4.setAtivo(true);
                produtoRepo.save(p4);

                System.out.println("Produtos de teste inicializados!");
            }
        };
    }
}