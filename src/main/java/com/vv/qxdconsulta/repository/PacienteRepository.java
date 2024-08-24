package com.vv.qxdconsulta.repository;

import com.vv.qxdconsulta.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByCpf(String cpf);
    Optional<Paciente> findByEmail(String email);
}
