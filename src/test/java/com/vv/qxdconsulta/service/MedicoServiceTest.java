package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicoServiceTest {

    @InjectMocks
    private MedicoService medicoService;

    @Mock
    private MedicoRepository medicoRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdicionarMedicoSucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12346579815", "Cardiologista");

        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.empty());
        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.empty());

        when(medicoRepository.save(medico)).thenReturn(medico);

        Medico result = medicoService.adicionarMedico(medico);

        assertNotNull(result);
        assertEquals(medico, result);

        verify(medicoRepository, times(1)).findByCpf(medico.getCpf());
        verify(medicoRepository, times(1)).findByCrm(medico.getCrm());
        verify(medicoRepository, times(1)).save(medico);
    }

    @Test
    void testAdicionarMedicoCpfJaCadastrado() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12346579815", "Cardiologista");

        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.of(medico));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.adicionarMedico(medico);
        });

        assertEquals("CPF já cadastrado: "+medico.getCpf(), exception.getMessage());

        verify(medicoRepository,times(1)).findByCpf(medico.getCpf());
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    void testAdicionarMedicoCrmJaCadastrado() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12346579815", "Cardiologista");

        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.empty());
        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.of(medico));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.adicionarMedico(medico);
        });

        assertEquals("CRM já cadastrado: "+medico.getCrm(), exception.getMessage());

        verify(medicoRepository,times(1)).findByCpf(medico.getCpf());
        verify(medicoRepository,times(1)).findByCrm(medico.getCrm());
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    public void testAlterarMedicoSucesso() {
        Medico medicoExistente = new Medico(UUID.randomUUID(), "Dr.Silva", "CRM12345", "13246578915","Cardiologia");
        Medico medicoAtualizado = new Medico(medicoExistente.getId(), "Dr. Sousa", "CRM12345", "13246578915","Pediatria");


        when(medicoRepository.findById(medicoExistente.getId())).thenReturn(Optional.of(medicoExistente));
        when(medicoRepository.save(medicoExistente)).thenReturn(medicoExistente);

        Medico result = medicoService.alterarMedico(medicoExistente.getId(), medicoAtualizado);

        assertEquals(medicoAtualizado.getNome(), result.getNome());
        assertEquals(medicoAtualizado.getCpf(), result.getCpf());
        assertEquals(medicoAtualizado.getCrm(), result.getCrm());
        assertEquals(medicoAtualizado.getEspecialização(), result.getEspecialização());


        verify(medicoRepository, times(1)).findById(medicoExistente.getId());
        verify(medicoRepository, times(1)).save(medicoExistente);
    }

    @Test
    public void testAlterarMedicoNaoEncontrado() {
        Medico medicoExistente = new Medico(UUID.randomUUID(), "Dr.Silva", "CRM12345", "13246578915","Cardiologia");
        Medico medicoAtualizado = new Medico(medicoExistente.getId(), "Dr. Sousa", "CRM56789", "13246578915","Pediatria");

        when(medicoRepository.findById(medicoExistente.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.alterarMedico(medicoExistente.getId(), medicoAtualizado);
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoRepository, times(1)).findById(medicoExistente.getId());
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    public void testAlterarMedicoMudandoCpf(){
        Medico medicoExistente = new Medico(UUID.randomUUID(), "Dr.Silva", "CRM12345", "13246578915","Cardiologia");
        Medico medicoAtualizado = new Medico(medicoExistente.getId(), "Dr. Sousa", "CRM12345", "13246578915","Pediatria");

        when(medicoRepository.findById(medicoExistente.getId())).thenReturn(Optional.of(medicoExistente));
        medicoAtualizado.setCpf("78945612315");
        when(medicoRepository.save(medicoExistente)).thenReturn(medicoExistente);

        Medico result = medicoService.alterarMedico(medicoExistente.getId(), medicoAtualizado);

        assertEquals(medicoExistente.getCpf(), result.getCpf());
        verify(medicoRepository, times(1)).save(medicoExistente);
    }

    @Test
    public void testAlterarMedicoMudandoCrm(){
        Medico medicoExistente = new Medico(UUID.randomUUID(), "Dr.Silva", "CRM12345", "13246578915","Cardiologia");
        Medico medicoAtualizado = new Medico(medicoExistente.getId(), "Dr. Sousa", "CRM12345", "13246578915","Pediatria");

        when(medicoRepository.findById(medicoExistente.getId())).thenReturn(Optional.of(medicoExistente));
        medicoAtualizado.setCrm("CRM54612");
        when(medicoRepository.save(medicoExistente)).thenReturn(medicoExistente);

        Medico result = medicoService.alterarMedico(medicoExistente.getId(), medicoAtualizado);

        assertEquals(medicoExistente.getCrm(), result.getCrm());
        verify(medicoRepository, times(1)).save(medicoExistente);
    }

    @Test
    public void testBuscarTodosMedicos() {
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia"));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM56412", "45678913291", "Pediatria"));

        when(medicoRepository.findAll()).thenReturn(medicoList);

        List<Medico> result = medicoService.buscarTodosMedicos();

        assertNotNull(result);
        assertEquals(medicoList.size(), result.size());
        assertTrue(result.containsAll(medicoList));

        verify(medicoRepository, times(1)).findAll();
    }

    @Test public void testBuscarTodosMedicosVazio(){
        when(medicoRepository.findAll()).thenReturn(Collections.emptyList());

        List<Medico> reslult = medicoService.buscarTodosMedicos();

        assertNotNull(reslult);
        assertTrue(reslult.isEmpty());

        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarMedicoPorCrmSucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.of(medico));

        Medico result = medicoService.buscarMedicoPorCrm(medico.getCrm());

        assertNotNull(result);
        assertEquals(medico.getNome(), result.getNome());
        assertEquals(medico.getCpf(), result.getCpf());
        assertEquals(medico.getCrm(), result.getCrm());
        assertEquals(medico.getEspecialização(), result.getEspecialização());

        verify(medicoRepository, times(1)).findByCrm(medico.getCrm());
    }

    @Test
    void testBuscarMedicoPorCrmNaoEncontrado() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorCrm(medico.getCrm());
        });

        assertEquals("Médico não encontrado com o CRM: " + medico.getCrm(), exception.getMessage());

        verify(medicoRepository, times(1)).findByCrm(medico.getCrm());
    }

    @Test
    void testBuscarMedicoPorCpfSucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.of(medico));

        Medico result = medicoService.buscarMedicoPorCpf(medico.getCpf());

        assertNotNull(result);
        assertEquals(medico.getNome(), result.getNome());
        assertEquals(medico.getCpf(), result.getCpf());
        assertEquals(medico.getCrm(), result.getCrm());
        assertEquals(medico.getEspecialização(), result.getEspecialização());

        verify(medicoRepository, times(1)).findByCpf(medico.getCpf());
    }

    @Test
    void testBuscarMedicoPorCpfNaoEncontrado() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorCpf(medico.getCpf());
        });

        assertEquals("Médico não encontrado com o CPF: " + medico.getCpf(), exception.getMessage());

        verify(medicoRepository, times(1)).findByCpf(medico.getCpf());
    }

    @Test
    void testBuscarMedicoPorIdSucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        Medico result = medicoService.buscarMedicoPorId(medico.getId());

        assertNotNull(result);
        assertEquals(medico.getNome(), result.getNome());
        assertEquals(medico.getCpf(), result.getCpf());
        assertEquals(medico.getCrm(), result.getCrm());
        assertEquals(medico.getEspecialização(), result.getEspecialização());

        verify(medicoRepository, times(1)).findById(medico.getId());
    }

    @Test
    void testBuscarMedicoPorIdNaoEncontrado() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorId(medico.getId());
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoRepository, times(1)).findById(medico.getId());
    }

    //BuscarMedicosPorNome
    @Test
    public void testBuscarMedicosPorNomeSucesso() {
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia"));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM54321", "45678912345", "Neurologia"));

        when(medicoRepository.findByNomeContainingIgnoreCase("Silva")).thenReturn(Arrays.asList(medicoList.get(1)));

        List<Medico> result = medicoService.buscarMedicosPorNome("Silva");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(medicoList.get(1)));

        verify(medicoRepository, times(1)).findByNomeContainingIgnoreCase("Silva");
    }

    @Test
    public void testBuscarMedicosPorNomeNaoEncontrado() {
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia"));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM54321", "45678912345", "Neurologia"));

        when(medicoRepository.findByNomeContainingIgnoreCase("Vieira")).thenReturn(Collections.emptyList());

        List<Medico> result = medicoService.buscarMedicosPorNome("Vieira");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoRepository, times(1)).findByNomeContainingIgnoreCase("Vieira");
    }

    @Test
    public void buscarMedicosPorEspecializacaoSucesso(){
        String especializacao = "Ortopedia";
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "15975326815", especializacao));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM45612", "35745695124", especializacao));

        when(medicoRepository.findByEspecializacao(especializacao)).thenReturn(medicoList);

        List<Medico> result = medicoService.buscarMedicosPorEspecializacao(especializacao);

        assertNotNull(result);
        assertEquals(medicoList.size(), result.size());
        assertTrue(result.containsAll(medicoList));

        verify(medicoRepository, times(1)).findByEspecializacao(especializacao);
    }

    @Test
    public void buscarMedicosPorEspecializacaoSemMedicos(){
        String especializacao = "Ortopedia";
        List<Medico> medicoList = new ArrayList<>();

        when(medicoRepository.findByEspecializacao(especializacao)).thenReturn(medicoList);

        List<Medico> result = medicoService.buscarMedicosPorEspecializacao(especializacao);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoRepository, times(1)).findByEspecializacao(especializacao);
    }

    @Test
    public void removerMedicoSucesso(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now(), 5);
        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());
        medico.setHorarioDisponivel(List.of(horarioDisponivel));

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        medicoService.removerMedico(medico.getId());

        verify(medicoRepository,times(1)).delete(medico);
    }

    @Test
    public void removerMedicoNaoEncontrado(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.removerMedico(medico.getId());
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoRepository, never()).delete(any(Medico.class));
    }

    @Test
    public void removerMedicoComConsultasMarcadas(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now(), 5);
        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());
        medico.setHorarioDisponivel(List.of(horarioDisponivel));

        List<Consulta> consultasAgendadas = new ArrayList<>();
        consultasAgendadas.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Consulta Geral", new Paciente(), medico));
        horarioDisponivel.setConsultasAgendadas(consultasAgendadas);

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.removerMedico(medico.getId());
        });

        assertEquals("O médico possui consultas marcadas e não pode ser excluído.", exception.getMessage());

        verify(medicoRepository, never()).delete(any(Medico.class));
    }
}