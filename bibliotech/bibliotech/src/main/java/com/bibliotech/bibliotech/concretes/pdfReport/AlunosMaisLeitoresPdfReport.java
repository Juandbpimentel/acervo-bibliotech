package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.dtos.response.AlunoLeiturasDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF de Alunos Mais Leitores.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class AlunosMaisLeitoresPdfReport extends AbstractPdfReport {
    private final List<AlunoLeiturasDTO> data;

    public AlunosMaisLeitoresPdfReport(List<AlunoLeiturasDTO> data) {
        this.data = data;
    }

    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        configureTable(table);

        // Configura larguras específicas das colunas
        table.setWidths(new float[]{2, 1, 1});

        // Título do relatório
        table.addCell(createTitleCell("Alunos Mais Leitores", 3));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Nome"));
        table.addCell(createHeaderCell("Turma"));
        table.addCell(createHeaderCell("Leituras"));

        // Dados
        for (AlunoLeiturasDTO aluno : data) {
            table.addCell(aluno.getNome());
            table.addCell(aluno.getSerie() + " " + aluno.getTurma());
            table.addCell(aluno.getQuantidade_leituras().toString());
        }

        document.add(table);
    }
}

