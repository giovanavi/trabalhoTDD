package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PacienteService {

    @Autowired
    PacienteRepository pacienteRepository;

    //registrar paciente
    public Paciente adicionarPaciente(Paciente paciente){
        //verificação se o CPF já está cadastrado
        if (pacienteRepository.findByCpf(paciente.getCpf()).isPresent()){
            throw new IllegalArgumentException("CPF já cadastrado: " + paciente.getCpf());
        }

        //verificação se o email já está cadastrado
        if (pacienteRepository.findByEmail(paciente.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email já cadastrado: " + paciente.getEmail());
        }

        return pacienteRepository.save(paciente);
    }

    //atualizar paciente
    public Paciente atualizarPaciente(UUID idPaciente, Paciente paciente){
        Paciente pacienteExistente = pacienteRepository.findById(idPaciente).orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        pacienteExistente.setName(paciente.getName());
        pacienteExistente.setEmail(paciente.getEmail());
        pacienteExistente.setCpf(paciente.getCpf());
        pacienteExistente.setContato(paciente.getContato());

        return pacienteRepository.save(pacienteExistente);
    }

    //buscar paciente
    public List<Paciente> buscarTodosPacientes(){
        return pacienteRepository.findAll();
    }

    public Paciente buscarPacientePorId(UUID idPaciente){
        return pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com o CPF: " + idPaciente));
    }

    //buscar paciente por cpf/email/nome
    public Paciente buscarPacientePorCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado com o CPF: " + cpf));
    }

    //buscar histórico de consultas
    public List<Consulta> buscarHistoricoDeConsultas(UUID idPaciente){
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado"));

        return paciente.getConsultas();
    }

}
