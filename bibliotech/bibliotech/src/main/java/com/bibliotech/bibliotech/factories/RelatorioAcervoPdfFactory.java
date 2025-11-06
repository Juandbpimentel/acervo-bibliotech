package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.RelatorioAcervoPdfReport;
import com.bibliotech.bibliotech.dtos.response.RelatorioAcervoDTO;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;

import java.util.List;

/**
 * Factory concreta para criação de relatórios PDF do Acervo Completo
 */
public class RelatorioAcervoPdfFactory extends PdfReportFactory {
    private final List<RelatorioAcervoDTO> data;

    public RelatorioAcervoPdfFactory(List<RelatorioAcervoDTO> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new RelatorioAcervoPdfReport(data);
    }
}
