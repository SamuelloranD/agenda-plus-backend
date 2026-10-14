package com.agendaplus.professionals.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

public record ProfissionalRequest(@NotBlank @Size(max = 150) String nome,
                                  @NotBlank @Size(max = 150) String especialidade,
                                  String imagem,
                                  @NotNull @Valid List<HorarioTrabalhoRequest> horariosTrabalho,
                                  Boolean ativo) {
    public void validarHorarios() {
        horariosTrabalho.forEach(HorarioTrabalhoRequest::validarPeriodo);
        var porDia = horariosTrabalho.stream().collect(Collectors.groupingBy(HorarioTrabalhoRequest::diaSemana));
        porDia.values().forEach(faixas -> {
            var ordenadas = faixas.stream().sorted(Comparator.comparing(HorarioTrabalhoRequest::inicio)).toList();
            for (int indice = 1; indice < ordenadas.size(); indice++) {
                if (ordenadas.get(indice).inicio().isBefore(ordenadas.get(indice - 1).fim())) {
                    throw new IllegalArgumentException("Há horários sobrepostos no mesmo dia.");
                }
            }
        });
    }
}
