package com.bibliotech.bibliotech.events;

import com.bibliotech.bibliotech.models.Emprestimo;

import java.time.LocalDateTime;

/**
 * Classe que representa um evento relacionado a empréstimo.
 * Encapsula informações sobre o empréstimo e o tipo de evento ocorrido.
 * Usada pelo padrão Observer para comunicação entre Subject e Observers.
 */
public class EmprestimoEvent {

    private final Emprestimo emprestimo;
    private final EventType type;
    private final LocalDateTime timestamp;
    private final String descricao;

    public EmprestimoEvent(Emprestimo emprestimo, EventType type) {
        this(emprestimo, type, null);
    }

    public EmprestimoEvent(Emprestimo emprestimo, EventType type, String descricao) {
        this.emprestimo = emprestimo;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.descricao = descricao;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public EventType getType() {
        return type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return "EmprestimoEvent{" +
                "type=" + type +
                ", timestamp=" + timestamp +
                ", emprestimoId=" + (emprestimo != null ? emprestimo.getId() : "null") +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}

