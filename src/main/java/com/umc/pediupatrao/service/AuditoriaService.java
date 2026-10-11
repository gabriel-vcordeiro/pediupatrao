package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /**
     * Registra um evento de auditoria com um único campo alterado ou resumo.
     */
    public Auditoria registrar(String operacao, String entidade, String entidadeId,
                               String campoAlterado, String valorAnterior, String novoValor,
                               String justificativa) {
        String usuario = obterUsuarioAutenticado();
        String perfil = obterPerfilAutenticado();

        Auditoria auditoria = new Auditoria(usuario, perfil, operacao, entidade, entidadeId);
        auditoria.setCampoAlterado(campoAlterado);
        auditoria.setValorAnterior(valorAnterior);
        auditoria.setNovoValor(novoValor);
        auditoria.setJustificativa(justificativa);
        auditoria.setDataHora(LocalDateTime.now());

        if (campoAlterado != null) {
            List<AlteracaoCampo> alteracoes = new ArrayList<>();
            alteracoes.add(new AlteracaoCampo(campoAlterado, valorAnterior, novoValor));
            auditoria.setAlteracoes(alteracoes);
        }

        log.info("Auditoria registrada: [{}] {} em {} ({}) por {} [{}]",
                operacao, campoAlterado, entidade, entidadeId, usuario, perfil);

        return auditoriaRepository.save(auditoria);
    }

    /**
     * Registra um evento de auditoria com múltiplos campos alterados.
     */
    public Auditoria registrarComAlteracoes(String operacao, String entidade, String entidadeId,
                                            List<AlteracaoCampo> alteracoes, String justificativa) {
        String usuario = obterUsuarioAutenticado();
        String perfil = obterPerfilAutenticado();

        Auditoria auditoria = new Auditoria(usuario, perfil, operacao, entidade, entidadeId);
        auditoria.setAlteracoes(alteracoes != null ? alteracoes : new ArrayList<>());
        auditoria.setJustificativa(justificativa);
        auditoria.setDataHora(LocalDateTime.now());

        if (alteracoes != null && !alteracoes.isEmpty()) {
            if (alteracoes.size() == 1) {
                auditoria.setCampoAlterado(alteracoes.get(0).getCampo());
                auditoria.setValorAnterior(alteracoes.get(0).getValorAnterior());
                auditoria.setNovoValor(alteracoes.get(0).getNovoValor());
            } else {
                auditoria.setCampoAlterado(alteracoes.size() + " campos alterados");
                auditoria.setValorAnterior("Diversos");
                auditoria.setNovoValor("Atualizados");
            }
        }

        log.info("Auditoria com {} campos registrada: [{}] em {} ({}) por {} [{}]",
                alteracoes != null ? alteracoes.size() : 0, operacao, entidade, entidadeId, usuario, perfil);

        return auditoriaRepository.save(auditoria);
    }

    /**
     * Consulta aos registros de auditoria permitida EXCLUSIVAMENTE para ADMIN e GERENTE.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> listarTodas() {
        return auditoriaRepository.findAllByOrderByDataHoraDesc();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> buscarPorEntidade(String entidade) {
        return auditoriaRepository.findByEntidadeOrderByDataHoraDesc(entidade);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> buscarPorEntidadeId(String entidadeId) {
        return auditoriaRepository.findByEntidadeIdOrderByDataHoraDesc(entidadeId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public List<Auditoria> buscarPorUsuario(String usuario) {
        return auditoriaRepository.findByUsuarioOrderByDataHoraDesc(usuario);
    }

    // --- Métodos utilitários de identificação do usuário corrente ---

    public String obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "SISTEMA";
    }

    public String obterPerfilAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String role = ga.getAuthority();
                if (role.startsWith("ROLE_")) {
                    return role.substring(5);
                }
                return role;
            }
        }
        return "SISTEMA";
    }
}
