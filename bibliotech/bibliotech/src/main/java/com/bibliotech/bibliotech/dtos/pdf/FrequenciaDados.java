package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record FrequenciaDados(List<FrequenciaItem> items) implements DadosRelatorio {
    public static record FrequenciaItem(String aluno, String atividade, String data) {}
}
