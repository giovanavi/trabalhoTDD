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
import java.util.*;


class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Mock
    private ConsultaService consultaService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testeAdicionarPacienteSucesso() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        when(pacienteRepository.findByCpf(paciente.getCpf())).thenReturn(Optional.empty());
        when(pacienteRepository.findByEmail(paciente.getEmail())).thenReturn(Optional.empty());
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente result = pacienteService.adicionarPaciente(paciente);

        assertNotNull(result);
        assertEquals(paciente, result);

        verify(pacienteRepository, times(1)).findByCpf(paciente.getCpf());
        verify(pacienteRepository, times(1)).findByEmail(paciente.getEmail());
        verify(pacienteRepository, times(1)).save(paciente);
    }

    @Test
    void testAdicionarPacienteCpfJaCdastrado() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        when(pacienteRepository.findByCpf((paciente.getCpf()))).thenReturn(Optional.of(paciente));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.adicionarPaciente(paciente);
        });

        assertEquals("CPF já cadastrado: " + paciente.getCpf(), exception.getMessage());

        verify(pacienteRepository, times(1)).findByCpf(paciente.getCpf());
        verify(pacienteRepository, never()).findByEmail(anyString());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void testAdicionarPacienteEmailJaCdastrado() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        when(pacienteRepository.findByEmail(paciente.getEmail())).thenReturn(Optional.of(paciente));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.adicionarPaciente(paciente);
        });

        assertEquals("Email já cadastrado: " + paciente.getEmail(), exception.getMessage());


        verify(pacienteRepository, times(1)).findByCpf(paciente.getCpf());
        verify(pacienteRepository, times(1)).findByEmail(paciente.getEmail());
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void testAtualizarPacienteSucesso() {
        UUID pacienteId = UUID.randomUUID();
        Paciente pacienteExistente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");
        Paciente pacienteAtualizado = new Paciente(pacienteId, "José", "josehumberto@email.com", "12345678914", "+5588777777777");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(pacienteExistente));

        when(pacienteRepository.save(pacienteExistente)).thenReturn(pacienteExistente);

        Paciente result = pacienteService.atualizarPaciente(pacienteId, pacienteAtualizado);

        assertNotNull(result);
        assertEquals(pacienteAtualizado.getName(), result.getName());
        assertEquals(pacienteAtualizado.getEmail(), result.getEmail());
        assertEquals(pacienteAtualizado.getCpf(), result.getCpf());
        assertEquals(pacienteAtualizado.getContato(), result.getContato());

        verify(pacienteRepository, times(1)).findById(pacienteId);
        verify(pacienteRepository, times(1)).save(pacienteExistente);
    }

    @Test
    void testAtualizarPacienteNaoEncontrado() {
        UUID pacienteId = UUID.randomUUID();
        Paciente pacienteExistente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");
        Paciente pacienteAtualizado = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588777777777");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.atualizarPaciente(pacienteId, pacienteAtualizado);
        });

        assertEquals("Paciente não encontrado", exception.getMessage());

        verify(pacienteRepository, times(1)).findById(pacienteId);
        verify(pacienteRepository, never()).save(any(Paciente.class));
    }

    @Test
    void testRemoverPacienteSucesso(){
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Cardiologia", paciente, null));
        consultaList.add( new Consulta(UUID.randomUUID(), LocalDateTime.now().minusDays(1), "Oftalmologia", paciente, null));

        paciente.setConsultas(consultaList);

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        pacienteService.removerPaciente(pacienteId);

        verify(consultaService, times(1)).removerConsulta(consultaList.get(0).getId());
        verify(consultaService, times(1)).removerConsulta(consultaList.get(1).getId());

        verify(pacienteRepository, times(1)).delete(paciente);
    }

    @Test
    void testRemoverPacienteNaoEncontrado(){
        UUID pacienteId = UUID.randomUUID();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.removerPaciente(pacienteId);
        });

        assertEquals("Paciente não encontrado", exception.getMessage());

        verify(consultaService, never()).removerConsulta(any(UUID.class));
        verify(pacienteRepository, never()).delete(any(Paciente.class));
    }

    @Test
    void testRemoverPacienteSemConsulta(){
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");
        paciente.setConsultas(new ArrayList<>());

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        pacienteService.removerPaciente(pacienteId);

        verify(consultaService, never()).removerConsulta(any(UUID.class));
        verify(pacienteRepository, times(1)).delete(paciente);
    }

    @Test
    void buscarTodosPacientesSucesso() {
        List<Paciente> pacienteList = new ArrayList<>();
        pacienteList.add(new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999"));
        pacienteList.add(new Paciente(UUID.randomUUID(), "Adriana Vieira", "adriana@email.com", "98765432107", "+5588666666666"));

        when(pacienteRepository.findAll()).thenReturn(pacienteList);

        List<Paciente> result = pacienteService.buscarTodosPacientes();

        assertNotNull(result);
        assertEquals(pacienteList.size(), result.size());
        assertTrue(result.containsAll(pacienteList));

        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    void buscarTodosPacientesSemPacientes() {
        List<Paciente> pacienteList = new ArrayList<>();

        when(pacienteRepository.findAll()).thenReturn(pacienteList);

        List<Paciente> result = pacienteService.buscarTodosPacientes();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(pacienteRepository, times(1)).findAll();
    }

    @Test
    void buscarPacientePorIdSucesso() {
        UUID pacienteId = UUID.randomUUID();
        Paciente paciente = new Paciente(pacienteId, "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));

        Paciente result = pacienteService.buscarPacientePorId(pacienteId);

        assertNotNull(result);
        assertEquals(paciente, result);

        verify(pacienteRepository, times(1)).findById(pacienteId);
    }

    @Test
    void buscarPacientePorIdNaoEncontrado(){
        UUID pacienteId = UUID.randomUUID();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarPacientePorId(pacienteId);
        });

        assertEquals("Paciente não encontrado com o ID: " + pacienteId, exception.getMessage());

        verify(pacienteRepository, times(1)).findById(pacienteId);
    }

    @Test
    void buscarPacientePorCpfSucesso() {
        String pacienteCpf = "12345678914";
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", pacienteCpf, "+5588999999999");

        when(pacienteRepository.findByCpf(pacienteCpf)).thenReturn(Optional.of(paciente));

        Paciente result = pacienteService.buscarPacientePorCpf(pacienteCpf);

        assertNotNull(result);
        assertEquals(paciente, result);

        verify(pacienteRepository, times(1)).findByCpf(pacienteCpf);
    }

    @Test
    void buscarPacientePorCpfNaoEncontrado(){
        String pacienteCpf = "12345678914";

        when(pacienteRepository.findByCpf(pacienteCpf)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarPacientePorCpf(pacienteCpf);
        });

        assertEquals("Paciente não encontrado com o CPF: " + pacienteCpf, exception.getMessage());

        verify(pacienteRepository, times(1)).findByCpf(pacienteCpf);
    }

    @Test
    public void buscarPacientesPorNomeSucesso(){
        List<Paciente> pacienteList = new ArrayList<>();
        pacienteList.add(new Paciente(UUID.randomUUID(), "João Silva", "joao.silva@email.com", "12345678901", "+5588999999999"));
        pacienteList.add(new Paciente(UUID.randomUUID(), "Maria Oliveira", "maria.oliveira@email.com", "98765432109", "+55887777777777"));

        when(pacienteRepository.findByNameContainingIgnoreCase("Silva")).thenReturn(List.of(pacienteList.get(0)));

        List<Paciente> result = pacienteService.buscarPacientePorNome("Silva");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(pacienteList.get(0)));

        verify(pacienteRepository, times(1)).findByNameContainingIgnoreCase("Silva");
    }

    @Test
    public void buscarPacientesPorNomeNaoEncontrado(){
        List<Paciente> pacienteList = new ArrayList<>();

        when(pacienteRepository.findByNameContainingIgnoreCase("Silva")).thenReturn(pacienteList);

        List<Paciente> result = pacienteService.buscarPacientePorNome("Silva");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(pacienteRepository, times(1)).findByNameContainingIgnoreCase("Silva");
    }

    @Test
    void testBuscarHistoricoDeConsultasSucesso() {
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678914", "+5588999999999");
        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Oftalmologia", paciente, null));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().minusDays(1), "Pediatria", paciente, null));
        paciente.setConsultas(consultaList);

        when(pacienteRepository.findById(paciente.getId())).thenReturn(Optional.of(paciente));

        List<Consulta> result = pacienteService.buscarHistoricoDeConsultas(paciente.getId());

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.contains(consultaList.get(0)));
        assertTrue(result.contains(consultaList.get(1)));

        verify(pacienteRepository, times(1)).findById(paciente.getId());
    }

    @Test
    void testBuscarHistoricoDeConsultasPacienteNaoEncontrado() {
        UUID pacienteId = UUID.randomUUID();

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pacienteService.buscarHistoricoDeConsultas(pacienteId);
        });

        assertEquals("Paciente não encontrado", exception.getMessage());

        verify(pacienteRepository, times(1)).findById(pacienteId);
    }


}