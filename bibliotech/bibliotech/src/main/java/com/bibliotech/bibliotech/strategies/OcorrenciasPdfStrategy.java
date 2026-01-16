package com.bibliotech.bibliotech.strategies;

import com.bibliotech.bibliotech.clients.PdfMicroserviceClient;
import com.bibliotech.bibliotech.dtos.pdf.OcorrenciasDados;
import com.bibliotech.bibliotech.dtos.pdf.PdfRequest;
import com.bibliotech.bibliotech.interfaces.PdfExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OcorrenciasPdfStrategy implements PdfExportStrategy {
    private final PdfMicroserviceClient client;

    public OcorrenciasPdfStrategy(PdfMicroserviceClient client) {
        this.client = client;
    }

    @Override
    public byte[] exportar(com.bibliotech.bibliotech.dtos.pdf.DadosRelatorio dados) throws Exception {
        if (!(dados instanceof OcorrenciasDados o)) {
            throw new IllegalArgumentException("Dados incompatíveis para OcorrenciasPdfStrategy");
        }
        var itens = o.items().stream()
                .map(i -> List.of(i.aluno(), i.registradaPor(), i.detalhes(), i.data()))
                .collect(Collectors.toList());

        var secao = Map.<String,Object>of(
                "componente", "tabela",
                "titulo", "Ocorrências",
                "colunas", List.of("Aluno", "Registrada Por", "Detalhes", "Data"),
                "linhas", itens
        );

        var payload = Map.<String,Object>of("secoes", List.of(secao));
        var req = new PdfRequest("builder", "ocorrencias.pdf", payload);
        return client.generatePdf(req);
    }
}  