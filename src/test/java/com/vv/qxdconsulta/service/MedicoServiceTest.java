package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MedicoServiceTest {

    @InjectMocks
    private MedicoService medicoService;

    @Mock
    private MedicoRepository medicoRepository;

    private Medico medicoExistente;
    private Medico medicoNovo;
    private UUID idMedico;
    private List<Medico> listaMedicos;
    private String cpf;
    private String crm;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar uma lista de médicos de exemplo
        listaMedicos = new ArrayList<>();

        Medico medico1 = new Medico();
        medico1.setNome("Medico 1");
        medico1.setCpf("11111111111");
        medico1.setCrm("CRM001");
        medico1.setEspecialização("Especialização 1");

        Medico medico2 = new Medico();
        medico2.setNome("Medico 2");
        medico2.setCpf("22222222222");
        medico2.setCrm("CRM002");
        medico2.setEspecialização("Especialização 2");

        listaMedicos.add(medico1);
        listaMedicos.add(medico2);

        // Configuração para o teste de alteração de médico
        idMedico = UUID.randomUUID();
        medicoExistente = new Medico();
        medicoExistente.setId(idMedico);
        medicoExistente.setNome("Nome Original");
        medicoExistente.setCpf("12345678900");
        medicoExistente.setCrm("CRM123");
        medicoExistente.setEspecialização("Especialização Original");

        medicoNovo = new Medico();
        medicoNovo.setNome("Nome Novo");
        medicoNovo.setCpf("09876543211");
        medicoNovo.setCrm("CRM456");
        medicoNovo.setEspecialização("Especialização Nova");

        cpf = medicoExistente.getCpf();
        crm = medicoExistente.getCrm();

        idMedico = medicoExistente.getId();
    }

    @Test
    void testAdicionarMedico_Sucesso() {
        Medico medico = new Medico();
        medico.setId(UUID.randomUUID());
        medico.setNome("Dr. João");
        medico.setCrm("123456");
        medico.setCpf("12345678901");
        medico.setEspecialização("Cardiologista");

        // Simula que o CPF e CRM não estão cadastrados
        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.empty());
        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.empty());

        // Simula o sucesso na operação de salvar o médico
        when(medicoRepository.save(any(Medico.class))).thenReturn(medico);

        Medico result = medicoService.adicionarMedico(medico);

        assertNotNull(result);
        assertEquals(medico.getNome(), result.getNome());
        assertEquals(medico.getCpf(), result.getCpf());
        assertEquals(medico.getCrm(), result.getCrm());
        assertEquals(medico.getEspecialização(), result.getEspecialização());

        verify(medicoRepository, times(1)).save(medico);
    }

    @Test
    void testAdicionarMedico_CpfJaCadastrado() {
        Medico medico = new Medico();
        medico.setId(UUID.randomUUID());
        medico.setNome("Dr. João");
        medico.setCrm("123456");
        medico.setCpf("12345678901");
        medico.setEspecialização("Cardiologista");

        // Simula que o CPF já está cadastrado
        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.of(medico));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.adicionarMedico(medico);
        });

        assertEquals("CPF já cadastrado: 123456", exception.getMessage());

        // Verifica se o mét odo save não foi chamado
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    void testAdicionarMedico_CrmJaCadastrado() {
        Medico medico = new Medico();
        medico.setId(UUID.randomUUID());
        medico.setNome("Dr. João");
        medico.setCrm("123456");
        medico.setCpf("12345678901");
        medico.setEspecialização("Cardiologista");

        // Simula que o CPF não está cadastrado, mas o CRM está
        when(medicoRepository.findByCpf(medico.getCpf())).thenReturn(Optional.empty());
        when(medicoRepository.findByCrm(medico.getCrm())).thenReturn(Optional.of(medico));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.adicionarMedico(medico);
        });

        assertEquals("Email já cadastrado: 123456", exception.getMessage());

        // Verifica se o método save não foi chamado
        verify(medicoRepository, never()).save(any(Medico.class));
    }

    @Test
    void testBuscarHorarioDisponivel_Sucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "123456", "12345678901", "Ortopedia");

        List<HorarioDisponivel> horarioDisponiveis = new ArrayList<>();
        horarioDisponiveis.add(new HorarioDisponivel(LocalDateTime.now(), 10));
        horarioDisponiveis.add(new HorarioDisponivel(LocalDateTime.now().plusDays(1), 10));
        medico.setHorarioDisponivel(horarioDisponiveis);

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        List<HorarioDisponivel> result = medicoService.buscarHorariosDisponiveis(medico.getId());

        // Verifica se o resultado não é nulo e se o horário é o esperado
        assertNotNull(result);
        assertEquals(horarioDisponiveis.size(), result.size());
        assertEquals(horarioDisponiveis, result);

        verify(medicoRepository, times(1)).findById(medico.getId());
    }

    @Test
    public void testAlterarMedico_Sucesso() {
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medicoExistente));
        when(medicoRepository.save(any(Medico.class))).thenReturn(medicoExistente);

        Medico medicoAtualizado = medicoService.alterarMedico(idMedico, medicoNovo);

        assertEquals("Nome Novo", medicoAtualizado.getNome());
        assertEquals("09876543211", medicoAtualizado.getCpf());
        assertEquals("CRM456", medicoAtualizado.getCrm());
        assertEquals("Especialização Nova", medicoAtualizado.getEspecialização());

        verify(medicoRepository, times(1)).findById(idMedico);
        verify(medicoRepository, times(1)).save(medicoExistente);
    }

    @Test
    public void testAlterarMedico_MedicoNaoEncontrado() {
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.alterarMedico(idMedico, medicoNovo);
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoRepository, times(1)).findById(idMedico);
    }

    @Test
    public void testBuscarTodosMedicos() {
        when(medicoRepository.findAll()).thenReturn(listaMedicos);

        List<Medico> medicosEncontrados = medicoService.buscarTodosMedicos();

        assertEquals(2, medicosEncontrados.size());
        assertEquals("Medico 1", medicosEncontrados.get(0).getNome());
        assertEquals("Medico 2", medicosEncontrados.get(1).getNome());

        verify(medicoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarMedicoPorCrm_Sucesso() {
        // Simula que o CRM existe no repositório
        when(medicoRepository.findByCpf(crm)).thenReturn(Optional.of(medicoExistente));

        Medico resultado = medicoService.buscarMedicoPorCrm(crm);

        assertNotNull(resultado);
        assertEquals(medicoExistente.getNome(), resultado.getNome());
        assertEquals(medicoExistente.getCpf(), resultado.getCpf());
        assertEquals(medicoExistente.getCrm(), resultado.getCrm());
        assertEquals(medicoExistente.getEspecialização(), resultado.getEspecialização());

        verify(medicoRepository, times(1)).findByCpf(crm);
    }

    @Test
    void testBuscarMedicoPorCrm_NaoEncontrado() {
        // Simula que o CRM não existe no repositório
        when(medicoRepository.findByCpf(crm)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorCrm(crm);
        });

        assertEquals("Médico não encontrado com o CPF: " + crm, exception.getMessage());

        verify(medicoRepository, times(1)).findByCpf(crm);
    }

    @Test
    void testBuscarMedicoPorCpf_Sucesso() {
        // Simula que o CPF existe no repositório
        when(medicoRepository.findByCpf(cpf)).thenReturn(Optional.of(medicoExistente));

        Medico resultado = medicoService.buscarMedicoPorCpf(cpf);

        assertNotNull(resultado);
        assertEquals(medicoExistente.getNome(), resultado.getNome());
        assertEquals(medicoExistente.getCpf(), resultado.getCpf());
        assertEquals(medicoExistente.getCrm(), resultado.getCrm());
        assertEquals(medicoExistente.getEspecialização(), resultado.getEspecialização());

        verify(medicoRepository, times(1)).findByCpf(cpf);
    }

    @Test
    void testBuscarMedicoPorCpf_NaoEncontrado() {
        // Simula que o CPF não existe no repositório
        when(medicoRepository.findByCpf(cpf)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorCpf(cpf);
        });

        assertEquals("Médico não encontrado com o CPF: " + cpf, exception.getMessage());

        verify(medicoRepository, times(1)).findByCpf(cpf);
    }

    @Test
    void testBuscarMedicoPorId_Sucesso() {
        // Simula que o médico existe no repositório
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medicoExistente));

        Medico resultado = medicoService.buscarMedicoPorId(idMedico);

        assertNotNull(resultado);
        assertEquals(medicoExistente.getNome(), resultado.getNome());
        assertEquals(medicoExistente.getCpf(), resultado.getCpf());
        assertEquals(medicoExistente.getCrm(), resultado.getCrm());
        assertEquals(medicoExistente.getEspecialização(), resultado.getEspecialização());

        verify(medicoRepository, times(1)).findById(idMedico);
    }

    @Test
    void testBuscarMedicoPorId_NaoEncontrado() {
        // Simula que o médico não existe no repositório
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicoService.buscarMedicoPorId(idMedico);
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoRepository, times(1)).findById(idMedico);
    }

}