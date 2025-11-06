package com.bibliotech.bibliotech.observers.concretes;

import com.bibliotech.bibliotech.events.EmprestimoEvent;
import com.bibliotech.bibliotech.events.EventType;
import com.bibliotech.bibliotech.models.Emprestimo;
import com.bibliotech.bibliotech.observers.EmprestimoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer concreto responsável por registrar logs de auditoria
 * quando eventos de empréstimo ocorrem.
 *
 * Permite rastreabilidade e auditoria de todas as operações sem
 * acoplar essa funcionalidade à lógica principal.
 */
@Component
public class AuditoriaObserver implements EmprestimoObserver {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaObserver.class);

    @Override
    public void update(EmprestimoEvent event) {
        EventType type = event.getType();
        Emprestimo emprestimo = event.getEmprestimo();

        String logMessage = construirMensagemLog(type, emprestimo, event.getDescricao());

        // Registra no log do sistema (em produção, poderia gravar em banco de dados)
        logger.info("AUDITORIA - {}", logMessage);

        // Aqui poderia ser implementada lógica adicional como:
        // - Gravar em tabela de auditoria no banco
        // - Enviar para sistema de monitoramento externo
        // - Integrar com sistema de BI/Analytics
    }

    private String construirMensagemLog(EventType type, Emprestimo emprestimo, String descricao) {
        String usuarioResponsavel = obterUsuarioResponsavel(type, emprestimo);

        return String.format(
                "[%s] Empréstimo ID: %d | Aluno: %s | Livro: %s | Usuário: %s%s",
                type,
                emprestimo.getId(),
                emprestimo.getAluno().getNome(),
                emprestimo.getExemplar().getLivro().getTitulo(),
                usuarioResponsavel,
                descricao != null ? " | Observação: " + descricao : ""
        );
    }

    private String obterUsuarioResponsavel(EventType type, Emprestimo emprestimo) {
        return switch (type) {
            case EMPRESTIMO_CRIADO ->
                emprestimo.getRealizadoPor() != null ?
                        emprestimo.getRealizadoPor().getNome() : "Sistema";
            case EMPRESTIMO_CONCLUIDO, EMPRESTIMO_CANCELADO, EMPRESTIMO_EXTRAVIADO ->
                emprestimo.getConcluidoPor() != null ?
                        emprestimo.getConcluidoPor().getNome() : "Sistema";
            default -> "Sistema";
        };
    }
}

