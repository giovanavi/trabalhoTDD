package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


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
        assertEquals(paciente, result);

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
        assertEquals(pacienteAtualizado, result);

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
    void buscarTodosPacientesSucesso() {
        List<Paciente> pacienteList = new ArrayList<>();
        pacienteList.add(new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999"));
        pacienteList.add(new Paciente(UUID.randomUUID(), "Adriana Vieira", "adriana@email.com", "98765432107", "+5588666666666"));

        //configura o mock para retornar a pacienteList
        when(pacienteRepository.findAll()).thenReturn(pacienteList);

        //executa o metodo de buscar todos os pacientes
        List<Paciente> result = pacienteService.buscarTodosPacientes();

        //verifica se o resulta não é nulo e se contem a lista de pacientes simulados e se os pacientes são iguais aos cadastrados
        assertNotNull(result);
        assertEquals(pacienteList.size(), result.size());
        for (int i = 0; i < pacienteList.size(); i++){
            assertEquals(pacienteList.get(i), result.get(i));
        }

        //verifica se o metodo findAll foi chamado uma vez
        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    void buscarPacientePorIdSucesso() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        // configurando mock para retornar o paciente encontrado
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        //executa o metodo de buscar paciente
        Paciente result = pacienteService.buscarPacientePorId(pacienteId);

        // verifica se o paciente encontrado está correto
        assertNotNull(result);
        assertEquals(paciente, result);

        // verifica que o metodo foi chamado uma vez
        verify(pacienteRepository, times(1)).findById(pacienteId);
    }

    @Test
    void buscarPacientePorIdNaoEncontrado(){
        UUID pacienteId = UUID.randomUUID();

        // configurando mock para retornar o paciente não encontrado
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        //executa a verififcação se a exceção é lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarPacientePorId(pacienteId);
        });

        //verifica mensagem de exceção
        String mensagemEsperada = "Paciente não encontrado com o ID: " + pacienteId;
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);

        //verifica que o metodo so foi chamado uma vez
        verify(pacienteRepository, times(1)).findById(pacienteId);
    }

    @Test
    void buscarPacientePorCpf() {
        String pacienteCpf = "12345678914";
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", pacienteCpf, "+5588999999999");

        // configurando mock para retornar o paciente encontrado
        when(pacienteRepository.findByCpf(pacienteCpf)).thenReturn(Optional.of(paciente));

        //executa o metodo de buscar paciente
        Paciente result = pacienteService.buscarPacientePorCpf(pacienteCpf);

        // verifica se o paciente encontrado está correto
        assertNotNull(result);
        assertEquals(paciente, result);

        // verifica que o metodo foi chamado uma vez
        verify(pacienteRepository, times(1)).findByCpf(pacienteCpf);
    }

    @Test
    void buscarPacientePorCpfNaoEncontrado(){
        String pacienteCpf = "12345678914";

        // configurando mock para retornar o paciente não encontrado
        when(pacienteRepository.findByCpf(pacienteCpf)).thenReturn(Optional.empty());

        //executa a verififcação se a exceção é lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarPacientePorCpf(pacienteCpf);
        });

        //verifica mensagem de exceção
        String mensagemEsperada = "Paciente não encontrado com o CPF: " + pacienteCpf;
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);

        //verifica que o metodo so foi chamado uma vez
        verify(pacienteRepository, times(1)).findByCpf(pacienteCpf);
    }

    @Test
    void testBuscarHistoricoDeConsultasSucesso() {
        UUID pacienteid = UUID.randomUUID();
        Paciente paciente = new Paciente(pacienteid, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        // criando um paciente com um historico de consultas
        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Oftalmologia", paciente, null));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().minusDays(1), "Pediatria", paciente, null));

        //adicionando a lista de consulta a um paciente
        paciente.setConsultas(consultaList);

        // configurando o mock para retornar o paciente
        when(pacienteRepository.findById(pacienteid)).thenReturn(Optional.of(paciente));

        // executando metodo de buscar historico
        List<Consulta> result = pacienteService.buscarHistoricoDeConsultas(pacienteid);

        //verificando se o resultado não é nulo e se contem o historico do paciente
        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        for (int i = 0; i < consultaList.size(); i++){
            assertEquals(consultaList.get(i), result.get(i));
        }

        // verifica se o metodo foi chamado apenas umas vez
        verify(pacienteRepository, times(1)).findById(pacienteid);
    }

    @Test
    void testBuscarHistoricoDeConsultasPacienteNaoEncontrado() {
        UUID pacienteId = UUID.randomUUID();

        // configurando o mock para simular que o paciente não foi encontrado
        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        //executa a verififcação se a exceção é lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarHistoricoDeConsultas(pacienteId);
        });

        //verifica a mensagem
        String mensagemEsperada = "Paciente não encontrado";
        String mensagemReal = exception.getMessage();
        assertEquals(mensagemEsperada, mensagemReal);

        //verifica se o metodo só foi chamado uma vez
        verify(pacienteRepository, times(1)).findById(pacienteId);
    }
}