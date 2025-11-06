package com.bibliotech.bibliotech.validators;

import com.bibliotech.bibliotech.exception.NotFoundException;
import com.bibliotech.bibliotech.exception.ValidationException;
import com.bibliotech.bibliotech.models.Aluno;
import com.bibliotech.bibliotech.repositories.AlunoRepository;
import org.springframework.stereotype.Component;

/**
 * Validador responsável pelas regras de negócio relacionadas a Alunos.
 * Encapsula validações para empréstimos e outras operações.
 */
@Component
public class AlunoValidator {

    private final AlunoRepository alunoRepository;

    public AlunoValidator(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    /**
     * Valida se o aluno existe e está apto para realizar empréstimos.
     *
     * @param idAluno ID do aluno a ser validado
     * @return Aluno validado
     * @throws NotFoundException se o aluno não for encontrado
     * @throws ValidationException se o aluno não estiver em situação regular
     */
    public Aluno validarParaEmprestimo(Integer idAluno) {
        if (idAluno == null) {
            throw new ValidationException("O ID do aluno não pode ser nulo.");
        }

        Aluno aluno = alunoRepository.findById(idAluno)
                .orElseThrow(() -> new NotFoundException("Aluno não encontrado"));

        if (!"regular".equals(aluno.getSituacao())) {
            throw new ValidationException("O aluno não está com a situação regular");
        }

        return aluno;
    }

    /**
     * Atualiza a situação do aluno para "débito" após realizar empréstimo.
     *
     * @param aluno Aluno a ter a situação atualizada
     */
    public void marcarComoDebito(Aluno aluno) {
        aluno.setSituacao("debito");
    }

    /**
     * Atualiza a situação do aluno para "regular" após devolver exemplar.
     *
     * @param aluno Aluno a ter a situação atualizada
     */
    public void marcarComoRegular(Aluno aluno) {
        aluno.setSituacao("regular");
    }

    /**
     * Atualiza a situação do aluno para "irregular" em caso de extravio.
     *
     * @param aluno Aluno a ter a situação atualizada
     */
    public void marcarComoIrregular(Aluno aluno) {
        aluno.setSituacao("irregular");
    }
}

