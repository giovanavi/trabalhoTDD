package com.vv.qxdconsulta.repository;

import com.vv.qxdconsulta.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    Optional<Medico> findByCrm(String crm);
    Optional<Medico> findByCpf(String cpf);


}
