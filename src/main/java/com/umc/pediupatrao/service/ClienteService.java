package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    // Permitido a ATENDENTE e GERENTE cadastrar clientes
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente);
    }

    // Consulta permitida a todos os perfis autenticados
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // Exclusão restrita
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public void excluir(@NonNull String id) {
        clienteRepository.findById(id).ifPresent(c -> {
            clienteRepository.deleteById(id);
            auditoriaService.registrar("EXCLUSÃO DE CLIENTE", "CLIENTE", id,
                    "nome", c.getNome(), "REMOVIDO", "Exclusão do cliente " + c.getNome());
        });
    }

    // Consulta de cliente por ID
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE', 'ATENDENTE')")
    public Optional<Cliente> buscarPorId(@NonNull String id) {
        return clienteRepository.findById(id);
    }

    // Salvar novo cliente ou atualizar cliente existente com auditoria detalhada de alterações cadastrais
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE')")
    public Cliente salvar(Cliente cliente) {
        // Novo cliente
        if (cliente.getId() == null || cliente.getId().isBlank()) {
            cliente.setId(null);
            Cliente salvo = clienteRepository.save(cliente);

            auditoriaService.registrar("CADASTRO DE CLIENTE", "CLIENTE", salvo.getId(),
                    "nome", "—", salvo.getNome(), "Cadastro inicial do cliente: " + salvo.getNome());

            return salvo;
        }

        // Atualização de cliente existente
        Cliente anterior = clienteRepository.findById(cliente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para atualização."));

        List<AlteracaoCampo> alteracoes = new ArrayList<>();
        verificarAlteracao(alteracoes, "nome", anterior.getNome(), cliente.getNome());
        verificarAlteracao(alteracoes, "telefone", anterior.getTelefone(), cliente.getTelefone());
        verificarAlteracao(alteracoes, "cep", anterior.getCep(), cliente.getCep());
        verificarAlteracao(alteracoes, "logradouro", anterior.getLogradouro(), cliente.getLogradouro());
        verificarAlteracao(alteracoes, "numero", anterior.getNumero(), cliente.getNumero());
        verificarAlteracao(alteracoes, "complemento", anterior.getComplemento(), cliente.getComplemento());
        verificarAlteracao(alteracoes, "bairro", anterior.getBairro(), cliente.getBairro());
        verificarAlteracao(alteracoes, "cidade", anterior.getCidade(), cliente.getCidade());
        verificarAlteracao(alteracoes, "estado", anterior.getEstado(), cliente.getEstado());
        verificarAlteracao(alteracoes, "endereco", anterior.getEndereco(), cliente.getEndereco());

        Cliente atualizado = clienteRepository.save(cliente);

        if (!alteracoes.isEmpty()) {
            for (AlteracaoCampo alt : alteracoes) {
                // Registra cada campo alterado para bater exatamente com o exemplo de auditoria da especificação
                auditoriaService.registrar("ALTERAÇÃO DE CLIENTE", "CLIENTE", atualizado.getId(),
                        alt.getCampo(), alt.getValorAnterior(), alt.getNovoValor(),
                        "Atualização do campo " + alt.getCampo() + " do cliente " + atualizado.getNome());
            }
        }

        return atualizado;
    }

    private void verificarAlteracao(List<AlteracaoCampo> lista, String nomeCampo, String vAntigo, String vNovo) {
        String antigoLimpo = vAntigo != null ? vAntigo.trim() : "";
        String novoLimpo = vNovo != null ? vNovo.trim() : "";

        if (!Objects.equals(antigoLimpo, novoLimpo)) {
            lista.add(new AlteracaoCampo(nomeCampo,
                    antigoLimpo.isEmpty() ? "—" : antigoLimpo,
                    novoLimpo.isEmpty() ? "—" : novoLimpo));
        }
    }
}
