package com.bibliotech.bibliotech.validators;

import com.bibliotech.bibliotech.exception.NotFoundException;
import com.bibliotech.bibliotech.exception.ValidationException;
import com.bibliotech.bibliotech.models.Exemplar;
import com.bibliotech.bibliotech.repositories.ExemplarRepository;
import org.springframework.stereotype.Component;

/**
 * Validador responsável pelas regras de negócio relacionadas a Exemplares.
 * Encapsula validações de disponibilidade e atualizações de situação.
 */
@Component
public class ExemplarValidator {

    private final ExemplarRepository exemplarRepository;

    public ExemplarValidator(ExemplarRepository exemplarRepository) {
        this.exemplarRepository = exemplarRepository;
    }

    /**
     * Valida se o exemplar existe e está disponível para empréstimo.
     *
     * @param idExemplar ID do exemplar a ser validado
     * @return Exemplar validado
     * @throws NotFoundException se o exemplar não for encontrado
     * @throws ValidationException se o exemplar não estiver disponível
     */
    public Exemplar validarDisponibilidade(Integer idExemplar) {
        if (idExemplar == null) {
            throw new ValidationException("O ID do exemplar não pode ser nulo.");
        }

        Exemplar exemplar = exemplarRepository.findById(idExemplar)
                .orElseThrow(() -> new NotFoundException("Exemplar não encontrado"));

        if (!"disponivel".equals(exemplar.getSituacao())) {
            throw new ValidationException("O exemplar não está disponível");
        }

        return exemplar;
    }

    /**
     * Marca o exemplar como emprestado.
     *
     * @param exemplar Exemplar a ter a situação atualizada
     */
    public void marcarComoEmprestado(Exemplar exemplar) {
        exemplar.setSituacao("emprestado");
    }

    /**
     * Marca o exemplar como disponível após devolução.
     *
     * @param exemplar Exemplar a ter a situação atualizada
     */
    public void marcarComoDisponivel(Exemplar exemplar) {
        exemplar.setSituacao("disponivel");
    }

    /**
     * Marca o exemplar como extraviado.
     *
     * @param exemplar Exemplar a ter a situação atualizada
     */
    public void marcarComoExtraviado(Exemplar exemplar) {
        exemplar.setSituacao("extraviado");
    }
}

