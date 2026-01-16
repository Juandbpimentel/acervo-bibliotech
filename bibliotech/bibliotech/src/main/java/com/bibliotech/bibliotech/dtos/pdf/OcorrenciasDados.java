package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record OcorrenciasDados(List<OcorrenciaItem> items) implements DadosRelatorio {
    public static record OcorrenciaItem(String aluno, String registradaPor, String detalhes, String data) {}
}
