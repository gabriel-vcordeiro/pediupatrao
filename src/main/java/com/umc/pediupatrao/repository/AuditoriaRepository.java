package com.umc.pediupatrao.repository;

import com.umc.pediupatrao.entity.Auditoria;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends MongoRepository<Auditoria, String> {

    List<Auditoria> findAllByOrderByDataHoraDesc();

    List<Auditoria> findByEntidadeOrderByDataHoraDesc(String entidade);

    List<Auditoria> findByEntidadeIdOrderByDataHoraDesc(String entidadeId);

    List<Auditoria> findByUsuarioOrderByDataHoraDesc(String usuario);
}
