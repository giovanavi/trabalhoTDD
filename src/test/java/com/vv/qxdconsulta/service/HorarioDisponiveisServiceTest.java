package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.repository.HorarioDisponivelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


public class HorarioDisponiveisServiceTest {

    @InjectMocks
    private HorarioDisponivelService horarioDisponivelService;

    @Mock
    private MedicoService medicoService;

    @Mock
    private HorarioDisponivelRepository horarioDisponivelRepository;

    @Mock
    private HorarioDisponivel horarioDisponivelClass;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdicionarHorarioDisponivelSucesso() {
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now(), 5);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);
        when(horarioDisponivelRepository.save(horarioDisponivel)).thenReturn(horarioDisponivel);

        HorarioDisponivel result = horarioDisponivelService.adicionarHorarioDisponivel(horarioDisponivel, medico.getCrm());

        assertEquals(horarioDisponivel, result);
        assertEquals(medico, result.getMedico());

        verify(horarioDisponivelRepository, times(1)).save(horarioDisponivel);
        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    void testAdicionarHorarioDisponivelMedicoNaoEncontrado(){
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now(), 5);
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenThrow(new IllegalArgumentException("Médico não encontrado com o CRM: "+ medico.getCrm()));

        // Tentar adicionar um horário com médico nulo e verificar se uma exceção é lançada
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.adicionarHorarioDisponivel(horarioDisponivel, medico.getCrm());
        });

        // Verificar a mensagem de erro
        assertEquals("Médico não encontrado com o CRM: " + medico.getCrm(), exception.getMessage());
        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
        verify(horarioDisponivelRepository, never()).save(any(HorarioDisponivel.class));
    }

    //listarHorariosDisponiveisPorMedico
    @Test
    public void testListarHorariosDisponiveisPorMedicoSucesso(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5));
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(2), 3));
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);

        List<HorarioDisponivel> result = horarioDisponivelService.listarHorariosDisponiveisPorMedico(medico.getCrm());

        assertEquals(horarioDisponivelList.size(), result.size());
        assertTrue(result.containsAll(horarioDisponivelList));

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    public void testListarHorariosDisponiveisPorMedicoNaoEncontrado(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5));
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(2), 3));
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenThrow( new IllegalArgumentException("Médico não encontrado com o CRM: "+ medico.getCrm()));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
           horarioDisponivelService.listarHorariosDisponiveisPorMedico(medico.getCrm());
        });

        assertEquals("Médico não encontrado com o CRM: "+ medico.getCrm(), exception.getMessage());

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    public void testListarHorariosDisponiveisPorMedicoSemHorarios(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
           horarioDisponivelService.listarHorariosDisponiveisPorMedico(medico.getCrm());
        });

        assertEquals("Esse medico não tem horários disponíveis", exception.getMessage());

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());

    }

    //buscarHorarioPorMedico
    @Test
    public void testBuscarHorarioPorMedicoSucesso(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        LocalDateTime horario = LocalDateTime.now().plusDays(1);
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        horarioDisponivelList.add(new HorarioDisponivel(horario, 5));
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(2), 3));
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);

        HorarioDisponivel result = horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horario);

        assertEquals(horario, result.getHorario());

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    public void testBuscarHorarioPorMedicoNaoEncontrado(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        LocalDateTime horario = LocalDateTime.now().plusDays(1);
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        horarioDisponivelList.add(new HorarioDisponivel(horario, 5));
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(2), 3));
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenThrow( new IllegalArgumentException("Médico não encontrado com o CRM: "+ medico.getCrm()));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horario);
        });

        assertEquals("Médico não encontrado com o CRM: "+medico.getCrm(), exception.getMessage());

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    public void testBuscarHorarioPorMedicoSemHorario(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678954", "Pediatria");
        LocalDateTime horario = LocalDateTime.now().plusDays(1);
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        medico.setHorarioDisponivel(horarioDisponivelList);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horario);
        });

        assertEquals("O médico não tem esse horário disponível", exception.getMessage());

        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    //verificarDisponibilidadeDeConsulta
    @Test
    public void testverificarDisponibilidadeDeConsultaSucesso(){

        when(horarioDisponivelClass.podeAgendar()).thenReturn(true);

        assertDoesNotThrow(() -> horarioDisponivelService.verificarDisponibilidadeDeConsulta(horarioDisponivelClass));

        verify(horarioDisponivelClass, times(1)).podeAgendar();
    }

    @Test
    public void testverificarDisponibilidadeDeConsultaFalha(){

        when(horarioDisponivelClass.podeAgendar()).thenReturn(false);

        Exception exception = assertThrows( IllegalArgumentException.class, () ->{
           horarioDisponivelService.verificarDisponibilidadeDeConsulta(horarioDisponivelClass);
        });

        assertEquals("Limite de consultas para este horário já atingido.", exception.getMessage());

        verify(horarioDisponivelClass, times(1)).podeAgendar();
    }

