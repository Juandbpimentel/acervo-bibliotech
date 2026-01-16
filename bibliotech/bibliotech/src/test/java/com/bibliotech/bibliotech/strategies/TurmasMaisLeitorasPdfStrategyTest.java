package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.TurmasDados;
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

class TurmasMaisLeitorasPdfStrategyTest {

    @Mock
    PdfMicroserviceClient client;

    TurmasMaisLeitorasPdfStrategy strategy;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        strategy = new TurmasMaisLeitorasPdfStrategy(client);
    }

    @Test
    void shouldMapItemsAndCallClient() throws Exception {
        var item = new TurmasDados.TurmaItem("1", "A", 10L, "Aluno X", 3L);
        var dados = new TurmasDados(List.of(item));
        byte[] fakePdf = "pdf-bytes".getBytes();
        when(client.generatePdf(any())).thenReturn(fakePdf);

        byte[] result = strategy.exportar(dados);

        assertArrayEquals(fakePdf, result);

        ArgumentCaptor<PdfRequest> captor = ArgumentCaptor.forClass(PdfRequest.class);
        verify(client, times(1)).generatePdf(captor.capture());
        assertEquals("builder", captor.getValue().templateName());
        assertEquals("turmas-mais-leitoras.pdf", captor.getValue().fileName());
        Map payload = captor.getValue().data();
        assertTrue(((List)((Map)payload).get("secoes")).size() == 1);
    }
}
