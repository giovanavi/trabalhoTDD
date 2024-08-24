package com.vv.qxdconsulta.service;

import com.vv.qxdconsulta.repository.ConsultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    //agendarConsulta
    //buscarConsultasPorMedico
    //buscarConsultasPorPaciente
    //buscaConsultasPorData
    //alterarConsulta
    //removerConsulta


}