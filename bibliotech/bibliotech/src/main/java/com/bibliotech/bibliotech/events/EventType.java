package com.bibliotech.bibliotech.events;

/**
 * Enum que define os tipos de eventos relacionados a empréstimos.
 * Usado pelo padrão Observer para diferenciar ações a serem tomadas.
 */
public enum EventType {
    /**
     * Empréstimo criado com sucesso
     */
    EMPRESTIMO_CRIADO,

    /**
     * Empréstimo concluído (devolvido)
     */
    EMPRESTIMO_CONCLUIDO,

    /**
     * Empréstimo cancelado
     */
    EMPRESTIMO_CANCELADO,

    /**
     * Empréstimo extraviado
     */
    EMPRESTIMO_EXTRAVIADO,

    /**
     * Prazo de empréstimo renovado
     */
    EMPRESTIMO_RENOVADO,

    /**
     * Empréstimo marcado como atrasado
     */
    EMPRESTIMO_ATRASADO
}

