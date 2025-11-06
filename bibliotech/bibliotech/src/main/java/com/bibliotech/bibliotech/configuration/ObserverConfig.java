package com.bibliotech.bibliotech.configuration;

import com.bibliotech.bibliotech.facades.EmprestimoFacade;
import com.bibliotech.bibliotech.observers.concretes.AuditoriaObserver;
import com.bibliotech.bibliotech.observers.concretes.NotificationObserver;
import com.bibliotech.bibliotech.observers.concretes.StatisticsObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Configuração do padrão Observer para eventos de empréstimo.
 * Registra automaticamente todos os observers quando o contexto Spring é inicializado.
 */
@Configuration
public class ObserverConfig {

    private static final Logger logger = LoggerFactory.getLogger(ObserverConfig.class);

    private final EmprestimoFacade emprestimoFacade;
    private final NotificationObserver notificationObserver;
    private final AuditoriaObserver auditoriaObserver;
    private final StatisticsObserver statisticsObserver;

    public ObserverConfig(
            EmprestimoFacade emprestimoFacade,
            NotificationObserver notificationObserver,
            AuditoriaObserver auditoriaObserver,
            StatisticsObserver statisticsObserver) {
        this.emprestimoFacade = emprestimoFacade;
        this.notificationObserver = notificationObserver;
        this.auditoriaObserver = auditoriaObserver;
        this.statisticsObserver = statisticsObserver;
    }

    /**
     * Registra todos os observers quando o contexto Spring é carregado.
     * Executado automaticamente após a inicialização do ApplicationContext.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void registerObservers() {
        logger.info("Registrando observers de empréstimo...");

        emprestimoFacade.attach(notificationObserver);
        logger.info("✓ NotificationObserver registrado");

        emprestimoFacade.attach(auditoriaObserver);
        logger.info("✓ AuditoriaObserver registrado");

        emprestimoFacade.attach(statisticsObserver);
        logger.info("✓ StatisticsObserver registrado");

        logger.info("Todos os observers de empréstimo foram registrados com sucesso!");
    }
}

