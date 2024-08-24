package com.vv.qxdconsulta.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "medicos")
public class Medico {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;
    @Column(nullable = false, length = 15)
    private String crm;
    @Column(nullable = false, length = 11)
    private String cpf;
    @Column(nullable = false, length = 50)
    private String especialização;

    @OneToMany(mappedBy = "medico", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioDisponivel> horarioDisponivel = new ArrayList<>();

    public Medico(UUID id, String nome, String crm, String cpf, String especialização) {
        this.id = id;
        this.nome = nome;
        this.crm = crm;
        this.cpf = cpf;
        this.especialização = especialização;
    }
}
