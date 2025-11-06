package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.dtos.response.RelatorioAcervoDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF do Acervo Completo.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class RelatorioAcervoPdfReport extends AbstractPdfReport {
    private final List<RelatorioAcervoDTO> data;

    public RelatorioAcervoPdfReport(List<RelatorioAcervoDTO> data) {
        this.data = data;
    }

    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        configureTable(table);

        // Configura larguras específicas das colunas
        table.setWidths(new float[]{5, 3, 2});

        // Título do relatório
        table.addCell(createTitleCell("Relatório Completo do Acervo", 3));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Título"));
        table.addCell(createHeaderCell("Autor"));
        table.addCell(createHeaderCell("Quantidade"));

        // Dados
        for (RelatorioAcervoDTO livro : data) {
            table.addCell(livro.getTitulo());
            table.addCell(livro.getAutor());
            table.addCell(livro.getQtdExemplares().toString());
        }

        document.add(table);
    }
}

