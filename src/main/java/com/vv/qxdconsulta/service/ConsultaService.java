package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.ConsultaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultaService {

    @Autowired
    MedicoService medicoService;
    @Autowired
    PacienteService pacienteService;
    @Autowired
    HorarioDisponivelService horarioDisponivelService;
    @Autowired
    ConsultaRepository consultaRepository;

    // agendarConsula ALTERAR
    public Consulta agendarConsulta(String crmMedico, String cpfPaciente, LocalDateTime dataHora, String tipoConsulta){

        Medico medico = medicoService.buscarMedicoPorCrm(crmMedico);
        Paciente paciente = pacienteService.buscarPacientePorCpf(cpfPaciente);

        // metodo para ver se tem horario disponível
        HorarioDisponivel horarioDisponivel = horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), dataHora);

        // verifico se posso agendar por conta da capacidade
        horarioDisponivelService.verificarDisponibilidadeDeConsulta(horarioDisponivel);

//         criar a consulta
        Consulta novaConsulta = new Consulta(UUID.randomUUID(), dataHora, tipoConsulta, paciente, medico, horarioDisponivel);

        // salvar as mudanças do médico e paciente (consultas no perfis deles) no banco de dados

        horarioDisponivel.getConsultasAgendadas().add(novaConsulta);
        consultaRepository.save(novaConsulta);

        return novaConsulta;
    }

    //buscar consulta por CRM de medico
    public List<Consulta> buscarConsultasPorMedico(String crm){
        Medico medico = medicoService.buscarMedicoPorCrm(crm);
        return consultaRepository.findByMedico(medico);
    }

    //buscar consulta por paciente
    public List<Consulta> buscarConsultasPorPaciente(String cpf){
        Paciente paciente = pacienteService.buscarPacientePorCpf(cpf);
        return consultaRepository.findByPaciente(paciente);
    }

    //buscar consulta por dia
    public List<Consulta> buscaConsultasPorData(UUID idMedico, LocalDateTime date) {
        // busca o médico pelo ID
        Medico medico = medicoService.buscarMedicoPorId(idMedico);

        // lista para guardar consultas do dia
        List<Consulta> consultaList = new ArrayList<>();

        // percorre os horarios disponíveis do medico
        for (HorarioDisponivel horario : medico.getHorarioDisponivel()){
            // se o horario disponível é o que eu quero
            if (horario.getHorario().toLocalDate().equals(date.toLocalDate())){
                // adiciono todas as consulta que eu encontrei do dia x na lista
                consultaList.addAll(horario.getConsultasAgendadas());
            }
        }

        //retorna lista de consultas
        return consultaList;
    }

    //atualizar apenas o horario da consulta
    public Consulta alterarHorarioDaConsulta(UUID consultaId, LocalDateTime novoHorario){
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        HorarioDisponivel horarioAntigo = consulta.getHorarioDisponivel();
        Medico medico = consulta.getMedico();

        // Verificar se o médico tem o novo horário disponível
        HorarioDisponivel novoHorarioDisponivel = horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), novoHorario);

        // Remover a consulta do horário antigo
        horarioAntigo.getConsultasAgendadas().remove(consulta);
        horarioDisponivelService.salvarMudancaDeHorario(horarioAntigo);

        // verificando disponibilidade do nova consulta por conta da capacidade
        horarioDisponivelService.verificarDisponibilidadeDeConsulta(novoHorarioDisponivel);

        novoHorarioDisponivel.getConsultasAgendadas().add(consulta);

        //atualizar a consulta com o novo horário
        consulta.setHorarioDisponivel(novoHorarioDisponivel);
        consulta.setDataHora(novoHorario);
        consultaRepository.save(consulta);

        return consulta;
    }

    // atualizar consulta
//    public Consulta alterarDadosDaConsulta(UUID consultaId, String tipoDeConsulta){
//
//        Consulta consulta = consultaRepository.findById(consultaId)
//                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));
//
//        // busca o horario atual da consulta e remove
//        HorarioDisponivel horarioAntigo = medicoService.buscarHorarioDisponivel(consulta.getMedico(), consulta.getDataHora());
//        horarioAntigo.getConsultasAgendadas().remove(consulta);
//
//        //verifica se o novo horario está disponivel e salva ele
//        HorarioDisponivel novoHorario = medicoService.buscarHorarioDisponivel(consulta.getMedico(), novaDataHora);
//
//        // atualiza detalhes da consulta
//        consulta.setDataHora(novaDataHora);
//        consulta.setTipoConsulta(tipoDeConsulta);
//        consulta.setHorarioDisponivel(novoHorario);
//
//        //adiciona a consulta ao novo horario
//        novoHorario.getConsultasAgendadas().add(consulta);
//
//        //atualiza a lista de horarios do medico
//        consulta.getMedico().getHorarioDisponivel().remove(horarioAntigo);
//        consulta.getMedico().getHorarioDisponivel().add(novoHorario);
//
//        medicoService.alterarMedico(consulta.getMedico().getId(), consulta.getMedico());
//
//        // adiciona a consulta com o novo horario
//        consultaRepository.save(consulta);
//
//        return consulta;
//    }

    //remover consulta
    public void removerConsulta(UUID consultaId){
        // busca consulta pelo ID
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta não encontrada"));

        //remover a consulta da lista de consultas no horario disponivel do medico
        for (HorarioDisponivel horario: consulta.getMedico().getHorarioDisponivel()){
            if (horario.getConsultasAgendadas().contains(consulta)){
                horario.getConsultasAgendadas().remove(consulta);
                break;
            }
        }

        //remove da lista de consultas do paciente
        Paciente paciente = consulta.getPaciente();
        paciente.getConsultas().remove(consulta);

        //salva as alterações
        medicoService.alterarMedico(consulta.getMedico().getId(), consulta.getMedico());
        pacienteService.atualizarPaciente(consulta.getPaciente().getId(), consulta.getPaciente());

        consultaRepository.delete(consulta);
    }
}

