package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.AcervoDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AcervoPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public AcervoPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof AcervoDados a)) {
            throw new IllegalArgumentException("Dados incompatíveis para AcervoPdfStrategy");
        }

        var linhas = a.items().stream()
                .map(i -> List.of(i.titulo(), i.autor(), i.quantidade()))
                .collect(Collectors.toList());

        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Relatório de Acervo",
                "colunas", List.of("Título", "Autor", "Quantidade"),
                "linhas", linhas
        );

        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "relatorio-acervo.pdf", payload);
        return client.generatePdf(req);
    }
}