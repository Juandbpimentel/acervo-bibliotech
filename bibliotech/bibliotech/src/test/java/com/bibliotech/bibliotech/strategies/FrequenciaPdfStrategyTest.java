package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.FrequenciaDados;
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

class FrequenciaPdfStrategyTest {

    @Mock
    PdfMicroserviceClient client;

    FrequenciaPdfStrategy strategy;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        strategy = new FrequenciaPdfStrategy(client);
    }

    @Test
    void shouldMapItemsAndCallClient() throws Exception {
        var item = new FrequenciaDados.FrequenciaItem("Maria", "Leitura", "15/01/2026");
        var dados = new FrequenciaDados(List.of(item));
        byte[] fakePdf = "pdf-bytes".getBytes();
        when(client.generatePdf(any())).thenReturn(fakePdf);

        byte[] result = strategy.exportar(dados);

        assertArrayEquals(fakePdf, result);

        ArgumentCaptor<PdfRequest> captor = ArgumentCaptor.forClass(PdfRequest.class);
        verify(client, times(1)).generatePdf(captor.capture());
        assertEquals("builder", captor.getValue().templateName());
        assertEquals("frequencia.pdf", captor.getValue().fileName());
        Map payload = captor.getValue().data();
        assertTrue(((List)((Map)payload).get("secoes")).size() == 1);
    }
}
