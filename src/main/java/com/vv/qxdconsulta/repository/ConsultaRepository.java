package com.vv.qxdconsulta.repository;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    List<Consulta> findByMedico(Medico medico);
    List<Consulta> findByPaciente(Paciente paciente);
    List<Consulta> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

}
