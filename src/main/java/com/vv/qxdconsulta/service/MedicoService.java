package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.ConsultaRepository;
import com.vv.qxdconsulta.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MedicoService {

    @Autowired
    MedicoRepository medicoRepository;

    @Autowired
    ConsultaService consultaService;

    @Autowired
    PacienteService pacienteService;


    // criar
    public Medico adicionarMedico(Medico medico){
        //verificação se o CPF já está cadastrado
        if (medicoRepository.findByCpf(medico.getCpf()).isPresent()){
            throw new IllegalArgumentException("CPF já cadastrado: " + medico.getCpf());
        }

        //verificação se o email já está cadastrado
        if (medicoRepository.findByCrm(medico.getCrm()).isPresent()){
            throw new IllegalArgumentException("CRM já cadastrado: " + medico.getCrm());
        }

        return medicoRepository.save(medico);
    }

    // busca os horários disponíveis do medico
    public HorarioDisponivel buscarHorarioDisponivel(Medico medico, LocalDateTime dataHora){
        // buscar o horário específico dentro dos horários disponíveis do médico
        System.out.println("Dentro do metodo buscarHorarioDisponivel....");
        for( HorarioDisponivel horarioDisponivel : medico.getHorarioDisponivel()) {
            if (horarioDisponivel.getHorario().toLocalDate().equals(dataHora.toLocalDate())) {
                return horarioDisponivel;
            }
        }

        // lança exceção caso nenhum horário seja correspondente for encontrado
        throw new IllegalArgumentException("Horário não disponível para a data e hora: " + dataHora);
    }

    // update
    public Medico alterarMedico(UUID idMedico, Medico medico){
        Medico medicoExistente = medicoRepository.findById(idMedico).orElseThrow(() -> new IllegalArgumentException("Médico não encontrado"));

        medicoExistente.setNome(medico.getNome());
        medicoExistente.setCpf(medico.getCpf());
        medicoExistente.setCrm(medico.getCrm());
        medicoExistente.setEspecialização(medico.getEspecialização());
        medicoExistente.setHorarioDisponivel(medico.getHorarioDisponivel());

        return medicoRepository.save(medicoExistente);
    }

    // findAll
    public List<Medico> buscarTodosMedicos(){
        return medicoRepository.findAll();
    }

    // buscar por crm
    public Medico buscarMedicoPorCrm(String crm) {
        return medicoRepository.findByCpf(crm)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com o CRM: " + crm));
    }

    // buscar por cpf
    public Medico buscarMedicoPorCpf(String cpf) {
        return medicoRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado com o CPF: " + cpf));
    }

//    buscar por id
    public Medico buscarMedicoPorId(UUID idMedico) {
    return medicoRepository.findById(idMedico)
            .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado"));
}

    public void removerMedico(UUID idMedico) {
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado"));

        // Para cada horário disponível do médico, remover todas as consultas agendadas
        for (HorarioDisponivel horarioDisponivel : medico.getHorarioDisponivel()) {
            List<Consulta> consultasRemover = new ArrayList<>(horarioDisponivel.getConsultasAgendadas());

            for (Consulta consulta : consultasRemover) {
                consultaService.removerConsulta(consulta.getId());
            }
        }

        medicoRepository.delete(medico);
    }

    // buscar uma lista de horarios disponíveis do medico
    // mover isso para HorarioDisponivelService
    //posso remover daqui, já fiz em horarioDispoivelService (listarHorariosDisponiveisPorMedico)
    public List<HorarioDisponivel> buscarHorariosDisponiveis(UUID idMedico){
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico não encontrado"));

        return medico.getHorarioDisponivel();
    }

    // mover isso para HorarioDisponivelService
    //já fiz lá, pode retirar daqui
    public List<HorarioDisponivel> buscarHorariosDisponiveisPorData(Medico medico, LocalDate data) {
        List<HorarioDisponivel> horarios = new ArrayList<>();
        for (HorarioDisponivel horario : medico.getHorarioDisponivel()) {
            if (horario.getHorario().toLocalDate().equals(data)) {
                horarios.add(horario);
            }
        }
        return horarios;
    }

}
