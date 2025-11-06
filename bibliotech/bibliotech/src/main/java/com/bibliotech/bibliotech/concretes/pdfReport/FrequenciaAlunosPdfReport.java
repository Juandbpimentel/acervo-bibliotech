package com.bibliotech.bibliotech.concretes.pdfReport;

import com.bibliotech.bibliotech.abstracts.AbstractPdfReport;
import com.bibliotech.bibliotech.models.FrequenciaAlunos;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfPTable;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Produto concreto responsável pela geração de relatório PDF de Frequência de Alunos.
 * Estende AbstractPdfReport usando o padrão Template Method.
 */
public class FrequenciaAlunosPdfReport extends AbstractPdfReport {
    private final List<FrequenciaAlunos> data;

    public FrequenciaAlunosPdfReport(List<FrequenciaAlunos> data) {
        this.data = data;
    }

    /**
     * Implementação específica do conteúdo para relatório de frequência.
     * Chamado pelo Template Method da classe pai.
     */
    @Override
    protected void addContent(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        configureTable(table);

        // Título do relatório
        table.addCell(createTitleCell("Frequência de Alunos", 3));

        // Cabeçalhos da tabela
        table.addCell(createHeaderCell("Aluno"));
        table.addCell(createHeaderCell("Atividade"));
        table.addCell(createHeaderCell("Data"));

        // Dados
        for (FrequenciaAlunos frequenciaAlunos : data) {
            table.addCell(frequenciaAlunos.getAluno().getNome());

            // Substitui underscores por espaços na atividade
            String atividade = frequenciaAlunos.getAtividade().replace("_", " ");
            table.addCell(atividade);

            table.addCell(frequenciaAlunos.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        document.add(table);
    }
}
