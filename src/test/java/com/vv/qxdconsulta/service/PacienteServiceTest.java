package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testAdicionarPacienteCpfJaCdastrado() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        // Configurando o mock para simular que o CPF já está cadastrado
        when(pacienteRepository.findByCpf((paciente.getCpf()))).thenReturn(Optional.of(paciente));

        //verificação de execução
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.adicionarPaciente(paciente);
        });

        // verifica a mensagem de exceção
        String mensagemEsperada = "CPF já cadastrado: " + paciente.getCpf();
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);

        //verifica se o metodo save não foi chamado
        verify(pacienteRepository, never()).save((any(Paciente.class)));

    }

    @Test
    void testAdicionarPacienteEmailJaCdastrado() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        // Configurando o mock para simular que o CPF já está cadastrado
        when(pacienteRepository.findByEmail(paciente.getEmail())).thenReturn(Optional.of(paciente));

        // verificação de execução
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.adicionarPaciente(paciente);
        });

        // verifica a mensagem de execução
        String mensagemEsperada = "Email já cadastrado: " + paciente.getEmail();
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);

        // verifica se o metodo save não foi chamado
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void testeAdicionarPacienteSucesso() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        // configurar os mocks para retornar Optional.empty(), simulando que o CPF e Email não estão cadastrados
        when(pacienteRepository.findByCpf(paciente.getCpf())).thenReturn(Optional.empty());
        when(pacienteRepository.findByEmail(paciente.getEmail())).thenReturn(Optional.empty());

        // configurar o mock para salvar o paciente
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(paciente);

        //executando o metodo
        Paciente result = pacienteService.adicionarPaciente(paciente);

        //verificação
        assertNotNull(result);
        assertEquals(paciente.getCpf(), result.getCpf());
        assertEquals(paciente.getEmail(), result.getEmail());

        //verificando se o metodo só foi chamado uma vez
        verify(pacienteRepository, times(1)).save(paciente);
    }

    @Test
    void testAtualizarPacienteSucesso() {
        UUID idPaciente = UUID.randomUUID();

        Paciente pacienteExistente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");
        Paciente pacienteAtualizado = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588777777777");

        //configurando mock para simular a busca do paciente existente
        when(pacienteRepository.findById(idPaciente)).thenReturn(Optional.of(pacienteExistente));

        //configurando mock para salvar o paciente atualizado
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(pacienteAtualizado);

        //executa o metodo de atualizar
        Paciente result = pacienteService.atualizarPaciente(idPaciente, pacienteAtualizado);

        //verifica se o paciente o paciente existe e foi atualizado corretamente
        assertNotNull(result);
        assertEquals(pacienteAtualizado.getEmail(), result.getName());
        assertEquals(pacienteAtualizado.getEmail(), result.getEmail());
        assertEquals(pacienteAtualizado.getContato(), result.getContato());

        //verifica se o metodo foi chamado apenas uma vez
        verify(pacienteRepository, times(1)).save(pacienteExistente);
    }

    @Test
    void testAtualizarPacienteNaoEncontrado() {
        UUID idPaciente = UUID.randomUUID();

        Paciente pacienteAtualizado = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588777777777");


        //configura o mock para simular que o paciente não foi encontrado
        when(pacienteRepository.findById(idPaciente)).thenReturn(Optional.empty());

        //executa o metodo e verifica se a exceção correta foi lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.atualizarPaciente(idPaciente, pacienteAtualizado);
        });

        //verifica a mensagem de exceção
        String mensagemEsperada = "Paciente não encontrado";
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);


        //verifica se o metodo de atualizar não foi chamado
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void buscarTodosPacientes() {
    }

    @Test
    void buscarPacientePorId() {
    }

    @Test
    void buscarPacientePorCpf() {
    }

    @Test
    void buscarHistoricoDeConsultas() {
    }
}