package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.model.Consulta;
import com.vv.qxdconsulta.model.HorarioDisponivel;
import com.vv.qxdconsulta.model.Medico;
import com.vv.qxdconsulta.model.Paciente;
import com.vv.qxdconsulta.repository.ConsultaRepository;
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
import static org.mockito.Mockito.*;


class ConsultaServiceTest {

    @Mock
    MedicoService medicoService;

    @Mock
    PacienteService pacienteService;

    @Mock
    HorarioDisponivelService horarioDisponivelService;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

    }

    //agendarConsulta
    @Test
    public void testAgendarConsultaSucesso(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "15975328415", "Ortopedia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "15975348615", "+5588999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5);

        Consulta consulta = new Consulta(UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Consulta Geral", paciente, medico);
        consulta.setHorarioDisponivel(horarioDisponivel);

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(paciente.getCpf())).thenReturn(paciente);
        when(horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horarioDisponivel.getHorario())).thenReturn(horarioDisponivel);
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consulta);

        Consulta result = consultaService.agendarConsulta(medico.getCrm(), paciente.getCpf(), horarioDisponivel.getHorario(), consulta.getTipoConsulta());

        assertNotNull(result);
        assertEquals(medico, result.getMedico());
        assertEquals(paciente, result.getPaciente());
        assertEquals(horarioDisponivel, result.getHorarioDisponivel());

        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    public void testAgendarConsultaMedicoNaoEncontrado(){
        String crmMedico = "123456";
        String cpfPaciente = "12345678915";
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        String tipoConsulta = "Geral";

        //configura o mock para lançar a exceção quando o medico não for encontrado pelo metodo buscarMedicoPorCrm
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenThrow(new IllegalArgumentException("Médico não encontrado com o CRM: " + crmMedico));

        //verifica se a exceção é lançada corretamente
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
           consultaService.agendarConsulta(crmMedico, cpfPaciente, dataHora, tipoConsulta);
        });

        assertEquals("Médico não encontrado com o CRM: " + crmMedico, exception.getMessage());

        verify(pacienteService, never()).buscarPacientePorCpf(anyString());
        verify(horarioDisponivelService, never()).salvarMudancaDeHorario(any(HorarioDisponivel.class));
        verify(horarioDisponivelService, never()).verificarDisponibilidadeDeConsulta(any(HorarioDisponivel.class));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    public void testAgendarConsultaPacienteNaoEncontrado(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. João", "12345", "11122233344", "Cardiologia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Maria Silva", "maria@example.com", "11122233344", "11999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5);
        String tipoConsulta = "Geral";

        //configura o mock para lançar a exceção quando o medico não for encontrado pelo metodo buscarMedicoPorCrm
        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(paciente.getCpf())).thenThrow(new IllegalArgumentException("Paciente não encontrado com o CPF: " + paciente.getCpf()));

        //verifica se a exceção é lançada corretamente
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            consultaService.agendarConsulta(medico.getCrm(), paciente.getCpf(), horarioDisponivel.getHorario(), tipoConsulta);
        });

        assertEquals("Paciente não encontrado com o CPF: " + paciente.getCpf(), exception.getMessage());

        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    public void testAgendarConsultaHorarioNaoDisponivel(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. João", "12345", "11122233344", "Cardiologia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Maria Silva", "maria@example.com", "11122233344", "11999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5);

        Consulta consulta = new Consulta(UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Consulta Geral", paciente, medico);
        consulta.setHorarioDisponivel(horarioDisponivel);

        //mock
        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(paciente.getCpf())).thenReturn(paciente);
        when(horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horarioDisponivel.getHorario())).thenReturn(horarioDisponivel);
        doThrow(new IllegalArgumentException("Limite de consultas para este horário já atingido."))
                .when(horarioDisponivelService).verificarDisponibilidadeDeConsulta(horarioDisponivel);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
           consultaService.agendarConsulta(medico.getCrm(), paciente.getCpf(), horarioDisponivel.getHorario(), consulta.getTipoConsulta());
        });

        assertEquals("Limite de consultas para este horário já atingido.", exception.getMessage());

        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    public void testestAgendarConsultaHorarioNaoEncontrado(){
        Medico medico = new Medico(UUID.randomUUID(), "Dr. João", "12345", "11122233344", "Cardiologia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "Maria Silva", "maria@example.com", "11122233344", "11999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now().plusDays(1), 5);
        String tipoConsulta = "Geral";

        when(medicoService.buscarMedicoPorCrm(medico.getCrm())).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(paciente.getCpf())).thenReturn(paciente);
        //mock para não encontrar o horario
        when(horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), horarioDisponivel.getHorario())).thenThrow( new IllegalArgumentException("O médico não tem esse horário disponível"));

        Exception exception = assertThrows( IllegalArgumentException.class, () ->{
           consultaService.agendarConsulta(medico.getCrm(), paciente.getCpf(), horarioDisponivel.getHorario(), tipoConsulta);
        });

        assertEquals("O médico não tem esse horário disponível", exception.getMessage());

        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    //buscarConsultaPorId
    @Test
    public void testBuscarConsultaPorIdSucesso(){
        UUID consultaId = UUID.randomUUID();
        Consulta consulta = new Consulta();
        consulta.setId(consultaId);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));

        Consulta result = consultaService.buscarConsultaPorId(consultaId);

        assertNotNull(result);
        assertEquals(consultaId, result.getId());

        verify(consultaRepository, times(1)).findById(consultaId);
    }

    @Test
    public void testBuscarConsultaPorIdNaoEncontrado(){
        UUID consultaId = UUID.randomUUID();
        Consulta consulta = new Consulta();
        consulta.setId(consultaId);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.buscarConsultaPorId(consultaId);
        });

        assertEquals("Consulta não encontrada", exception.getMessage());

        verify(consultaRepository, times(1)).findById(consultaId);
    }

    //buscarConsultasPorMedico
    @Test
    public void testBuscarConsultaPorMedicoSucesso(){
        String crmMedico = "123456";
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "15975385215", "Ortopedia");

        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Ortopedia", null, medico,null));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Geral", null, medico,null));

        //mock de retorno de medico e consultas
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        when(consultaRepository.findByMedico(medico)).thenReturn(consultaList);

        //chamando o metodo do service
        List<Consulta> result = consultaService.buscarConsultasPorMedico(crmMedico);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.containsAll(consultaList));

        verify(consultaRepository, times(1)).findByMedico(medico);
        verify(medicoService, times(1)).buscarMedicoPorCrm(medico.getCrm());
    }

    @Test
    public void testBuscarConsultaPorMedicoNaoEncontrado(){
        String crmMedico = "123456";

        //mock para lançar exceção quando o medico não for encontrado
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenThrow(new IllegalArgumentException("Médico não encontrado com o CRM: " + crmMedico));

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.buscarConsultasPorMedico(crmMedico);
        });

        assertEquals("Médico não encontrado com o CRM: " + crmMedico, exception.getMessage());

        verify(consultaRepository, never()).findByMedico(any(Medico.class));
    }

    @Test
    public void testBuscarConsultaPorMedicoSemConsulta(){
        String crmMedico = "123456";
        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "159753852450", "Geral");

        //mock para rertornar o medico com lista de consultas vazia
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        when(consultaRepository.findByMedico(medico)).thenReturn(new ArrayList<>());

        //executa o metodo
        List<Consulta> result = consultaService.buscarConsultasPorMedico(crmMedico);

        //verificações
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoService, times(1)).buscarMedicoPorCrm(crmMedico);
        verify(consultaRepository, times(1)).findByMedico(medico);
    }

    //buscarConsultasPorPaciente
    @Test
    public void testBuscarConsultaPorPacienteSucesso(){
        String cpfPaciente = "12345678915";
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", cpfPaciente, "+5588999999999");

        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Geral", paciente, null, null));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().plusDays(1), "Pediatria", paciente, null, null));

        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenReturn(paciente);
        when(consultaRepository.findByPaciente(paciente)).thenReturn(consultaList);

        List<Consulta> result = consultaService.buscarConsultasPorPaciente(cpfPaciente);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.containsAll(consultaList));

        verify(pacienteService, times(1)).buscarPacientePorCpf(cpfPaciente);
        verify(consultaRepository, times(1)).findByPaciente(paciente);
    }

    @Test
    public void testBuscarConsultaPorPacienteNaoEncontrado(){
        String cpfPaciente = "12345678915";

        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenThrow(new IllegalArgumentException("Paciente não encontrado com o CPF: "+ cpfPaciente));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            consultaService.buscarConsultasPorPaciente(cpfPaciente);
        });

        assertEquals("Paciente não encontrado com o CPF: "+cpfPaciente, exception.getMessage());

        verify(consultaRepository, never()).findByPaciente(any(Paciente.class));
    }

    @Test
    public void testBuscarConsultaPorPacienteSemConsulta(){
        String cpfPaciente = "12345678958";
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", cpfPaciente, "+5588999999999");

        //mock para rertornar o paciente com lista de consultas vazia
        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenReturn(paciente);
        when(consultaRepository.findByPaciente(paciente)).thenReturn(new ArrayList<>());

        //executa o metodo
        List<Consulta> result = consultaService.buscarConsultasPorPaciente(cpfPaciente);

        //verificações
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(pacienteService, times(1)).buscarPacientePorCpf(cpfPaciente);
        verify(consultaRepository, times(1)).findByPaciente(paciente);
    }

    //buscaConsultasPorData
    @Test
    public void testBuscarConsultaPorDataSucesso(){
        UUID medicoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now();
        Medico medico = new Medico(medicoId, "Dr. Silva", "CRM1234", "12345678915", "Pediatra");

        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), dataHora, "Geral", new Paciente(), medico));
        consultaList.add(new Consulta(UUID.randomUUID(), dataHora, "Geral", new Paciente(), medico));

        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(dataHora, 5);
        horarioDisponivel.getConsultasAgendadas().addAll(consultaList);

        medico.getHorarioDisponivel().add(horarioDisponivel);

        //mock para retornar o medico
        when(medicoService.buscarMedicoPorId(medicoId)).thenReturn(medico);

        List<Consulta> result = consultaService.buscaConsultasPorData(medicoId, dataHora);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.containsAll(consultaList));

        verify(medicoService, times(1)).buscarMedicoPorId(medicoId);
    }

    @Test
    public void testBuscarConsultaPorDataComMedicoNaoEncnotrado(){
        UUID medicoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now();

        when(medicoService.buscarMedicoPorId(medicoId)).thenThrow(new IllegalArgumentException("Médico não encontrado"));

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.buscaConsultasPorData(medicoId, dataHora);
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(medicoService, times(1)).buscarMedicoPorId(medicoId);
    }

    @Test
    public void testBuscarConsultaPorDataSemConsultas(){
        UUID medicoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now();
        Medico medico = new Medico(medicoId, "Dr. Silva", "CRM12345", "12346579885", "Ortopedista");

        medico.setHorarioDisponivel(new ArrayList<>());

        when(medicoService.buscarMedicoPorId(medicoId)).thenReturn(medico);

        List<Consulta> result = consultaService.buscaConsultasPorData(medicoId, dataHora);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoService, times(1)).buscarMedicoPorId(medicoId);
    }

    //buscarConsultasPorEspecializacao
    @Test
    public void testBuscarConsultaPorEspecializacaoSuceso(){
        String especializacao = "Ortopedia";
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12354", "15975385215", especializacao));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM65432", "17425896315", especializacao));
        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Consulta Geral", new Paciente(), medicoList.get(0)));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().plusDays(2), "Consulta Especializada", new Paciente(), medicoList.get(1)));

        when(medicoService.buscarMedicosPorEspecializacao(especializacao)).thenReturn(medicoList);
        when(consultaRepository.findByMedico(medicoList.get(0))).thenReturn(consultaList.subList(0,1));
        when(consultaRepository.findByMedico(medicoList.get(1))).thenReturn(consultaList.subList(1,2));

        List<Consulta> result = consultaService.buscarConsultasPorEspecializacao(especializacao);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.containsAll(consultaList));

        verify(medicoService, times(1)).buscarMedicosPorEspecializacao(especializacao);
        verify(consultaRepository, times(1)).findByMedico(medicoList.get(0));
        verify(consultaRepository, times(1)).findByMedico(medicoList.get(1));
    }

    @Test
    public void testBuscarConsultaPorEspecializacaoSemMedicosComEspecializacao(){
        String especializacao = "Ortopedia";

        when(medicoService.buscarMedicosPorEspecializacao(especializacao)).thenReturn(new ArrayList<>());

        List<Consulta> result = consultaService.buscarConsultasPorEspecializacao(especializacao);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoService, times(1)).buscarMedicosPorEspecializacao(especializacao);
        verify(consultaRepository, never()).findByMedico(any(Medico.class));
    }

    @Test
    public void testBuscarConsultaPorEspecializacaoMedicosSemConsultas(){
        String especializacao = "Ortopedia";
        List<Medico> medicoList = new ArrayList<>();
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12354", "15975385215", especializacao));
        medicoList.add(new Medico(UUID.randomUUID(), "Dr. Sousa", "CRM65432", "17425896315", especializacao));

        when(medicoService.buscarMedicosPorEspecializacao(especializacao)).thenReturn(medicoList);
        when(consultaRepository.findByMedico(medicoList.get(0))).thenReturn(new ArrayList<>());
        when(consultaRepository.findByMedico(medicoList.get(1))).thenReturn(new ArrayList<>());

        List<Consulta> result = consultaService.buscarConsultasPorEspecializacao(especializacao);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoService, times(1)).buscarMedicosPorEspecializacao(especializacao);
        verify(consultaRepository, times(1)).findByMedico(medicoList.get(0));
        verify(consultaRepository, times(1)).findByMedico(medicoList.get(1));
    }

    //buscarConsultasPorIntervaloDeDatas
    @Test
    public void testBuscarConsultaPorIntervaloDeDatasSucesso(){
        LocalDateTime dataInico = LocalDateTime.now().minusDays(1);
        LocalDateTime dataFinal = LocalDateTime.now().plusDays(1);

        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now(), "Consulta Geral", new Paciente(), new Medico()));
        consultaList.add(new Consulta(UUID.randomUUID(), LocalDateTime.now().plusHours(1), "Consulta Geral", new Paciente(), new Medico()));

        when(consultaRepository.findByDataHoraBetween(dataInico, dataFinal)).thenReturn(consultaList);

        List<Consulta> result = consultaService.buscarConsultasPorIntervaloDeDatas(dataInico, dataFinal);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertTrue(result.containsAll(consultaList));

        verify(consultaRepository, times(1)).findByDataHoraBetween(dataInico, dataFinal);
    }

    @Test
    public void testBuscarConsultaPorIntervaloDeDatasSemConsultas(){
        LocalDateTime dataInico = LocalDateTime.now().minusDays(1);
        LocalDateTime dataFinal = LocalDateTime.now().plusDays(1);

        when(consultaRepository.findByDataHoraBetween(dataInico, dataFinal)).thenReturn(new ArrayList<>());

        List<Consulta> result = consultaService.buscarConsultasPorIntervaloDeDatas(dataInico, dataFinal);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(consultaRepository, times(1)).findByDataHoraBetween(dataInico, dataFinal);
    }

    //alterarHorarioDaConsulta
    @Test
    public void alterarHorarioConsultaSucesso(){
        UUID consultaId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(2);

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        HorarioDisponivel horarioAntigo = new HorarioDisponivel(LocalDateTime.now(), 5);
        HorarioDisponivel novoHorarioDisponivel = new HorarioDisponivel(novoHorario, 5);

        Consulta consulta = new Consulta(consultaId, LocalDateTime.now(), "Consulta Geral", new Paciente(), medico);
        consulta.setHorarioDisponivel(horarioAntigo);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), novoHorario)).thenReturn(novoHorarioDisponivel);

        Consulta result = consultaService.alterarHorarioDaConsulta(consultaId, novoHorario);

        //verifica se o horario antigo foi removido
        assertFalse(horarioAntigo.getConsultasAgendadas().contains(consulta));
        verify(horarioDisponivelService, times(1)).salvarMudancaDeHorario(horarioAntigo);

        //verifica se a consulta foi adicionada ao novo horario
        assertTrue(novoHorarioDisponivel.getConsultasAgendadas().contains(consulta));

        //verifica se a consulta foi atualizada
        assertEquals(novoHorario, result.getDataHora());
        assertEquals(novoHorarioDisponivel, result.getHorarioDisponivel());

        verify(consultaRepository, times(1)).save(consulta);
        verify(horarioDisponivelService, times(1)).verificarDisponibilidadeDeConsulta(novoHorarioDisponivel);
    }

    @Test
    public void testAlterarHorarioConsultaNaoEncontrada(){
        UUID consultaId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(2);

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        HorarioDisponivel horarioAntigo = new HorarioDisponivel(LocalDateTime.now(), 5);

        Consulta consulta = new Consulta(consultaId, LocalDateTime.now(), "Consulta Geral", new Paciente(), medico);
        consulta.setHorarioDisponivel(horarioAntigo);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.alterarHorarioDaConsulta(consultaId, novoHorario);
        });

        assertEquals("Consulta não encontrada", exception.getMessage());

        verify(horarioDisponivelService, never()).buscarHorarioPorMedico(anyString(), any(LocalDateTime.class));
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    @Test
    public void testAlterarHorarioConsultaComHorarioNaoDisponivel(){
        UUID consultaId = UUID.randomUUID();
        LocalDateTime novoHorario = LocalDateTime.now().plusDays(2);

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        HorarioDisponivel horarioAntigo = new HorarioDisponivel(LocalDateTime.now(), 5);
        HorarioDisponivel novoHorarioDisponivel = new HorarioDisponivel(novoHorario, 5);

        Consulta consulta = new Consulta(consultaId, LocalDateTime.now(), "Consulta Geral", new Paciente(), medico);
        consulta.setHorarioDisponivel(horarioAntigo);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(horarioDisponivelService.buscarHorarioPorMedico(medico.getCrm(), novoHorario)).thenReturn(novoHorarioDisponivel);
        doThrow(new IllegalArgumentException("Limite de consultas para esté horário já atingido"))
                .when(horarioDisponivelService).verificarDisponibilidadeDeConsulta(novoHorarioDisponivel);

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
            consultaService.alterarHorarioDaConsulta(consultaId, novoHorario);
        });

        assertEquals("Limite de consultas para esté horário já atingido", exception.getMessage());

        verify(horarioDisponivelService,times(1)).verificarDisponibilidadeDeConsulta(novoHorarioDisponivel);
        verify(consultaRepository, never()).save(any(Consulta.class));
    }

    //removerConsulta
    @Test
    public void testRemoverConsultaSucesso(){
        UUID consultaId = UUID.randomUUID();

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12345678951", "Cardiologia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José", "jose@email.com", "12345678915", "+5588999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(LocalDateTime.now(), 5);

        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());
        medico.setHorarioDisponivel(List.of(horarioDisponivel));
        paciente.setConsultas(new ArrayList<>());

        Consulta consulta = new Consulta(consultaId, LocalDateTime.now(), "Consulta Geral", paciente, medico);
        consulta.setHorarioDisponivel(horarioDisponivel);

        horarioDisponivel.getConsultasAgendadas().add(consulta);
        paciente.getConsultas().add(consulta);


        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        consultaService.removerConsulta(consultaId);

        assertFalse(horarioDisponivel.getConsultasAgendadas().contains(consulta));
        assertFalse(paciente.getConsultas().contains(consulta));

        verify(medicoService, times(1)).alterarMedico(medico.getId(), medico);
        verify(pacienteService, times(1)).atualizarPaciente(paciente.getId(), paciente);
        verify(consultaRepository, times(1)).delete(consulta);
    }

    @Test
    public void testRemoverConsultaNaoEncontrada(){
        UUID consultaId = UUID.randomUUID();

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.removerConsulta(consultaId);
        });

        assertEquals("Consulta não encontrada", exception.getMessage());

        verify(medicoService, never()).alterarMedico(any(UUID.class), any(Medico.class));
        verify(pacienteService, never()).atualizarPaciente(any(UUID.class), any(Paciente.class));
        verify(consultaRepository, never()).delete(any(Consulta.class));
    }

    @Test
    public void testRemoverConsultaNaoAssociadaAoHorario(){
        UUID consultaId = UUID.randomUUID();
        LocalDateTime horarioConsulta = LocalDateTime.now();

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM13245", "1597515948", "Cardiologia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José", "jose@email.com", "12345678901", "999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(horarioConsulta, 5);

        horarioDisponivel.setConsultasAgendadas(new ArrayList<>());
        medico.setHorarioDisponivel(List.of(horarioDisponivel));
        paciente.setConsultas(new ArrayList<>());

        Consulta consulta = new Consulta(consultaId, horarioConsulta, "Consulta Geral", paciente, medico);
        consulta.setHorarioDisponivel(horarioDisponivel);

        paciente.getConsultas().add(consulta);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));

        consultaService.removerConsulta(consultaId);

        //verifica se a consulta foi removida
        assertFalse(paciente.getConsultas().contains(consulta));
        //verifica se a consulta não está presente
        assertFalse(horarioDisponivel.getConsultasAgendadas().contains(consulta));

        verify(medicoService,times(1)).alterarMedico(medico.getId(), medico);
        verify(pacienteService, times(1)).atualizarPaciente(paciente.getId(), paciente);

        verify(consultaRepository, times(1)).delete(consulta);
    }

}