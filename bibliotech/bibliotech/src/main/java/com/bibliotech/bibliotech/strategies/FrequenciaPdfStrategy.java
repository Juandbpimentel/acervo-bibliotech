package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.FrequenciaDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FrequenciaPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public FrequenciaPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof FrequenciaDados f)) {
            throw new IllegalArgumentException("Dados incompatíveis para FrequenciaPdfStrategy");
        }

        var linhas = f.items().stream()
                .map(i -> List.of(i.aluno(), i.atividade(), i.data()))
                .collect(Collectors.toList());

        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Frequência",
                "colunas", List.of("Aluno", "Atividade", "Data"),
                "linhas", linhas
        );

        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "frequencia.pdf", payload);
        return client.generatePdf(req);
    }
}  