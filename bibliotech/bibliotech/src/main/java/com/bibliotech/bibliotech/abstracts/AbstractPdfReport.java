package com.bibliotech.bibliotech.abstracts;

import com.bibliotech.bibliotech.interfaces.PdfReportInterface;
import com.bibliotech.bibliotech.utils.PdfHeaderUtil;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

/**
 * Classe abstrata que implementa o padrão Template Method para geração de PDFs.
 * Define o esqueleto do algoritmo de geração, permitindo que subclasses
 * personalizem etapas específicas (principalmente o conteúdo da tabela).
 */
public abstract class AbstractPdfReport implements PdfReportInterface {

    /**
     * Template Method - Define o fluxo completo de geração do PDF.
     * Este método é final para garantir que a estrutura não seja alterada.
     */
    @Override
    public final byte[] generate() throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        // Etapas do template
        addHeader(document, writer);
        addContent(document);
        addFooter(document, writer);

        document.close();

        return out.toByteArray();
    }

    /**
     * Hook Method - Adiciona cabeçalho padrão ao documento.
     * Pode ser sobrescrito para cabeçalhos customizados.
     */
    protected void addHeader(Document document, PdfWriter writer) throws DocumentException {
        PdfHeaderUtil.addHeader(document, writer);
    }

    /**
     * Hook Method - Adiciona rodapé ao documento.
     * Implementação padrão vazia. Subclasses podem sobrescrever para adicionar rodapé.
     */
    protected void addFooter(Document document, PdfWriter writer) throws DocumentException {
        // Implementação padrão vazia - pode ser sobrescrita
    }

    /**
     * Método abstrato - DEVE ser implementado pelas subclasses.
     * Responsável por adicionar o conteúdo específico de cada tipo de relatório.
     */
    protected abstract void addContent(Document document) throws DocumentException;

    /**
     * Método utilitário para criar célula de título centralizada.
     * Reutilizável por todas as subclasses.
     */
    protected PdfPCell createTitleCell(String title, int colspan) {
        Font fontBold18 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        PdfPCell cell = new PdfPCell(new Phrase(title, fontBold18));
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8.0f);
        return cell;
    }

    /**
     * Método utilitário para criar célula de cabeçalho de tabela.
     * Reutilizável por todas as subclasses.
     */
    protected PdfPCell createHeaderCell(String text) {
        Font fontBold12 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        return new PdfPCell(new Phrase(text, fontBold12));
    }

    /**
     * Método utilitário para configurar tabela com espaçamento padrão.
     * Reutilizável por todas as subclasses.
     */
    protected void configureTable(PdfPTable table) {
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
    }
}

