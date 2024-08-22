package com.vv.qxdconsulta.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@Entity
@Table(name = "horarios_disponiveis")
public class HorarioDisponivel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false)
    private LocalDateTime horario;
    @Column(nullable = false)
    private int capacidadeMaxima;

    @OneToMany(mappedBy = "horarioDisponivel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consulta> consultasAgendadas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    public boolean podeAgendar() {
        return consultasAgendadas.size() < capacidadeMaxima;
    }
}
