package com.bibliotech.bibliotech.factories;

import com.bibliotech.bibliotech.abstracts.PdfReportFactory;
import com.bibliotech.bibliotech.concretes.pdfReport.LivrosMaisLidosPdfReport;
import com.bibliotech.bibliotech.dtos.response.LivrosMaisLidosDTO;
import com.bibliotech.bibliotech.interfaces.PdfReportInterface;

import java.util.List;

public class LivrosMaisLidosPdfFactory extends PdfReportFactory {
    private final List<LivrosMaisLidosDTO> data;

    public LivrosMaisLidosPdfFactory(List<LivrosMaisLidosDTO> data) {
        this.data = data;
    }

    @Override
    public PdfReportInterface createReport() {
        return new LivrosMaisLidosPdfReport(data);
    }
}
