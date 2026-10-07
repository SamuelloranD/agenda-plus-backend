package com.agendaplus.shared.domain.model;

import java.util.regex.Pattern;

public final class ImagemData {
    public static final int MAX_DATA_URL_LENGTH = 2_800_000;

    private static final Pattern DATA_URL_RASTER = Pattern.compile(
            "^data:image/(jpeg|png|webp);base64,[A-Za-z0-9+/]+={0,2}$");

    private ImagemData() {}

    public static void validar(String imagem) {
        if (imagem == null) return;
        if (imagem.length() > MAX_DATA_URL_LENGTH) {
            throw new IllegalArgumentException("A imagem deve ter no mÃ¡ximo 2 MiB.");
        }
        if (!DATA_URL_RASTER.matcher(imagem).matches()) {
            throw new IllegalArgumentException("A imagem deve ser uma Data URL JPEG, PNG ou WebP vÃ¡lida.");
        }
    }
}
