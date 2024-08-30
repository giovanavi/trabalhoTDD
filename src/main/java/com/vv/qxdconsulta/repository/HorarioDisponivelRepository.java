package com.vv.qxdconsulta.repository;

import com.vv.qxdconsulta.model.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, UUID> {

    List<HorarioDisponivel> findByHorarioBetween(LocalDateTime dataComeco, LocalDateTime dataFinal);
}
