package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.dtos.response.LivrosMaisLidosDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF de Livros Mais Lidos.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class LivrosMaisLidosPdfReport extends AbstractPdfReport {
    private final List<LivrosMaisLidosDTO> data;

    public LivrosMaisLidosPdfReport(List<LivrosMaisLidosDTO> data) {
        this.data = data;
    }

    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        configureTable(table);

        // Título do relatório
        table.addCell(createTitleCell("Livros Mais Lidos", 2));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Título"));
        table.addCell(createHeaderCell("Quantidade de Empréstimos"));

        // Dados
        for (LivrosMaisLidosDTO livro : data) {
            table.addCell(livro.getTitulo());
            table.addCell(livro.getQuantidadeEmprestimos().toString());
        }

        document.add(table);
    }
}
