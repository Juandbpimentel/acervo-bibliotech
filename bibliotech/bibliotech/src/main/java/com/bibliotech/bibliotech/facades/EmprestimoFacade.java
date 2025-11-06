package com.bibliotech.bibliotech.facades;

import com.bibliotech.bibliotech.dtos.request.EmprestimoRequestDTO;
import com.bibliotech.bibliotech.dtos.request.EmprestimoRequestDTOConcluir;
import com.bibliotech.bibliotech.dtos.request.mappers.EmprestimoRequestMapper;
import com.bibliotech.bibliotech.dtos.response.EmprestimoResponseDTO;
import com.bibliotech.bibliotech.dtos.response.mappers.EmprestimoResponseMapper;
import com.bibliotech.bibliotech.events.EmprestimoEvent;
import com.bibliotech.bibliotech.events.EventType;
import com.bibliotech.bibliotech.exception.NotFoundException;
import com.bibliotech.bibliotech.models.Aluno;
import com.bibliotech.bibliotech.models.Emprestimo;
import com.bibliotech.bibliotech.models.Exemplar;
import com.bibliotech.bibliotech.models.Usuario;
import com.bibliotech.bibliotech.observers.EmprestimoObserver;
import com.bibliotech.bibliotech.observers.EmprestimoSubject;
import com.bibliotech.bibliotech.repositories.EmprestimoRepository;
import com.bibliotech.bibliotech.repositories.UsuarioRepository;
import com.bibliotech.bibliotech.services.TokenService;
import com.bibliotech.bibliotech.validators.AlunoValidator;
import com.bibliotech.bibliotech.validators.EmprestimoValidator;
import com.bibliotech.bibliotech.validators.ExemplarValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade que simplifica as operações complexas de empréstimo.
 * Implementa o padrão Observer (Subject) para notificar interessados sobre eventos.
 *
 * Benefícios:
 * - Reduz acoplamento entre camadas
 * - Encapsula lógica de múltiplos subsistemas
 * - Permite extensão via observers sem modificar o código existente
 * - Facilita testes de integração
 */
@Component
public class EmprestimoFacade implements EmprestimoSubject {

    private static final Logger logger = LoggerFactory.getLogger(EmprestimoFacade.class);

    private final AlunoValidator alunoValidator;
    private final ExemplarValidator exemplarValidator;
    private final EmprestimoValidator emprestimoValidator;
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRequestMapper emprestimoRequestMapper;
    private final EmprestimoResponseMapper emprestimoResponseMapper;
    private final TokenService tokenService;

    // Lista de observers (padrão Observer)
    private final List<EmprestimoObserver> observers = new ArrayList<>();

    public EmprestimoFacade(
            AlunoValidator alunoValidator,
            ExemplarValidator exemplarValidator,
            EmprestimoValidator emprestimoValidator,
            EmprestimoRepository emprestimoRepository,
            UsuarioRepository usuarioRepository,
            EmprestimoRequestMapper emprestimoRequestMapper,
            EmprestimoResponseMapper emprestimoResponseMapper,
            TokenService tokenService) {
        this.alunoValidator = alunoValidator;
        this.exemplarValidator = exemplarValidator;
        this.emprestimoValidator = emprestimoValidator;
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioRepository = usuarioRepository;
        this.emprestimoRequestMapper = emprestimoRequestMapper;
        this.emprestimoResponseMapper = emprestimoResponseMapper;
        this.tokenService = tokenService;
    }

    // ========== Implementação do padrão Observer (Subject) ==========

