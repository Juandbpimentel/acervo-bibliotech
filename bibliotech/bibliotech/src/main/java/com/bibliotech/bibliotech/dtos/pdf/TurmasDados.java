package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record TurmasDados(List<TurmaItem> items) implements DadosRelatorio {
    public static record TurmaItem(String serie, String turma, Long quantidadeLeiturasTurma, String nomeAluno, Long quantidadeLeiturasAluno) {}
}
