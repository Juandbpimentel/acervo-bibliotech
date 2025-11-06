package com.bibliotech.bibliotech.observers.concretes;

import com.bibliotech.bibliotech.events.EmprestimoEvent;
import com.bibliotech.bibliotech.events.EventType;
import com.bibliotech.bibliotech.models.Emprestimo;
import com.bibliotech.bibliotech.observers.EmprestimoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observer concreto responsável por coletar estatísticas e métricas
 * sobre os empréstimos realizados.
 *
 * Permite análise de dados sem acoplar essa funcionalidade à lógica principal.
 * Em produção, poderia integrar com ferramentas de BI, Prometheus, etc.
 */
@Component
public class StatisticsObserver implements EmprestimoObserver {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsObserver.class);

    // Em produção, esses contadores estariam em cache/banco/sistema de métricas
    private int totalEmprestimos = 0;
    private int totalConclusoes = 0;
    private int totalRenovacoes = 0;
    private int totalExtravios = 0;

    @Override
    public void update(EmprestimoEvent event) {
        EventType type = event.getType();
        Emprestimo emprestimo = event.getEmprestimo();

        switch (type) {
            case EMPRESTIMO_CRIADO -> registrarNovoEmprestimo(emprestimo);
            case EMPRESTIMO_CONCLUIDO -> registrarConclusao(emprestimo);
            case EMPRESTIMO_RENOVADO -> registrarRenovacao(emprestimo);
            case EMPRESTIMO_EXTRAVIADO -> registrarExtravio(emprestimo);
            default -> logger.debug("Evento {} não gera estatística", type);
        }
    }

    private void registrarNovoEmprestimo(Emprestimo emprestimo) {
        totalEmprestimos++;

        logger.info("ESTATÍSTICA - Novo empréstimo registrado. Total: {}", totalEmprestimos);

        // Aqui poderia:
        // - Incrementar contador no Redis
        // - Enviar métrica para Prometheus
        // - Registrar em tabela de estatísticas
        // - Atualizar dashboard em tempo real
    }

    private void registrarConclusao(Emprestimo emprestimo) {
        totalConclusoes++;

        logger.info("ESTATÍSTICA - Empréstimo concluído. Total de conclusões: {}", totalConclusoes);

        // Calcular tempo médio de empréstimo
        // Atualizar taxa de devolução no prazo
    }

    private void registrarRenovacao(Emprestimo emprestimo) {
        totalRenovacoes++;

        logger.info("ESTATÍSTICA - Empréstimo renovado. Total de renovações: {}", totalRenovacoes);

        // Identificar livros mais renovados
        // Analisar padrões de renovação
    }

    private void registrarExtravio(Emprestimo emprestimo) {
        totalExtravios++;

        logger.warn("ESTATÍSTICA - Livro extraviado. Total de extravios: {}", totalExtravios);

        // Alertar sobre taxa de extravio
        // Identificar exemplares problemáticos
    }

    // Métodos para recuperar estatísticas (poderiam ser expostos via endpoint REST)
    public int getTotalEmprestimos() {
        return totalEmprestimos;
    }

    public int getTotalConclusoes() {
        return totalConclusoes;
    }

    public int getTotalRenovacoes() {
        return totalRenovacoes;
    }

    public int getTotalExtravios() {
        return totalExtravios;
    }
}