//    buscarHorariosDisponiveisPorData
    @Test
    public void testBuscarHorariosDisponiveisPorDataSucesso(){
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5));
        horarioDisponivelList.add(new HorarioDisponivel(LocalDateTime.now().plusDays(2), 3));

        LocalDate horarioDeBusca = LocalDate.now().plusDays(1);
        LocalDateTime dataComeco = horarioDeBusca.atStartOfDay();
        LocalDateTime dataFinal = horarioDeBusca.atTime(LocalTime.MAX);

        when(horarioDisponivelRepository.findByHorarioBetween(dataComeco,dataFinal)).thenReturn(horarioDisponivelList);

        List<HorarioDisponivel> result = horarioDisponivelService.buscarHorariosDisponiveisPorData(horarioDeBusca);

        assertEquals(horarioDisponivelList.size(), result.size());
        assertTrue(result.containsAll(horarioDisponivelList));

        // Verificar se o método findByHorarioBetween foi chamado corretamente
        verify(horarioDisponivelRepository, times(1)).findByHorarioBetween(dataComeco, dataFinal);
    }

    @Test
    public void testBuscarHorariosDisponiveisPorDataFalha(){
        List<HorarioDisponivel> horarioDisponivelList = new ArrayList<>();

        LocalDate horarioDeBusca = LocalDate.now().plusDays(1);
        LocalDateTime dataComeco = horarioDeBusca.atStartOfDay();
        LocalDateTime dataFinal = horarioDeBusca.atTime(LocalTime.MAX);

        when(horarioDisponivelRepository.findByHorarioBetween(dataComeco, dataFinal)).thenReturn(horarioDisponivelList);

        List<HorarioDisponivel> result = horarioDisponivelService.buscarHorariosDisponiveisPorData(horarioDeBusca);

        // Verificar se a lista retornada está vazia
        assertTrue(result.isEmpty());

        // Verificar se o método findByHorarioBetween foi chamado corretamente
        verify(horarioDisponivelRepository, times(1)).findByHorarioBetween(dataComeco, dataFinal);


    }

    //atualizarHorarioDisponivel
    @Test
    public void testAtualizarHorarioDisponivelSucesso(){
        UUID horarioId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(3);
        int novaCapacidade = 10;
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(3), 3);
        horarioDisponivel.setId(horarioId);
        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.of(horarioDisponivel));
        when(horarioDisponivelRepository.save(horarioDisponivel)).thenReturn(horarioDisponivel);

        HorarioDisponivel result = horarioDisponivelService.atualizarHorarioDisponivel(horarioId, novoHorario, novaCapacidade);

        assertEquals(novoHorario, result.getHorario());
        assertEquals(novaCapacidade, result.getCapacidadeMaxima());

        verify(horarioDisponivelRepository, times(1)).save(horarioDisponivel);
    }

    @Test
    public void testAtualizarHorarioDisponivelNaoEncontrado(){
        UUID horarioId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(3);
        int novaCapacidade = 10;
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(3), 3);
        horarioDisponivel.setId(horarioId);
        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.atualizarHorarioDisponivel(horarioId, novoHorario, novaCapacidade);
        });

        assertEquals("Horário não encontrado", exception.getMessage());

        verify(horarioDisponivelRepository, never()).save(any(HorarioDisponivel.class));
    }

    @Test
    public void testAtualizarHorarioDisponivelPossuiConsultas(){
        UUID horarioId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(3);
        int novaCapacidade = 10;
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(3), 3);
        horarioDisponivel.setId(horarioId);
        horarioDisponivel.getConsultasAgendadas().add( new Consulta());

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.of(horarioDisponivel));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.atualizarHorarioDisponivel(horarioId, novoHorario, novaCapacidade);
        });

        assertEquals("Não é possível alterar um horário que possui consultas agendadas", exception.getMessage());

        verify(horarioDisponivelRepository, never()).save(any(HorarioDisponivel.class));
    }

//    removerHorarioDisponivel
    @Test
    public void testRemoverHorarioDisponivelSucesso(){
        UUID horarioId = UUID.randomUUID();
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel();
        horarioDisponivel.setId(horarioId);
        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.of(horarioDisponivel));

        horarioDisponivelService.removerHorarioDisponivel(horarioId);

        verify(horarioDisponivelRepository, times(1)).delete(horarioDisponivel);
    }

    @Test
    public void testRemoverHorarioDisponivelNaoEncontrado(){
        UUID horarioId = UUID.randomUUID();
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel();
        horarioDisponivel.setId(horarioId);

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.removerHorarioDisponivel(horarioId);
        });

        assertEquals("Horário não encontrado", exception.getMessage());

        verify(horarioDisponivelRepository, never()).delete(horarioDisponivel);
    }

    @Test
    public void testRemoverHorarioDisponivelPossuiConsultas(){
        UUID horarioId = UUID.randomUUID();
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel();
        horarioDisponivel.setId(horarioId);
        horarioDisponivel.getConsultasAgendadas().add(new Consulta());

        when(horarioDisponivelRepository.findById(horarioId)).thenReturn(Optional.of(horarioDisponivel));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.removerHorarioDisponivel(horarioId);
        });

        assertEquals("Não é possível excluir o horário. Existem consultas agendadas", exception.getMessage());

        verify(horarioDisponivelRepository, never()).delete(horarioDisponivel);
    }

}
