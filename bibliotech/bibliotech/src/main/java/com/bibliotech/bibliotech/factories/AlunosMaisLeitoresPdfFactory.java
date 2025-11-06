package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.AlunosMaisLeitoresPdfReport;
import com.bibliotech.bibliotech.dtos.response.AlunoLeiturasDTO;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;

import java.util.List;

/**
 * Factory concreta para criação de relatórios PDF de Alunos Mais Leitores
 */
public class AlunosMaisLeitoresPdfFactory extends PdfReportFactory {
    private final List<AlunoLeiturasDTO> data;

    public AlunosMaisLeitoresPdfFactory(List<AlunoLeiturasDTO> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new AlunosMaisLeitoresPdfReport(data);
    }
}

