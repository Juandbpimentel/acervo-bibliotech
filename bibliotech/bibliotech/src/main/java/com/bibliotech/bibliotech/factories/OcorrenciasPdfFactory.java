package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.OcorrenciasPdfReport;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;
import com.bibliotech.bibliotech.models.Ocorrencia;

import java.util.List;

/**
 * Factory concreta para criação de relatórios PDF de Ocorrências
 */
public class OcorrenciasPdfFactory extends PdfReportFactory {
    private final List<Ocorrencia> data;

    public OcorrenciasPdfFactory(List<Ocorrencia> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new OcorrenciasPdfReport(data);
    }
}
