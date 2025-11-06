package com.bibliotech.bibliotech.services;

import com.bibliotech.bibliotech.dtos.response.AlunoLeiturasDTO;
import com.bibliotech.bibliotech.dtos.response.LivrosMaisLidosDTO;
import com.bibliotech.bibliotech.dtos.response.RelatorioAcervoDTO;
import com.bibliotech.bibliotech.dtos.response.TurmaLeiturasDTO;
import com.bibliotech.bibliotech.factories.*;
import com.bibliotech.bibliotech.models.FrequenciaAlunos;
import com.bibliotech.bibliotech.models.Ocorrencia;
import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Serviço de exportação de PDFs refatorado usando o padrão Factory Method.
 * Cada tipo de relatório é delegado para sua respectiva factory,
 * eliminando a "classe Deus" e melhorando a separação de responsabilidades.
 */
@Service
public class PdfExportService {

    /**
     * Exporta lista de frequência de alunos para PDF
     */
    public byte[] exportFrequenciaAlunosToPdf(List<FrequenciaAlunos> frequenciaAlunosList) throws DocumentException {
        FrequenciaAlunosPdfFactory factory = new FrequenciaAlunosPdfFactory(frequenciaAlunosList);
        return factory.generatePdf();
    }

    /**
     * Exporta lista de ocorrências para PDF
     */
    public byte[] exportOcorrenciasToPdf(List<Ocorrencia> ocorrenciasList) throws DocumentException {
        OcorrenciasPdfFactory factory = new OcorrenciasPdfFactory(ocorrenciasList);
        return factory.generatePdf();
    }

    /**
     * Exporta lista de turmas mais leitoras para PDF
     */
    public byte[] exportTurmasMaisLeitoras(List<TurmaLeiturasDTO> turmasMaisLeitoras) throws DocumentException {
        TurmasMaisLeitorasPdfFactory factory = new TurmasMaisLeitorasPdfFactory(turmasMaisLeitoras);
        return factory.generatePdf();
    }

    /**
     * Exporta lista de alunos mais leitores para PDF
     */
    public byte[] exportAlunosMaisLeitores(List<AlunoLeiturasDTO> alunos) throws DocumentException {
        AlunosMaisLeitoresPdfFactory factory = new AlunosMaisLeitoresPdfFactory(alunos);
        return factory.generatePdf();
    }

    /**
     * Exporta lista de livros mais lidos para PDF
     */
    public byte[] exportLivrosMaisLidos(List<LivrosMaisLidosDTO> livrosMaisLidos) throws DocumentException {
        LivrosMaisLidosPdfFactory factory = new LivrosMaisLidosPdfFactory(livrosMaisLidos);
        return factory.generatePdf();
    }

    /**
     * Exporta relatório completo do acervo para PDF
     */
    public byte[] exportRelatorioAcervo(List<RelatorioAcervoDTO> relatorioAcervo) throws DocumentException {
        RelatorioAcervoPdfFactory factory = new RelatorioAcervoPdfFactory(relatorioAcervo);
        return factory.generatePdf();
    }
}

