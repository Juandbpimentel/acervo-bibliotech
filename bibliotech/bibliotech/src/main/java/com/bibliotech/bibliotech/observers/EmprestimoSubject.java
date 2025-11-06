package com.bibliotech.bibliotech.observers;

import com.bibliotech.bibliotech.events.EmprestimoEvent;

/**
 * Interface Subject do padrão Observer.
 * Define o contrato para objetos que publicam eventos de empréstimo
 * e gerenciam a lista de observadores.
 */
public interface EmprestimoSubject {

    /**
     * Adiciona um observador à lista de interessados em eventos de empréstimo.
     *
     * @param observer Observador a ser adicionado
     */
    void attach(EmprestimoObserver observer);

    /**
     * Remove um observador da lista de interessados.
     *
     * @param observer Observador a ser removido
     */
    void detach(EmprestimoObserver observer);

    /**
     * Notifica todos os observadores sobre um evento de empréstimo.
     *
     * @param event Evento a ser propagado para os observadores
     */
    void notifyObservers(EmprestimoEvent event);
}

