package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.AcervoDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AcervoPdfStrategyTest {

    @Mock
    PdfMicroserviceClient client;

    AcervoPdfStrategy strategy;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        strategy = new AcervoPdfStrategy(client);
    }

    @Test
    void shouldMapItemsAndCallClient() throws Exception {
        var item = new AcervoDados.AcervoItem("Livro A", "Autor B", 3);
        var dados = new AcervoDados(List.of(item));
        byte[] fakePdf = "pdf-bytes".getBytes();
        when(client.generatePdf(any())).thenReturn(fakePdf);

        byte[] result = strategy.exportar(dados);

        assertArrayEquals(fakePdf, result);

        ArgumentCaptor<PdfRequest> captor = ArgumentCaptor.forClass(PdfRequest.class);
        verify(client, times(1)).generatePdf(captor.capture());
        assertEquals("builder", captor.getValue().templateName());
        assertEquals("relatorio-acervo.pdf", captor.getValue().fileName());
        Map payload = captor.getValue().data();
        assertTrue(((List)((Map)payload).get("secoes")).size() == 1);
    }
}
