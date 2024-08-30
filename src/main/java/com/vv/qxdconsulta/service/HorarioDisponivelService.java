package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.repository.HorarioDisponivelRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class HorarioDisponivelService {

    @Autowired
    MedicoService medicoService;

    @Autowired
    HorarioDisponivelRepository horarioDisponivelRepository;

    @Autowired
    ConsultaService consultaService;

    //provavelmente será apagado
    public void salvarHorarioDaConsulta(HorarioDisponivel horario, Consulta consulta) {
        System.out.println("Entrando em salvarHorarioDaConsulta...");
        if (!horario.podeAgendar()) {
            System.out.println("Capacidade excedida, lançando exceção...");
            throw new IllegalArgumentException("Limite de consultas para este horário já atingido.");
        }
        System.out.println("Capacidade disponível, salvando consulta...");
        horario.getConsultasAgendadas().add(consulta);
        horarioDisponivelRepository.save(horario);
        System.out.println("Consulta salva com sucesso.");
    }

    public HorarioDisponivel adicionarHorarioDisponivel(HorarioDisponivel horario, String crmMedico) {
        Medico medico = medicoService.buscarMedicoPorCrm(crmMedico);
        horario.setMedico(medico);
        //aqui ele já está associando o horario ao medico e salvando essa informação
        return horarioDisponivelRepository.save(horario);
    }

    public void salvarMudancaDeHorario(HorarioDisponivel horarioDisponivel){
        horarioDisponivelRepository.save(horarioDisponivel);
    }

    public List<HorarioDisponivel> listarHorariosDisponiveisPorMedico(String crmMedico){
        Medico medico = medicoService.buscarMedicoPorCrm(crmMedico);
        if (medico.getHorarioDisponivel().isEmpty()){
            throw new IllegalArgumentException("Esse medico não tem horários disponíveis");
        }
        return medico.getHorarioDisponivel();
    }

    //metodo auxiliar para verificar o horario para o medico, usado no metodo de alterar Horario da Consulta.
    public HorarioDisponivel buscarHorarioPorMedico(String crmMedico, LocalDateTime novoHorario){
        Medico medico = medicoService.buscarMedicoPorCrm(crmMedico);
        for (HorarioDisponivel horario: medico.getHorarioDisponivel()){
            if (horario.getHorario().equals(novoHorario)){
                return horario;
            }
        }

        throw new IllegalArgumentException("O médico não tem esse horário disponível");
    }

    public void verificarDisponibilidadeDeConsulta(HorarioDisponivel horarioDisponivel){
        if (!horarioDisponivel.podeAgendar()){
            throw new IllegalArgumentException("Limite de consultas para este horário já atingido.");
        }
    }

    //estou convertendo a data passada por parametro em LocalDateTime do começo ao fim daquela data.
    public List<HorarioDisponivel> buscarHorariosDisponiveisPorData(LocalDate data){
        LocalDateTime dataComeco = data.atStartOfDay();
        LocalDateTime dataFinal = data.atTime(LocalTime.MAX);
        return horarioDisponivelRepository.findByHorarioBetween(dataComeco, dataFinal);
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

    public void removerHorarioDisponivel(UUID idHorario) {
        HorarioDisponivel horarioDisponivel = horarioDisponivelRepository.findById(idHorario)
                .orElseThrow( () -> new IllegalArgumentException("Horário não encontrado"));

        if (!horarioDisponivel.getConsultasAgendadas().isEmpty()){
            throw new IllegalArgumentException("Não é possível excluir o horário. Existem consultas agendadas");
        }

        horarioDisponivelRepository.delete(horarioDisponivel);
    }

}
