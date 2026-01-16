package com.bibliotech.bibliotech.dtos.pdf;

import java.util.List;

public record LivrosDados(List<LivroItem> items) implements DadosRelatorio {
    public static record LivroItem(String titulo, Long quantidadeEmprestimos) {}
}
