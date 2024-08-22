package com.vv.qxdconsulta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QxdConsultaApplication {

	public static void main(String[] args) {
		SpringApplication.run(QxdConsultaApplication.class, args);
	}

//	ver se quando salvar os pacientes, médicos e consulta o RandomUUID tem que ser chamado, ou se o banco de dados gera ele sozinho e como devo colocar no model para que isso aconteça.

}
