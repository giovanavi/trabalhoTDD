package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import org.springframework.stereotype.Service;

@Service
public class HorarioDisponivelService {

    public void salvarHorarioDaConsulta(HorarioDisponivel horario, Consulta consulta) {
        if (horario.podeAgendar()) {
            horario.getConsultasAgendadas().add(consulta);
        } else {
            throw new IllegalArgumentException("Limite de consultas para este horário já atingido.");
        }
    }

}
