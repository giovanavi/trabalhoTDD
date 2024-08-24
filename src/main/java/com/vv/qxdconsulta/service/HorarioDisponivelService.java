package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.repository.HorarioDisponivelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class HorarioDisponivelService {

    @Autowired
    MedicoService medicoService;

    @Autowired
    HorarioDisponivelRepository horarioDisponivelRepository;

    @Autowired
    ConsultaService consultaService;


    public void salvarHorarioDaConsulta(HorarioDisponivel horario, Consulta consulta) {
        if (horario.podeAgendar()) {
            horario.getConsultasAgendadas().add(consulta);
        } else {
            throw new IllegalArgumentException("Limite de consultas para este horário já atingido.");
        }
    }

    public HorarioDisponivel adicionarHorarioDisponivel(HorarioDisponivel horario, Medico medico) {
        medico.getHorarioDisponivel().add(horario);
        medicoService.alterarMedico(medico.getId(), medico);
        return horario;
    }

    public HorarioDisponivel atualizarHorarioDisponivel(UUID idHorario, LocalDateTime novoHorario, int novaCapacidade){
        HorarioDisponivel horario = horarioDisponivelRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horário não encontrado"));

        // verificar se o horário possui consultas
        if (!horario.getConsultasAgendadas().isEmpty()){
            throw new IllegalArgumentException("Não é possível alterar um horário que possui consultas agendadas");
        }

        //atualizar detalhes do horario
        horario.setHorario(novoHorario);
        horario.setCapacidadeMaxima(novaCapacidade);

        return horarioDisponivelRepository.save(horario);
    }

    public void removerHorarioDisponivel(UUID idHorario, Medico medico) {
        HorarioDisponivel horarioParaRemover = null;

        //encontra o horario a ser removido
        for (HorarioDisponivel horario: medico.getHorarioDisponivel()){
            if (horario.getId().equals(idHorario)){
                horarioParaRemover = horario;
                break;
            }
        }

        //verifica se o horario foi encontrado
        if (horarioParaRemover == null){
            throw new IllegalArgumentException("Horario não encontrado");
        }

        //verifica se o horario possui consultas agendada
        if (!horarioParaRemover.getConsultasAgendadas().isEmpty()){
            //aqui vamos remover ou cancelar as consultas associadas antes de excluir o horario em si
            for (Consulta consulta: horarioParaRemover.getConsultasAgendadas()){
                consultaService.removerConsulta(consulta.getId());
            }
        }

        // remove o horário da lista do médico
        medico.getHorarioDisponivel().remove(horarioParaRemover);

        // salva as alterações no médico
        medicoService.alterarMedico(medico.getId(), medico);

    }

}
