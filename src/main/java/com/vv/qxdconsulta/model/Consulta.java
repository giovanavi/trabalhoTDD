package com.vv.qxdconsulta.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false)
    private LocalDateTime dataHora;
    @Column(nullable = false, length = 50)
    private String tipoConsulta;

    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne
    @JoinColumn(name = "horario_id", nullable = false)
    private HorarioDisponivel horarioDisponivel;

    public Consulta(UUID id, LocalDateTime dataHora, String tipoConsulta, Paciente paciente, Medico medico) {
        this.id = id;
        this.dataHora = dataHora;
        this.tipoConsulta = tipoConsulta;
        this.paciente = paciente;
        this.medico = medico;
    }
}
