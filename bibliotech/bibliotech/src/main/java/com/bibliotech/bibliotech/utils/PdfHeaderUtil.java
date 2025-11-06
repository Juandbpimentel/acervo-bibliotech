package com.bibliotech.bibliotech.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

/**
 * Classe utilitária para adicionar cabeçalhos padronizados em PDFs
 */
public class PdfHeaderUtil {

    /**
     * Adiciona o cabeçalho padrão da escola no documento PDF
     */
    public static void addHeader(Document document, PdfWriter writer) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // Criar um parágrafo para o cabeçalho
        Paragraph headerContent = new Paragraph();
        headerContent.setLeading(12); // Set line spacing
        headerContent.add(new Chunk("E.E.M.T.I. ADELINO CUNHA ALCÂNTARA\n", headerFont));
        headerContent.add(new Chunk("Av. Cel. Neco Martins, 317, Centro – CEP: 62670-000 – Telefone (85)3315-7014\n", subHeaderFont));
        headerContent.add(new Chunk("Email: eemtadelinoaicantara@escola.ce.gov.br\n", subHeaderFont));
        headerContent.add(new Chunk("São Gonçalo do Amarante – Ce\n", subHeaderFont));
        headerContent.add(new Chunk("CNPJ: 079545140295-30\n", subHeaderFont));
        headerContent.add(new Chunk("NEP: 23269014\n", subHeaderFont));

        headerContent.setAlignment(Element.ALIGN_CENTER);
        document.add(headerContent);

        // Desenhar um retângulo ao redor do cabeçalho
        PdfContentByte canvas = writer.getDirectContent();
        canvas.saveState();
        canvas.setLineWidth(1f);
        canvas.setColorStroke(Color.BLACK);

        float x = document.left() - 10; // X position (left margin)
        float y = document.top() - 75; // Y position (adjusted to fit content)
        float width = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() + 20; // Width
        float height = 80; // Height
        float radius = 10; // Corner radius

        canvas.roundRectangle(x, y, width, height, radius);
        canvas.stroke();
        canvas.restoreState();

        // Adicionando o resto do cabeçalho entre o retângulo e o conteúdo
        Font boldSubHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font regularSubHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // Cria um parágrafo para "Entidade/Escola" com rótulo em negrito
        Paragraph entity = new Paragraph();
        entity.add(new Chunk("Entidade/Escola: ", boldSubHeaderFont)); // Bold label
        entity.add(new Chunk("EEMTI ADELINO CUNHA ALCÂNTARA", regularSubHeaderFont)); // Regular text
        entity.setAlignment(Element.ALIGN_LEFT);
        document.add(entity);

        // Cria um parágrafo para "Município" com rótulo em negrito
        Paragraph municipality = new Paragraph();
        municipality.add(new Chunk("Município: ", boldSubHeaderFont)); // Bold label
        municipality.add(new Chunk("SÃO GONÇALO DO AMARANTE – CE   CREDE:02", regularSubHeaderFont)); // Regular text
        municipality.setAlignment(Element.ALIGN_LEFT);
        document.add(municipality);
    }
}

