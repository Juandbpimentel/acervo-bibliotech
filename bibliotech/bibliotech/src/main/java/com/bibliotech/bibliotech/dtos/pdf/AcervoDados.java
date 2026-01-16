package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record AcervoDados(List<AcervoItem> items) implements DadosRelatorio {
    public static record AcervoItem(String titulo, String autor, Integer quantidade) {}
}
