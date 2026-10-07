package com.agendaplus.shared.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ImagemDataTest {
    private static final String PREFIXO = "data:image/jpeg;base64,";

    @Test
    void aceitaDataUrlRasterValida() {
        assertThatCode(() -> ImagemData.validar("data:image/jpeg;base64,YQ==")).doesNotThrowAnyException();
        assertThatCode(() -> ImagemData.validar("data:image/png;base64,YQ==")).doesNotThrowAnyException();
        assertThatCode(() -> ImagemData.validar("data:image/webp;base64,YQ==")).doesNotThrowAnyException();
    }

    @Test
    void aceitaImagemNula() {
        assertThatCode(() -> ImagemData.validar(null)).doesNotThrowAnyException();
    }

    @Test
    void aceitaDataUrlNoLimiteDeTamanho() {
        var imagem = PREFIXO + "A".repeat(ImagemData.MAX_DATA_URL_LENGTH - PREFIXO.length());

        assertThatCode(() -> ImagemData.validar(imagem)).doesNotThrowAnyException();
    }

    @Test
    void rejeitaSvg() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ImagemData.validar("data:image/svg+xml;base64,YQ=="));
    }

    @Test
    void rejeitaMimeNaoPermitido() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ImagemData.validar("data:image/gif;base64,YQ=="));
    }

    @Test
    void rejeitaDataUrlMalformada() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ImagemData.validar("data:image/jpeg,YQ=="));
    }

    @Test
    void rejeitaImagemAcimaDoLimite() {
        var imagem = PREFIXO + "A".repeat(ImagemData.MAX_DATA_URL_LENGTH - PREFIXO.length() + 1);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> ImagemData.validar(imagem));
    }
}
