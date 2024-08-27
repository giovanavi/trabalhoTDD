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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class HorarioDisponiveisServiceTest {

    @InjectMocks
    private HorarioDisponivelService horarioDisponivelService;

    @Mock
    private MedicoService medicoService;

    @Mock
    private HorarioDisponivelRepository horarioDisponivelRepository;

    @Mock
    private ConsultaService consultaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSalvarHorarioDaConsulta_LimiteDeConsultasNaoAtingido() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setCapacidadeMaxima(2);
        horario.setConsultasAgendadas(new ArrayList<>());

        Consulta consulta = new Consulta();

        // Mock do repositório para retornar o horário disponível
        when(horarioDisponivelRepository.save(any(HorarioDisponivel.class))).thenReturn(horario);

        horarioDisponivelService.salvarHorarioDaConsulta(horario, consulta);

        assertEquals(1, horario.getConsultasAgendadas().size());
        assertTrue(horario.getConsultasAgendadas().contains(consulta));
        verify(horarioDisponivelRepository).save(horario); // Verifica se o save foi chamado
    }

    @Test
    void testSalvarHorarioDaConsulta_LimiteDeConsultasAtingido() {
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setCapacidadeMaxima(1);
        horario.setConsultasAgendadas(new ArrayList<>());
        horario.getConsultasAgendadas().add(new Consulta());

        Consulta consulta = new Consulta();

        // Mock do repositório para retornar o horário disponível
        when(horarioDisponivelRepository.save(any(HorarioDisponivel.class))).thenReturn(horario);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.salvarHorarioDaConsulta(horario, consulta);
        });

        assertEquals("Limite de consultas para este horário já atingido.", exception.getMessage());
        verify(horarioDisponivelRepository, never()).save(any(HorarioDisponivel.class)); // Verifica se o save não foi chamado
    }

    @Test
    void testAtualizarHorarioDisponivel() {
        UUID idHorario = UUID.randomUUID();
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setId(idHorario);
        horario.setHorario(LocalDateTime.now().minusDays(1));
        horario.setCapacidadeMaxima(10);
        horario.setConsultasAgendadas(new ArrayList<>());

        when(horarioDisponivelRepository.findById(idHorario)).thenReturn(Optional.of(horario));
        when(horarioDisponivelRepository.save(any(HorarioDisponivel.class))).thenReturn(horario);

        LocalDateTime novoHorario = LocalDateTime.now();
        int novaCapacidade = 20;
        HorarioDisponivel resultado = horarioDisponivelService.atualizarHorarioDisponivel(idHorario, novoHorario, novaCapacidade);

        assertEquals(novoHorario, resultado.getHorario());
        assertEquals(novaCapacidade, resultado.getCapacidadeMaxima());
        verify(horarioDisponivelRepository).save(any(HorarioDisponivel.class));
    }

    @Test
    void testAtualizarHorarioDisponivel_HorarioComConsultas() {
        UUID idHorario = UUID.randomUUID();
        HorarioDisponivel horario = new HorarioDisponivel();
        horario.setId(idHorario);
        horario.setHorario(LocalDateTime.now().minusDays(1));
        horario.setCapacidadeMaxima(10);
        horario.setConsultasAgendadas(new ArrayList<>());
        horario.getConsultasAgendadas().add(new Consulta());

        when(horarioDisponivelRepository.findById(idHorario)).thenReturn(Optional.of(horario));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            horarioDisponivelService.atualizarHorarioDisponivel(idHorario, LocalDateTime.now(), 20);
        });

        assertEquals("Não é possível alterar um horário que possui consultas agendadas", exception.getMessage());
    }

}
