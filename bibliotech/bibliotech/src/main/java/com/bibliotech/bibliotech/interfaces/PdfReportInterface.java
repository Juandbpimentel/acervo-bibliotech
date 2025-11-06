package com.bibliotech.bibliotech.interfaces;

import com.lowagie.text.DocumentException;

public interface PdfReportInterface {
    byte[] generate() throws DocumentException;
}
