package com.agendaplus.identity.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {
    private static final Pattern FORMATO = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
            + "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+");

    public Email {
        if (valor == null) throw new IllegalArgumentException("Email obrigatório.");
        valor = valor.strip().toLowerCase(Locale.ROOT);
        if (valor.length() > 254 || !FORMATO.matcher(valor).matches()
                || valor.indexOf('@') > 64) {
            throw new IllegalArgumentException("Email inválido.");
        }
    }
}
