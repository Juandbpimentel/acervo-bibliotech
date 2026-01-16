package com.bibliotech.bibliotech.abstracts;

import com.bibliotech.bibliotech.interfaces.PdfReportInterface;
import com.lowagie.text.DocumentException;


public abstract class PdfReportFactory {
    public abstract PdfReportInterface createReport();

    public byte[] generatePdf() throws DocumentException {
        PdfReportInterface report = createReport();
        return report.generate();
    }
}
