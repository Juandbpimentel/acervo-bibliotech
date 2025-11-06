package com.bibliotech.bibliotech.observers;

import com.bibliotech.bibliotech.events.EmprestimoEvent;

/**
 * Interface Observer do padrão Observer.
 * Define o contrato para objetos que desejam ser notificados
 * sobre eventos relacionados a empréstimos.
 */
public interface EmprestimoObserver {

    /**
     * Método chamado quando um evento de empréstimo ocorre.
     *
     * @param event Evento contendo informações sobre o empréstimo e tipo do evento
     */
    void update(EmprestimoEvent event);
}

