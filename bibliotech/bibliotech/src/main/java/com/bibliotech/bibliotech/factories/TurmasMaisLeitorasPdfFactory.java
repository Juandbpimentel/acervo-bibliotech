package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.TurmasMaisLeitorasPdfReport;
import com.bibliotech.bibliotech.dtos.response.TurmaLeiturasDTO;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;

import java.util.List;

/**
 * Factory concreta para criação de relatórios PDF de Turmas Mais Leitoras
 */
public class TurmasMaisLeitorasPdfFactory extends PdfReportFactory {
    private final List<TurmaLeiturasDTO> data;

    public TurmasMaisLeitorasPdfFactory(List<TurmaLeiturasDTO> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new TurmasMaisLeitorasPdfReport(data);
    }
}

