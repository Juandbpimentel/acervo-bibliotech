package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.FrequenciaAlunosPdfReport;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;
import com.bibliotech.bibliotech.models.FrequenciaAlunos;

import java.util.List;

public class FrequenciaAlunosPdfFactory extends PdfReportFactory {
    private final List<FrequenciaAlunos> data;

    public FrequenciaAlunosPdfFactory(List<FrequenciaAlunos> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new FrequenciaAlunosPdfReport(data);
    }
}
