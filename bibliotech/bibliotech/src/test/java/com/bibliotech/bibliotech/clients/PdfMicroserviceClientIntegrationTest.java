package com.bibliotech.bibliotech.clients;

import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfMicroserviceClientIntegrationTest {

    static MockWebServer server;

    @BeforeAll
    static void start() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stop() throws Exception {
        if (server != null) server.shutdown();
    }

    @Test
    void client_callsExternalPdfEndpoint_andReturnsBytes() throws Exception {
        byte[] expected = "pdf-contents".getBytes();
        server.enqueue(new MockResponse().setResponseCode(200).addHeader("Content-Type", "application/pdf").setBody(new okio.Buffer().write(expected)));

        WebClient.Builder builder = WebClient.builder();
        PdfMicroserviceClient client = new PdfMicroserviceClient(builder, server.url("").toString(), 5L, 1);

        var payload = Map.<String,Object>of("secoes", java.util.List.of(Map.of("componente","texto","conteudo","oi")));
        var req = new PdfRequest("builder","t.pdf", payload);
        byte[] actual = client.generatePdf(req);

        assertArrayEquals(expected, actual);

        var recorded = server.takeRequest();
        assertEquals("/generate-pdf", recorded.getPath());
        String body = recorded.getBody().readUtf8();
        assertTrue(body.contains("\"templateName\""));
        assertTrue(body.contains("\"secoes\""));
    }
}