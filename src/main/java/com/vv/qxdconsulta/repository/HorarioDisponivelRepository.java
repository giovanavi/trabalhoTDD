package com.vv.qxdconsulta.repository;

import com.vv.qxdconsulta.model.HorarioDisponivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HorarioDisponivelRepository extends JpaRepository<HorarioDisponivel, UUID> {
}
