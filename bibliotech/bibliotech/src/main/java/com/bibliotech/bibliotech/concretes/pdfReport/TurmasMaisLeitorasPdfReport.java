package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.dtos.response.TurmaLeiturasDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF de Turmas Mais Leitoras.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class TurmasMaisLeitorasPdfReport extends AbstractPdfReport {
    private final List<TurmaLeiturasDTO> data;

    public TurmasMaisLeitorasPdfReport(List<TurmaLeiturasDTO> data) {
        this.data = data;
    }

    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        configureTable(table);

        // Título do relatório
        table.addCell(createTitleCell("Turmas Leitoras", 4));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Turma"));
        table.addCell(createHeaderCell("Leituras da Turma"));
        table.addCell(createHeaderCell("Aluno Destaque"));
        table.addCell(createHeaderCell("Leituras do Aluno"));

        // Dados
        for (TurmaLeiturasDTO turmaLeiturasDTO : data) {
            table.addCell(turmaLeiturasDTO.getSerie() + " " + turmaLeiturasDTO.getTurma());
            table.addCell(turmaLeiturasDTO.getQuantidadeLeiturasTurma().toString());
            table.addCell(turmaLeiturasDTO.getNomeAluno());
            table.addCell(turmaLeiturasDTO.getQuantidadeLeiturasAluno().toString());
        }

        document.add(table);
    }
}

