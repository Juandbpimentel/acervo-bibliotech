package com.bibliotech.bibliotech.observers.concretes;

import com.bibliotech.bibliotech.events.EmprestimoEvent;
import com.bibliotech.bibliotech.events.EventType;
import com.bibliotech.bibliotech.models.Emprestimo;
import com.bibliotech.bibliotech.observers.EmprestimoObserver;
import com.bibliotech.bibliotech.services.NotificationService;
import com.bibliotech.bibliotech.utils.FormatarData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer concreto responsável por enviar notificações por email
 * quando eventos de empréstimo ocorrem.
 *
 * Implementa o padrão Observer para desacoplar a lógica de notificação
 * da lógica de negócio de empréstimos.
 */
@Component
public class NotificationObserver implements EmprestimoObserver {

    private static final Logger logger = LoggerFactory.getLogger(NotificationObserver.class);

    private final NotificationService notificationService;

    public NotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void update(EmprestimoEvent event) {
        EventType type = event.getType();
        Emprestimo emprestimo = event.getEmprestimo();

        try {
            switch (type) {
                case EMPRESTIMO_CRIADO -> enviarNotificacaoEmprestimoCriado(emprestimo);
                case EMPRESTIMO_CONCLUIDO -> enviarNotificacaoEmprestimoConcluido(emprestimo);
                case EMPRESTIMO_RENOVADO -> enviarNotificacaoEmprestimoRenovado(emprestimo);
                case EMPRESTIMO_EXTRAVIADO -> enviarNotificacaoEmprestimoExtraviado(emprestimo);
                default -> logger.debug("Tipo de evento não requer notificação: {}", type);
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar notificação para evento {}: {}", type, e.getMessage(), e);
        }
    }

    private void enviarNotificacaoEmprestimoCriado(Emprestimo emprestimo) {
        String assunto = "Confirmação de Empréstimo - Biblioteca";
        String dataPrazoFormatada = FormatarData.formatarData(emprestimo.getDataPrazo());

        String mensagem = String.format(
                """
                Olá %s,
                
                Seu empréstimo foi realizado com sucesso!
                
                Detalhes do empréstimo:
                - Livro: %s
                - Data de devolução: %s
                - Situação: Pendente
                
                Por favor, devolva o livro até a data indicada para evitar atrasos.
                Você pode renovar o empréstimo até 3 vezes, caso necessário.
                
                Atenciosamente,
                Biblioteca Adelino Cunha.
                """,
                emprestimo.getAluno().getNome(),
                emprestimo.getExemplar().getLivro().getTitulo(),
                dataPrazoFormatada
        );

        boolean enviado = notificationService.sendNotification(
                emprestimo.getAluno().getEmail(),
                assunto,
                mensagem
        );

        if (enviado) {
            logger.info("Notificação de empréstimo criado enviada para: {}",
                    emprestimo.getAluno().getEmail());
        } else {
            logger.warn("Falha ao enviar notificação de empréstimo criado para: {}",
                    emprestimo.getAluno().getEmail());
        }
    }

    private void enviarNotificacaoEmprestimoConcluido(Emprestimo emprestimo) {
        String assunto = "Empréstimo Concluído - Biblioteca";
        String dataEmprestimoFormatada = FormatarData.formatarData(emprestimo.getDataEmprestimo());
        String dataConclusaoFormatada = FormatarData.formatarData(emprestimo.getDataConclusao());

        String mensagem = String.format(
                """
                Olá %s,
                
                Confirmamos a devolução do livro "%s".
                
                Detalhes:
                - Data do empréstimo: %s
                - Data da devolução: %s
                - Situação: Concluído
                
                Obrigado por utilizar nossa biblioteca!
                
                Atenciosamente,
                Biblioteca Adelino Cunha.
                """,
                emprestimo.getAluno().getNome(),
                emprestimo.getExemplar().getLivro().getTitulo(),
                dataEmprestimoFormatada,
                dataConclusaoFormatada
        );

        notificationService.sendNotification(
                emprestimo.getAluno().getEmail(),
                assunto,
                mensagem
        );

        logger.info("Notificação de empréstimo concluído enviada para: {}",
                emprestimo.getAluno().getEmail());
    }

    private void enviarNotificacaoEmprestimoRenovado(Emprestimo emprestimo) {
        String assunto = "Empréstimo Renovado - Biblioteca";
        String novaDataPrazoFormatada = FormatarData.formatarData(emprestimo.getDataPrazo());

        String mensagem = String.format(
                """
                Olá %s,
                
                Seu empréstimo do livro "%s" foi renovado com sucesso!
                
                Nova data de devolução: %s
                Número de renovações: %d/3
                
                Por favor, devolva o livro até a nova data indicada.
                
                Atenciosamente,
                Biblioteca Adelino Cunha.
                """,
                emprestimo.getAluno().getNome(),
                emprestimo.getExemplar().getLivro().getTitulo(),
                novaDataPrazoFormatada,
                emprestimo.getQtdRenovacao()
        );

        notificationService.sendNotification(
                emprestimo.getAluno().getEmail(),
                assunto,
                mensagem
        );

        logger.info("Notificação de renovação enviada para: {}",
                emprestimo.getAluno().getEmail());
    }

    private void enviarNotificacaoEmprestimoExtraviado(Emprestimo emprestimo) {
        String assunto = "IMPORTANTE: Livro Extraviado - Biblioteca";

        String mensagem = String.format(
                """
                Olá %s,
                
                Registramos o extravio do livro "%s".
                
                Sua situação foi alterada para "irregular" até a regularização.
                Por favor, entre em contato com a biblioteca para resolver a situação.
                
                Atenciosamente,
                Biblioteca Adelino Cunha.
                """,
                emprestimo.getAluno().getNome(),
                emprestimo.getExemplar().getLivro().getTitulo()
        );

        notificationService.sendNotification(
                emprestimo.getAluno().getEmail(),
                assunto,
                mensagem
        );

        logger.warn("Notificação de extravio enviada para: {}",
                emprestimo.getAluno().getEmail());
    }
}

