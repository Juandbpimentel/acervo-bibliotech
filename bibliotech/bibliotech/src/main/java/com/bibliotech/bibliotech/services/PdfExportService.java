package com.bibliotech.bibliotech.services;

import com.bibliotech.bibliotech.dtos.pdf.AcervoDados;
import com.bibliotech.bibliotech.dtos.pdf.AlunosMaisLeitoresDados;
import com.bibliotech.bibliotech.dtos.pdf.FrequenciaDados;
import com.bibliotech.bibliotech.dtos.response.AlunoLeiturasDTO;
import com.bibliotech.bibliotech.dtos.response.LivrosMaisLidosDTO;
import com.bibliotech.bibliotech.dtos.response.RelatorioAcervoDTO;
import com.bibliotech.bibliotech.dtos.response.TurmaLeiturasDTO;
import com.bibliotech.bibliotech.factories.*;
import com.bibliotech.bibliotech.models.FrequenciaAlunos;
import com.bibliotech.bibliotech.models.Ocorrencia;
import com.lowagie.text.DocumentException;
import com.bibliotech.bibliotech.strategies.AlunosMaisLeitoresPdfStrategy;
import com.bibliotech.bibliotech.strategies.FrequenciaPdfStrategy;
import com.bibliotech.bibliotech.strategies.AcervoPdfStrategy;
import com.bibliotech.bibliotech.strategies.OcorrenciasPdfStrategy;
import com.bibliotech.bibliotech.strategies.LivrosMaisLidosPdfStrategy;
import com.bibliotech.bibliotech.strategies.TurmasMaisLeitorasPdfStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de exportação de PDFs. Gradualmente migrando de factories para strategies
 * que chamam o `pdf-microservice`.
 */
@Service
public class PdfExportService {

    private final AlunosMaisLeitoresPdfStrategy alunosStrategy;
    private final FrequenciaPdfStrategy frequenciaStrategy;
    private final AcervoPdfStrategy acervoStrategy;
    private final OcorrenciasPdfStrategy ocorrenciasStrategy;
    private final LivrosMaisLidosPdfStrategy livrosStrategy;
    private final TurmasMaisLeitorasPdfStrategy turmasStrategy;

    public PdfExportService(AlunosMaisLeitoresPdfStrategy alunosStrategy,
                            FrequenciaPdfStrategy frequenciaStrategy,
                            AcervoPdfStrategy acervoStrategy,
                            OcorrenciasPdfStrategy ocorrenciasStrategy,
                            LivrosMaisLidosPdfStrategy livrosStrategy,
                            TurmasMaisLeitorasPdfStrategy turmasStrategy) {
        this.alunosStrategy = alunosStrategy;
        this.frequenciaStrategy = frequenciaStrategy;
        this.acervoStrategy = acervoStrategy;
        this.ocorrenciasStrategy = ocorrenciasStrategy;
        this.livrosStrategy = livrosStrategy;
        this.turmasStrategy = turmasStrategy;
    }

    /**
     * Exporta lista de frequência de alunos para PDF (migrado para strategy)
     */
    public byte[] exportFrequenciaAlunosToPdf(List<FrequenciaAlunos> frequenciaAlunosList) throws DocumentException {
        try {
            java.util.List<FrequenciaDados.FrequenciaItem> items = frequenciaAlunosList.stream()
                    .map(f -> new FrequenciaDados.FrequenciaItem(
                            f.getAluno().getNome(),
                            f.getAtividade().replace("_", " "),
                            f.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ))
                    .collect(Collectors.toList());
            return frequenciaStrategy.exportar(new FrequenciaDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }

    /**
     * Exporta lista de ocorrências para PDF (migrado para strategy)
     */
    public byte[] exportOcorrenciasToPdf(List<Ocorrencia> ocorrenciasList) throws DocumentException {
        try {
            var items = ocorrenciasList.stream()
                    .map(o -> new com.bibliotech.bibliotech.dtos.pdf.OcorrenciasDados.OcorrenciaItem(
                            o.getAluno().getNome(),
                            o.getRegistradaPor().getNome(),
                            o.getDetalhes(),
                            o.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    )).collect(java.util.stream.Collectors.toList());
            return ocorrenciasStrategy.exportar(new com.bibliotech.bibliotech.dtos.pdf.OcorrenciasDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }

    /**
     * Exporta lista de turmas mais leitoras para PDF (migrado para strategy)
     */
    public byte[] exportTurmasMaisLeitoras(List<TurmaLeiturasDTO> turmasMaisLeitoras) throws DocumentException {
        try {
            var items = turmasMaisLeitoras.stream()
                    .map(t -> new com.bibliotech.bibliotech.dtos.pdf.TurmasDados.TurmaItem(
                            t.getSerie() == null ? null : t.getSerie().toString(),
                            t.getTurma(),
                            t.getQuantidadeLeiturasTurma(),
                            t.getNomeAluno(),
                            t.getQuantidadeLeiturasAluno()
                    )).collect(java.util.stream.Collectors.toList());
            return turmasStrategy.exportar(new com.bibliotech.bibliotech.dtos.pdf.TurmasDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }

    /**
     * Exporta lista de alunos mais leitores para PDF (migrado para strategy)
     */
    public byte[] exportAlunosMaisLeitores(List<AlunoLeiturasDTO> alunos) throws DocumentException {
        try {
            java.util.List<AlunosMaisLeitoresDados.AlunoItem> items = alunos.stream()
                    .map(a -> new AlunosMaisLeitoresDados.AlunoItem(
                            a.getNome(),
                            a.getSerie() == null ? null : a.getSerie().toString(),
                            a.getTurma(),
                            a.getQuantidade_leituras()
                    ))
                    .collect(Collectors.toList());
            return alunosStrategy.exportar(new AlunosMaisLeitoresDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }

    /**
     * Exporta lista de livros mais lidos para PDF (migrado para strategy)
     */
    public byte[] exportLivrosMaisLidos(List<LivrosMaisLidosDTO> livrosMaisLidos) throws DocumentException {
        try {
            var items = livrosMaisLidos.stream()
                    .map(l -> new com.bibliotech.bibliotech.dtos.pdf.LivrosDados.LivroItem(l.getTitulo(), l.getQuantidadeEmprestimos()))
                    .collect(java.util.stream.Collectors.toList());
            return livrosStrategy.exportar(new com.bibliotech.bibliotech.dtos.pdf.LivrosDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }

    /**
     * Exporta relatório completo do acervo para PDF (migrado para strategy)
     */
    public byte[] exportRelatorioAcervo(List<RelatorioAcervoDTO> relatorioAcervo) throws DocumentException {
        try {
            var items = relatorioAcervo.stream()
                    .map(r -> new AcervoDados.AcervoItem(
                            r.getTitulo(),
                            r.getAutor(),
                            r.getQtdExemplares()
                    ))
                    .collect(Collectors.toList());
            return acervoStrategy.exportar(new AcervoDados(items));
        } catch (Exception e) {
            throw new DocumentException(e.getMessage());
        }
    }
} 

