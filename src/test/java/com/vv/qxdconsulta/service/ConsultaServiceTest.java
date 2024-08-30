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

import java.time.LocalDate;
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
    void testAgendarConsultaSucesso(){
        String crmMedico = "123456";
        String cpfPaciente = "12345678915";
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        String tipoConsulta = "Geral";

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "15975328415", "Ortopedia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", cpfPaciente, "+5588999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(dataHora, 10);

        //configurando mock
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenReturn(paciente);
        when(medicoService.buscarHorarioDisponivel(medico, dataHora)).thenReturn(horarioDisponivel);

        //executa o metodo agendar consulta
        Consulta consulta = consultaService.agendarConsulta(crmMedico, cpfPaciente, dataHora, tipoConsulta);

        //verificações
        assertNotNull(consulta);
        assertEquals(dataHora, consulta.getDataHora());
        assertEquals(tipoConsulta, consulta.getTipoConsulta());
        assertEquals(paciente, consulta.getPaciente());
        assertEquals(medico, consulta.getMedico());

        //verifica se a consulta foi adicionada ao horario disponivel
        boolean consultaAgendada = false;
        for (Consulta c: horarioDisponivel.getConsultasAgendadas()){
            if (c.equals(consulta)){
                consultaAgendada = true;
                break;
            }
        }
        assertTrue(consultaAgendada);

        //verifica se os metodos foram chamados foram chamados corretamente
        verify(horarioDisponivelService, times(1)).salvarHorarioDaConsulta(horarioDisponivel, consulta);
        verify(medicoService, times(1)).alterarMedico(any(UUID.class), any(Medico.class));
        verify(pacienteService, times(1)).atualizarPaciente(any(UUID.class), any(Paciente.class));

    }

    @Test
    void testAgendarConsultaMedicoNaoEncontrado(){
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
        verify(horarioDisponivelService, never()).salvarHorarioDaConsulta(any(HorarioDisponivel.class), any(Consulta.class));
        verify(medicoService, never()).alterarMedico(any(UUID.class), any(Medico.class));
    }

    @Test
    void testAgendarConsultaPacienteNaoEncontrado(){
        String crmMedico = "123456";
        String cpfPaciente = "12345678915";
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        String tipoConsulta = "Geral";

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "15975328415", "Ortopedia");

        //configura o mock para lançar a exceção quando o medico não for encontrado pelo metodo buscarMedicoPorCrm
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenThrow(new IllegalArgumentException("Paciente não encontrado com o CPF: " + cpfPaciente));

        //verifica se a exceção é lançada corretamente
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            consultaService.agendarConsulta(crmMedico, cpfPaciente, dataHora, tipoConsulta);
        });

        assertEquals("Paciente não encontrado com o CPF: " + cpfPaciente, exception.getMessage());

        verify(horarioDisponivelService, never()).salvarHorarioDaConsulta(any(HorarioDisponivel.class), any(Consulta.class));
        verify(medicoService, never()).alterarMedico(any(UUID.class), any(Medico.class));
        verify(pacienteService, never()).atualizarPaciente(any(UUID.class), any(Paciente.class));
    }

    @Test
    void testAgendarConsultaHorarioNaoDisponivel(){
        String crmMedico = "123456";
        String cpfPaciente = "12345678915";
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        String tipoConsulta = "Geral";

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "15975328415", "Ortopedia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", cpfPaciente, "+5588999999999");

        //
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenReturn(paciente);
        when(medicoService.buscarHorarioDisponivel(medico, dataHora)).thenThrow(new IllegalArgumentException("Horário não disponível"));

        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
           consultaService.agendarConsulta(crmMedico, cpfPaciente, dataHora, tipoConsulta);
        });

        assertEquals("Horário não disponível", exception.getMessage());

        verify(horarioDisponivelService, never()).salvarHorarioDaConsulta(any(HorarioDisponivel.class), any(Consulta.class));
        verify(medicoService, never()).alterarMedico(any(UUID.class), any(Medico.class));
        verify(pacienteService, never()).atualizarPaciente(any(UUID.class), any(Paciente.class));
    }

    //VERIFICAR NOVAMENTE METODOS REALACIONADDAS A CONSULTA E CAPCACIDADE (CONSULTA, HORARIOS DISPONIVEIS)
    @Test
    void testAgendarConsultaExcedendoCapacidade(){
        String crmMedico = "123456";
        String cpfPaciente = "12345678915";
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1);
        LocalDateTime outroHorario = LocalDateTime.now();
        System.out.println("DataHora: " + dataHora.toLocalDate());
        System.out.println("Outro Horario: " + dataHora.toLocalDate());
        String tipoConsulta = "Geral";

        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", crmMedico, "15975328415", "Ortopedia");
        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", cpfPaciente, "+5588999999999");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(dataHora, 1);

        Consulta consulta = new Consulta(UUID.randomUUID(), dataHora, "Geral", paciente, medico);

        horarioDisponivel.getConsultasAgendadas().add(consulta);

        System.out.println("Verificando pode agendar: " + horarioDisponivel.podeAgendar());
        horarioDisponivelService.salvarHorarioDaConsulta(horarioDisponivel, consulta);

        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenReturn(medico);
        System.out.println("When Medico: " + medico.getId());
        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenReturn(paciente);
        System.out.println("When Paciente: " + paciente.getId());
        when(medicoService.buscarHorarioDisponivel(medico, dataHora)).thenReturn(horarioDisponivel);
        System.out.println("When Horario disponivel: " + horarioDisponivel.getHorario().toLocalDate());

        Exception exception = assertThrows(IllegalArgumentException.class, ()-> {
            consultaService.agendarConsulta(crmMedico, cpfPaciente, dataHora, tipoConsulta);
        });

        assertEquals("Limite de consultas para este horário já atingido.", exception.getMessage());

        verify(horarioDisponivelService, never()).salvarHorarioDaConsulta(any(HorarioDisponivel.class), any(Consulta.class));
        verify(medicoService, never()).alterarMedico(any(UUID.class), any(Medico.class));
        verify(pacienteService, never()).atualizarPaciente(any(UUID.class), any(Paciente.class));
    }

    //buscarConsultasPorMedico
    @Test
    void testBuscarConsultaPorMedicoSucesso(){
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
        assertEquals(consultaList, result);

        verify(consultaRepository, times(1)).findByMedico(medico);
    }

    @Test
    void testBuscarConsultaPorMedicoNaoEncontrado(){
        String crmMedico = "123456";

        //mock para lançar exceção quando o medico não for encontrado
        when(medicoService.buscarMedicoPorCrm(crmMedico)).thenThrow(new IllegalArgumentException("Médico não encontrado"));

        Exception exception = assertThrows(IllegalArgumentException.class, ()->{
           consultaService.buscarConsultasPorMedico(crmMedico);
        });

        assertEquals("Médico não encontrado", exception.getMessage());

        verify(consultaRepository, never()).findByMedico(any(Medico.class));
    }

    @Test
    void testBuscarConsultaPorMedicoSemConsulta(){
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

        verify(consultaRepository, times(1)).findByMedico(medico);

    }

    //buscarConsultasPorPaciente
    @Test
    void testBuscarConsultaPorPacienteSucesso(){
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
        assertEquals(consultaList, result);

        verify(pacienteService, times(1)).buscarPacientePorCpf(cpfPaciente);
        verify(consultaRepository, times(1)).findByPaciente(paciente);
    }

    @Test
    void testBuscarConsultaPorPacienteNaoEncontrado(){
        String cpfPaciente = "12345678915";

        when(pacienteService.buscarPacientePorCpf(cpfPaciente)).thenThrow(new IllegalArgumentException("Paciente não encontrado"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            consultaService.buscarConsultasPorPaciente(cpfPaciente);
        });

        assertEquals("Paciente não encontrado", exception.getMessage());

        verify(consultaRepository, never()).findByPaciente(any(Paciente.class));
    }

    @Test
    void testBuscarConsultaPorPacienteSemConsulta(){
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

        verify(consultaRepository, times(1)).findByPaciente(paciente);

    }

    //buscaConsultasPorData
    @Test
    void testBuscarConsultaPorDataSucesso(){
        UUID medicoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now();
        LocalDate data = dataHora.toLocalDate();
        Medico medico = new Medico(medicoId, "Dr. Silva", "CRM1234", "12345678915", "Pediatra");
        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(dataHora, 5);
        List<Consulta> consultaList = new ArrayList<>();
        consultaList.add(new Consulta(UUID.randomUUID(), dataHora, "Geral", null, medico, horarioDisponivel));

        horarioDisponivel.getConsultasAgendadas().addAll(consultaList);
        medico.getHorarioDisponivel().add(horarioDisponivel);

        //mock para retornar o medico
        when(medicoService.buscarMedicoPorId(medicoId)).thenReturn(medico);

        List<Consulta> result = consultaService.buscaConsultasPorData(medicoId, dataHora);

        assertNotNull(result);
        assertEquals(consultaList.size(), result.size());
        assertEquals(consultaList, result);

        verify(medicoService, times(1)).buscarMedicoPorId(medicoId);
    }

    @Test
    void testBuscarConsultaPorDataComMedicoNaoEncnotrado(){
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
    void testBuscarConsultaPorDataSemConsultas(){
        UUID medicoId = UUID.randomUUID();
        LocalDateTime dataHora = LocalDateTime.now();
        Medico medico = new Medico(medicoId, "Dr. Silva", "CRM12345", "12346579885", "Ortopedista");

        HorarioDisponivel horarioDisponivel = new HorarioDisponivel(dataHora, 5);

        when(medicoService.buscarMedicoPorId(medicoId)).thenReturn(medico);

        List<Consulta> result = consultaService.buscaConsultasPorData(medicoId, dataHora);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(medicoService, times(1)).buscarMedicoPorId(medicoId);
    }

    //alterarConsulta
    //rever metodos de consulta e entender melhor a relação entre HD e Consulta e Medico
//    @Test
//    void testAlterarConsultaSucesso(){
//        UUID consultId = UUID.randomUUID();
//        LocalDateTime novaDataHora = LocalDateTime.now().plusDays(1);
//        String novoTipoConsulta = "Cardiologia";
//
//        Medico medico = new Medico(UUID.randomUUID(), "Dr. Silva", "CRM12345", "12346579815", "Cardiologista");
//        Paciente paciente = new Paciente(UUID.randomUUID(), "José Humberto", "josehumberto@email.com", "12345678948", "+55889999999999");
//        HorarioDisponivel horarioAntigo = new HorarioDisponivel(LocalDateTime.now(), 5);
//        HorarioDisponivel novoHorario = new HorarioDisponivel(novaDataHora, 5);
//
//        Consulta consultaExistente = new Consulta(consultId, LocalDateTime.now(), "Ortopedia", paciente, medico, horarioAntigo);
//
//        when(consultaRepository.findById(consultId)).thenReturn(Optional.of(consultaExistente));
//        when(medicoService.buscarHorarioDisponivel(medico, novaDataHora)).thenReturn(novoHorario);
//
//        Consulta result = consultaService.alterarConsulta(consultId, novaDataHora, novoTipoConsulta);
//
//        assertNotNull(result);
//        assertEquals(novaDataHora, result.getDataHora());
//        assertEquals(novoTipoConsulta, result.getTipoConsulta());
//        assertEquals(novoHorario, result.getHorarioDisponivel());
//
//        verify(consultaRepository, times(1)).save(result);
//        verify(medicoService, times(1)).alterarMedico(medico.getId(), medico);
//        verify(pacienteService, times(1)).atualizarPaciente(paciente.getId(), paciente);
//    }

    @Test
    void testAlterarConsultaNaoEncontrada(){

    }

    @Test
    void testAlterarConsultaComHorarioNaoDisponivel(){

    }

    @Test
    void testAlterarConsultaExcedendoCapacidade(){

    }

    //removerConsulta
    @Test
    void testRemoverConsultaSucesso(){

    }

    @Test
    void testRemoverConsultaNaoEncontrada(){

    }

}