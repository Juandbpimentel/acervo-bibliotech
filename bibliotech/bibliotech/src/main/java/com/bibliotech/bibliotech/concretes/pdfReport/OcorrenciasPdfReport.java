package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.models.Ocorrencia;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF de Ocorrências.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class OcorrenciasPdfReport extends AbstractPdfReport {
    private final List<Ocorrencia> data;

    public OcorrenciasPdfReport(List<Ocorrencia> data) {
        this.data = data;
    }

    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        configureTable(table);

        // Configura larguras específicas das colunas
        table.setWidths(new float[]{2, 2, 5});

        // Título do relatório
        table.addCell(createTitleCell("Ocorrências", 3));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Aluno"));
        table.addCell(createHeaderCell("Registrada por"));
        table.addCell(createHeaderCell("Detalhes"));

        // Dados
        for (Ocorrencia ocorrencia : data) {
            table.addCell(ocorrencia.getAluno().getNome());
            table.addCell(ocorrencia.getRegistradaPor().getNome());
            table.addCell(ocorrencia.getDetalhes());
        }

        document.add(table);
    }
}

