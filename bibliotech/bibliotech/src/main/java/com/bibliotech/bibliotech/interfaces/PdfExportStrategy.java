package com.bibliotech.bibliotech.interfaces;

import com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio;

public interface PdfExportStrategy {
    /**
     * Exporta o relatório representado por {@link DadosRelatorio} para um PDF em bytes.
     * Implementações devem delegar a renderização para um microserviço externo.
     */
    byte[] exportar(DadosRelatorio dados) throws Exception;
} 