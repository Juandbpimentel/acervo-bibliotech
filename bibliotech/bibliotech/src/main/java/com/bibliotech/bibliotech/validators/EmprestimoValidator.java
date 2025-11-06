package com.bibliotech.bibliotech.validators;

import com.bibliotech.bibliotech.exception.NotFoundException;
import com.bibliotech.bibliotech.exception.ValidationException;
import com.bibliotech.bibliotech.models.Emprestimo;
import com.bibliotech.bibliotech.repositories.EmprestimoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Validador responsável pelas regras de negócio relacionadas a Empréstimos.
 * Encapsula validações de conclusão, cancelamento e renovação.
 */
@Component
public class EmprestimoValidator {

    private final EmprestimoRepository emprestimoRepository;

    public EmprestimoValidator(EmprestimoRepository emprestimoRepository) {
        this.emprestimoRepository = emprestimoRepository;
    }

    /**
     * Valida se o empréstimo existe.
     *
     * @param id ID do empréstimo
     * @return Empréstimo encontrado
     * @throws NotFoundException se não encontrado
     */
    public Emprestimo validarExistencia(Integer id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emprestimo com o ID " + id + " não encontrado."));
    }

    /**
     * Valida se o empréstimo pode ser cancelado.
     *
     * @param emprestimo Empréstimo a ser validado
     * @throws ValidationException se já estiver cancelado
     */
    public void validarParaCancelamento(Emprestimo emprestimo) {
        if ("cancelado".equals(emprestimo.getSituacao())) {
            throw new ValidationException("Emprestimo ja cancelado.");
        }
    }

    /**
     * Valida se o empréstimo pode ser concluído.
     *
     * @param emprestimo Empréstimo a ser validado
     * @throws ValidationException se já estiver concluído
     */
    public void validarParaConclusao(Emprestimo emprestimo) {
        if ("cancelado".equals(emprestimo.getSituacao()) ||
            "entregue".equals(emprestimo.getSituacao()) ||
            "extraviado".equals(emprestimo.getSituacao())) {
            throw new ValidationException("Emprestimo ja concluido.");
        }
    }

    /**
     * Valida se o empréstimo pode ser renovado.
     *
     * @param emprestimo Empréstimo a ser validado
     * @throws ValidationException se não puder ser renovado
     */
    public void validarParaRenovacao(Emprestimo emprestimo) {
        validarParaConclusao(emprestimo);

        if (ChronoUnit.DAYS.between(emprestimo.getDataEmprestimo(), LocalDate.now()) > 30) {
            throw new ValidationException("Renovação não permitida. O prazo máximo para renovação foi excedido.");
        }

        if (emprestimo.getQtdRenovacao() >= 3) {
            throw new ValidationException("Renovação não permitida. O número máximo de renovações foi atingido.");
        }
    }
}