    @Override
    public void attach(EmprestimoObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            logger.debug("Observer {} anexado. Total de observers: {}",
                    observer.getClass().getSimpleName(), observers.size());
        }
    }

    @Override
    public void detach(EmprestimoObserver observer) {
        if (observers.remove(observer)) {
            logger.debug("Observer {} removido. Total de observers: {}",
                    observer.getClass().getSimpleName(), observers.size());
        }
    }

    @Override
    public void notifyObservers(EmprestimoEvent event) {
        logger.debug("Notificando {} observers sobre evento: {}", observers.size(), event.getType());

        for (EmprestimoObserver observer : observers) {
            try {
                observer.update(event);
            } catch (Exception e) {
                logger.error("Erro ao notificar observer {}: {}",
                        observer.getClass().getSimpleName(), e.getMessage(), e);
                // Não propaga a exceção para não interromper outros observers
            }
        }
    }

    // ========== Métodos de negócio com notificações ==========

    @Transactional
    public EmprestimoResponseDTO realizarEmprestimo(EmprestimoRequestDTO requestDTO) {
        // 1. Validar aluno
        Aluno aluno = alunoValidator.validarParaEmprestimo(requestDTO.getIdAluno());

        // 2. Validar exemplar
        Exemplar exemplar = exemplarValidator.validarDisponibilidade(requestDTO.getIdExemplar());

        // 3. Obter usuário realizador
        Usuario usuario = obterUsuarioAtual();

        // 4. Criar empréstimo
        Emprestimo emprestimo = criarEmprestimo(requestDTO, aluno, exemplar, usuario);

        // 5. Atualizar situações
        atualizarSituacoesParaEmprestimo(aluno, exemplar);

        // 6. Salvar
        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        // 7. ⭐ Notificar observers sobre criação
        notifyObservers(new EmprestimoEvent(emprestimoSalvo, EventType.EMPRESTIMO_CRIADO));

        // 8. Mapear para DTO
        return emprestimoResponseMapper.toDto(emprestimoSalvo);
    }

    @Transactional
    public String cancelarEmprestimo(Integer id) {
        // 1. Validar existência e possibilidade de cancelamento
        Emprestimo emprestimo = emprestimoValidator.validarExistencia(id);
        emprestimoValidator.validarParaCancelamento(emprestimo);

        // 2. Obter usuário que está cancelando
        Usuario usuario = obterUsuarioAtual();

        // 3. Atualizar empréstimo
        emprestimo.setSituacao("cancelado");
        emprestimo.setConcluidoPor(usuario);
        emprestimo.setDataConclusao(LocalDate.now());

        // 4. Liberar aluno e exemplar
        alunoValidator.marcarComoRegular(emprestimo.getAluno());
        exemplarValidator.marcarComoDisponivel(emprestimo.getExemplar());

        // 5. Salvar
        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        // 6. ⭐ Notificar observers sobre cancelamento
        notifyObservers(new EmprestimoEvent(emprestimoSalvo, EventType.EMPRESTIMO_CANCELADO));

        return "Emprestimo cancelado com sucesso.";
    }

    @Transactional
    public String concluirEmprestimo(Integer id, EmprestimoRequestDTOConcluir dtoConcluir) {
        // 1. Validar existência e possibilidade de conclusão
        Emprestimo emprestimo = emprestimoValidator.validarExistencia(id);
        emprestimoValidator.validarParaConclusao(emprestimo);

        // 2. Obter usuário que está concluindo
        Usuario usuario = obterUsuarioAtual();

        // 3. Atualizar dados básicos
        emprestimo.setObservacao(dtoConcluir.getObservacao());
        emprestimo.setDataConclusao(LocalDate.now());
        emprestimo.setConcluidoPor(usuario);

        // 4. Atualizar situações conforme tipo de conclusão
        EventType eventType;
        if (dtoConcluir.isExtraviado()) {
            atualizarSituacoesParaExtravio(emprestimo);
            eventType = EventType.EMPRESTIMO_EXTRAVIADO;
        } else {
            atualizarSituacoesParaDevolucao(emprestimo);
            eventType = EventType.EMPRESTIMO_CONCLUIDO;
        }

        // 5. Salvar
        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        // 6. ⭐ Notificar observers sobre conclusão/extravio
        notifyObservers(new EmprestimoEvent(emprestimoSalvo, eventType, dtoConcluir.getObservacao()));

        return dtoConcluir.isExtraviado()
                ? "Emprestimo extraviado com sucesso."
                : "Emprestimo concluido com sucesso.";
    }

    @Transactional
    public String renovarPrazo(Integer id) {
        // 1. Validar existência e possibilidade de renovação
        Emprestimo emprestimo = emprestimoValidator.validarExistencia(id);
        emprestimoValidator.validarParaRenovacao(emprestimo);

        // 2. Atualizar prazo
        atualizarPrazo(emprestimo);

        // 3. Incrementar contador de renovações
        emprestimo.setQtdRenovacao(emprestimo.getQtdRenovacao() + 1);

        // 4. Salvar
        Emprestimo emprestimoSalvo = emprestimoRepository.save(emprestimo);

        // 5. ⭐ Notificar observers sobre renovação
        notifyObservers(new EmprestimoEvent(emprestimoSalvo, EventType.EMPRESTIMO_RENOVADO));

        return "Prazo renovado com sucesso.";
    }

    // ========== Métodos Privados Auxiliares ==========
    // ...existing code...

    private Emprestimo criarEmprestimo(
            EmprestimoRequestDTO requestDTO,
            Aluno aluno,
            Exemplar exemplar,
            Usuario usuario) {

        Emprestimo emprestimo = emprestimoRequestMapper.toEntity(requestDTO);
        emprestimo.setAluno(aluno);
        emprestimo.setExemplar(exemplar);
        emprestimo.setRealizadoPor(usuario);
        emprestimo.setSituacao("pendente");

        return emprestimo;
    }

    private void atualizarSituacoesParaEmprestimo(Aluno aluno, Exemplar exemplar) {
        alunoValidator.marcarComoDebito(aluno);
        exemplarValidator.marcarComoEmprestado(exemplar);
    }

    private void atualizarSituacoesParaDevolucao(Emprestimo emprestimo) {
        emprestimo.setSituacao("entregue");
        alunoValidator.marcarComoRegular(emprestimo.getAluno());
        exemplarValidator.marcarComoDisponivel(emprestimo.getExemplar());
    }

    private void atualizarSituacoesParaExtravio(Emprestimo emprestimo) {
        emprestimo.setSituacao("extraviado");
        alunoValidator.marcarComoIrregular(emprestimo.getAluno());
        exemplarValidator.marcarComoExtraviado(emprestimo.getExemplar());
    }

    private void atualizarPrazo(Emprestimo emprestimo) {
        if ("atrasado".equals(emprestimo.getSituacao())) {
            emprestimo.setDataPrazo(LocalDate.now().plusDays(7));
            emprestimo.setSituacao("pendente");
        } else {
            emprestimo.setDataPrazo(emprestimo.getDataPrazo().plusDays(7));
        }
    }

    private Usuario obterUsuarioAtual() {
        return usuarioRepository.findById(tokenService.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}

