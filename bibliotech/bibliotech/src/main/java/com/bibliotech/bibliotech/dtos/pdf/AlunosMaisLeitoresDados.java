package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record AlunosMaisLeitoresDados(List<AlunoItem> items) implements DadosRelatorio {
    public static record AlunoItem(String nome, String serie, String turma, Long quantidadeLeituras) {}
}
