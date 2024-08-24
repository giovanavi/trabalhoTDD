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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        assertEquals("CPF já cadastrado: 12345678901", exception.getMessage());

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
        // Cria um horário atual para ser usado no teste
        LocalDateTime horarioAtual = LocalDateTime.now();

        // Cria uma instância de HorarioDisponivel com o horário atual e capacidade máxima
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(horarioAtual, 10);

        // Cria uma instância de Medico e define os horários disponíveis
        Medico medico = new Medico();
        medico.setHorarioDisponivel(List.of(horarioDisponivel));
        horarioDisponivel.setMedico(medico);  // Associa o horário ao médico

        // Executa o método buscarHorarioDisponivel
        HorarioDisponivel resultado = medicoService.buscarHorarioDisponivel(medico, horarioAtual);

        // Verifica se o resultado não é nulo e se o horário é o esperado
        assertNotNull(resultado);
        assertEquals(horarioAtual, resultado.getHorario());
    }


}