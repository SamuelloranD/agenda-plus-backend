package com.agendaplus.professionals.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfissionalTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void normalizaEAtualizaDadosProfissionais() {
        var profissional = new Profissional(ID, " Ana ", " Corte ");

        assertThat(profissional.getId()).isEqualTo(ID);
        assertThat(profissional.getNome()).isEqualTo("Ana");
        assertThat(profissional.getEspecialidade()).isEqualTo("Corte");

        profissional.atualizarDados(" Beatriz ", " Manicure ");

        assertThat(profissional.getNome()).isEqualTo("Beatriz");
        assertThat(profissional.getEspecialidade()).isEqualTo("Manicure");
    }

    @Test
    void rejeitaNomeInvalido() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, null, "Corte"));
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, " ", "Corte"));
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, "a".repeat(151), "Corte"));
    }

    @Test
    void rejeitaEspecialidadeInvalida() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, "Ana", null));
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, "Ana", " "));
        assertThatIllegalArgumentException().isThrownBy(() -> new Profissional(ID, "Ana", "a".repeat(151)));
    }

    @Test
    void substituiHorariosEProtegeAListaExposta() {
        var profissional = new Profissional(ID, "Ana", "Corte");
        var segunda = horario(DayOfWeek.MONDAY);
        var terca = horario(DayOfWeek.TUESDAY);

        profissional.substituirHorarios(List.of(segunda));
        assertThat(profissional.getHorariosTrabalho()).containsExactly(segunda);

        profissional.substituirHorarios(List.of(terca));
        assertThat(profissional.getHorariosTrabalho()).containsExactly(terca);
        assertThatThrownBy(() -> profissional.getHorariosTrabalho().add(segunda))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void preservaCriacaoAoReaplicarCallbackEAtualizaModificacao() {
        var profissional = new Profissional(ID, "Ana", "Corte");

        profissional.onCreate();
        var criadoEm = profissional.getCreatedAt();
        var atualizadoEm = profissional.getUpdatedAt();
        profissional.onCreate();

        assertThat(profissional.getCreatedAt()).isEqualTo(criadoEm);
        assertThat(profissional.getUpdatedAt()).isEqualTo(atualizadoEm);

        profissional.onUpdate();

        assertThat(profissional.getUpdatedAt()).isAfterOrEqualTo(atualizadoEm);
    }

    private HorarioTrabalho horario(DayOfWeek dia) {
        return new HorarioTrabalho(UUID.randomUUID(), dia, LocalTime.of(9, 0), LocalTime.of(12, 0));
    }
}
